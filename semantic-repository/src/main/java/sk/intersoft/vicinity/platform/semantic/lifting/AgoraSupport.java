package sk.intersoft.vicinity.platform.semantic.lifting;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Ontology2Thing;
import sk.intersoft.vicinity.platform.semantic.Repository;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.lifting.model.AgoraMapping;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.DataSchema;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.InteractionPattern;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.ThingDescription;
import sk.intersoft.vicinity.platform.semantic.ontology.NamespacePrefix;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.ontology.OntologyResource;
import sk.intersoft.vicinity.platform.semantic.util.UniqueID;

import java.util.*;

public class AgoraSupport {
    final static Logger logger = LoggerFactory.getLogger(AgoraSupport.class.getName());

    private ThingDescription thing = null;

    private static String THING_DESCRIPTION_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "ThingDescription"));
    private static String VALUE_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "Value"));
    private static String HAS_VALUE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.core, "hasValue"));

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
    private static String MAPPING_ROOT_MODE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.mappings, "rootMode"));

    private static String LINK_CLASS = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "Link"));
    private static String HREF = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "href"));
    private static String MEDIA_TYPE = Namespaces.toURI(Namespaces.prefixed(NamespacePrefix.wot, "mediaType"));


    public AgoraSupport(ThingDescription thing) {
        this.thing = thing;
    }

    Repository repository = Repository.getInstance();
    ValueFactory factory = SimpleValueFactory.getInstance();

    private String getObjectTDURI(String oid) {
        return OntologyResource.thingDescriptionURI(oid);
    }

    private void addStatements(Set<Statement> statements,
                               IRI context) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        logger.debug("ADDING STATEMENTS TO CONTEXT [" + context + "]");
        try {
            connection.begin();
            for (Statement s : statements) {
                logger.debug("adding: " +
                        Namespaces.toPrefixed(s.getSubject().stringValue()) + " " +
                        Namespaces.toPrefixed(s.getPredicate().stringValue()) + " " +
                        Namespaces.toPrefixed(s.getObject().stringValue()));
                connection.add(s, context);
            }
            connection.commit();
        } catch (Exception e) {
            logger.error("", e);
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    private void addThingTD(IRI thingIRI,
                            IRI thingContextIRI) throws Exception {
        IRI tdIRI = factory.createIRI(getObjectTDURI(thing.oid));

        logger.debug("adding thing TD");
        logger.debug("Thing IRI: " + thingIRI);
        logger.debug("Thing Context IRI: " + thingContextIRI);
        logger.debug("TD IRI: " + tdIRI);

        // thing extension:
        Set<Statement> thingExtension = new HashSet<Statement>();
        thingExtension.add(factory.createStatement(thingIRI, factory.createIRI(DESCRIBED_BY), tdIRI));

        // thing description
        Set<Statement> thingDescription = new HashSet<Statement>();
        thingDescription.add(factory.createStatement(tdIRI, factory.createIRI(RDF_TYPE), factory.createIRI(THING_DESCRIPTION_CLASS)));
        thingDescription.add(factory.createStatement(tdIRI, factory.createIRI(DESCRIBES), thingIRI));
        thingDescription.add(factory.createStatement(tdIRI, factory.createIRI(IDENTIFIER), factory.createLiteral(thing.oid)));

        addStatements(thingExtension, thingContextIRI);
        addStatements(thingDescription, tdIRI);
    }

    private IRI createIRI(String prefix, String value) {
        return factory.createIRI(
                Namespaces.toURI(
                        Namespaces.prefixed(
                                prefix,
                                value)));
    }

    private void traverse(DataSchema schema, Set<AgoraMapping> mappings, List<String> path) {

    }

    private void addMappings(IRI mappingIRI, DataSchema output) {
        logger.debug("adding all mapping for: \n"+output.toString(0));
        Set<AgoraMapping> mappings = new HashSet<AgoraMapping>();

        traverse(output, mappings, new ArrayList<String>());
    }


    private void addValueTD(InteractionPattern property,
                            IRI thingContextIRI) throws Exception {
        IRI propertyIRI = factory.createIRI(property.jsonExtension.get(Ontology2Thing.URI_KEY));

        String valueId = UniqueID.create();
        IRI valueIRI = createIRI(NamespacePrefix.thing, valueId);
        IRI valueTDIRI = factory.createIRI(getObjectTDURI(valueId));


        logger.debug("adding thing TD");
        logger.debug("Property IRI: " + propertyIRI);
        logger.debug("Property Value IRI: " + valueIRI);
        logger.debug("Property Value TD IRI: " + valueTDIRI);
        logger.debug("Property Value TD mapping IRI: " + valueTDIRI);
        logger.debug("Thing Context IRI: " + thingContextIRI);

        // property extension:
        Set<Statement> propertyExtension = new HashSet<Statement>();
        propertyExtension.add(factory.createStatement(propertyIRI, factory.createIRI(HAS_VALUE), valueIRI));

        propertyExtension.add(factory.createStatement(valueIRI, factory.createIRI(RDF_TYPE), factory.createIRI(VALUE_CLASS)));
        propertyExtension.add(factory.createStatement(valueIRI, factory.createIRI(DESCRIBED_BY), valueTDIRI));

        // thing description
        Set<Statement> valueDescription = new HashSet<Statement>();
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(RDF_TYPE), factory.createIRI(THING_DESCRIPTION_CLASS)));
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(DESCRIBES), valueIRI));
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(IDENTIFIER), factory.createLiteral(valueId)));

        IRI mappingIRI = createIRI(NamespacePrefix.thingDescription, UniqueID.create());
        valueDescription.add(factory.createStatement(valueTDIRI, factory.createIRI(HAS_ACCESS_MAPPING), mappingIRI));
        valueDescription.add(factory.createStatement(mappingIRI, factory.createIRI(RDF_TYPE), factory.createIRI(ACCESS_MAPPING_CLASS)));

        IRI resourceIRI = createIRI(NamespacePrefix.thingDescription, UniqueID.create());
        valueDescription.add(factory.createStatement(mappingIRI, factory.createIRI(MAPS_RESOURCE_FROM), resourceIRI));
        valueDescription.add(factory.createStatement(resourceIRI, factory.createIRI(RDF_TYPE), factory.createIRI(LINK_CLASS)));
        valueDescription.add(factory.createStatement(resourceIRI, factory.createIRI(HREF), factory.createLiteral("/objects/"+thing.oid+"/properties/"+property.id)));
        valueDescription.add(factory.createStatement(resourceIRI, factory.createIRI(MEDIA_TYPE), factory.createLiteral("application/json")));

        addMappings(mappingIRI, property.readEndpoint.output);

//        addStatements(propertyExtension, thingContextIRI);
//        addStatements(valueDescription, valueTDIRI);

    }

    private void addValuesTD(IRI thingContextIRI) throws Exception {

        logger.debug("adding TD support for thing readable properties");

        for (Map.Entry<String, InteractionPattern> entry : thing.properties.entrySet()) {
            InteractionPattern prop = entry.getValue();
            if(prop.readEndpoint != null && prop.readEndpoint.output != null) {
                addValueTD(prop, thingContextIRI);
            }
        }

    }

    public void add(){
        logger.debug("adding support for: ["+thing.oid+"]");
        try{
            RepositoryConnection connection = repository.getConnection();
            try{
                String uri = OntologyResource.thingInstanceURI(thing.oid);
                String contextURI = OntologyResource.thingInstanceURI(thing.oid);

                Graph graph = repository.loadGraph(uri, contextURI);

                logger.debug("getting graph for OID: [" + thing.oid + "]");
                logger.debug("OID URI: "+uri);
                logger.debug("OID CONTEXT URI: "+contextURI);

                if(graph == null) throw new Exception("Missing semantic graph for thing [oid="+thing.oid+"]!");
                logger.debug("graph: \n"+graph.describe());

                IRI thingIRI = factory.createIRI(uri);
                IRI thingContextIRI = factory.createIRI(contextURI);

                logger.debug("Thing IRI: "+thingIRI);
                logger.debug("Thing context IRI: "+thingContextIRI);

                addThingTD(thingIRI, thingContextIRI);
                addValuesTD(thingContextIRI);

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
    }

}
