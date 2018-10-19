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
        iid2oid.put("test-agora-1", "f277a99c-b8cd-4eff-a7fc-9a19c0a663a6");
        iid2oid.put("test-agora-2", "9e452da2-0371-44e0-aa60-8325eedc2533");
        iid2oid.put("test-agora-3", "a5643934-16c8-4a91-a015-380ef4f52a38");
        iid2oid.put("test-agora-4", "b4538b89-2cd1-4614-bcd0-d16d93c513e6");
        iid2oid.put("test-agora-5", "03bda50b-7d69-4279-837e-ef344481d391");
        iid2oid.put("test-bad", "test-bad");
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
//        t.delete("test-agora-naming");
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

        t.populate("test-bad");

    }
}
