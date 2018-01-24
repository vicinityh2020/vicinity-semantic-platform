package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import sk.intersoft.vicinity.platform.semantic.sparql.SPARQL;

import java.util.logging.Level;

public class SPARQLResource extends ServerResource {
    @Post()
    public String execute(Representation entity) throws Exception {



        SPARQL sparql = new SPARQL();
        try{
            String queryJSON = entity.getText();
            JSONObject object = new JSONObject(queryJSON);
            String query = object.getString("query");
            getLogger().log(Level.INFO, "EXECUTE SPARQL: \n"+query);

            JSONObject result = sparql.query(query);
            return ServiceResponse.success(result).toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }

}
