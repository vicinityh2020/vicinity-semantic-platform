package sk.intersoft.vicinity.platform.semantic.ontology;

public class OntologyResource {
    public static String thingInstancePrefixed(String oid) {
        return Namespaces.prefixed(NamespacePrefix.thing, oid);
    }

    public static String thingInstanceURI(String oid) {
        String prefixed = thingInstancePrefixed(oid);
        return Namespaces.toURI(prefixed);
    }

    public static String thingDescriptionURI(String id) {
        return Namespaces.toURI(
                        Namespaces.prefixed(
                                NamespacePrefix.thingDescription,
                                id));
    }
}
