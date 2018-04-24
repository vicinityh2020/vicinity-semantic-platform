package sk.intersoft.vicinity.platform.semantic.lifting;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingJSON;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;
import sk.intersoft.vicinity.platform.semantic.ontology.NamespacePrefix;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;
import sk.intersoft.vicinity.platform.semantic.util.JSONUtil;
import sk.intersoft.vicinity.platform.semantic.util.UniqueID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ThingsLifter {
    public static String JSONLD_SCHEMA_LOCATION = System.getProperty("jsonld.schema.location");

    final static Logger logger = LoggerFactory.getLogger(ThingsLifter.class.getName());
    private Set<String> types;
    private Set<String> properties;

    public ThingsLifter(Set<String> types, Set<String> properties) {
        this.types = types;
        this.properties = properties;

        logger.info("THINGS-LIFTER INITIALIZED WITH: ");
        logger.info("types: " + this.types);
        logger.info("properties: " + this.properties);
    }

    private void instantiateObjects(JSONObject object) {
        if (object.has(ThingJSON.idAnnotation)) {
        } else {
            String id = UniqueID.create();
            String instance = Namespaces.prefixed(NamespacePrefix.data, id);
            object.put(ThingJSON.idAnnotation, instance);
        }
        for (String key : object.keySet()) {
            Object value = object.get(key);
            if (value instanceof JSONObject) {
                instantiateObjects((JSONObject) value);
            } else if (value instanceof JSONArray) {
                JSONArray array = object.getJSONArray(key);
                Iterator i = array.iterator();
                while (i.hasNext()) {
                    Object item = i.next();
                    if (item instanceof JSONObject) {
                        instantiateObjects((JSONObject) item);
                    }
                }
            }

        }
    }


    private void resolveType(JSONObject object, ArrayList<String> errors) {

        String type = JSONUtil.getString(ThingJSON.type, object);
        if (type != null) {
            String thingType = Namespaces.prefixed(NamespacePrefix.core, type);
            if (types.contains(thingType)) {
                object.put(ThingJSON.typeAnnotation, thingType);
                object.remove(ThingJSON.type);
            } else {
                errors.add("unknown ontology thing [type]: [" + type + "]");
            }
        } else {
            errors.add("missing thing [type]");
        }
    }

    private void resolveAffect(JSONObject object, String key, ArrayList<String> errors) {

        String affect = JSONUtil.getString(key, object);
        if (affect != null) {
            String instance = Namespaces.prefixed(NamespacePrefix.core, affect);
            if (properties.contains(instance)) {
                object.put(key, instance);
            } else {
                errors.add("unknown ontology property [" + key + "]: [" + affect + "] in: " + object.toString());
            }
        } else {
            errors.add("missing property [" + key + "] in: " + object.toString());
        }
    }

    private void resolveData(JSONObject object, String key, boolean mandatory, ArrayList<String> errors) {
        JSONObject data = JSONUtil.getObject(key, object);
        if(mandatory && data == null){
            errors.add("missing ["+key+"] in: "+object.toString());
        }
        else if (data != null){
            data.put(ThingJSON.jsonSource, data.toString());
        }
    }

    private void resolveLink(JSONObject object, String linkType, ArrayList<String> errors) {

        String href = JSONUtil.getString(ThingJSON.href, object);
        if(href == null) {
            errors.add("missing [href] in: "+object.toString());
        }

        resolveData(object, ThingJSON.output, true, errors);
        resolveData(object, ThingJSON.input, (linkType.equals(ThingJSON.writeLink)), errors);
    }

    private void resolveLinks(JSONObject object, ArrayList<String> errors) {

        JSONObject read = JSONUtil.getObject(ThingJSON.readLink, object);
        JSONObject write = JSONUtil.getObject(ThingJSON.writeLink, object);
        if(read == null && write == null) {
            errors.add("at least one link must be provided! missing [read_link] or [write_link] in: "+object.toString());
        }

        if(read != null){
            resolveLink(read, ThingJSON.readLink, errors);
        }
        if(write != null){
            resolveLink(write, ThingJSON.writeLink, errors);
        }
    }

    private void liftProperty(JSONObject object, ArrayList<String> errors)  {
        String pid = JSONUtil.getString(ThingJSON.pid, object);
        if(pid == null) errors.add("missing [pid] in property description: "+object.toString());

        resolveAffect(object, ThingJSON.monitors, errors);
        resolveLinks(object, errors);
        object.remove(ThingJSON.type);
        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Property"));

    }

    private void liftProperties(JSONObject thing, ArrayList<String> errors)  {
        if(thing.has(ThingJSON.properties)) {
            JSONArray properties = thing.getJSONArray(ThingJSON.properties);
            Iterator i = properties.iterator();
            while(i.hasNext()){
                JSONObject property = (JSONObject)i.next();
                liftProperty(property, errors);
            }
        }
    }


    private void liftAction(JSONObject object, ArrayList<String> errors)  {
        String aid = JSONUtil.getString(ThingJSON.aid, object);
        if(aid == null) errors.add("missing [aid] in action description: "+object.toString());

        resolveAffect(object, ThingJSON.affects, errors);
        resolveLinks(object, errors);
        object.remove(ThingJSON.type);
        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Action"));

    }

    private void liftActions(JSONObject thing, ArrayList<String> errors)  {
        if(thing.has(ThingJSON.actions)) {
            JSONArray actions = thing.getJSONArray(ThingJSON.actions);
            Iterator i = actions.iterator();
            while(i.hasNext()){
                JSONObject action = (JSONObject)i.next();
                liftAction(action, errors);
            }
        }
    }

    private void liftEvent(JSONObject object, ArrayList<String> errors)  {
        String eid = JSONUtil.getString(ThingJSON.eid, object);
        if(eid == null) errors.add("missing [eid] in: "+object.toString());

        resolveAffect(object, ThingJSON.monitors, errors);

        JSONObject output = JSONUtil.getObject(ThingJSON.output, object);
        if(output == null) errors.add("missing [output] in: "+object.toString());

        object.remove(ThingJSON.type);
        object.put(ThingJSON.typeAnnotation, Namespaces.prefixed(NamespacePrefix.wot, "Event"));

        resolveData(object, ThingJSON.output, true, errors);

    }

    private void liftEvents(JSONObject thing, ArrayList<String> errors)  {
        if(thing.has(ThingJSON.events)) {
            JSONArray events = thing.getJSONArray(ThingJSON.events);
            Iterator i = events.iterator();
            while(i.hasNext()){
                JSONObject event = (JSONObject)i.next();
                liftEvent(event, errors);
            }
        }
    }

    private void lift(JSONObject thing, ArrayList<String> errors) {

        thing.put("@context", JSONLD_SCHEMA_LOCATION);


        String oid = JSONUtil.getString(ThingJSON.oid, thing);
        if(oid == null) errors.add("missing thing [oid]");

        thing.put(ThingJSON.idAnnotation, Namespaces.prefixed(NamespacePrefix.data, oid));

        resolveType(thing, errors);
        liftProperties(thing, errors);
        liftActions(thing, errors);
        liftEvents(thing, errors);

        instantiateObjects(thing);

    }

    public ThingsLifterResult lift(String data) {
        ArrayList<String> errors = new ArrayList<String>();

        JSONObject thing = null;
        try {
            thing = new JSONObject(data);
            lift(thing, errors);
        }
        catch(Exception e) {
            errors.add("unable to parse thing json");
        }

        return new ThingsLifterResult(thing, errors);

    }
}
