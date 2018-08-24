package sk.intersoft.vicinity.platform.semantic;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;

public class Repository {
    HTTPRepository repository = null;
    private static Repository instance = null;
    final static Logger logger = LoggerFactory.getLogger(Repository.class.getName());

    public Repository() {
    }
    public Repository(String endpoint) {
        logger.info("CREATING HTTP REPOSITORY INSTANCE WITH: "+endpoint);
        this.repository = new HTTPRepository(endpoint);
    }

    public static Repository getInstance(){
        String endpoint = System.getProperty("graphdb.endpoint");
//        logger.info("CREATING REPOSITORY INSTANCE: "+endpoint);
//        if(instance == null) {
//            if(endpoint != null){
//                logger.info("REPOSITORY ENDPOINT: "+endpoint);
//                instance = new Repository(endpoint);
//            }
//            else {
//                logger.info("REPOSITORY ENDPOINT IS NOT SET!!! :: "+endpoint);
//            }
//        }
//        logger.info("singleton instance");
//        return instance;

        logger.info("not singleton instance");
        return new Repository(endpoint);
    }

    public RepositoryConnection getConnection() throws Exception {
        return repository.getConnection();
    }
    public void close(RepositoryConnection connection) throws Exception {
        try{
            connection.close();
        }
        catch(Exception e){
            logger.error("CLOSE EXCEPTION", e);
        }
        finally {
            connection.close();
        }
    }

    public void clear() throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{
            connection.clear();
        }
        catch(Exception e){
            logger.error("CLEAR EXCEPTION", e);
        }
        finally {
            connection.close();
        }
    }

    public void clearInstances() throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{
            connection.clear();
        }
        catch(Exception e){
            logger.error("CLEAR INSTANCE EXCEPTION", e);

        }
        finally {
            connection.close();
        }
    }

    public Graph loadGraph(String baseURI, String contextURI) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        Graph graph = new Graph(baseURI);
        try{
            RepositoryConnection connection = repository.getConnection();
            try{
                RepositoryResult<Statement> result =
                        connection.getStatements(null, null, null, true, factory.createIRI(contextURI));
                logger.debug("GRAPH LOADER ["+factory.createIRI(contextURI)+"] .. triples .. has next: "+result.hasNext());
                if(result.hasNext()){
                    while (result.hasNext()) {
                        Statement st = result.next();


                        graph.add(st);
                    }
                    return graph;
                }
            }
            catch(Exception e){
                logger.error("LOAD GRAPH EXCEPTION", e);
            }
            finally {
                connection.close();
            }
        }
        catch(Exception e){
            logger.error("EXCEPTION", e);
        }
        return null;
    }

}
