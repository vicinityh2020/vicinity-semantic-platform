package sk.intersoft.vicinity.platform.semantic.service;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import sk.intersoft.vicinity.platform.semantic.service.resource.*;

public class SemanticRepositoryApplication extends Application {
    public static final String TEST = "/test";
    public static final String ALIVE = "/alive";
    public static final String SPARQL = "/sparql";
    public static final String ANNOTATIONS = "/annotations";
    public static final String ANNOTATIONS_HIERARCHY = "/annotations/hierarchy";
    public static final String VALIDATE_TD = "/td/validate";
    public static final String CREATE_TD = "/td/create";
    public static final String REMOVE_TD = "/td/remove/{oid}";

    public static final String CREATE_AGENTS = "/agents/create";
    public static final String REMOVE_AGENTS = "/agents/delete";

    private ChallengeAuthenticator createApiGuard(Restlet next) {

        ChallengeAuthenticator apiGuard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "realm");

        apiGuard.setNext(next);

        // In case of anonymous access supported by the API.
        apiGuard.setOptional(true);

        return apiGuard;
    }

    public Router createApiRouter() {
        Router apiRouter = new Router(getContext());
        apiRouter.attach(TEST, TestResource.class);
        apiRouter.attach(TEST+"/", TestResource.class);

        apiRouter.attach(ALIVE, AliveResource.class);
        apiRouter.attach(ALIVE+"/", AliveResource.class);

        apiRouter.attach(SPARQL, SPARQLResource.class);
        apiRouter.attach(SPARQL+"/", SPARQLResource.class);

        apiRouter.attach(ANNOTATIONS, AnnotationsResource.class);
        apiRouter.attach(ANNOTATIONS+"/", AnnotationsResource.class);

        apiRouter.attach(ANNOTATIONS_HIERARCHY, AnnotationsHierarchyResource.class);
        apiRouter.attach(ANNOTATIONS_HIERARCHY+"/", AnnotationsHierarchyResource.class);

        apiRouter.attach(VALIDATE_TD, ValidateThingResource.class);
        apiRouter.attach(VALIDATE_TD+"/", ValidateThingResource.class);

        apiRouter.attach(CREATE_TD, Thing2OntologyResource.class);
        apiRouter.attach(CREATE_TD+"/", Thing2OntologyResource.class);

        apiRouter.attach(REMOVE_TD, RemoveThingFromOntologyResource.class);
        apiRouter.attach(REMOVE_TD+"/", RemoveThingFromOntologyResource.class);

        apiRouter.attach(CREATE_AGENTS, Agents2OntologyResource.class);
        apiRouter.attach(CREATE_AGENTS+"/", Agents2OntologyResource.class);

        apiRouter.attach(REMOVE_AGENTS, RemoveAgentsFromOntologyResource.class);
        apiRouter.attach(REMOVE_AGENTS+"/", RemoveAgentsFromOntologyResource.class);

        return apiRouter;
    }

    public Restlet createInboundRoot() {

        Router apiRouter = createApiRouter();
        ChallengeAuthenticator guard = createApiGuard(apiRouter);
        return guard;
    }

}
