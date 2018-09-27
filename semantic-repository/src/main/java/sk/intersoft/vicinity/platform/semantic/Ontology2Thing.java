package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingJSON;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.*;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.ontology.OntologyResource;

import java.util.Set;

public class Ontology2Thing {
    Repository repository = Repository.getInstance();
    final static Logger logger = LoggerFactory.getLogger(Ontology2Thing.class.getName());

    public static final String URI_KEY = "uri";

    private String getValue(String graphProperty,
                            Graph graph) throws Exception  {
        String value = graph.value(graphProperty);
        if(value != null){
            return Namespaces.toPrefixed(value);
        }
        else throw new Exception("graph property ["+graphProperty+"] not found!");
    }

    private DataSchema addDataSchema(String key, Graph graph) throws Exception {
        String content = getValue("wot:"+key, graph);
        return DataSchema.create(new JSONObject(content), new ThingValidator(true));
    }

    private InteractionPatternEndpoint addLink(Graph graph, String linkType) throws Exception {
//        logger.debug("adding link: \n" + graph.describe());
        InteractionPatternEndpoint link = new InteractionPatternEndpoint();

        link.href = getValue("wot:href", graph);
        link.output = addDataSchema(ThingJSON.OUTPUT_RAW_JSON_STRING, graph);
        if(linkType.equals(InteractionPatternEndpoint.WRITE)){
            link.input = addDataSchema(ThingJSON.INPUT_RAW_JSON_STRING, graph);
        }
        return link;
    }

    private void addLinks(InteractionPattern pattern, Graph graph) throws Exception {
//        logger.debug("ADDING LINKS");

        Graph read = graph.subGraph("wot:isReadableThrough");
        Graph write = graph.subGraph("wot:isWritableThrough");
        if(read != null){
            pattern.readEndpoint = addLink(read, InteractionPatternEndpoint.READ);
        }
        if(write != null){
            pattern.writeEndpoint = addLink(write, InteractionPatternEndpoint.WRITE);
        }

    }

    private InteractionPattern addProperty(Graph graph) throws Exception {
//        logger.debug("ADDING PROPERTY ... ");

        InteractionPattern pattern = new InteractionPattern();
        pattern.id = getValue("wot:interactionName", graph);
        pattern.refersTo = getValue("sosa:observes", graph);

        pattern.jsonExtension.put(URI_KEY, graph.baseURI);
        addLinks(pattern, graph);

        return pattern;
    }
    private InteractionPattern addAction(Graph graph) throws Exception {
//        logger.debug("ADDING ACTION ... ");

        InteractionPattern pattern = new InteractionPattern();
        pattern.id = getValue("wot:interactionName", graph);
        pattern.refersTo = getValue("sosa:forProperty", graph);

        addLinks(pattern, graph);

        return pattern;
    }
    private InteractionPattern addEvent(Graph graph) throws Exception {
//        logger.debug("ADDING EVENT ... ");

        InteractionPattern pattern = new InteractionPattern();
        pattern.id = getValue("wot:interactionName", graph);
        pattern.refersTo = getValue("sosa:observes", graph);

        pattern.output = addDataSchema(ThingJSON.OUTPUT_RAW_JSON_STRING, graph);

        return pattern;
    }

    private void addThingInteractionPatterns(ThingDescription thing, Graph graph) throws Exception {
        Set<Graph> patterns = graph.subGraphs("wot:providesInteractionPattern");


        for(Graph pattern : patterns) {
            String type = getValue("rdf:type", pattern);

//            logger.debug("ADDING PATTERN: \n" + pattern.describe());

            String typeName = Namespaces.valueFromPrefixed(Namespaces.toPrefixed(type));
            if(typeName.equals("Property")){
                InteractionPattern property = addProperty(pattern);
                thing.properties.put(property.id, property);
            }
            else if(typeName.equals("Action")){
                InteractionPattern action = addAction(pattern);
                thing.actions.put(action.id, action);
            }
            else if(typeName.equals("Event")){
                InteractionPattern event = addEvent(pattern);
                thing.events.put(event.id, event);
            }
        }
    }


    private void addThingProperties(ThingDescription thing, Graph graph) throws Exception {

        thing.oid = Namespaces.valueFromPrefixed(Namespaces.toPrefixed(graph.baseURI));
        thing.adapterId = getValue("wot:adapter-id", graph);
        thing.name = getValue("wot:name", graph);
        thing.type = Namespaces.toPrefixed(getValue("wot:type", graph));
    }



    public ThingDescription toThing(String oid) throws Exception {
        String uri = OntologyResource.thingInstanceURI(oid);
        String contextURI = OntologyResource.thingInstanceURI(oid);

        Graph graph = repository.loadGraph(uri, contextURI);

        logger.debug("getting graph for OID: [" + oid + "]");
        logger.debug("OID URI: "+uri);
        logger.debug("OID CONTEXT URI: "+contextURI);

        if(graph == null) throw new Exception("Missing semantic graph for thing [oid="+oid+"]!");
//        logger.debug("got graph: \n"+graph.describe());
        logger.debug("got graph");

        ThingDescription thing = new ThingDescription();
        addThingProperties(thing, graph);
        addThingInteractionPatterns(thing, graph);

        return thing;
    }

    public JSONObject toJSON(ThingDescription thing) throws Exception {
        // double validation
        ThingDescription validated = ThingDescription.create(ThingDescription.toJSON(thing), new ThingValidator(true));
        return ThingDescription.toJSON(validated);
    }
}
