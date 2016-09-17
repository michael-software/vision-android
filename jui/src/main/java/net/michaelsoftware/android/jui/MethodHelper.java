package net.michaelsoftware.android.jui;

/**
 * Created by Michael on 30.08.2016.
 */

import android.util.Log;

import net.michaelsoftware.android.jui.models.ActionModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
            action = action.replaceAll("\"", "'");


            String[] ops = action.split("'");

            ArrayList<String> params = new ArrayList<>();
            for (int i = 1; i < ops.length; i++) {
                params.add(ops[i]);

                i++;
            }

            Object[] parameters = params.toArray();
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



            if (action.startsWith("openUrl") && params.size() == 1) {
                parser.openUrl(params.get(0));
            } else if (action.startsWith("submit") && params.size() < 2) {
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

                        if (!Tools.empty(name) && action.startsWith(name)) {
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
