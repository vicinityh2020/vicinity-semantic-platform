package sk.intersoft.vicinity.platform.semantic;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.lifting.AgoraSupport;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;

import java.util.HashMap;
import java.util.Map;

public class PopulateAgoraTest {
    String jsonPath = "/semantic-repository/src/test/resources/json/";
    String thingsPath = jsonPath+"agora/population/";

    private static Map<String, String> iid2oid;
    static{
        iid2oid =  new HashMap<String, String>();
//        iid2oid.put("test-agora-1", "test-agora-1");
//        iid2oid.put("test-agora-2", "test-agora-2");
//        iid2oid.put("test-agora-3", "test-agora-3");
//        iid2oid.put("test-agora-4", "test-agora-4");
//        iid2oid.put("test-agora-5", "test-agora-5");

        iid2oid.put("test-agora-1", "f3f9bf96-9af0-451b-be46-24821587f4a3");
        iid2oid.put("test-agora-2", "dda138c3-d05a-48f9-8d12-f19debe23d85");
        iid2oid.put("test-agora-3", "d53e4402-d895-4d1f-918f-310764c8a2b6");
        iid2oid.put("test-agora-4", "f6a67fe1-e185-4058-a95d-0d9e27ab052d");
        iid2oid.put("test-agora-5", "ab6594e3-7924-4296-8b74-1cdce699cc5d");

        iid2oid.put("test-to-play", "3af84bac-4bce-411b-91c7-2a5fc3787add");



//        iid2oid.put("test-bad", "test-bad");
    }

    public void create(String oid) {
        try{
            String json = TestUtil.file2string(TestUtil.path(thingsPath+oid+".json"));

            System.out.println("POPULATING: "+oid);

            String realOid = iid2oid.get(oid);
            System.out.println("REAL OID TO POPULATE: "+realOid);


            JSONObject update = new JSONObject(json);
            update.put("oid", realOid);
            System.out.println("CHANGED INPUT: \n"+update.toString(2));

            Thing2Ontology handler = new Thing2Ontology();
            JSONObject result;
            result = handler.create(update.toString());

            System.out.println("RESULT: \n"+result.toString(2));

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void delete(String oid) {
        try{

            System.out.println("DELETE: "+oid);

            String realOid = iid2oid.get(oid);
            System.out.println("REAL OID TO DELETE: "+realOid);

            if(realOid != null){
                Thing2Ontology handler = new Thing2Ontology();
                boolean result = handler.delete(realOid);

                System.out.println("DELETE RESULT: "+result);
            }


        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }


    public void populate(String oid){
        delete(oid);
        create(oid);
    }


    public static void main(String[] args) throws  Exception {
        PopulateAgoraTest t = new PopulateAgoraTest();
//
//        t.populate("test-agora-1");
//        t.populate("test-agora-2");
//        t.populate("test-agora-3");
//        t.populate("test-agora-4");
//        t.populate("test-agora-5");
//        t.delete("test-agora-1");
//        t.delete("test-agora-2");
//        t.delete("test-agora-3");
//        t.delete("test-agora-4");
//        t.delete("test-agora-5");


        t.populate("test-to-play");
    }
}
