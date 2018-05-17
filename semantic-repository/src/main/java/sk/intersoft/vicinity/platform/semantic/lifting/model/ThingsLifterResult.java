package sk.intersoft.vicinity.platform.semantic.lifting.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ThingsLifterResult {
    public JSONObject thing = null;
    public List<String> errors = new ArrayList<String>();

    public ThingsLifterResult(JSONObject thing, List<String> errors){
        this.thing = thing;
        this.errors = errors;
    }
    public ThingsLifterResult(JSONObject thing){
        this.thing = thing;
    }


    public JSONObject failure() {
        JSONObject out = new JSONObject();
        JSONArray list = new JSONArray();
        for(String error : errors) {
            list.put(error);
        }
        out.put("errors", list);
        return out;
    }
}
