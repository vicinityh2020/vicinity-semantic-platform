package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;
import sk.intersoft.vicinity.platform.semantic.lifting.model.AnnotationsHierarchy;

import java.util.Set;

public class AnnotationsHierarchyResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(AnnotationsHierarchyResource.class.getName());

    @Get("json")
    public String getAnnotations() throws Exception {
        AnnotationsHierarchy ann = new AnnotationsHierarchy();
        try{
            return ServiceResponse.success(ann.dump()).toString(2);
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }
    }

}
