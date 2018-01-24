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
            e.printStackTrace();
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
        System.out.println("PROPS RESULT: \n"+result);

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
        System.out.println("PROPS RESULT: \n"+result);

        return result;
    }


    public void populate(JSONObject thing) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{
            RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);

            System.out.println("----------------------");
            System.out.println("THING STUFF:");

            String contextURI = OntologyResource.thingContextURI(thing.getString(ThingJSON.oid));
            System.out.println("CtX: "+contextURI);

            TreeModel graph = new TreeModel();
            rdfParser.setRDFHandler(new StatementCollector(graph));
            rdfParser.parse(new StringReader(thing.toString()), "");

            Iterator<Statement> i = graph.iterator();
            while(i.hasNext()) {
                Statement st = i.next();

                System.out.println(
                        st.getSubject() + " " +
                                st.getPredicate() + " "+
                                st.getObject());
                System.out.println(
                        Namespaces.toPrefixed(st.getSubject().stringValue()) + " " +
                                Namespaces.toPrefixed(st.getPredicate().stringValue()) + " "+
                                Namespaces.toPrefixed(st.getObject().stringValue()));
                System.out.println("");

                connection.add(st, factory.createIRI(contextURI));
            }
            System.out.println("----------------------");

        }
        finally {
            connection.close();
        }

    }

    public JSONObject populate(String data)  {
        ThingsLifter lifter = new ThingsLifter(getTypes(), getProperties());
        ThingsLifterResult lifting = lifter.lift(data);

        System.out.println("LIFTING: "+lifting.lifting);
        if(lifting.lifting != null){
            System.out.println(lifting.lifting.toString(2));
        }
        System.out.println("LIFTING ERRORS: "+lifting.errors.size());
        for(String error : lifting.errors) {
            System.out.println("> "+error);
        }

        if(lifting.lifting != null && lifting.errors.isEmpty()){
            System.out.println("POPULATING!");
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
                e.printStackTrace();
            }
        }
        else {
            System.out.println("NOT POPULATING!");

        }

        return lifting.failure();
    }

    public boolean contextExists(String contextURI, RepositoryConnection connection) throws Exception {
        System.out.println("checking if context exists: ["+contextURI+"]");
        RepositoryResult<Statement> result =
                connection.getStatements(null, null, null, true, factory.createIRI(contextURI));
        return result.hasNext();

    }



    public boolean delete(String oid) throws Exception {
        System.out.println("DELETING INSTANCE FOR: ["+oid+"]");
        String contextURI = OntologyResource.thingContextURI(oid);
        System.out.println("CONTEXT URI: ["+contextURI+"]");

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
                e.printStackTrace();
            }
            finally {
                connection.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
}
