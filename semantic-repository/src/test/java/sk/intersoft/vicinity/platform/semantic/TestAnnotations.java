package sk.intersoft.vicinity.platform.semantic;

import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.lifting.AgoraSupport;
import sk.intersoft.vicinity.platform.semantic.lifting.model.AgoraMapping;
import sk.intersoft.vicinity.platform.semantic.lifting.model.AnnotationsHierarchy;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.DataSchema;
import sk.intersoft.vicinity.platform.semantic.lifting.model.thing.ThingValidator;

import java.util.Set;

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


    public void agoraMapping() {
        try{
            String jsonPath = "/semantic-repository/src/test/resources/json/";
            String json = TestUtil.file2string(TestUtil.path(jsonPath+"schema-1.json"));

            ThingValidator v = new ThingValidator(false);
            DataSchema s = DataSchema.create(new JSONObject(json), v);

            System.out.println("SCHEMA: "+s.toString(2));
            System.out.println(v.failureMessage().toString(2));
            if(v.errors.size() > 0) throw new Exception("ERRORS!");

            AgoraSupport a = new AgoraSupport(null);
            Set<AgoraMapping> ms = a.getMappings(s, false);
            System.out.println("all mapping: ");
            for(AgoraMapping m : ms){
                System.out.println(m.toString());
            }


        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) throws  Exception {
        TestAnnotations t = new TestAnnotations();
//        t.traverse();
        t.agoraMapping();
    }
}
