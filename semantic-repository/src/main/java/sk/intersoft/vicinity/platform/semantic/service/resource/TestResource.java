package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.TestSimulator;
import sk.intersoft.vicinity.platform.semantic.Thing2Ontology;

public class TestResource extends ServerResource {
    final static Logger logger = LoggerFactory.getLogger(TestResource.class.getName());

    @Get()
    public String test() throws Exception {

        logger.info("=============================");
        logger.info("=============================");
        logger.info("EXECUTE TEST");

        TestSimulator handler = new TestSimulator();
        try{
            JSONObject result = handler.test();
            return ServiceResponse.success(result).toString();
        }
        catch(Exception e){
            return ServiceResponse.failure(e).toString();
        }

    }

}
