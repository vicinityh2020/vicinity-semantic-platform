package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.lifting.model.ThingsLifterResult;

public class TestThing2Ontology {
    String jsonPath = "/semantic-repository/src/test/resources/json/";

    public void populate(boolean create) {
        try{
//            String json = TestUtil.file2string(TestUtil.path(jsonPath+"example-thing.json"));
            String json = TestUtil.file2string(TestUtil.path(jsonPath+"agora-thing.json"));

            System.out.println("INPUT: \n"+json);
            System.out.println("CREATE: "+create);

            Thing2Ontology handler = new Thing2Ontology();
            JSONObject result;
            if(create){
                result = handler.create(json);
            }
            else{
                result = handler.update(json);
            }

            System.out.println("RESULT: \n"+result.toString(2));

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void testDelete() {
        try{

            String uuid = "test-agora";
            System.out.println("DELETE: "+uuid);

            Thing2Ontology handler = new Thing2Ontology();
            boolean result = handler.delete(uuid);

            System.out.println("RESULT: "+result);

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void o2t() {
        try{

            String oid = "test3";
            System.out.println("O2T: "+oid);

            Ontology2Thing handler = new Ontology2Thing();
            JSONObject result = handler.toJSON(handler.toThing(oid));

            System.out.println("RESULT: "+result);

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void validate(){
        Thing2Ontology handler = new Thing2Ontology();

        String json = TestUtil.file2string(TestUtil.path(jsonPath+"example-thing-validation.json"));

        ThingsLifterResult result = handler.validateAndLift(json.toString());
        if(result.errors.size() > 0){
            System.out.println("ERRORS: \n"+result.failureArray().toString(2));
        }
        else{
            System.out.println("success!");
        }


    }

    public static void main(String[] args) throws  Exception {
        TestThing2Ontology t = new TestThing2Ontology();
//        t.populate(true);
        t.populate(false);
//        t.o2t();
//        t.testDelete();
//        t.validate();
    }
}
