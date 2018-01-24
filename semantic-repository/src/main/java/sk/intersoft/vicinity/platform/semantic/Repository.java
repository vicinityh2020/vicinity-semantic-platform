package sk.intersoft.vicinity.platform.semantic;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;

public class Repository {
    HTTPRepository repository = null;
    private static Repository instance = null;

    public Repository() {
    }
    public Repository(String endpoint) {
        System.out.println("CREATING HTTP REPOSITORY INSTANCE WITH: "+endpoint);
        this.repository = new HTTPRepository(endpoint);
    }

    public static Repository getInstance(){
        String endpoint = System.getProperty("graphdb.endpoint");
        System.out.println("CREATING REPOSITORY INSTANCE: "+endpoint);
        if(instance == null) {
            if(endpoint != null){
                System.out.println("REPOSITORY ENDPOINT: "+endpoint);
                instance = new Repository(endpoint);
            }
            else {
                System.out.println("REPOSITORY ENDPOINT IS NOT SET!!! :: "+endpoint);
            }
        }
        return instance;
    }

    public RepositoryConnection getConnection() throws Exception {
        return repository.getConnection();
    }
    public void close(RepositoryConnection connection) throws Exception {
        try{
            connection.close();
        }
        catch(Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        finally {
            connection.close();
        }
    }

    public Graph loadGraph(String baseURI, String contextURI) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        System.out.println("loading graph from context: ["+contextURI+"]");
        Graph graph = new Graph(baseURI);
        try{
            RepositoryConnection connection = repository.getConnection();
            try{
                RepositoryResult<Statement> result =
                        connection.getStatements(null, null, null, true, factory.createIRI(contextURI));
                while (result.hasNext()) {
                    Statement st = result.next();
                    graph.add(st);
                }

                return graph;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                connection.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
