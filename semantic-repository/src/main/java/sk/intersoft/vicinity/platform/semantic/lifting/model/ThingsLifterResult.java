package sk.intersoft.vicinity.platform.semantic.lifting.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ThingsLifterResult {
    public JSONObject lifting = null;
    public List<String> errors = new ArrayList<String>();

    public ThingsLifterResult(JSONObject lifting, List<String> errors){
        this.lifting = lifting;
        this.errors = errors;
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
