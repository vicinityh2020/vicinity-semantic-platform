package sk.intersoft.vicinity.platform.semantic.ontology;

public class OntologyResource {
    public static String thingInstancePrefixed(String oid) {
        return Namespaces.prefixed(NamespacePrefix.data, oid);
    }

    public static String thingInstanceURI(String oid) {
        String prefixed = thingInstancePrefixed(oid);
        return Namespaces.toURI(prefixed);
    }


}
