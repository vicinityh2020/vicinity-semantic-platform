package sk.intersoft.vicinity.platform.semantic.lifting.model;

public class AgoraMapping {
    public String jsonPath;
    public String key;
    public String predicate;

    public AgoraMapping(String jsonPath, String key, String predicate){
        this.jsonPath = jsonPath;
        this.key = key;
        this.predicate = predicate;
    }


    public String toString() {
        return "["+jsonPath+" :: key: ["+key+"] predicate:["+predicate+"]]";
    }
}
