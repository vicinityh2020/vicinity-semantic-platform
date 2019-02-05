package sk.intersoft.vicinity.platform.semantic;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.lifting.ThingsLifter;
import sk.intersoft.vicinity.platform.semantic.lifting.model.contracts.AgentThing;
import sk.intersoft.vicinity.platform.semantic.ontology.NamespacePrefix;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.ontology.OntologyResource;
import sk.intersoft.vicinity.platform.semantic.utils.DateTimeUtil;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static sk.intersoft.vicinity.platform.semantic.lifting.ThingsLifter.JSONLD_SCHEMA_LOCATION;

public class Agent2Ontology {
    final static Logger logger = LoggerFactory.getLogger(Agent2Ontology.class.getName());
    Repository repository = Repository.getInstance();
    ValueFactory factory = SimpleValueFactory.getInstance();

    public boolean delete(String oid, boolean execute, RepositoryConnection connection)  {
        String contextURI = OntologyResource.thingInstanceURI(oid);
        String uri = OntologyResource.thingInstanceURI(oid);

        logger.info("DELETE AGENT URI: ["+uri+"]");
        logger.info("DELETE AGENT CONTEXT URI: ["+contextURI+"]");

        try{
            if(execute){
                connection.begin();
            }
            logger.debug("deleting context graph: "+contextURI);
            connection.clear(factory.createIRI(contextURI));

            if(execute){
                connection.commit();
            }
            return true;

        }
        catch(Exception e){
            logger.error("", e);
            return false;
        }

    }

    public String populate(JSONObject agent) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{

            String oid = agent.getString(AgentThing.OUT_OID_KEY);
            logger.info("POPULATING NEW AGENT: "+agent.toString());

            logger.info("FIRST DELETE ...");
            long dstart = DateTimeUtil.millis();
            delete(oid, true, connection);
            long dend = DateTimeUtil.duration(dstart);
            logger.info("DELETE TOOK: "+DateTimeUtil.format(dend));

            long start = DateTimeUtil.millis();
            logger.info("POPULATING NEW AGENT: "+agent.toString());

            RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);

            String contextURI = OntologyResource.thingInstanceURI(oid);
            IRI contextIRI = factory.createIRI(contextURI);

            agent.put("@context", ThingsLifter.JSONLD_SCHEMA_LOCATION);
            agent.put("@id", Namespaces.prefixed(NamespacePrefix.thing, oid));

            logger.info("LIFTED AS INSTANCE: "+agent.toString());

            logger.info("POPULATING ONTOLOGY WITH AGENT with CtX: "+contextURI+ " / " + contextIRI);

            TreeModel graph = new TreeModel();
            rdfParser.setRDFHandler(new StatementCollector(graph));
            rdfParser.parse(new StringReader(agent.toString()), "");

            logger.info("GRAPH: "+graph);

            connection.begin();

            Iterator<Statement> i = graph.iterator();
            while(i.hasNext()) {
                Statement st = i.next();

//                logger.debug("TRIPLE IN: " +
//                        Namespaces.toPrefixed(st.getSubject().stringValue()) + " " +
//                        Namespaces.toPrefixed(st.getPredicate().stringValue()) + " "+
//                        Namespaces.toPrefixed(st.getObject().stringValue()));
//                logger.debug(
//                        st.getSubject() + " " +
//                                st.getPredicate() + " "+
//                                st.getObject());

                connection.add(st, contextIRI);

            }

            connection.commit();


            long end = DateTimeUtil.duration(start);
            logger.info("POPULATION TOOK: "+DateTimeUtil.format(end));
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
            connection.rollback();
        }
        finally {
            connection.close();
        }
        return agent.getString(AgentThing.OUT_OID_KEY);
    }

    public JSONObject create(String data) throws Exception {
        logger.info("CREATING AGENTS: "+data);
        JSONArray created = new JSONArray();
        JSONArray notCreated = new JSONArray();
        Iterator<Object> i = new JSONArray(data).iterator();
        while(i.hasNext()) {
            Object item = i.next();
            JSONObject agent = AgentThing.create(item);
            logger.info("CREATING AGENT: "+agent.toString());
            String oid = populate(agent);
            if(oid != null){
                created.put(oid);
            }
            else {
                notCreated.put(oid);
            }
        }

        JSONObject out = new JSONObject();
        out.put("created", created);
        out.put("failed", notCreated);

        return out;
    }


    public JSONObject bulkDelete(List<String> oids) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        JSONObject out = new JSONObject();
        JSONArray deleted = new JSONArray();
        JSONArray notDeleted = new JSONArray();
        out.put("deleted", deleted);
        out.put("failed", notDeleted);
        logger.info("BULK DELETE: "+oids);
        try{
            long start = DateTimeUtil.millis();

            connection.begin();
            for(String oid: oids){
                if(delete(oid, false, connection)){
                    deleted.put(oid);
                }
                else{
                    notDeleted.put(oid);
                }
            }
            connection.commit();

            long end = DateTimeUtil.duration(start);
            logger.info("BULK DELETE TOOK: "+DateTimeUtil.format(end));
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
            connection.rollback();
        }
        finally {
            connection.close();
        }
        return out;
    }

    public JSONObject delete(String data) throws Exception {
        logger.info("DELETING AGENTS: "+data);
        JSONArray deleted = new JSONArray();
        JSONArray notDeleted = new JSONArray();

        List<String> oids = new ArrayList<String>();
        Iterator<Object> i = new JSONArray(data).iterator();
        while(i.hasNext()) {
            Object item = i.next();
            if (item instanceof String) {
                oids.add((String) item);
            } else {
                throw new Exception("Expected oid:string instead of: " + item);
            }

        }

        return bulkDelete(oids);

    }

}
