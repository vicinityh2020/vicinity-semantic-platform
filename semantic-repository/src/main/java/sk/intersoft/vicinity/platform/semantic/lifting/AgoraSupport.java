package sk.intersoft.vicinity.platform.semantic.lifting;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Ontology2Thing;
import sk.intersoft.vicinity.platform.semantic.Repository;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.lifting.model.AgoraMapping;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.DataSchema;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.DataSchemaField;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.InteractionPattern;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.ThingDescription;
import sk.intersoft.vicinity.platform.semantic.ontology.NamespacePrefix;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.ontology.OntologyResource;
import sk.intersoft.vicinity.platform.semantic.sparql.SPARQL;
import sk.intersoft.vicinity.platform.semantic.util.UniqueID;
import sk.intersoft.vicinity.platform.semantic.utils.JSONUtil;

import java.util.*;

public class AgoraSupport {
    final static Logger logger = LoggerFactory.getLogger(AgoraSupport.class.getName());

    private ThingDescription thing = null;
    private Map<IRI, Set<Statement>> statements = new HashMap<IRI, Set<Statement>>();

    public static String WOT_THING_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "Thing"));
    public static String REPRESENTS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "represents"));
    public static String IS_REPRESENTED_BY = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "isRepresentedBy"));

    public static String HAS_CONTEXT_GRAPH = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "hasContextGraph"));

    private static String THING_DESCRIPTION_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "ThingDescription"));
    private static String VALUE_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "Value"));
    private static String HAS_VALUE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "hasValue"));
    private static String LITERAL_VALUE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "literalValue"));
    private static String THING_NAME = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "thingName"));

    private static String RDF_TYPE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.rdf, "type"));

    private static String DESCRIBED_BY = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "describedBy"));
    private static String DESCRIBES = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "describes"));
    private static String IDENTIFIER = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "identifier"));


    private static String ACCESS_MAPPING_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "AccessMapping"));
    private static String MAPPING_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "Mapping"));
    private static String HAS_ACCESS_MAPPING = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "hasAccessMapping"));
    private static String HAS_MAPPING = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "hasMapping"));
    private static String MAPS_RESOURCE_FROM = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "mapsResourceFrom"));
    private static String MAPPING_KEY = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "key"));
    private static String MAPPING_PREDICATE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "predicate"));
    private static String MAPPING_JSON_PATH = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "jsonPath"));

    private static String LINK_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "Link"));
    private static String HREF = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "href"));
    private static String MEDIA_TYPE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "mediaType"));

    private static String HAS_COMPONENT = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "hasComponent"));

    private static String TIMESTAMP_PREDICATE = "core:timestamp";
    private static String VALUE_PREDICATE = "core:value";

    public static final Set<String> mappingPredicates =
            Collections.unmodifiableSet(new HashSet<String>() {{
                add(TIMESTAMP_PREDICATE);
                add(VALUE_PREDICATE);
            }});

    public AgoraSupport(ThingDescription thing) {
        this.thing = thing;
        this.statements = new HashMap<IRI, Set<Statement>>();
    }

    Repository repository = Repository.getInstance();
    ValueFactory factory = SimpleValueFactory.getInstance();

    private String getObjectTDURI(String oid) {
        return OntologyResource.thingDescriptionURI(oid);
    }

    private void addStatements() throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try {
            connection.begin();

            for(Map.Entry<IRI, Set<Statement>> entry : statements.entrySet()) {
                IRI context = entry.getKey();
                Set<Statement> statementSet = entry.getValue();

                logger.debug("ADDING STATEMENTS TO CONTEXT [" + context + "]");
                for (Statement s : statementSet) {
                    logger.debug("adding: " +
                            Namespaces.toPrefixed(s.getSubject().stringValue()) + " " +
                            Namespaces.toPrefixed(s.getPredicate().stringValue()) + " " +
                            Namespaces.toPrefixed(s.getObject().stringValue()));
                    connection.add(s, context);
                }
            }

            connection.commit();
        } catch (Exception e) {
            logger.error("", e);
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    private void addStatements(IRI context, Set<Statement> sts) {
        Set<Statement> content = statements.get(context);
        if(content != null){
            content.addAll(sts);
            statements.put(context, content);
        }
        else {
            statements.put(context, sts);
        }
    }

    private void addThingTD(IRI thingIRI,
                            IRI thingContextIRI,
                            IRI tedIRI,
                            IRI tedContextIRI) throws Exception {
        IRI tdIRI = factory.createIRI(getObjectTDURI(thing.oid));

        logger.debug("adding thing TD");
        logger.debug("Thing IRI: " + thingIRI);
        logger.debug("Thing Context IRI: " + thingContextIRI);
        logger.debug("TD IRI: " + tdIRI);
        logger.debug("TED IRI: " + tedIRI);
        logger.debug("TED Context IRI: " + tedContextIRI);

        // thing extension:
        Set<Statement> thingExtension = new HashSet<Statement>();
        thingExtension.add(factory.createStatement(thingIRI, factory.createIRI(DESCRIBED_BY), tdIRI));

        // thing description
        Set<Statement> thingDescription = new HashSet<Statement>();
        thingDescription.add(factory.createStatement(tdIRI, factory.createIRI(RDF_TYPE), factory.createIRI(THING_DESCRIPTION_CLASS)));
        thingDescription.add(factory.createStatement(tdIRI, factory.createIRI(DESCRIBES), thingIRI));
        thingDescription.add(factory.createStatement(tdIRI, factory.createIRI(IDENTIFIER), factory.createLiteral(thing.oid)));

        thingExtension.add(factory.createStatement(thingIRI, factory.createIRI(HAS_CONTEXT_GRAPH), tdIRI));

        Set<Statement> tedComponents = new HashSet<Statement>();
        tedComponents.add(factory.createStatement(tedIRI, factory.createIRI(HAS_COMPONENT), thingIRI));


        addStatements(thingContextIRI, thingExtension);
        addStatements(tdIRI, thingDescription);
        addStatements(tedContextIRI, tedComponents);
    }

    private IRI createIRI(String prefix, String value) {
        return factory.createIRI(
                Namespaces.toURI(
                        Namespaces.prefixed(
                                prefix,
                                value)));
    }

    private static void traverse(DataSchema schema,
                                 Set<AgoraMapping> mappings,
                                 String path,
                                 boolean validate) {
        if(schema.isArray()){
            traverse(schema.item, mappings, path+".[*]", validate);
        }
        else if(schema.isObject()){
            if(schema.field != null){
                for(DataSchemaField field : schema.field){
                    if(field.predicate != null){
                        AgoraMapping mapping = new AgoraMapping("$"+path, field.name, field.predicate);
                        if(validate){
                            for(String predicate : mappingPredicates){
                                if(predicate.equals(field.predicate)){
                                    mappings.add(mapping);
                                }
                            }
                        }
                        else {
                            mappings.add(mapping);
                        }
                    }

                    traverse(field.schema, mappings, path+".['"+field.name+"']", validate);
                }
            }
        }
    }

    public static Set<AgoraMapping> getMappings(DataSchema output, boolean validate) {
        logger.debug("adding all mapping for: \n"+output.toString(0));
        Set<AgoraMapping> mappings = new HashSet<AgoraMapping>();

        traverse(output, mappings, "", validate);



        return mappings;
    }


    private void addValueTD(InteractionPattern property,
                            IRI thingIRI,
                            IRI thingContextIRI,
                            IRI tedIRI,
                            IRI tedContextIRI) throws Exception {
        IRI propertyIRI = factory.createIRI(property.jsonExtension.get(Ontology2Thing.URI_KEY));

        String valueId = UniqueID.create();
        IRI valueIRI = createIRI(NamespacePrefix.thing, valueId);
        IRI valueTDIRI = factory.createIRI(getObjectTDURI(valueId));


        logger.debug("adding thing TD: ");
        logger.debug("Property IRI: " + propertyIRI);
        logger.debug("Property Value IRI: " + valueIRI);
        logger.debug("Property Value TD IRI: " + valueTDIRI);
        logger.debug("Property Value TD mapping IRI: " + valueTDIRI);
        logger.debug("Thing IRI: " + thingIRI);
        logger.debug("Thing Context IRI: " + thingContextIRI);
        logger.debug("TED IRI: " + tedIRI);
        logger.debug("TED Context IRI: " + tedContextIRI);

        // property extension:
        Set<Statement> propertyExtension = new HashSet<Statement>();
        propertyExtension.add(factory.createStatement(propertyIRI, factory.createIRI(HAS_VALUE), valueIRI));

        // value thing
        Set<Statement> valueThing = new HashSet<Statement>();
        valueThing.add(factory.createStatement(valueIRI, factory.createIRI(RDF_TYPE), factory.createIRI(VALUE_CLASS)));
        valueThing.add(factory.createStatement(valueIRI, factory.createIRI(DESCRIBED_BY), valueTDIRI));
        valueThing.add(factory.createStatement(valueIRI, factory.createIRI(THING_NAME), factory.createLiteral(valueId)));

        // td components
        Set<Statement> tedComponents = new HashSet<Statement>();
        tedComponents.add(factory.createStatement(tedIRI, factory.createIRI(HAS_COMPONENT), valueIRI));


        // thing description
        Set<Statement> valueDescription = new HashSet<Statement>();
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(RDF_TYPE), factory.createIRI(THING_DESCRIPTION_CLASS)));
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(DESCRIBES), valueIRI));
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(IDENTIFIER), factory.createLiteral(valueId)));

        IRI hasMappingIRI = createIRI(NamespacePrefix.bnode, UniqueID.create());
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(HAS_ACCESS_MAPPING), hasMappingIRI));
        valueDescription.add(factory.createStatement(hasMappingIRI, factory.createIRI(RDF_TYPE), factory.createIRI(ACCESS_MAPPING_CLASS)));

        IRI resourceIRI = createIRI(NamespacePrefix.bnode, UniqueID.create());
        valueDescription.add(factory.createStatement(hasMappingIRI, factory.createIRI(MAPS_RESOURCE_FROM), resourceIRI));
        valueDescription.add(factory.createStatement(resourceIRI, factory.createIRI(RDF_TYPE), factory.createIRI(LINK_CLASS)));
        valueDescription.add(factory.createStatement(resourceIRI, factory.createIRI(HREF), factory.createLiteral("/objects/"+thing.oid+"/properties/"+property.id)));
        valueDescription.add(factory.createStatement(resourceIRI, factory.createIRI(MEDIA_TYPE), factory.createLiteral("application/json")));

        Set<AgoraMapping> mappings = getMappings(property.readEndpoint.output, true);

        logger.debug("acquired thing value mappings: ");
        for(AgoraMapping m : mappings) {
            logger.debug(m.toString());
        }
        for(AgoraMapping m : mappings) {
            IRI mappingIRI = createIRI(NamespacePrefix.bnode, UniqueID.create());
            valueDescription.add(factory.createStatement(hasMappingIRI, factory.createIRI(HAS_MAPPING), mappingIRI));

            valueDescription.add(factory.createStatement(mappingIRI, factory.createIRI(RDF_TYPE), factory.createIRI(MAPPING_CLASS)));
            if(m.jsonPath != null && !m.jsonPath.trim().equals("$")){
                valueDescription.add(factory.createStatement(mappingIRI, factory.createIRI(MAPPING_JSON_PATH), factory.createLiteral(m.jsonPath)));
            }
            valueDescription.add(factory.createStatement(mappingIRI, factory.createIRI(MAPPING_KEY), factory.createLiteral(m.key)));
            valueDescription.add(factory.createStatement(mappingIRI, factory.createIRI(MAPPING_PREDICATE), factory.createIRI(LITERAL_VALUE)));

        }

        Set<Statement> thingContexts = new HashSet<Statement>();
        thingContexts.add(factory.createStatement(thingIRI, factory.createIRI(HAS_CONTEXT_GRAPH), valueTDIRI));
        thingContexts.add(factory.createStatement(thingIRI, factory.createIRI(HAS_CONTEXT_GRAPH), valueIRI));

        addStatements(thingContextIRI, thingContexts);
        addStatements(thingContextIRI, propertyExtension);
        addStatements(valueIRI, valueThing);
        addStatements(valueTDIRI, valueDescription);
        addStatements(tedContextIRI, tedComponents);
    }

    private void addValuesTD(IRI thingIRI,
                             IRI thingContextIRI,
                             IRI tedIRI,
                             IRI tedContextIRI) throws Exception {

        logger.debug("adding TD support for thing readable properties");

        for (Map.Entry<String, InteractionPattern> entry : thing.properties.entrySet()) {
            InteractionPattern prop = entry.getValue();
            if(prop.readEndpoint != null && prop.readEndpoint.output != null) {
                addValueTD(prop, thingIRI, thingContextIRI, tedIRI, tedContextIRI);
            }
        }

    }

    public static String getTEDInstance(){
        String query = "PREFIX core: <"+Namespaces.nsToPrefixURI(Namespaces.core)+"> " +
                "PREFIX rdf: <"+Namespaces.nsToPrefixURI(Namespaces.rdf)+"> " +
                "select ?ted where {" +
                "?es rdf:type core:ThingEcosystemDescription ." +
                "?es core:describes ?ted ." +
                "}";


        try{
            SPARQL sparql = new SPARQL();
            JSONObject result = sparql.query(query);


            List<JSONObject> bindings = JSONUtil.getObjectArray("bindings", result.getJSONObject("results"));
            if(bindings.size() > 0) {
                JSONObject ted = bindings.get(0);

                String tedURI = JSONUtil.getObject("ted", ted).getString("value");
                return tedURI;

            }
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
        }

        return null;
    }

    public void add(){
        logger.debug("adding support for: ["+thing.oid+"]");
        try{
            String uri = OntologyResource.thingInstanceURI(thing.oid);
            String contextURI = OntologyResource.thingInstanceURI(thing.oid);
            String tedContextURI = Namespaces.ted;


            logger.debug("OID URI: "+uri);
            logger.debug("OID CONTEXT URI: "+contextURI);
            logger.debug("TED CONTEXT URI: "+tedContextURI);


            IRI thingIRI = factory.createIRI(uri);
            IRI thingContextIRI = factory.createIRI(contextURI);
            IRI tedContextIRI = factory.createIRI(tedContextURI);
            String tedInstance = getTEDInstance();

            if(tedInstance == null){
                throw new Exception("TED instance NOT found!");
            }

            IRI tedIRI = factory.createIRI(tedInstance);

            logger.debug("Thing IRI: "+thingIRI);
            logger.debug("Thing context IRI: "+thingContextIRI);
            logger.debug("TED IRI: "+tedIRI);
            logger.debug("TED Context IRI: "+tedContextIRI);


            addThingTD(thingIRI, thingContextIRI, tedIRI, tedContextIRI);
            addValuesTD(thingIRI, thingContextIRI, tedIRI, tedContextIRI);

            addStatements();
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
        }
    }

    public static boolean isThing(String uri){
//        logger.debug("is thing: "+uri);
        String prefixed = Namespaces.toPrefixed(uri);
        String prefix = Namespaces.prefixFromPrefixed(prefixed);
//        logger.debug("prefixed: "+prefixed);
//        logger.debug("prefix: "+prefix);
        if(prefix.equals(NamespacePrefix.thing)){
//            logger.debug("is thing");
            return true;
        }
        else {
//            logger.debug("not thing");
            return false;
        }
    }


    public static void deleteTEDRelations(String thingURI,
                                          Set<String> contexts,
                                          String tedURI,
                                          RepositoryConnection conn) {

        ValueFactory factory = SimpleValueFactory.getInstance();

        IRI tedIRI = factory.createIRI(tedURI);

        logger.info("DELETING TED RELATIONS FOR THING: ["+thingURI+"]");
        logger.info("TED IRI: ["+tedIRI+"]");
        try{
            Statement st = factory.createStatement(tedIRI, factory.createIRI(HAS_COMPONENT), factory.createIRI(thingURI));
            logger.info("removing TED -> THING statement: "+st);
            conn.remove(st);

            for(String ctx : contexts) {
                if(isThing(ctx)){
                    Statement ctxst = factory.createStatement(tedIRI, factory.createIRI(HAS_COMPONENT), factory.createIRI(ctx));
                    logger.info("removing TED -> sub-THING statement: "+ctxst);
                    conn.remove(ctxst);
                }
            }
        }
        catch(Exception e){
            logger.error("error deleting TED content", e);
        }
    }

    public static Set<Statement> addthingRepresentation(String thingURI,
                                              ThingDescription thing) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        try {
            logger.debug("adding agora thing representation for: "+thingURI);

            IRI thingIRI = factory.createIRI(thingURI);

            String rURI = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.bnode, UniqueID.create()));
            IRI rIRI = factory.createIRI(rURI);
            String thingTypeURI = Namespaces.toURI(thing.type);
            IRI thingTypeIRI = factory.createIRI(thingTypeURI);

            logger.debug("thing URI: "+thingURI);
            logger.debug("thing IRI: "+thingIRI);

            logger.debug("representation URI: "+rURI);
            logger.debug("representation IRI: "+rIRI);

            logger.debug("thing name: "+thing.name);
            logger.debug("thing type: "+thing.type);

            logger.debug("thing type URI: "+thingTypeURI);
            logger.debug("thing type IRI: "+thingTypeIRI);

            Set<Statement> thingRepresentation = new HashSet<Statement>();
            thingRepresentation.add(factory.createStatement(thingIRI, factory.createIRI(REPRESENTS), rIRI));

            thingRepresentation.add(factory.createStatement(rIRI, factory.createIRI(RDF_TYPE), thingTypeIRI));
            thingRepresentation.add(factory.createStatement(rIRI, factory.createIRI(IS_REPRESENTED_BY), thingIRI));
            thingRepresentation.add(factory.createStatement(rIRI, factory.createIRI(THING_NAME), factory.createLiteral(thing.name)));


            return thingRepresentation;
        }
        catch(Exception e){
            logger.error("error adding agora thing representation ", e);
            return new HashSet<Statement>();
        }
    }
}
