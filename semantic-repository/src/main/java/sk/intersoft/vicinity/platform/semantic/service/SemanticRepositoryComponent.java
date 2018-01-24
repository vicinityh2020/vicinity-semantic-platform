package sk.intersoft.vicinity.platform.semantic.service;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class SemanticRepositoryComponent extends Component {
    public SemanticRepositoryComponent() throws Exception {
        getServers().add(Protocol.HTTP, Integer.parseInt(System.getProperty("server.port")));
        getDefaultHost().attach("/semantic-repository", new SemanticRepositoryApplication());

    }
}
