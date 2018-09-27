package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;

public class PopulateAgoraTest {
    String jsonPath = "/semantic-repository/src/test/resources/json/";
    String thingsPath = jsonPath+"agora/population/";

    public void create(String oid) {
        try{
            String json = TestUtil.file2string(TestUtil.path(thingsPath+oid+".json"));

            System.out.println("POPULATING: ");
            System.out.println("INPUT: \n"+json);

            Thing2Ontology handler = new Thing2Ontology();
            JSONObject result;
            result = handler.create(json);

            System.out.println("RESULT: \n"+result.toString(2));

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void delete(String oid) {
        try{

            System.out.println("DELETE: "+oid);

            Thing2Ontology handler = new Thing2Ontology();
            boolean result = handler.delete(oid);

            System.out.println("DELETE RESULT: "+result);

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
        t.populate("test-agora-1");
        t.populate("test-agora-2");
        t.populate("test-agora-3");
        t.populate("test-agora-4");
        t.populate("test-agora-5");
    }
}
