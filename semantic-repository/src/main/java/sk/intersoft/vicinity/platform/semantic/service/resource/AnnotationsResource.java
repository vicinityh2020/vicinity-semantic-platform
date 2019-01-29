package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;

import java.util.Set;

public class AnnotationsResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(AnnotationsResource.class.getName());

    private JSONArray toArray(Set<String> annotations) {
        JSONArray array = new JSONArray();
        for(String a: annotations) {
            array.put(a);
        }
        return array;
    }

    @Get("json")
    public String getAnnotations() throws Exception {
        Thing2Ontology handler = new Thing2Ontology();
        try{
            JSONArray deviceArray = toArray(handler.getDeviceTypes());
            JSONArray serviceArray = toArray(handler.getServiceTypes());
            JSONArray propertiesArray = toArray(handler.getProperties());
            JSONArray locationsArray = toArray(handler.getLocationTypes());
            JSONObject result = new JSONObject();
            result.put("device", deviceArray);
            result.put("service", serviceArray);
            result.put("property", propertiesArray);
            result.put("location", locationsArray);
            return ServiceResponse.success(result).toString(2);
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }
    }

}
