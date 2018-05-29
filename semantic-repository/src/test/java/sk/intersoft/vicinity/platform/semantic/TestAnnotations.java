package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.lifting.model.AnnotationsHierarchy;

public class TestAnnotations {

    public void traverse() {
        try{

            AnnotationsHierarchy ann = new AnnotationsHierarchy();
            JSONObject result = ann.dump();

            System.out.println("RESULT: "+result.toString(2));

        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }


    public static void main(String[] args) throws  Exception {
        TestAnnotations t = new TestAnnotations();
        t.traverse();
    }
}
