package net.michaelsoftware.android.jui.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Michael on 27.11.2015.
 * Class to parse a Json String synchronosly
 */
public class JsonParser {
    HashMap<Object, Object> hashMap = new HashMap<>();

    public JsonParser(String jsonStr) {
        if (jsonStr != null) {
            Object json;

            try {
                json = new JSONTokener(jsonStr).nextValue();

                if (json instanceof JSONObject) {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    hashMap = jsonObjectToHashMap(jsonObject);
                } else if (json instanceof JSONArray) {
                    JSONArray jsonArray = new JSONArray(jsonStr);

                    hashMap = jsonArrayToHashMap(jsonArray);
                }
            } catch (JSONException e) {
                /*
                hashMap = new HashMap<>();
                    HashMap<Object, Object> hashMapHeading = new HashMap<>();
                    hashMapHeading.put("type","heading");
                    hashMapHeading.put("value","Die Antwort des Servers war fehlerhaft:");

                    HashMap<Object, Object> hashMapText = new HashMap<>();
                    hashMapText.put("type","text");
                    hashMapText.put("value",jsonStr);

                hashMap.put(0, hashMapHeading);
                hashMap.put(1, hashMapText);*/

                hashMap = null;

                e.printStackTrace();
            }

        }
    }

    public JsonParser(JSONArray jsonArray) {
        try {
            hashMap = jsonArrayToHashMap(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JsonParser(JSONObject jsonObject) {
        try {
            hashMap = jsonObjectToHashMap(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private HashMap<Object, Object> jsonArrayToHashMap(JSONArray jsonArray) throws JSONException {
        HashMap<Object, Object> lHashMap = new HashMap<>();

        for (int i=0; i<jsonArray.length(); i++) {
            Object item = jsonArray.get(i);

            lHashMap = this.insertItem(lHashMap, i, item);
        }

        return lHashMap;
    }

    private HashMap<Object,Object> jsonObjectToHashMap(JSONObject jsonObject) throws JSONException {
        HashMap<Object, Object> lHashMap = new HashMap<>();

        Iterator<String> iter = jsonObject.keys();

        while (iter.hasNext()) {
            String key = iter.next();
            Object value = jsonObject.get(key);

            lHashMap = this.insertItem(hashMap, key, value);
        }

        return lHashMap;
    }

    private HashMap<Object, Object> insertItem(HashMap<Object, Object> hashMap, Object key, Object value) {
        if(value instanceof JSONArray) {
            JsonParser pJsonParser= new JsonParser((JSONArray) value);
            HashMap pHashMap = pJsonParser.getHashMap();
            hashMap.put(key, pHashMap);
        } else if(value instanceof JSONObject) {
            JsonParser pJsonParser= new JsonParser((JSONObject) value);
            HashMap pHashMap = pJsonParser.getHashMap();
            hashMap.put(key, pHashMap);
        } else if(value instanceof String) {
            hashMap.put(key, value);
        } else if(value instanceof Integer) {
            hashMap.put(key, value);
        } else if(value instanceof Boolean){
            hashMap.put(key, value);
        }

        return hashMap;
    }

    public HashMap<Object, Object> getHashMap() {
        return hashMap;
    }
}
