package sk.intersoft.vicinity.platform.semantic;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;

import java.util.*;

public class TestSimulator {
    final static Logger logger = LoggerFactory.getLogger(TestSimulator.class.getName());

    Repository repository = Repository.getInstance();
    ValueFactory factory = SimpleValueFactory.getInstance();

    public List<String> contexts() throws Exception {
        List<String> ctxs = new ArrayList<>();
        try{
            RepositoryConnection connection = repository.getConnection();
            try{
                RepositoryResult<Resource> contexts = connection.getContextIDs();
                if(contexts.hasNext()){
                    while(contexts.hasNext()){
                        String r = contexts.next().stringValue();
                        if(r.contains("things")){
                            ctxs.add(r);
                        }
                    }
                }
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
        Collections.shuffle(ctxs);
        return ctxs;
    }

    public JSONObject test() {
        logger.info("TEST SIMULATOR");
        JSONObject result = new JSONObject();
        try{
            List<String> ctxs = contexts();
            if(!ctxs.isEmpty()){

                String ctx = ctxs.get(0);
                System.out.println("selecting random context: "+ctx);
                Graph g = repository.loadGraph(ctx, ctx);
                result.put("context", ctx);
                result.put("graph", g.size());
            }
        }
        catch(Exception e){
            logger.error("", e);
            result.put("error", e.getMessage());
        }
        return result;

    }
}
