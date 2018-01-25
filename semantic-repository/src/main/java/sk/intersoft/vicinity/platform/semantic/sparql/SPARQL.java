package sk.intersoft.vicinity.platform.semantic.sparql;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.Repository;

import java.io.ByteArrayOutputStream;

public class SPARQL {
    Repository repository = Repository.getInstance();
    final static Logger logger = LoggerFactory.getLogger(SPARQL.class.getName());

    public JSONObject query(String query) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(new SPARQLResultsJSONWriter(out));

            return new JSONObject(out.toString());
        }
        catch (Exception e){
            logger.error("EXCEPTION", e);
            throw e;
        }
        finally {
            connection.close();
        }
    }

}
