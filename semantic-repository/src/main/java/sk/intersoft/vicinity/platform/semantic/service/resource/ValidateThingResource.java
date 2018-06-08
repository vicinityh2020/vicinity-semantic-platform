package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;
import sk.intersoft.vicinity.platform.semantic.utils.JSONUtil;

import java.util.ArrayList;
import java.util.List;

public class ValidateThingResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(ValidateThingResource.class.getName());

    private JSONObject wrap(JSONObject thing) {
        JSONObject result = new JSONObject();

        try{
            result.put("oid", thing.getString("oid"));
        }
        catch(Exception e){
            result.put("oid", "unknown!");
        }

        return result;
    }


    private JSONObject success(JSONObject result) {
        result.put("message", "Thing is valid");
        return result;
    }
    private JSONObject fail(JSONObject result, String message) {
        JSONArray errors = new JSONArray();
        errors.put(ServiceResponse.encode(message));
        result.put("errors", errors);
        return result;
    }
    private JSONObject fail(JSONObject result, JSONArray errors) {
        result.put("errors", errors);
        return result;
    }

    private JSONObject validate(JSONObject thing) {
        Thing2Ontology handler = new Thing2Ontology();
        thing.put("adapter-id", "simulated-adapter-id");
        JSONObject wrapper = wrap(thing);
        try{
            ThingsLifterResult result = handler.validateAndLift(thing.toString());
            if(result.errors.size() > 0){
                return fail(wrapper, result.failureArray());
            }
            else{
                return success(wrapper);
            }

        }
        catch(Exception e){
            return fail(wrapper, e.getMessage());
        }

    }

    private JSONArray validate(List<JSONObject> things) {
        JSONArray results = new JSONArray();
        for(JSONObject t : things){
            results.put(validate(t));
        }
        return results;
    }


    @Post()
    public String validate(Representation entity) throws Exception {

        logger.info("EXECUTE Thing VALIDATOR");

        try{
            JSONObject object = new JSONObject(entity.getText());
            if(object.has("adapter-id") && object.has("thing-descriptions")){
                logger.info("VALIDATING ADAPTER OBJECTS");
                JSONArray array = object.getJSONArray("thing-descriptions");
                List<JSONObject> things = JSONUtil.getObjectArray("thing-descriptions", object);
                if(things != null){
                    return ServiceResponse.success(validate(things)).toString(2);
                }
                else {
                    throw new Exception("unable to get array of thing descriptions!");
                }
            }
            else {
                logger.info("VALIDATING SINGLE THING");
                return ServiceResponse.success(validate(object)).toString(2);
            }
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }

}
