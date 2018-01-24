package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;

import java.util.logging.Level;

public class Thing2OntologyResource extends ServerResource {
    @Post()
    public String execute(Representation entity) throws Exception {

        getLogger().log(Level.INFO, "EXECUTE POST:");

        Thing2Ontology handler = new Thing2Ontology();
        try{
            JSONObject result = handler.populate(entity.getText());
            return ServiceResponse.success(result).toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }
}
