package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;

public class ValidateThingResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(ValidateThingResource.class.getName());

    @Post()
    public String validate(Representation entity) throws Exception {

        logger.info("EXECUTE Thing VALIDATOR");

        Thing2Ontology handler = new Thing2Ontology();
        try{
            ThingsLifterResult result = handler.validateAndLift(entity.getText());
            if(result.errors.size() > 0){
                return ServiceResponse.failure(result.failure()).toString();
            }
            else{
                return ServiceResponse.success("Thing Description is valid").toString();
            }

        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }

}
