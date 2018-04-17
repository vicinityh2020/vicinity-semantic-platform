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
import java.util.List;
import java.util.Set;

public class Ontology2Thing {
    Repository repository = Repository.getInstance();
    final static Logger logger = LoggerFactory.getLogger(Ontology2Thing.class.getName());

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
        addProperty("wot:name", ThingJSON.name, thing, graph);
        addProperty("rdf:type", ThingJSON.typeAnnotation, thing, graph);
        addProperty("wot:thingName", ThingJSON.oid, thing, graph);

        thing.put(ThingJSON.type, Namespaces.valueFromPrefixed(thing.getString(ThingJSON.typeAnnotation)));

    }


    private void addLink(String property,
                         String key,
                         JSONObject object,
                         Graph graph) {
        Graph link = graph.subGraph(property);

        System.out.println("ADDING LINK ["+key+"]: "+link);

        if(link != null){
            System.out.println("LINK ["+key+"]: \n"+link.describe());
            JSONObject linkJSON = new JSONObject();

            addProperty("wot:href", ThingJSON.href, linkJSON, link);
            Graph output = link.subGraph("wot:hasOutputData");
            if(output != null) {
                linkJSON.put(ThingJSON.output, new JSONObject(output.value("wot:jsonSource")));
            }

            Graph input = link.subGraph("wot:hasInputData");
            if(input != null) {
                linkJSON.put(ThingJSON.input, new JSONObject(input.value("wot:jsonSource")));
            }

            object.put(key, linkJSON);
        }


    }

    private void addLinks(JSONObject object, Graph graph) {
        System.out.println("ADDING LINKS");

        addLink("wot:isReadableThrough", ThingJSON.readLink, object, graph);
        addLink("wot:isWritableThrough", ThingJSON.writeLink, object, graph);
    }




    private void addProperty(Graph property, JSONObject thing, Graph graph) {
        JSONObject object = new JSONObject();
        System.out.println("ADDING PROPERTY\n"+property.describe());


        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Property"));
        addProperty("wot:interactionName", ThingJSON.pid, object, property);
        addProperty("sosa:observes", ThingJSON.observes, object, property);
        object.put(ThingJSON.monitors, Namespaces.valueFromPrefixed(object.getString(ThingJSON.observes)));

        addLinks(object, property);

        thing.getJSONArray(ThingJSON.properties).put(object);
    }


    private void addAction(Graph action, JSONObject thing, Graph graph) {
        JSONObject object = new JSONObject();

        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Action"));
        addProperty("wot:interactionName", ThingJSON.aid, object, action);
        addProperty("sosa:forProperty", ThingJSON.forProperty, object, action);
        object.put(ThingJSON.affects, Namespaces.valueFromPrefixed(object.getString(ThingJSON.forProperty)));

        addLinks(object, action);

        thing.getJSONArray(ThingJSON.actions).put(object);
    }

    private void addEvent(Graph event, JSONObject thing, Graph graph) {
        JSONObject object = new JSONObject();

        System.out.println("ADDING EVENT\n"+event.describe());
        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Event"));
        addProperty("wot:interactionName", ThingJSON.eid, object, event);
        addProperty("sosa:observes", ThingJSON.observes, object, event);
        object.put(ThingJSON.monitors, Namespaces.valueFromPrefixed(object.getString(ThingJSON.observes)));

        Graph output = event.subGraph("wot:hasOutputData");
//        System.out.println("EVENT output:\n"+output);
        if(output != null){
            object.put(ThingJSON.output, new JSONObject(output.value("wot:jsonSource")));
        }

        thing.getJSONArray(ThingJSON.events).put(object);
    }

    private void addThingInteractionPatterns(JSONObject thing, Graph graph) {
        Set<Graph> patterns = graph.subGraphs("wot:providesInteractionPattern");


        for(Graph pattern : patterns) {
            String type = pattern.value("rdf:type");

            System.out.println("ADDING PATTERN: \n"+pattern.describe());

            if(type != null){
                String typeName = Namespaces.valueFromPrefixed(Namespaces.toPrefixed(type));
                if(typeName.equals("Action")){
                    addAction(pattern, thing, graph);
                }
                else if(typeName.equals("Property")){
                    addProperty(pattern, thing, graph);
                }
                else if(typeName.equals("Event")){
                    addEvent(pattern, thing, graph);
                }
            }
        }
    }

    public JSONObject toJSON(String oid) throws Exception {
        logger.debug("getting graph for OID: ["+oid+"]");

        JSONObject thing = new JSONObject();
        thing.put(ThingJSON.properties, new JSONArray());
        thing.put(ThingJSON.actions, new JSONArray());
        thing.put(ThingJSON.events, new JSONArray());

        String uri = OntologyResource.thingInstanceURI(oid);
        String contextURI = OntologyResource.thingInstanceURI(oid);

        logger.debug("OID URI: "+uri);
        logger.debug("OID CONTEXT URI: "+contextURI);

        Graph graph = repository.loadGraph(uri, contextURI);

        if(graph == null) throw new Exception("Missing semantic graph for thing [oid="+oid+"]!");
        logger.debug("graph: \n"+graph.describe());
        addThingProperties(thing, graph);
        addThingInteractionPatterns(thing, graph);

        return thing;
    }
}
