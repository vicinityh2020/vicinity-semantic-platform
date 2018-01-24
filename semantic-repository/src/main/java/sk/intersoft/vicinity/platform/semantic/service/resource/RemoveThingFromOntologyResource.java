package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;

import java.util.logging.Level;

public class RemoveThingFromOntologyResource extends ServerResource {
    @Delete()
    public String doRemoval() throws Exception {

        try{
            String oid = getAttribute("oid");

            getLogger().log(Level.INFO, "EXECUTE DELETE FOR: ["+oid+"]");

            Thing2Ontology handler = new Thing2Ontology();
            boolean result = handler.delete(oid.trim());
            return ServiceResponse.success(ServiceResponse.REMOVED, result+"").toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }
    }
}
