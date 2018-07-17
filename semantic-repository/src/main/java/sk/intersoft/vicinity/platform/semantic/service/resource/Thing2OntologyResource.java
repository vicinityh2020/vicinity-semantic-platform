package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;

import java.util.logging.Level;

public class Thing2OntologyResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(Thing2OntologyResource.class.getName());

    @Post()
    public String create(Representation entity) throws Exception {

        logger.info("=============================");
        logger.info("=============================");
        logger.info("EXECUTE Thing2Ontology CREATE");

        Thing2Ontology handler = new Thing2Ontology();
        try{
            String payload = entity.getText();
            logger.info("CREATE PAYLOAD: \n" +payload);
            JSONObject result = handler.create(payload);
            return ServiceResponse.success(result).toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }

    @Put()
    public String update(Representation entity) throws Exception {

        logger.info("=============================");
        logger.info("=============================");
        logger.info("EXECUTE Thing2Ontology UPDATE");

        Thing2Ontology handler = new Thing2Ontology();
        try{
            String payload = entity.getText();
            logger.info("UPDATE PAYLOAD: \n" +payload);
            JSONObject result = handler.update(payload);
            return ServiceResponse.success(result).toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }
}
