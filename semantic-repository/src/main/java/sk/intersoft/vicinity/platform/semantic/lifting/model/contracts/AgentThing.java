package sk.intersoft.vicinity.platform.semantic.lifting.model.contracts;

import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.utils.JSONUtil;

public class AgentThing {

    public static String OID_KEY = "oid";
    public static String NAME_KEY = "name";


    public static String OUT_OID_KEY = "agent_oid";
    public static String OUT_NAME_KEY = "agent_name";
    public static String OUT_TYPE_KEY = "agent_type";


    public static JSONObject create(Object o) throws Exception{
        if(o instanceof JSONObject){
            JSONObject object = (JSONObject)o;
            String oid = JSONUtil.getString(OID_KEY, object);
            if(oid == null){
                throw new Exception("missing agent [key:"+OID_KEY+"] in: "+object);
            }

            String name = JSONUtil.getString(NAME_KEY, object);
            if(name == null){
                throw new Exception("missing agent [key:"+NAME_KEY+"] in: "+object);
            }


            JSONObject agent = new JSONObject();
            agent.put(OUT_OID_KEY, oid);
            agent.put(OUT_NAME_KEY, name);
            agent.put(OUT_TYPE_KEY, "core:Agent");
            return agent;

        }
        else throw new Exception("agent json must be object: "+o.toString());
    }

}
