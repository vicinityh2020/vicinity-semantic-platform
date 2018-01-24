package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingJSON;
import sk.intersoft.vicinity.platform.semantic.ontology.NamespacePrefix;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.ontology.OntologyResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ontology2Thing {
    Repository repository = Repository.getInstance();
    Logger logger = LoggerFactory.getLogger(Ontology2Thing.class.getName());

    private void addProperty(String graphProperty,
                             String key,
                             JSONObject thing,
                             Graph graph) {
        String value = graph.value(graphProperty);
        if(value != null){
            thing.put(key, Namespaces.toPrefixed(value));
        }
    }

    private void addThingProperties(JSONObject thing, Graph graph) {
        thing.put(ThingJSON.idAnnotation, Namespaces.toPrefixed(graph.baseURI));
        thing.put(ThingJSON.oid, Namespaces.valueFromPrefixed(Namespaces.toPrefixed(graph.baseURI)));
        addProperty("rdf:type", ThingJSON.typeAnnotation, thing, graph);
        addProperty("wot:thingName", ThingJSON.oid, thing, graph);
    }


    private void addLinks(String property,
                          String key,
                          JSONObject object,
                          Graph graph) {
        Set<Graph> links = graph.subGraphs(property);
        List<JSONObject> linkJSONs = new ArrayList<JSONObject>();

        for(Graph link : links) {
            JSONObject o = new JSONObject();

            addProperty("wot:href", ThingJSON.href, o, link);
            linkJSONs.add(o);
        }

        if(linkJSONs.size() > 0){

            JSONArray list = new JSONArray();
            for(JSONObject o : linkJSONs){
                list.put(o);
            }

            object.put(key, list);
        }

    }

    private void addLinks(JSONObject object, Graph graph) {
        addLinks("wot:isReadableThrough", ThingJSON.readLinks, object, graph);
        addLinks("wot:isWritableThrough", ThingJSON.writeLinks, object, graph);
    }



    private void addOutput(JSONObject object, Graph graph) {
        Set<Graph> outputs = graph.subGraphs("wot:hasOutputData");
        List<JSONObject> outputJSONs = new ArrayList<JSONObject>();
        for(Graph output : outputs) {
            JSONObject o = new JSONObject();

            addProperty("wot:hasValueType", ThingJSON.datatype, o, output);
            addProperty("wot:isMeasuredIn", ThingJSON.units, o, output);
            outputJSONs.add(o);
        }

        if(outputJSONs.size() > 0){

            if(outputJSONs.size() == 1){
                object.put(ThingJSON.output, outputJSONs.get(0));
            }
            else {
                JSONArray list = new JSONArray();
                for(JSONObject o : outputJSONs){
                    list.put(o);
                }

                object.put(ThingJSON.output, list);
            }
        }
    }


    private void addInput(JSONObject object, Graph graph) {
        Set<Graph> inputs = graph.subGraphs("wot:hasInputData");
        List<JSONObject> inputJSONs = new ArrayList<JSONObject>();
        for(Graph input : inputs) {
            JSONObject o = new JSONObject();

            addProperty("wot:hasValueType", ThingJSON.datatype, o, input);
            addProperty("wot:isMeasuredIn", ThingJSON.units, o, input);
            inputJSONs.add(o);
        }

        if(inputJSONs.size() > 0){

            if(inputJSONs.size() == 1){
                object.put(ThingJSON.input, inputJSONs.get(0));
            }
            else {
                JSONArray list = new JSONArray();
                for(JSONObject o : inputJSONs){
                    list.put(o);
                }

                object.put(ThingJSON.input, list);
            }
        }
    }

    private void addProperty(Graph property, JSONObject thing, Graph graph) {
        JSONObject object = new JSONObject();

        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Property"));
        addProperty("wot:interactionName", ThingJSON.pid, object, property);
        addProperty("sosa:observes", ThingJSON.observes, object, property);

        addOutput(object, property);
        addInput(object, property);

        addLinks(object, property);

        thing.getJSONArray(ThingJSON.properties).put(object);
    }


    private void addAction(Graph action, JSONObject thing, Graph graph) {
        JSONObject object = new JSONObject();

        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Action"));
        addProperty("wot:interactionName", ThingJSON.aid, object, action);
        addProperty("sosa:forProperty", ThingJSON.forProperty, object, action);

        addOutput(object, action);
        addInput(object, action);

        addLinks(object, action);

        thing.getJSONArray(ThingJSON.actions).put(object);
    }

    private void addThingInteractionPatterns(JSONObject thing, Graph graph) {
        Set<Graph> patterns = graph.subGraphs("wot:providesInteractionPattern");
        Set<JSONObject> properties = new HashSet<JSONObject>();
        Set<JSONObject> actions = new HashSet<JSONObject>();

        for(Graph pattern : patterns) {
            String type = pattern.value("rdf:type");
            if(type != null){
                String typeName = Namespaces.valueFromPrefixed(Namespaces.toPrefixed(type));
                if(typeName.equals("Action")){
                    addAction(pattern, thing, graph);
                }
                else if(typeName.equals("Property")){
                    addProperty(pattern, thing, graph);
                }
            }
        }
    }

    public JSONObject toJSON(String oid)  {
        logger.debug("getting graph for OID: "+oid);

        JSONObject thing = new JSONObject();
        thing.put(ThingJSON.properties, new JSONArray());
        thing.put(ThingJSON.actions, new JSONArray());
        thing.put("GENERATED", "YO");

        String uri = OntologyResource.thingInstanceURI(oid);
        String contextURI = OntologyResource.thingContextURI(oid);

        Graph graph = repository.loadGraph(uri, contextURI);

        logger.debug("graph: \n"+graph.describe());
        addThingProperties(thing, graph);
        addThingInteractionPatterns(thing, graph);

        return thing;
    }
}
