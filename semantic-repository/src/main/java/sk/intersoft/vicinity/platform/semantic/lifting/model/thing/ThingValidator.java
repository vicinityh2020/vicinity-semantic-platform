package sk.intersoft.vicinity.platform.semantic.lifting.model.thing;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ThingValidator {
    final static Logger logger = LoggerFactory.getLogger(ThingValidator.class.getName());

    public boolean failOnError;
    public ThingDescription thing = null;
    public List<String> errors = new ArrayList<String>();

    public ThingValidator(boolean failOnError){
        this.failOnError = failOnError;
    }

    public ThingDescription create(JSONObject object) {
        try{
            ThingDescription thing = ThingDescription.create(object, this);
            if(!failed() && thing != null){
                this.thing = thing;
                return thing;
            }
        }
        catch(Exception e){
            logger.error("", e);
        }
        return null;
    }

    public String identify(String id, JSONObject object) {
        if(id != null) return "["+id+"]";
        return object.toString();
    }

    public boolean error(String error) throws Exception {
        String encode = error.replaceAll("\"", "\'");
        errors.add(encode);
        if(failOnError) {
            throw new Exception(encode);
        }
        return true;
    }

    public boolean failed(){
        return (errors.size() > 0);
    }

    public JSONObject failureMessage() {
        JSONObject out = new JSONObject();
        JSONArray list = new JSONArray();
        for(String error : errors) {
            list.put(error);
        }
        out.put("errors", list);
        return out;
    }

}
