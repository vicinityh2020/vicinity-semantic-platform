package sk.intersoft.vicinity.platform.semantic.lifting.model.contracts;

import org.json.JSONArray;
import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.utils.JSONUtil;

import java.util.List;

public class AgentThing {

    // AGENT KEYS
    public static String OID_KEY = "oid";
    public static String NAME_KEY = "name";


    public static String OUT_OID_KEY = "agent_oid";
    public static String OUT_NAME_KEY = "agent_name";
    public static String OUT_TYPE_KEY = "agent_type";

    // CONTRACT KEYS
    public static String CONTRACT_OID_KEY = "contract_id";
    public static String CONTRACT_TYPE_KEY = "contract_type";
    public static String WRITE_RIGHTS_KEY = "write_rights";
    public static String REQUESTED_SERVICE_KEY = "requested_service";
    public static String PETITIONER_ITEMS_KEY = "petitioner_items";
    public static String SERVICE_OWNER_KEY = "service_owner";
    public static String SERVICE_PETITIONER_KEY = "service_petitioner";


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

    public static String valueOrException(String key, JSONObject object) throws Exception {
        String value = JSONUtil.getString(key, object);
        if(value == null){
            throw new Exception("missing contract [key:"+key+"] in: "+object);
        }
        return value;
    }
    public static JSONObject createContract(Object o) throws Exception{
        if(o instanceof JSONObject){
            JSONObject object = (JSONObject)o;
            String contractOID = valueOrException(CONTRACT_OID_KEY, object);
            boolean write = JSONUtil.getBoolean(WRITE_RIGHTS_KEY, object);
            String requestedService = "thing:"+valueOrException(REQUESTED_SERVICE_KEY, object);
            String serviceOwner = "thing:"+valueOrException(SERVICE_OWNER_KEY, object);
            String servicePetitioner = "thing:"+valueOrException(SERVICE_PETITIONER_KEY, object);

            List<String> items = JSONUtil.getStringArray(PETITIONER_ITEMS_KEY, object);
            JSONArray itemsArr = new JSONArray();
            for(String i : items){
                itemsArr.put("thing:"+i);
            }

            JSONObject contract = new JSONObject();
            contract.put(CONTRACT_OID_KEY, contractOID);
            contract.put(WRITE_RIGHTS_KEY, write);
            contract.put(REQUESTED_SERVICE_KEY, requestedService);
            contract.put(SERVICE_OWNER_KEY, serviceOwner);
            contract.put(SERVICE_PETITIONER_KEY, servicePetitioner);
            contract.put(PETITIONER_ITEMS_KEY, itemsArr);
            contract.put(CONTRACT_TYPE_KEY, "core:Contract");
            return contract;

        }
        else throw new Exception("agent json must be object: "+o.toString());
    }

}
