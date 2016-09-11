package de.michaelsoftware.android.Vision.tools.network;

import android.os.AsyncTask;

import java.util.HashMap;

/**
 * Created by Michael on 29.11.2015.
 * An asynchronous JSON Parser. Will maybe be removed in the future TODO
 */
public class JsonParserAsync extends AsyncTask<String, Integer, HashMap<Object, Object>> {
    private Object obj;
    private java.lang.reflect.Method method;
    private Object specialData = null;

    @Override
    protected HashMap<Object, Object> doInBackground(String... params) {
        if(params.length > 0) {
            JsonParser jsonParser = new JsonParser(params[0]);

            return jsonParser.getHashMap();
        }

        return null;
    }

    public void setOutput(Object c, String methodName) {
        try {
            obj = c;

            if(this.specialData != null) {
                method = c.getClass().getMethod(methodName, HashMap.class, Object.class);
            } else {
                method = c.getClass().getMethod(methodName, HashMap.class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected void onPostExecute(HashMap<Object, Object> result) {
        if(method != null && obj != null && result != null) {
            try {
                if(this.specialData != null) {
                    method.invoke(obj, result, this.specialData);
                } else {
                    method.invoke(obj, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setSpecialData(Object specialData) {
        this.specialData = specialData;
    }
}
