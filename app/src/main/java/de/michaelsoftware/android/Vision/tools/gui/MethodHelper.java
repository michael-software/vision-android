package de.michaelsoftware.android.Vision.tools.gui;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Created by Michael on 28.12.2015.
 * Helps managing method calls by GUIHelper (String to method call)
 */
public class MethodHelper {
    private boolean secureMode = false;
    public static final String[] list = {"openPlugin", "openMedia", "refreshCurrentPlugin", "refreshCurrentView", "refreshCurrentViewParameter", "openUrl", "toggleView", "addViews"};

    public MethodHelper() {

    }

    public MethodHelper(boolean secureMode) { // Used for security reasons later (Downloading a file without notify the user)
        this.secureMode = secureMode;
    }

    public void call(String action, Object mainActivity) {
        Logs.d(this, "Call by invoke: " + action);
        if(action != null && !action.equals("")) {

            Method method;
            String actionRaw = action;
            action = action.replaceAll("\"", "'");

            if (action.startsWith("openPlugin")) {
                String[] ops = action.split("'");

                ArrayList<String> params = new ArrayList<>();
                for (int i = 1; i < ops.length; i++) {
                    params.add(ops[i]);
                    i++;
                }

                if (params.size() == 3) {
                    try {
                        method = mainActivity.getClass().getMethod("openPlugin", String.class, String.class, String.class);
                        method.invoke(mainActivity, params.get(0), params.get(1), params.get(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("openMedia")) {
                String[] ops = action.split("'");

                ArrayList<String> params = new ArrayList<>();
                for (int i = 1; i < ops.length; i++) {
                    params.add(ops[i]);
                    i++;
                }

                if (params.size() == 2) {
                    try {
                        method = mainActivity.getClass().getMethod("openMedia", String.class, String.class);
                        method.invoke(mainActivity, params.get(0), params.get(1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("refreshCurrentPlugin")) {
                String[] ops = action.split("'");

                ArrayList<String> params = new ArrayList<>();
                for (int i = 1; i < ops.length; i++) {
                    params.add(ops[i]);
                    i++;
                }

                if (params.size() > 0) {
                    try {
                        method = mainActivity.getClass().getMethod("refreshCurrentPlugin", String.class);
                        method.invoke(mainActivity, params.get(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("refreshCurrentView")) {
                String[] ops = action.split("'");

                ArrayList<String> params = new ArrayList<>();
                for (int i = 1; i < ops.length; i++) {
                    params.add(ops[i]);
                    i++;
                }

                if (params.size() > 1) {
                    try {
                        method = mainActivity.getClass().getMethod("refreshCurrentView", String.class, String.class);
                        method.invoke(mainActivity, params.get(0), params.get(1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("refreshCurrentViewParameter")) {
                String[] ops = actionRaw.split("'");

                ArrayList<String> params = new ArrayList<>();
                for (int i = 1; i < ops.length; i++) {
                    params.add(ops[i]);
                    i++;
                }

                if (params.size() > 2) {
                    try {
                        method = mainActivity.getClass().getMethod("refreshCurrentViewParameter", String.class, String.class, String.class);
                        method.invoke(mainActivity, params.get(0), params.get(1), params.get(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("openUrl")) {
                String[] ops = action.split("'");

                ArrayList<String> params = new ArrayList<>();
                for (int i = 1; i < ops.length; i++) {
                    params.add(ops[i]);
                    i++;
                }

                if (params.size() > 0) {
                    try {
                        method = mainActivity.getClass().getMethod("openUrl", String.class);
                        method.invoke(mainActivity, params.get(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("toggleView")) {
                String[] ops = action.split("'");

                if (ops.length > 1) {
                    try {
                        method = mainActivity.getClass().getMethod("toggleView", String.class);
                        method.invoke(mainActivity, ops[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("addViews")) {
                String[] ops = actionRaw.split("(?<=([^\\\\]))'");

                if (ops.length > 1) {
                    try {
                        if(ops.length == 3 && !secureMode) {
                            method = mainActivity.getClass().getMethod("addViews", String.class);
                            method.invoke(mainActivity, ops[1].replaceAll("\\\\'", "'"));
                        } else if(ops.length == 5) {
                            method = mainActivity.getClass().getMethod("addViews", String.class, String.class);
                            method.invoke(mainActivity, ops[1], ops[3].replaceAll("\\\\'", "'"));
                        } else if(ops.length == 7) {
                            method = mainActivity.getClass().getMethod("addViews", String.class, String.class, String.class);
                            method.invoke(mainActivity, ops[1], ops[3], ops[5].replaceAll("\\\\'", "'"));
                        } else if(ops.length == 9) {
                            method = mainActivity.getClass().getMethod("addViews", String.class, String.class, String.class, String.class);
                            method.invoke(mainActivity, ops[1], ops[3], ops[5], ops[7].replaceAll("\\\\'", "'"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.startsWith("reloadMenu")) {
                try {
                    method = mainActivity.getClass().getMethod("loadMenu", Boolean.class);
                    method.invoke(mainActivity, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
