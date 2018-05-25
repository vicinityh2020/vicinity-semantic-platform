package sk.intersoft.vicinity.platform.semantic;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.sparql.SPARQL;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Scanner;

public class TestClient {
    HttpClient client = HttpClientBuilder.create().build();
    public String ENDPOINT = "http://localhost:9004/semantic-repository/";
//    public String ENDPOINT = "http://94.130.151.234:9004/semantic-repository/";

    public String get(String uri) {
        System.out.println("DO GET: " + uri);
        try {
            HttpGet request = new HttpGet(uri);
            HttpResponse response = client.execute(request);
            System.out.println("executed: " + response);

            HttpEntity entity = response.getEntity();
            System.out.println("entity: " + entity);

            if (entity != null) return EntityUtils.toString(entity);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String post(String uri, String json) {
        try{
            HttpPost request = new HttpPost(uri);

            request.addHeader("Accept", "application/json");
            request.addHeader("Content-Type", "application/json");

            StringEntity data = new StringEntity(json);

            request.setEntity(data);

            HttpResponse response = client.execute(request);

            return EntityUtils.toString(response.getEntity());

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public String delete(String uri) {
        try{
            HttpDelete request = new HttpDelete(uri);


            HttpResponse response = client.execute(request);

            return EntityUtils.toString(response.getEntity());

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public void query(){
        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX : <http://iot.linkeddata.es/def/core#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX wot: <http://iot.linkeddata.es/def/wot#>" +
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>" +
                "PREFIX core: <http://iot.linkeddata.es/def/core#>" +
//                "select ?x where {" +
//                "?x rdf:type ssn:Property ." +
//                "}";
//                "select ?x where {" +
//                "?x rdfs:subClassOf :Device ." +
//                "}";
                "select ?x where {" +
                "?x rdf:type core:Device ." +
                "}";

        System.out.println("TEST SPQRQL for: "+query);
        JSONObject queryJSON = new JSONObject();
        queryJSON.put("query", query);
        String endpoint = ENDPOINT + "sparql";
        System.out.println("ENDPOINT: "+endpoint);

        System.out.println("query post: "+(new JSONObject(post(endpoint, queryJSON.toString()))).toString(2));

    }

    public void remove(String oid){
        System.out.println("DELETE OID : "+oid);
        System.out.println("result: \n"+(new JSONObject(delete(ENDPOINT + "td/remove/"+oid))).toString(2));

    }

    public void create(){
        String source = new File("").getAbsolutePath() + "/semantic-repository/src/test/resources/json/example-thing-validation.json";
        JSONObject json = new JSONObject(TestUtil.file2string(source));
        System.out.println("CREATE TD: \n"+json.toString(2));
        System.out.println("query post: "+(new JSONObject(post(ENDPOINT + "td/create", json.toString()))).toString(2));

    }
    public void validate(){
        String source = new File("").getAbsolutePath() + "/semantic-repository/src/test/resources/json/example-thing-validation.json";
        JSONObject json = new JSONObject(TestUtil.file2string(source));
        System.out.println("VALIDATE TD: \n"+json.toString(2));
        System.out.println("post: "+(new JSONObject(post(ENDPOINT + "td/validate", json.toString()))).toString(2));

    }

    public static void main(String[] args) throws  Exception {
        TestClient t = new TestClient();
//        t.query();
//        t.remove("abc2");
//        t.create();
        t.validate();
    }

}
