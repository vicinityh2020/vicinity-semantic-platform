package sk.intersoft.vicinity.platform.semantic.service.resource;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ServiceResponse {
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";
    public static final String REASON = "reason";
    public static final String REMOVED = "removed";
    public static final String DATA = "data";

    public static JSONObject success(String key, Object result){
        JSONObject response = new JSONObject();
        response.put(STATUS, SUCCESS);
        response.put(key, result);
        return response;
    }

    public static JSONObject success(Object result){
        JSONObject response = new JSONObject();
        response.put(STATUS, SUCCESS);
        response.put(DATA, result);
        return response;
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static JSONObject failure(Exception exception){
        JSONObject response = new JSONObject();
        response.put(STATUS, FAILURE);
        response.put(REASON, exception.getMessage());
//        response.put(REASON, getStackTrace(exception));
        return response;
    }

    public static JSONObject failure(Object o){
        JSONObject response = new JSONObject();
        response.put(STATUS, FAILURE);
        response.put(REASON, o);
//        response.put(REASON, getStackTrace(exception));
        return response;
    }
}
