package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;

public class TestThing2Ontology {
    String jsonPath = "/semantic-repository/src/test/resources/json/";

    public void populate() {
        try{
            String json = TestUtil.file2string(TestUtil.path(jsonPath+"example-thing.json"));

            System.out.println("INPUT: \n"+json);

            Thing2Ontology handler = new Thing2Ontology();
            JSONObject result = handler.populate(json);

            System.out.println("RESULT: \n"+result.toString(2));

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void testDelete() {
        try{

            String uuid = "test3";
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
            JSONObject result = handler.toJSON(oid);

            System.out.println("RESULT: "+result);

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }


    public static void main(String[] args) throws  Exception {
        TestThing2Ontology t = new TestThing2Ontology();
        t.populate();
//        t.o2t();
//        t.testDelete();
    }
}
