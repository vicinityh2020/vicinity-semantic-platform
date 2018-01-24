package sk.intersoft.vicinity.platform.semantic.sparql;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.Repository;

import java.io.ByteArrayOutputStream;

public class SPARQL {
    Repository repository = Repository.getInstance();

    public JSONObject query(String query) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(new SPARQLResultsJSONWriter(out));

            return new JSONObject(out.toString());
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        finally {
            connection.close();
        }
    }

}
