package sk.intersoft.vicinity.platform.semantic.service;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

public class SemanticRepositoryComponent extends Component {
    public SemanticRepositoryComponent() throws Exception {
        Server server = getServers().add(Protocol.HTTP, Integer.parseInt(System.getProperty("server.port")));
        server.getContext().getParameters().add("maxThreads", "100");

        getDefaultHost().attach("/semantic-repository", new SemanticRepositoryApplication());

    }
}
