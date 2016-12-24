package net.michaelsoftware.android.jui;

/**
 * Created by Michael on 30.08.2016.
 */

import android.util.Log;

import net.michaelsoftware.android.jui.models.ActionModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Michael on 28.12.2015.
 * Helps managing method calls by GUIHelper (String to method call)
 */
public class MethodHelper {
    private JuiParser parser;
    private boolean secureMode = false;
    public static final String[] list = {"openPlugin", "openMedia", "refreshCurrentPlugin", "refreshCurrentView", "refreshCurrentViewParameter", "openUrl", "toggleView", "addViews"};

    public MethodHelper() {

    }

    public MethodHelper(boolean secureMode) { // Used for security reasons later (Downloading a file without notify the user)
        this.secureMode = secureMode;
    }

    public MethodHelper(JuiParser parser) { // Used for security reasons later (Downloading a file without notify the user)
        this.parser = parser;
    }

    public void call(String action) {
        if(action != null && !action.equals("")) {

            Method method;
            String actionRaw = action;
            //action = action.replaceAll("\"", "'");

            Log.d("action", action);

            action = action.replaceAll("((?:.?)*)\\(((?:.?)*)\\)", "$2"); // removes name
            //action = action.replaceAll("/ ,/g", ",").replaceAll("/, /g", ","); // deletes whitespace


            ArrayList<String> params = new ArrayList<>();

            if(!action.equals("")) {
                Log.d("actionRegex", action);

                action = action.trim();


                if (action.charAt(0) == '\'')
                    action = action.substring(1, action.length());

                if (action.charAt(action.length() - 1) == '\'')
                    action = action.substring(0, action.length() - 1);

                String[] ops = action.split("','");

                for (int i = 0; i < ops.length; i++) {
                    Log.d("ops", ops[i]);

                    params.add(ops[i]);
                }
            }

            Object[] parameters = params.toArray();

            Log.d("actionParams", params.toString());
/*
            if(ops.length-2 > 0) {
                parameters = new Object[ops.length - 2];

                for (int i = 1, j = 0; i < ops.length; i++) {

                    if (i < ops.length - 1) {
                        parameters[j] = ops[i];
                        j++;
                    }

                    Log.d("parameters", ops[i]+"");

                    i++;
                }
            }*/



            if (actionRaw.startsWith("openUrl") && params.size() == 1) {
                parser.openUrl(params.get(0));
            } else if (actionRaw.startsWith("submit") && params.size() < 2) {
                if(params.size() == 0) {
                    parser.submit();
                } else {
                    parser.submit(params.get(0));
                }
            } else {
                List<ActionModel> ls = parser.getActions();

                if(!Tools.empty(ls)) {
                    for(int i = 0, x = ls.size(); i < x; i++) {
                        String name = ls.get(i).name;
                        Object object = ls.get(i).object;
                        Class<?> classObject = object.getClass();

                        if (!Tools.empty(name) && actionRaw.startsWith(name)) {
                            if (parameters.length == params.size() && params.size() == ls.get(i).parameters) {
                                try {
                                    Method[] validMethods = classObject.getMethods();

                                    for (Method method1 : validMethods) {
                                        if (method1.getName().equals(name) && method1.getParameterTypes().length == params.size()) {
                                            method1.invoke(object, parameters);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
