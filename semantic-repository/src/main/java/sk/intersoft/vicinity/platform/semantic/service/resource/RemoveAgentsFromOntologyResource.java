package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Agent2Ontology;
import sk.intersoft.vicinity.platform.semantic.utils.DateTimeUtil;

public class RemoveAgentsFromOntologyResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(RemoveAgentsFromOntologyResource.class.getName());

    @Post()
    public String delete(Representation entity) throws Exception {

        logger.info("=============================");
        logger.info("=============================");
        logger.info("EXECUTE Agent2Ontology DELETE");

        Agent2Ontology handler = new Agent2Ontology();
        try{
            String payload = entity.getText();
            logger.info("DELETE PAYLOAD: \n" +payload);

            long start = DateTimeUtil.millis();

            JSONObject result = handler.delete(payload);

            long end = DateTimeUtil.duration(start);
            logger.info("DELETE TOOK: " +DateTimeUtil.format(end));

            return ServiceResponse.success(result).toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }

}
