package sk.intersoft.vicinity.platform.semantic.ontology;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Namespaces {
    public static final String vicinity = "http://vicinity.eu";
    public static final String data = vicinity+"/data";

    public static final String core = "http://iot.linkeddata.es/def/core";
    public static final String wot = "http://iot.linkeddata.es/def/wot";
    public static final String sosa = "http://www.w3.org/ns/sosa/";
    public static final String ssn = "http://purl.oclc.org/NET/ssnx/ssn";

    public static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
    public static final String rdfs = "http://www.w3.org/2000/01/rdf-schema";
    public static final String xsd = "http://www.w3.org/2001/XMLSchema";

    private static final Map<String, String> uriMapping =
            Collections.unmodifiableMap(new HashMap<String, String>() {{
                put(NamespacePrefix.rdf, rdf);
                put(NamespacePrefix.rdfs, rdfs);
                put(NamespacePrefix.xsd, xsd);

                put(NamespacePrefix.core, core);
                put(NamespacePrefix.wot, wot);
                put(NamespacePrefix.sosa, sosa);
                put(NamespacePrefix.ssn, ssn);
                put(NamespacePrefix.data, data);
            }});

    public static String prefixed(String prefix, String value) {
        return prefix + ":" + value;
    }

    public static String toURI(String prefixed) {
        String[] parts = prefixed.split(":");

        if(parts.length == 2){
            String prefix = parts[0];
            String value = parts[1];

            String base = uriMapping.get(prefix);
            if(base != null){
                if(base.endsWith("/")){
                    return base+value;
                }
                else {
                    return base+"#"+value;
                }
            }
        }
        return prefixed;
    }

    public static String getPrefix(String uri) {
        for (Map.Entry<String, String> entry : uriMapping.entrySet()) {
            String prefix = entry.getKey();
            String uriPart = entry.getValue();
            if(uriPart.equals(uri)) return prefix;
        }
        return null;

    }

    public static List<String> sharpSplitURI(String uri) {
        String[] parts = uri.split("#");

        if(parts.length == 2){
            String uriPart = parts[0];
            String value = parts[1];

            String prefix = getPrefix(uriPart);
            if(prefix != null){
                List<String> out = new ArrayList<String>();
                out.add(prefix);
                out.add(value);
                return out;
            }
        }

        return new ArrayList<String>();
    }


    public static List<String> slashSplitURI(String uri) {
        String[] parts = uri.split("/");



        if(parts.length > 1){
            String value = parts[parts.length - 1];
            List<String> uriParts = new ArrayList<String>();
            for(int i = 0; i < (parts.length - 1); i++){
                uriParts.add(parts[i]);
            }
            String uriPart = StringUtils.join(uriParts, "/")+"/";

            String prefix = getPrefix(uriPart);
            if(prefix != null){
                List<String> out = new ArrayList<String>();
                out.add(prefix);
                out.add(value);
                return out;
            }
        }

        return new ArrayList<String>();
    }

    public static List<String> splitURI(String uri) {
        List<String> sharp = sharpSplitURI(uri);
        if(sharp.size() == 2) return sharp;

        List<String> slash = slashSplitURI(uri);
        if(slash.size() == 2) return slash;

        return new ArrayList<String>();
    }

    public static String toPrefixed(String uri) {
        List<String> parts = splitURI(uri);

        if(parts.size() == 2){
            String prefix = parts.get(0);
            String value = parts.get(1);

            return prefix+":"+value;
        }
        else {

        }
        return uri;
    }

    public static String valueFromPrefixed(String prefixed) {
        String[] prefixParts = prefixed.split(":");
        if(prefixParts.length == 2) {
            return prefixParts[1];
        }
        else{
            return null;
        }
    }
}
