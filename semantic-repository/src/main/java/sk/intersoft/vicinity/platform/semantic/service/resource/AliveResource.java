package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class AliveResource extends ServerResource {

    @Get("txt")
    public String doSomeGet() throws Exception {
        return "SEMANTIC REPO IS ALIVE";
    }

}
