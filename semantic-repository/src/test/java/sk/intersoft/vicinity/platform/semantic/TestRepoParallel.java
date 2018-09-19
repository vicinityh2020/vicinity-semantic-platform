package sk.intersoft.vicinity.platform.semantic;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import org.slf4j.LoggerFactory;
import sk.intersoft.vicinity.platform.semantic.graph.Graph;
import sk.intersoft.vicinity.platform.semantic.sparql.SPARQL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.*;


public class TestRepoParallel {
    static {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));

        for(String log:loggers) {
            Logger logger = (Logger) LoggerFactory.getLogger(log);
            logger.setLevel(Level.INFO);
            logger.setAdditive(false);
        }

    }


    HttpClient client = HttpClientBuilder.create().build();
    public String ENDPOINT = "http://localhost:9004/semantic-repository/";
//    public String ENDPOINT = "http://94.130.151.234:9004/semantic-repository/";
//    public String ENDPOINT = "http://159.69.26.108:9004/semantic-repository/";


    public String path = new File("").getAbsolutePath() + "/semantic-repository/src/test/resources/log/log";
    public BufferedWriter w;

    public void open() throws Exception {
        w = new BufferedWriter(new FileWriter(path, true));
    }
    public void close() throws Exception {
        w.close();
    }
    public void log(String out) throws Exception {
        try{
            open();
            w.write(out);
            close();
        }
        catch(Exception e) {
        }
    }
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

    public String staticGET(String uri) {
        System.out.println("DO static GET: " + uri);
        try {
            HttpClient client = HttpClientBuilder.create().build();

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

    public String put(String uri, String json) {
        try{
            HttpPut request = new HttpPut(uri);

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

    public class LoadThread implements Runnable {
        private String id;

        public LoadThread(String id) {
            this.id = id;
        }

        public void run() {
            try{
                String out = "";
                long start = System.nanoTime();

                System.out.println("THREAD STARTED ["+start+"]: ID ["+id+"]");
                long end = System.nanoTime();
                long diff = end - start;
                out += "THREAD COMPLETED in ["+start+" - "+end+"]: [] ID ["+id+"]\n";
                System.out.print(out);
                log(out);

            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }


    public class TestThread implements Runnable {
        private String id;

        public TestThread(String id) {
            this.id = id;
        }

        public void run() {
            try{
                String endpoint = ENDPOINT + "test?id="+id;
                long start = System.nanoTime();

                System.out.println("THREAD STARTED ["+start+"]: ID ["+id+"]");

                String out = "";
                String result = "";
                System.out.println("THREAD ["+id+"] call: "+endpoint);

                result = staticGET(endpoint);
                System.out.print("THREAD ["+id+"] result: "+result);

//                for (int i = 0; i < 30; i++){
//                    System.out.println("THREAD ["+id+"] ["+i+"] call: "+endpoint);
//                }

                long end = System.nanoTime();
                long diff = end - start;
                out += "THREAD COMPLETED in ["+start+" - "+end+"]: ["+result.length()+"] ID ["+id+"]\n";
                System.out.print(out);
                log(out);
            }
            catch(Exception e){
                System.out.println("THREAD ["+id+"] EXCEPTION");
                e.printStackTrace();
            }
        }
    }


    public class QueryThread implements Runnable {
        private String id;
        private int action;

        public QueryThread(String id) {
            this.id = id;
            this.action = 1;
        }
        public QueryThread(String id, int action) {
            this.id = id;
            this.action = action;
        }

        public void run() {
            String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT * WHERE {?s ?p ?o.} LIMIT 100";
            try{


                String a = "populate";
                if(action == 1){
                    a = "query";
                }
                String source = new File("").getAbsolutePath() + "/semantic-repository/src/test/resources/json/example-thing.json";
                JSONObject json = new JSONObject(TestUtil.file2string(source));

                String out = "";
                String endpoint = ENDPOINT + "sparql";
                long start = System.nanoTime();

                System.out.println("THREAD STARTED ["+start+"]: ID ["+id+"/"+action+"]");

                String result = "";
                if(action == 0){
                    System.out.println("THREAD ["+id+"] create: "+ENDPOINT);
                    result = put(ENDPOINT + "td/create", json.toString());
                }
                else if (action == 1){
                    System.out.println("THREAD ["+id+"] query: "+endpoint);

                    for (int i = 0; i < 30; i++){
                        JSONObject queryJSON = new JSONObject();
                        String qx = q.replace("?s", "?s"+System.nanoTime()).replace("?o", "?o"+System.nanoTime());;
                        queryJSON.put("query", qx);
                        result = post(endpoint, queryJSON.toString());

                    }
                }

                long end = System.nanoTime();
                long diff = end - start;
                out += "THREAD COMPLETED in ["+start+" - "+end+"]: ["+result.length()+"] ID ["+id+"/"+action+"]\n";
                System.out.print(out);
                log(out);
            }
            catch(Exception e){
                System.out.println("THREAD ["+id+"] EXCEPTION");
                e.printStackTrace();
            }
        }
    }

    public class MultiThread implements Runnable {


        public void run() {
            try{



                System.out.println("MULTI THREAD STARTED ");

                Thread t1 = new Thread(new QueryThread("1"));
                Thread t2 = new Thread(new QueryThread("2"));
                Thread t3 = new Thread(new QueryThread("3"));
                Thread t4 = new Thread(new QueryThread("4"));
                Thread t5 = new Thread(new QueryThread("5"));
                Thread t6 = new Thread(new QueryThread("6"));
                Thread t7 = new Thread(new QueryThread("7"));
                Thread t8 = new Thread(new QueryThread("8"));
                Thread t9 = new Thread(new QueryThread("9"));
                Thread t10 = new Thread(new QueryThread("10"));
                Thread t11 = new Thread(new QueryThread("11"));
                Thread t12 = new Thread(new QueryThread("12"));
                Thread t13 = new Thread(new QueryThread("13"));
                Thread t14 = new Thread(new QueryThread("14"));
                Thread t15 = new Thread(new QueryThread("15"));
                Thread t16 = new Thread(new QueryThread("16"));
                Thread t17 = new Thread(new QueryThread("17"));
                Thread t18 = new Thread(new QueryThread("18"));
                Thread t19 = new Thread(new QueryThread("19"));
                Thread t20 = new Thread(new QueryThread("20"));


                Thread t21 = new Thread(new QueryThread("21", 1));
                Thread t22 = new Thread(new QueryThread("22", 1));
                Thread t23 = new Thread(new QueryThread("23", 1));
                Thread t24 = new Thread(new QueryThread("24", 1));
                Thread t25 = new Thread(new QueryThread("25", 1));
                Thread t26 = new Thread(new QueryThread("26", 1));
                Thread t27 = new Thread(new QueryThread("27", 1));
                Thread t28 = new Thread(new QueryThread("28", 1));
                Thread t29 = new Thread(new QueryThread("29", 1));
                Thread t30 = new Thread(new QueryThread("30", 1));
                Thread t31 = new Thread(new QueryThread("31", 1));
                Thread t32 = new Thread(new QueryThread("32", 1));
                Thread t33 = new Thread(new QueryThread("33", 1));
                Thread t34 = new Thread(new QueryThread("34", 1));
                Thread t35 = new Thread(new QueryThread("35", 1));
                Thread t36 = new Thread(new QueryThread("36", 1));
                Thread t37 = new Thread(new QueryThread("37", 1));
                Thread t38 = new Thread(new QueryThread("38", 1));
                Thread t39 = new Thread(new QueryThread("39", 1));


                t1.start();
                t2.start();
                t3.start();
                t4.start();
                t5.start();
                t6.start();
                t7.start();
                t8.start();
                t9.start();
                t10.start();
                t11.start();
                t12.start();
                t13.start();
                t14.start();
                t15.start();
                t16.start();
                t17.start();
                t18.start();
                t19.start();
                t20.start();

                t21.start();
                t22.start();
                t23.start();
                t24.start();
                t25.start();
                t26.start();
                t27.start();
                t28.start();
                t29.start();
                t30.start();
                t31.start();
                t32.start();
                t33.start();
                t34.start();
                t35.start();
                t36.start();
                t37.start();
                t38.start();
                t39.start();

                System.out.println("MULTI THREAD DONE");
            }
            catch(Exception e){
                System.out.println("MULTI THREAD EXCEPTION");
                e.printStackTrace();
            }
        }
    }

    public void parallelTest() throws Exception {
        Thread t1 = new Thread(new TestThread("1"));
        Thread t2 = new Thread(new TestThread("2"));
        Thread t3 = new Thread(new TestThread("3"));
        Thread t4 = new Thread(new TestThread("4"));
        Thread t5 = new Thread(new TestThread("5"));
        Thread t6 = new Thread(new TestThread("6"));
        Thread t7 = new Thread(new TestThread("7"));
        Thread t8 = new Thread(new TestThread("8"));
        Thread t9 = new Thread(new TestThread("9"));
        Thread t10 = new Thread(new TestThread("10"));
        Thread t11 = new Thread(new TestThread("11"));
        Thread t12 = new Thread(new TestThread("12"));
        Thread t13 = new Thread(new TestThread("13"));
        Thread t14 = new Thread(new TestThread("14"));
        Thread t15 = new Thread(new TestThread("15"));
        Thread t16 = new Thread(new TestThread("16"));
        Thread t17 = new Thread(new TestThread("17"));
        Thread t18 = new Thread(new TestThread("18"));
        Thread t19 = new Thread(new TestThread("19"));
        Thread t20 = new Thread(new TestThread("20"));



        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        t9.start();
        t10.start();
        t11.start();
        t12.start();
        t13.start();
        t14.start();
        t15.start();
        t16.start();
        t17.start();
        t18.start();
        t19.start();
        t20.start();
    }


    public void parallelQuery() throws Exception {
        Thread t1 = new Thread(new QueryThread("1"));
        Thread t2 = new Thread(new QueryThread("2"));
        Thread t3 = new Thread(new QueryThread("3"));
        Thread t4 = new Thread(new QueryThread("4"));
        Thread t5 = new Thread(new QueryThread("5"));
        Thread t6 = new Thread(new QueryThread("6"));
        Thread t7 = new Thread(new QueryThread("7"));
        Thread t8 = new Thread(new QueryThread("8"));
        Thread t9 = new Thread(new QueryThread("9"));
        Thread t10 = new Thread(new QueryThread("10"));
        Thread t11 = new Thread(new QueryThread("11"));
        Thread t12 = new Thread(new QueryThread("12"));
        Thread t13 = new Thread(new QueryThread("13"));
        Thread t14 = new Thread(new QueryThread("14"));
        Thread t15 = new Thread(new QueryThread("15"));
        Thread t16 = new Thread(new QueryThread("16"));
        Thread t17 = new Thread(new QueryThread("17"));
        Thread t18 = new Thread(new QueryThread("18"));
        Thread t19 = new Thread(new QueryThread("19"));
        Thread t20 = new Thread(new QueryThread("20"));


//        Thread t21 = new Thread(new QueryThread("21", 1));
//        Thread t22 = new Thread(new QueryThread("22", 1));
//        Thread t23 = new Thread(new QueryThread("23", 1));
//        Thread t24 = new Thread(new QueryThread("24", 1));
//        Thread t25 = new Thread(new QueryThread("25", 1));
//        Thread t26 = new Thread(new QueryThread("26", 1));
//        Thread t27 = new Thread(new QueryThread("27", 1));
//        Thread t28 = new Thread(new QueryThread("28", 1));
//        Thread t29 = new Thread(new QueryThread("29", 1));
//        Thread t30 = new Thread(new QueryThread("30", 1));
//        Thread t31 = new Thread(new QueryThread("31", 1));
//        Thread t32 = new Thread(new QueryThread("32", 1));
//        Thread t33 = new Thread(new QueryThread("33", 1));
//        Thread t34 = new Thread(new QueryThread("34", 1));
//        Thread t35 = new Thread(new QueryThread("35", 1));
//        Thread t36 = new Thread(new QueryThread("36", 1));
//        Thread t37 = new Thread(new QueryThread("37", 1));
//        Thread t38 = new Thread(new QueryThread("38", 1));
//        Thread t39 = new Thread(new QueryThread("39", 1));


        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        t9.start();
        t10.start();
        t11.start();
        t12.start();
        t13.start();
        t14.start();
        t15.start();
        t16.start();
        t17.start();
        t18.start();
        t19.start();
        t20.start();

//        t21.start();
//        t22.start();
//        t23.start();
//        t24.start();
//        t25.start();
//        t26.start();
//        t27.start();
//        t28.start();
//        t29.start();
//        t30.start();
//        t31.start();
//        t32.start();
//        t33.start();
//        t34.start();
//        t35.start();
//        t36.start();
//        t37.start();
//        t38.start();
//        t39.start();

    }

    public void test() throws Exception {
        System.out.println("TEST GRAPH");
        String endpoint = ENDPOINT + "test";
        System.out.println("ENDPOINT: "+endpoint);

        String result = get(endpoint);
        System.out.println("result: "+result);

    }

    public void q() throws Exception {
        String query = "SELECT * WHERE {?s ?p ?o.}";
        System.out.println("TEST SPQRQL for: "+query);
        JSONObject queryJSON = new JSONObject();
        queryJSON.put("query", query);
        String endpoint = ENDPOINT + "sparql";
        System.out.println("ENDPOINT: "+endpoint);

        String result = post(endpoint, queryJSON.toString());
        System.out.println("query result: "+result.length());

    }

    public void multiParallel() throws Exception {
        Thread t1 = new Thread(new MultiThread());
        Thread t2 = new Thread(new MultiThread());
        Thread t3 = new Thread(new MultiThread());
        Thread t4 = new Thread(new MultiThread());
        Thread t5 = new Thread(new MultiThread());
        Thread t6 = new Thread(new MultiThread());
        Thread t7 = new Thread(new MultiThread());
        Thread t8 = new Thread(new MultiThread());
        Thread t9 = new Thread(new MultiThread());
        Thread t10 = new Thread(new MultiThread());
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        t9.start();
        t10.start();
    }

    public void parallelLoad() throws Exception {
        Thread t1 = new Thread(new LoadThread("1"));
        Thread t2 = new Thread(new LoadThread("2"));
        Thread t3 = new Thread(new LoadThread("3"));
        Thread t4 = new Thread(new LoadThread("4"));
        Thread t5 = new Thread(new LoadThread("5"));
        Thread t6 = new Thread(new LoadThread("6"));
        Thread t7 = new Thread(new LoadThread("7"));
        Thread t8 = new Thread(new LoadThread("8"));
        Thread t9 = new Thread(new LoadThread("9"));
        Thread t10 = new Thread(new LoadThread("10"));

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        t9.start();
        t10.start();
    }

    public static void main(String[] args) throws  Exception {
        TestRepoParallel t = new TestRepoParallel();

        t.parallelQuery();
//        t.parallelTest();
//        t.parallelLoad();
//        t.multiParallel();
//        t.q();
    }

}
