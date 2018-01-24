package sk.intersoft.vicinity.platform.semantic.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONUtil {

    public static List<String> getStringArray(String key, JSONObject object) {
        List<String> result = new ArrayList<String>();
        try{
            if(object.has(key)) {
                Object value = object.get(key);
                if(value instanceof String) {
                    result.add(object.getString(key).trim());
                    return result;
                }
                else if(value instanceof JSONArray) {
                    Iterator i = object.getJSONArray(key).iterator();
                    while(i.hasNext()){
                        Object item = i.next();
                        if(item instanceof String){
                            result.add(((String)item).trim());
                        }
                        else return null;
                    }
                    return result;
                }
                else return null;
            }
            else{
                return null;
            }

        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getString(String key, JSONObject object)  {
        List<String> array = getStringArray(key, object);
        if(array != null && array.size() > 0) return array.get(0);
        else return null;
    }


    public static List<JSONObject> getObjectArray(String key, JSONObject object) {
        List<JSONObject> result = new ArrayList<JSONObject>();
        try{
            if(object.has(key)) {
                Object value = object.get(key);
                if(value instanceof JSONObject) {
                    result.add(object.getJSONObject(key));
                    return result;
                }
                else if(value instanceof JSONArray) {
                    Iterator i = object.getJSONArray(key).iterator();
                    while(i.hasNext()){
                        Object item = i.next();
                        if(item instanceof JSONObject){
                            result.add((JSONObject)item);
                        }
                        else return null;
                    }
                    return result;
                }
                else return null;
            }
            else{
                return null;
            }

        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getObject(String key, JSONObject object) {
        List<JSONObject> array = getObjectArray(key, object);
        if(array != null && array.size() > 0) return array.get(0);
        else return null;
    }
}
