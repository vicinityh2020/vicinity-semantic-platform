package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;

public class TestAgent2Ontology {
    String jsonPath = "/semantic-repository/src/test/resources/json/agent/";

    public void populate() {
        try {
            String json = TestUtil.file2string(TestUtil.path(jsonPath + "a-1.json"));

            System.out.println("POPULATING: ");
            System.out.println("INPUT: \n" + json);
            Agent2Ontology handler = new Agent2Ontology();
            JSONObject result = handler.create(json);
            System.out.println("RESULT: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        try {
            String json = TestUtil.file2string(TestUtil.path(jsonPath + "a-1-d.json"));

            System.out.println("DELETING: ");
            System.out.println("INPUT: \n" + json);
            Agent2Ontology handler = new Agent2Ontology();
            JSONObject result = handler.delete(json);
            System.out.println("RESULT: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void populateContracts() {
        try {
            String json = TestUtil.file2string(TestUtil.path(jsonPath + "c-1.json"));

            System.out.println("POPULATING: ");
            System.out.println("INPUT: \n" + json);
            Agent2Ontology handler = new Agent2Ontology();
            JSONObject result = handler.createContracts(json);
            System.out.println("RESULT: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws  Exception {
        TestAgent2Ontology t = new TestAgent2Ontology();
//        t.delete();
//        t.populate();
        t.populateContracts();
//        t.delete();
//        t.deleteContracts();
//        t.o2t();
//        t.validate();
    }

}
