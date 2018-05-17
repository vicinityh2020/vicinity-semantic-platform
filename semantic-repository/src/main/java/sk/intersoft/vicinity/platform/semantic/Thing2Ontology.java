package sk.intersoft.vicinity.platform.semantic;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.lifting.ThingsLifter;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingJSON;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.ontology.OntologyResource;
import sk.intersoft.vicinity.platform.semantic.sparql.SPARQL;
import sk.intersoft.vicinity.platform.semantic.util.JSONUtil;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Thing2Ontology {
    Repository repository = Repository.getInstance();
    ValueFactory factory = SimpleValueFactory.getInstance();

    final static Logger logger = LoggerFactory.getLogger(Thing2Ontology.class.getName());

    private Set<String> extract(String query, String key)  {
        try{
            SPARQL sparql = new SPARQL();
            JSONObject result = sparql.query(query);
            List<JSONObject> bindings = JSONUtil.getObjectArray("bindings", result.getJSONObject("results"));
            Set<String> values = new HashSet<String>();
            for(JSONObject b : bindings) {
                JSONObject value = b.getJSONObject(key);
                String uri = JSONUtil.getString("value", value);
                values.add(Namespaces.toPrefixed(uri));
            }
            return values;
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
        }

        return new HashSet<String>();
    }

    private Set<String> getProperties()  {
        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX : <http://iot.linkeddata.es/def/core#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX wot: <http://iot.linkeddata.es/def/wot#>" +
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>" +
                "select ?x where {" +
                "?x rdf:type ssn:Property ." +
                "}";

        Set<String> result = extract(query, "x");
        logger.info("PROPS RESULT: \n"+result);

        return result;
    }
    private Set<String> getTypes()  {
        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX : <http://iot.linkeddata.es/def/core#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX wot: <http://iot.linkeddata.es/def/wot#>" +
                "select ?x where {" +
                "?x rdfs:subClassOf :Device ." +
                "}";

        Set<String> result = extract(query, "x");
        logger.info("PROPS RESULT: \n"+result);

        return result;
    }


    public void populate(JSONObject thing) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{
            RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);

            logger.info("THING POPULATION STUFF:");

            String contextURI = OntologyResource.thingInstanceURI(thing.getString(ThingJSON.oid));
            logger.info("CtX: "+contextURI);

            TreeModel graph = new TreeModel();
            rdfParser.setRDFHandler(new StatementCollector(graph));
            rdfParser.parse(new StringReader(thing.toString()), "");


            connection.begin();

            Iterator<Statement> i = graph.iterator();
            while(i.hasNext()) {
                Statement st = i.next();

                logger.debug(
                        st.getSubject() + " " +
                                st.getPredicate() + " "+
                                st.getObject());
                logger.debug(
                        Namespaces.toPrefixed(st.getSubject().stringValue()) + " " +
                                Namespaces.toPrefixed(st.getPredicate().stringValue()) + " "+
                                Namespaces.toPrefixed(st.getObject().stringValue()));

                connection.add(st, factory.createIRI(contextURI));
            }
            logger.info("THING POPULATION STUFF: DONE");

            connection.commit();
        }
        catch(Exception e){
            connection.rollback();
        }
        finally {
            connection.close();
        }

    }

    public JSONObject populate(String data)  {
        ThingsLifter lifter = new ThingsLifter(getTypes(), getProperties());
        ThingsLifterResult lifting = lifter.lift(data);

        logger.info("LIFTING: "+lifting.lifting);
        if(lifting.lifting != null){
            logger.info(lifting.lifting.toString(2));
        }
        logger.info("LIFTING ERRORS: "+lifting.errors.size());
        for(String error : lifting.errors) {
            logger.info("> "+error);
        }

        if(lifting.lifting != null && lifting.errors.isEmpty()){
            logger.info("POPULATING!");
            try{
                populate(lifting.lifting);
                Ontology2Thing generator = new Ontology2Thing();
                JSONObject thing = generator.toJSON(lifting.lifting.getString(ThingJSON.oid));

                JSONObject out = new JSONObject();
                out.put("lifting", thing);
                return out;

            }
            catch(Exception e) {
                lifting.errors.add("something went ape during ontology population!");
                logger.error("EXCEPTION", e);
            }
        }
        else {
            logger.info("NOT POPULATING!");

        }

        return lifting.failure();
    }

    public boolean contextExists(String contextURI, RepositoryConnection connection) throws Exception {
        logger.debug("checking if context exists: ["+contextURI+"]");
        RepositoryResult<Statement> result =
                connection.getStatements(null, null, null, true, factory.createIRI(contextURI));
        return result.hasNext();

    }



    public boolean delete(String oid) throws Exception {
        logger.info("DELETING INSTANCE FOR: ["+oid+"]");
        String contextURI = OntologyResource.thingInstanceURI(oid);
        logger.info("CONTEXT URI: ["+contextURI+"]");

        try{
            RepositoryConnection connection = repository.getConnection();
            try{
                boolean contextExists = contextExists(contextURI, connection);
                if(contextExists){
                    connection.clear(factory.createIRI(contextURI));
                    return true;
                }
                else return false;

            }
            catch(Exception e){
                logger.error("EXCEPTION", e);
            }
            finally {
                connection.close();
            }
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
        }

        return false;
    }
}
