package sk.intersoft.vicinity.platform.semantic.lifting.model;

import org.json.JSONArray;
import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.ThingDescription;
import sk.intersoft.vicinity.platform.semantic.service.resource.ServiceResponse;

import java.util.ArrayList;
import java.util.List;

public class ThingsLifterResult {
    public ThingDescription thing = null;
    public JSONObject thingJSON = null;
    public List<String> errors = new ArrayList<String>();

    public ThingsLifterResult(ThingDescription thing,
                              JSONObject thingJSON){
        this.thing = thing;
        this.thingJSON = thingJSON;
    }

    public ThingsLifterResult(List<String> errors){
        this.errors = errors;
    }

    public JSONArray failureArray() {
        JSONArray list = new JSONArray();
        for(String error : errors) {
            list.put(ServiceResponse.encode(error));
        }
        return list;
    }

    public JSONObject failure() {
        JSONObject out = new JSONObject();
        out.put("errors", failureArray());
        return out;
    }
}
