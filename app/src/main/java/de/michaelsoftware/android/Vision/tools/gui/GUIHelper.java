package de.michaelsoftware.android.Vision.tools.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnClickListener;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnLongClickListener;
import de.michaelsoftware.android.Vision.tools.gui.listener.OnSwipeTouchListener;
import de.michaelsoftware.android.Vision.tools.gui.listener.UploadOnClickListener;
import de.michaelsoftware.android.Vision.tools.gui.views.ButtonList;
import de.michaelsoftware.android.Vision.tools.gui.views.ColorView;
import de.michaelsoftware.android.Vision.tools.gui.views.EditTextView;
import de.michaelsoftware.android.Vision.tools.gui.views.Editor;
import de.michaelsoftware.android.Vision.tools.gui.views.Image;
import de.michaelsoftware.android.Vision.tools.gui.views.LinearList;
import de.michaelsoftware.android.Vision.tools.gui.views.Range;
import de.michaelsoftware.android.Vision.tools.gui.views.Spoiler;
import de.michaelsoftware.android.Vision.tools.gui.views.Table;
import de.michaelsoftware.android.Vision.tools.gui.views.Text;
import de.michaelsoftware.android.Vision.tools.gui.views.TouchWebView;
import de.michaelsoftware.android.Vision.tools.gui.views.UploadButton;
import de.michaelsoftware.android.Vision.tools.network.HttpPostJsonHelper;

/**
 * Created by Michael on 29.11.2015.
 * Free non-commercial use and modify of the source code
 */
public class GUIHelper {
    private LinearLayout linear;
    private ScrollView scroll;
    private Activity mainActivity;
    public AlertDialog alertDialog;

    private int padding       = 30;
    private int paddingTop    = padding;
    private int paddingLeft   = padding;
    private int paddingRight  = padding;
    private int paddingBottom = padding;

    private String swipeTop = "", swipeLeft = "", swipeRight = "", swipeBottom = "";

    public static final int REQUEST_FILE = 2534;

    HashMap<String, Object> editElements = new HashMap<>();
    HashMap<String, Object> idElements = new HashMap<>();

    public static String sep1 = "%!#|params|#!%";
    private View currentView;

    public GUIHelper(Activity main, LinearLayout pLinear, ScrollView pScroll) {
        this.mainActivity = main;
        this.linear = pLinear;
        this.scroll = pScroll;
    }

    public void clear() {
        linear.removeAllViews();
        this.editElements.clear();
        this.scroll.setBackgroundColor(Color.TRANSPARENT);

        this.swipeTop = "";
        this.swipeLeft = "";
        this.swipeRight = "";
        this.swipeBottom = "";
        this.scroll.setOnTouchListener(null);
        if(mainActivity instanceof MainActivity) {
            ((MainActivity) mainActivity).enableRefresh();
        }

        this.scroll.scrollTo(0, 0);
    }

    @SuppressWarnings("unchecked")
    public void parse(HashMap<Object, Object> hashMap) {
        if(mainActivity instanceof MainActivity)
            ((MainActivity) mainActivity).enableRefresh();

        linear.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        if(hashMap != null && hashMap.containsKey("type") && hashMap.get("type") instanceof String
                && ((String) hashMap.get("type")).equalsIgnoreCase("flyover")
                && hashMap.containsKey("value") && hashMap.get("value") instanceof HashMap) {

            HashMap<Object, Object> hashMapValue = (HashMap) hashMap.get("value");
            LinearLayout view = new LinearLayout(mainActivity);
            view.setOrientation(LinearLayout.VERTICAL);
            view.setPadding((int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_horizontal_margin),
                    (int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_vertical_margin),
                    (int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_horizontal_margin),
                    (int) ResourceHelper.getDimen(mainActivity, R.dimen.activity_vertical_margin));

            for (int i = 0; i < hashMapValue.size(); i++) {
                if (hashMapValue.get(i) instanceof HashMap) {
                    View el = this.parseElement((HashMap) hashMapValue.get(i), true);

                    if (el != null)
                        view.addView(el);
                }
            }

            AlertDialog.Builder alertDialogBuilder;
            if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
                alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogDark);
            } else {
                alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogLight);
            }

            ScrollView scrollView = new ScrollView(mainActivity);
            scrollView.addView(view);
            alertDialogBuilder.setView(scrollView);
            alertDialogBuilder.setCancelable(true);
            this.alertDialog = alertDialogBuilder.create();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(this.alertDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            try {
                this.alertDialog.show();
                this.alertDialog.getWindow().setAttributes(lp);
            } catch (Exception e) {
                // WindowManager$BadTokenException will be caught and the app would
                // not display the 'Force Close' message
                e.printStackTrace();
            }
        } else if(hashMap != null && hashMap.containsKey("redirect") && hashMap.get("redirect") instanceof HashMap && mainActivity instanceof MainActivity) {
            HashMap hm = (HashMap) hashMap.get("redirect");
            String s1 = (String) hm.get(0);
            String s2 = (String) hm.get(1);
            String s3 = (String) hm.get(2);
            ((MainActivity) mainActivity).openPlugin(s1, s2, s3);
            ((MainActivity) mainActivity).historyRemoveLast();
        } else if(hashMap != null) {
            this.clear();

            HashMap<Object, Object> data = hashMap;

            if(hashMap.containsKey("data") && hashMap.get("data") instanceof HashMap) {
                data = (HashMap<Object, Object>) hashMap.get("data");
            }

            if(hashMap.containsKey("head") && hashMap.get("head") instanceof HashMap) {
                this.parseHeader((HashMap<Object, Object>) hashMap.get("head"));
            }

            for (int i = 0; i < data.size(); i++) {
                if (data.get(i) instanceof HashMap) {
                    View el = this.parseElement((HashMap) data.get(i), true);

                    if (el != null)
                        linear.addView(el);
                }
            }
        }
    }

    private void parseHeader(HashMap<Object, Object> hashMap) {
        Object valueBackgroundColor = hashMap.get("bgcolor");
        if (valueBackgroundColor != null && valueBackgroundColor instanceof String) {
            this.scroll.setBackgroundColor(FormatHelper.parseColor((String) valueBackgroundColor));
        } else {
            this.scroll.setBackgroundColor(Color.TRANSPARENT);
        }

        if(hashMap.containsKey("refreshable") && hashMap.get("refreshable") instanceof String && ((String) hashMap.get("refreshable")).equalsIgnoreCase("FALSE")) {
            if(mainActivity instanceof MainActivity) {
                ((MainActivity) mainActivity).disableRefresh();
            }
        }

        if(hashMap.containsKey("scroll") && hashMap.get("scroll") instanceof String && ((String) hashMap.get("scroll")).equalsIgnoreCase("BOTTOM")) {
            Log.d("scroll", "bottom");
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        if(hashMap.containsKey("share") && hashMap.get("share") instanceof HashMap && mainActivity instanceof MainActivity) {
            HashMap<Object, Object> share = (HashMap<Object, Object>) hashMap.get("share");

            if(share.containsKey("name") && share.get("name") instanceof String) {
                String name = (String) share.get("name");

                String page = "home";
                if(share.containsKey("view") && share.get("view") instanceof String) {
                    page = (String) share.get("view");
                }

                String command = "";
                if(share.containsKey("command") && share.get("command") instanceof String) {
                    command = (String) share.get("command");
                }

                ((MainActivity) mainActivity).setShareable(name, page, command);
            } else {
                ((MainActivity) mainActivity).setShareable(false);
            }
        } else if(mainActivity instanceof MainActivity) {
            ((MainActivity) mainActivity).setShareable(false);
        }

        Object swipeTopAction = hashMap.get("swipetop");
        Object swipeLeftAction = hashMap.get("swipeleft");
        Object swipeRightAction = hashMap.get("swiperight");
        Object swipeBottomAction = hashMap.get("swipebottom");

        if(swipeTopAction != null || swipeLeftAction != null || swipeRightAction != null || swipeBottomAction != null) {

            if(swipeTopAction != null && swipeTopAction instanceof String) {
                this.swipeTop = (String) swipeTopAction;
            }

            if(swipeLeftAction != null && swipeLeftAction instanceof String) {
                this.swipeLeft = (String) swipeLeftAction;
            }

            if(swipeRightAction != null && swipeRightAction instanceof String) {
                this.swipeRight = (String) swipeRightAction;
            }

            if(swipeBottomAction != null && swipeBottomAction instanceof String) {
                if(mainActivity instanceof MainActivity) {
                    ((MainActivity) mainActivity).disableRefresh();
                }

                this.swipeBottom = (String) swipeBottomAction;
            }

            scroll.setOnTouchListener(new OnSwipeTouchListener(mainActivity) {
                public void onSwipeTop() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeTop, mainActivity);

                    Logs.toast(mainActivity, "top", Toast.LENGTH_LONG);
                }

                public void onSwipeRight() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeRight, mainActivity);

                    Logs.toast(mainActivity, "right", Toast.LENGTH_LONG);
                }

                public void onSwipeLeft() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeLeft, mainActivity);

                    Logs.toast(mainActivity, "left", Toast.LENGTH_LONG);
                }

                public void onSwipeBottom() {
                    MethodHelper methodHelper = new MethodHelper(false);
                    methodHelper.call(swipeBottom, mainActivity);

                    Logs.toast(mainActivity, "bottom", Toast.LENGTH_LONG);
                }

            });
        }
    }

    @SuppressWarnings("unchecked")
    public View parseElement(HashMap<Object, Object> hashMap, boolean allElements) {
        View el = null;

        Object valueValue = hashMap.get("value");

        if (hashMap.get("value") instanceof String) {
            if (hashMap.get("value").equals(sep1) && mainActivity instanceof MainActivity) {
                valueValue = ((MainActivity) mainActivity).getCurrentParameter();
                hashMap.put("value", valueValue);
            }
        }

        if (hashMap.get("click") instanceof String) {
            if (((String) hashMap.get("click")).contains(sep1) && mainActivity instanceof MainActivity) {

                String params = ((MainActivity) mainActivity).getCurrentParameter();

                String valueClick = ((String) hashMap.get("click")).replace(sep1, params);

                hashMap.put("click", valueClick);
            }
        }

        if (hashMap.get("type") instanceof String) {
            String valueType = (String) hashMap.get("type");

            el = this.parseSingleLineElements(valueType, valueValue, hashMap);

            if(allElements) {
                if (valueType.equalsIgnoreCase("list")) {
                    if (valueValue instanceof HashMap) {
                        ArrayList<Object> mArray = new ArrayList<>();
                        ArrayList<String> mArrayActions = new ArrayList<>();
                        ArrayList<String> mArrayActionsLong = new ArrayList<>();

                        Object valueOnClick = hashMap.get("click");
                        Object valueOnLongClick = hashMap.get("longclick");

                        for (int j = 0; j < ((HashMap) valueValue).size(); j++) {
                            if (((HashMap) valueValue).containsKey(j)) {
                                Object elementValue = ((HashMap) valueValue).get(j);

                                if (elementValue instanceof String) {
                                    mArray.add(elementValue);
                                } else if (elementValue instanceof HashMap) {
                                    mArray.add(elementValue);
                                } else {
                                    mArray.add("");
                                }
                            } else {
                                mArray.add("");
                            }

                            if (valueOnClick instanceof HashMap) {
                                if (((HashMap) valueOnClick).containsKey(j)) {
                                    Object elementOnClick = ((HashMap) valueOnClick).get(j);

                                    if (elementOnClick instanceof String) {
                                        mArrayActions.add((String) elementOnClick);
                                    } else {
                                        mArrayActions.add("");
                                    }
                                } else {
                                    mArrayActions.add("");
                                }
                            } else {
                                mArrayActions.add("");
                            }

                            if (valueOnLongClick instanceof HashMap) {
                                if (((HashMap) valueOnLongClick).containsKey(j)) {
                                    Object elementOnClick = ((HashMap) valueOnLongClick).get(j);

                                    if (elementOnClick instanceof String) {
                                        mArrayActionsLong.add((String) elementOnClick);
                                    }
                                } else {
                                    mArrayActionsLong.add("");
                                }
                            }
                        }
                        el = addListView(mArray, mArrayActions, mArrayActionsLong);
                    }
                } else if (valueType.equalsIgnoreCase("table")) {
                    Table table = new Table(mainActivity, this);
                        table.parseHashMap(hashMap);

                    el = table;
                } else if (valueType.equalsIgnoreCase("frame")) {
                    Object src = hashMap.get("src");
                    Object url = hashMap.get("url");

                    if ((src instanceof String && !src.equals("")) || url instanceof String && !url.equals("")) {
                        String adress;
                        if (src instanceof String && !src.equals("")) {
                            adress = (String) src;
                        } else {
                            adress = (String) url;
                        }

                        if (hashMap.containsKey("mode") && hashMap.get("mode") instanceof String && ((String)hashMap.get("mode")).equalsIgnoreCase("secure")) {
                            el = addFrame(adress, false);
                        } else {
                            el = addFrame(adress, true);
                        }
                    }
                } else if (valueType.equalsIgnoreCase("warning")) {
                    if (valueValue instanceof String) {
                        el = addWarning((String) valueValue);
                    }
                } else if (valueType.equalsIgnoreCase("widget")) {
                    if (valueValue instanceof HashMap) {
                        LinearLayout t1 = new LinearLayout(mainActivity);
                        t1.setOrientation(LinearLayout.VERTICAL);

                        for (int j = 0; j < ((HashMap) valueValue).size(); j++) {
                            if (((HashMap) valueValue).containsKey(j)) {
                                Object elementValue = ((HashMap) valueValue).get(j);

                                if (elementValue instanceof HashMap) {
                                    HashMap element = ((HashMap) elementValue);

                                    View view = this.parseElement((HashMap<Object, Object>) element, true);
                                    t1.addView(view);
                                }
                            }
                        }

                        el = t1;
                    }
                } else if (valueType.equalsIgnoreCase("spoiler")) {
                    if(valueValue instanceof HashMap) {
                        String valueLabel = "Spoiler";

                        if(hashMap.containsKey("label") && hashMap.get("label") instanceof String) {
                            valueLabel = (String) hashMap.get("label");
                        }

                        Spoiler spoiler = new Spoiler(this, mainActivity, valueLabel, (HashMap<Object, Object>) valueValue);

                        if(hashMap.containsKey("default") && hashMap.get("default") instanceof String && ((String) hashMap.get("default")).equalsIgnoreCase("SHOW")) {
                            spoiler.setVisible(true);
                        }

                        el = spoiler;
                    }
                } else if (valueType.equalsIgnoreCase("buttonlist")) {
                    if (valueValue instanceof HashMap) {
                        el = addButtonList((HashMap<Object, Object>) valueValue);
                    }
                } else if (valueType.equalsIgnoreCase("range")) {
                    if(hashMap.containsKey("name") && hashMap.get("name") instanceof String) {

                        Object label = hashMap.get("label");
                        if (label != null && label instanceof String) {
                            el = new LinearLayout(mainActivity);
                            View et = new Range(mainActivity, hashMap);
                            View tv = addTextView((String) label);

                            editElements.put((String) hashMap.get("name"), et);

                            ((LinearLayout) el).setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout) el).addView(tv);
                            if (et != null)
                                ((LinearLayout) el).addView(et);
                        } else {
                            el = new Range(mainActivity, hashMap);
                            editElements.put((String) hashMap.get("name"), el);
                        }
                    }
                } else if (valueType.equalsIgnoreCase("editor")) {
                    if(hashMap.containsKey("name") && hashMap.get("name") instanceof String) {

                        Object label = hashMap.get("label");
                        if (label != null && label instanceof String) {
                            el = new LinearLayout(mainActivity);
                            View et = new Editor(mainActivity, hashMap);
                            View tv = addTextView((String) label);

                            editElements.put((String) hashMap.get("name"), et);

                            ((LinearLayout) el).setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout) el).addView(tv);
                            if (et != null)
                                ((LinearLayout) el).addView(et);
                        } else {
                            el = new Editor(mainActivity, hashMap);
                            editElements.put((String) hashMap.get("name"), el);
                        }
                    }
                }
            }

            if (el != null) {
                if (hashMap.get("visible") != null && hashMap.get("visible") instanceof String) {
                    String valueVisible = (String) hashMap.get("visible");

                    if (valueVisible.equalsIgnoreCase("hidden")) {
                        el.setVisibility(View.INVISIBLE);
                    } else if (valueVisible.equalsIgnoreCase("away")) {
                        return null;
                    }
                }

                Object height = hashMap.get("height");
                if (height != null && height instanceof String) {
                    if (height.equals("100%")) {
                        ViewGroup.LayoutParams vc = linear.getLayoutParams();
                        vc.height = ViewGroup.LayoutParams.MATCH_PARENT;

                        linear.setPadding(0, 0, 0, 0);
                        el.setLayoutParams(vc);
                    }
                }

                Object width = hashMap.get("width");
                if (width != null && width instanceof String) {
                    if (width.equals("100%")) {
                        ViewGroup.LayoutParams vc = linear.getLayoutParams();
                        vc.width = ViewGroup.LayoutParams.MATCH_PARENT;

                        linear.setPadding(0, 0, 0, 0);
                        el.setLayoutParams(vc);
                    }
                    /* else if(FormatHelper.isInt(width)) {
                        int heightOld = el.getMeasuredHeight(); /* TODO: divide by zero
                        int widthOld = el.getMeasuredWidth();

                        int widthNew = FormatHelper.stringToInt((String) width, 100);
                        int heightNew = (widthNew / widthOld) * heightOld;


                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthNew, heightNew);
                        el.setLayoutParams(params);

                    }*/
                }

                Object valueColor = hashMap.get("color");
                if (valueColor != null && valueColor instanceof String) {
                    if(el instanceof TextView) // Although Buttons
                        ((TextView) el).setTextColor(FormatHelper.parseColor((String) valueColor));
                }

                Object valueBackgroundColor = hashMap.get("bgcolor");
                if (valueBackgroundColor != null && valueBackgroundColor instanceof String) {
                    el.setBackgroundColor(FormatHelper.parseColor((String) valueBackgroundColor));
                }

                Object id = hashMap.get("id");
                if (id != null && (id instanceof String || id instanceof Integer)) {
                    idElements.put(id+"", el);
                }

                int marginTop = 0;
                int marginLeft = 0;
                int marginRight = 0;
                int marginBottom = 0;

                Object marginValue = hashMap.get("margin");
                if (marginValue != null && marginValue instanceof Integer) {
                    int marginInt = Math.round( Math.round( (int) marginValue*1.5 ) );
                    marginInt = FormatHelper.getPxFromDp(mainActivity, marginInt);

                    marginTop = marginLeft = marginRight = marginBottom = marginInt;
                }

                Object marginTopValue = hashMap.get("marginTop");
                if (marginTopValue != null && marginTopValue instanceof Integer) {
                    int marginInt = Math.round( Math.round( (int) marginTopValue*1.5 ) );
                    marginTop = FormatHelper.getPxFromDp(mainActivity, marginInt);
                }

                Object marginLeftValue = hashMap.get("marginLeft");
                if (marginLeftValue != null && marginLeftValue instanceof Integer) {
                    int marginInt = Math.round( Math.round( (int) marginLeftValue*1.5 ) );
                    marginLeft = FormatHelper.getPxFromDp(mainActivity, marginInt);
                }

                Object marginRightValue = hashMap.get("marginBottom");
                if (marginRightValue != null && marginRightValue instanceof Integer) {
                    int marginInt = Math.round( Math.round( (int) marginRightValue*1.5 ) );
                    marginRight = FormatHelper.getPxFromDp(mainActivity, marginInt);
                }

                Object marginBottomValue = hashMap.get("marginBottom");
                if (marginBottomValue != null && marginBottomValue instanceof Integer) {
                    int marginInt = Math.round( Math.round( (int) marginBottomValue*1.5 ) );
                    marginBottom = FormatHelper.getPxFromDp(mainActivity, marginInt);
                }

                if(marginTop > 0 || marginLeft > 0 || marginRight > 0 || marginBottom > 0) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
                    el.setLayoutParams(layoutParams);
                }
            }
        }
        return el;
    }

    private View parseSingleLineElements(String valueType, Object valueValue, HashMap<Object, Object> hashMap) {
        View el = null;

        if (valueType.equalsIgnoreCase("text")) {
            if (valueValue instanceof String) {
                el = new Text(mainActivity, hashMap);
            }
        } else if (valueType.equalsIgnoreCase("hline")) {
            el = addHorizontalLine();
        } else if (valueType.equalsIgnoreCase("heading") || valueType.equalsIgnoreCase("headingSmall")) {
            if (valueValue instanceof String) {
                if (valueType.equalsIgnoreCase("heading")) {
                    el = addTextView((String) valueValue, "heading");
                } else {
                    el = addTextView((String) valueValue, "headingSmall");
                }
            }
        } else if (valueType.equalsIgnoreCase("checkbox")) {
            boolean checked = false;

            if (hashMap.get("checked") != null && hashMap.get("checked") instanceof String && ((String) hashMap.get("checked")).equalsIgnoreCase("checked")) {
                checked = true;
            }

            Object valueName = hashMap.get("name");

            if (valueName instanceof String) {
                Object label = hashMap.get("label");
                if (label != null && label instanceof String) {
                    el = addCheckbox((String) valueName, checked, (String) label);
                } else {
                    el = addCheckbox((String) valueName, checked, "");
                }
            }
        } else if (valueType.equalsIgnoreCase("submit")) {
            if (valueValue instanceof String) {
                el = addSubmitButton((String) valueValue);
            }
        } else if (valueType.equalsIgnoreCase("button")) {
            Object valueClick = hashMap.get("click");
            Object valueLongClick = hashMap.get("longclick");

            if (valueValue instanceof String) {
                if (valueClick instanceof String && valueLongClick instanceof String) {
                    el = addButton((String) valueValue, (String) valueClick, (String) valueLongClick);
                } else if(valueLongClick instanceof String) {
                    el = addButton((String) valueValue, "", (String) valueLongClick);
                } else if(valueClick instanceof String) {
                    el = addButton((String) valueValue, (String) valueClick, "");
                } else {
                    el = addButton((String) valueValue, "", "");
                }
            }
        } else if (valueType.equalsIgnoreCase("select")) {
            if(valueValue instanceof HashMap && hashMap.containsKey("name") && hashMap.get("name") instanceof String) {
                String name = (String) hashMap.get("name");

                Object label = hashMap.get("label");
                if (label != null && label instanceof String) {
                    el = new LinearLayout(mainActivity);
                    View et = addSpinner((HashMap) valueValue, name);
                    View tv = addTextView((String) label);

                    ((LinearLayout) el).setOrientation(LinearLayout.VERTICAL);
                    ((LinearLayout) el).addView(tv);
                    if (et != null)
                        ((LinearLayout) el).addView(et);
                } else {
                    el = addSpinner((HashMap) valueValue, name);
                }
            }
        } else if (valueType.equalsIgnoreCase("image")) {
            if (valueValue instanceof String) {
                Image im = new Image(mainActivity);
                im.parseHashMap(hashMap);

                el = im;
            }
        } else if (valueType.equalsIgnoreCase("link")) {
            Object valueClick = hashMap.get("click");
            Object valueLongClick = hashMap.get("longclick");

            if (valueValue instanceof String) {
                if (valueClick instanceof String && valueLongClick instanceof String) {
                    el = addLink((String) valueValue, (String) valueClick, (String) valueLongClick);
                } else if(valueLongClick instanceof String) {
                    el = addLink((String) valueValue, "", (String) valueLongClick);
                } else if(valueClick instanceof String) {
                    el = addLink((String) valueValue, (String) valueClick, "");
                } else {
                    el = addLink((String) valueValue, "", "");
                }
            }
        } else if(valueType.equalsIgnoreCase("file")) {
            if(hashMap.containsKey("name") && hashMap.get("name") instanceof String) {
                String valueName = (String) hashMap.get("name");
                el = addUpload(valueName, hashMap);
            }
        } else if (valueType.equalsIgnoreCase("textarea") || valueType.equalsIgnoreCase("input") || valueType.equalsIgnoreCase("password")) {
            Object valueName = hashMap.get("name");

            if (valueName instanceof String) {
                Object label = hashMap.get("label");
                if (label != null && label instanceof String) {
                    el = new LinearLayout(mainActivity);

                    View et = addEditText(hashMap, (String) valueName);
                    View tv = addTextView((String) label);

                    ((LinearLayout) el).setOrientation(LinearLayout.VERTICAL);
                    ((LinearLayout) el).addView(tv);
                    if (et != null)
                        ((LinearLayout) el).addView(et);
                } else {
                    el = addEditText(hashMap, (String) valueName);
                }
            }
        } else if (valueType.equalsIgnoreCase("color")) {
            Object valueName = hashMap.get("name");

            if (valueName instanceof String) {
                Object label = hashMap.get("label");
                if (label != null && label instanceof String) {
                    el = new LinearLayout(mainActivity);

                    View et = new ColorView(mainActivity, hashMap);
                    this.editElements.put((String) valueName, et);

                    View tv = addTextView((String) label);

                    ((LinearLayout) el).setOrientation(LinearLayout.VERTICAL);
                    ((LinearLayout) el).addView(tv);
                    if (et != null)
                        ((LinearLayout) el).addView(et);
                } else {
                    el =  new ColorView(mainActivity, hashMap);
                    this.editElements.put((String) valueName, el);
                }
            }
        }

        return el;
    }

    @SuppressWarnings("unused") // used by invoke
    public void addViews(HashMap hashMap) {
        for (int i = 0; i < hashMap.size(); i++) {
            if (hashMap.get(i) instanceof HashMap) {
                View el = this.parseElement((HashMap) hashMap.get(i), true);

                if (el != null)
                    linear.addView(el);
            }
        }
    }

    private View addUpload(String pName, HashMap<Object, Object> hashMap) {
        UploadButton bt = new UploadButton(mainActivity);
        bt.setOnClickListener(new UploadOnClickListener(this));

        if(pName != null && !pName.equals("")) {
            editElements.put(pName, bt);
        }

        return bt;
    }

    private View addLink(String s, String c, String lc) {
        TextView tv = new TextView(mainActivity);
        tv.setText(s);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        int color = ResourceHelper.getColor(mainActivity, R.color.red);
        tv.setTextColor(color);

        if(!c.equals("")) {
            tv.setOnClickListener(new CustomOnClickListener(mainActivity, c));
        }

        if(!lc.equals("")) {
            tv.setOnLongClickListener(new CustomOnLongClickListener(mainActivity, lc));
        }

        return tv;
    }

    private View addButtonList(HashMap<Object,Object> valueValue) {
        return new ButtonList(mainActivity, valueValue);
    }

    private View addSpinner(HashMap valueValue, String name) {
        List<String> spinnerArray = new ArrayList<>();

        for (int j = 0; j < valueValue.size(); j++) {
            if(valueValue.get(j) instanceof String) {
                spinnerArray.add((String) valueValue.get(j));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner el = new Spinner(mainActivity);
        el.setAdapter(adapter);

        editElements.put(name, el);

        return el;
    }

    private View addWarning(String valueValue) {
        final Dialog dialog;

        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            dialog = new Dialog(this.mainActivity, R.style.DialogDark);
        } else {
            dialog = new Dialog(this.mainActivity, R.style.DialogLight);
        }

        dialog.setContentView(de.michaelsoftware.android.Vision.R.layout.dialog_warning);
        dialog.setTitle("Warnung");

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(de.michaelsoftware.android.Vision.R.id.warning_text);
        text.setText(valueValue);

        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            text.setTextColor(ResourceHelper.getColor(mainActivity, R.color.textColorDark));
        }

        Button dialogButton = (Button) dialog.findViewById(de.michaelsoftware.android.Vision.R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        try {
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private View addCheckbox(String pName, boolean checked, String pLabel) {
        CheckBox chk = new CheckBox(this.mainActivity);
        chk.setText(pLabel);
        chk.setChecked(checked);

        if(pName != null && !pName.isEmpty()) {
            editElements.put(pName, chk);

            return chk;
        }

        return null;
    }

    @SuppressLint("NewApi")
    private View addFrame(String src, boolean secureMode) {
        WebView wv = new TouchWebView(mainActivity);
        wv.loadUrl(src);
        WebSettings webSettings = wv.getSettings();

        if(secureMode || Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setJavaScriptEnabled(true);
        }

        wv.setVerticalScrollBarEnabled(true);
        wv.setHorizontalScrollBarEnabled(true);

        webSettings.setAllowContentAccess(true);
        wv.setWebViewClient(new WebViewClient());
            ViewGroup.LayoutParams vc2 = linear.getLayoutParams();
            vc2.height = ViewGroup.LayoutParams.MATCH_PARENT;
        wv.setLayoutParams(vc2);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        webSettings.setAllowFileAccess(false);
        webSettings.setPluginState(WebSettings.PluginState.OFF);

        if(mainActivity instanceof MainActivity)
            ((MainActivity) mainActivity).disableRefresh();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR) {
            try {
                Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", Boolean.TYPE);
                m1.invoke(webSettings, Boolean.TRUE);

                Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", Boolean.TYPE);
                m2.invoke(webSettings, Boolean.TRUE);

                Method m3 = WebSettings.class.getMethod("setDatabasePath", String.class);
                m3.invoke(webSettings, mainActivity.getApplicationContext().getFilesDir().getPath() + mainActivity.getPackageName() + "/databases/");

                Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", Long.TYPE);
                m4.invoke(webSettings, 1024*1024*8);

                Method m5 = WebSettings.class.getMethod("setAppCachePath", String.class);
                m5.invoke(webSettings, mainActivity.getApplicationContext().getFilesDir().getPath() + mainActivity.getPackageName() + "/cache/");

                Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", Boolean.TYPE);
                m6.invoke(webSettings, Boolean.TRUE);
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Logs.d(this, e.getMessage());
            }
        }

        return wv;
    }

    public View addTextView(String s) {
        TextView t1 = new TextView(mainActivity);
        t1.setText(s);

        //linear.addView(t1);
        return t1;
    }

    private View addTextView(String s, String pType) {
        if (pType.equalsIgnoreCase("heading")) {
            TextView t1 = new TextView(mainActivity);

            if (Build.VERSION.SDK_INT < 23) {
                //noinspection deprecation
                t1.setTextAppearance(mainActivity, android.R.style.TextAppearance_Large);
            } else {
                t1.setTextAppearance(android.R.style.TextAppearance_Large);
            }

            t1.setText(s);
            if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
                t1.setTextColor(ResourceHelper.getColor(mainActivity, R.color.textColorDark));
            } else {
                t1.setTextColor(ResourceHelper.getColor(mainActivity, R.color.textColorLight));
            }

            //linear.addView(t1);
            return t1;
        }

        if (pType.equalsIgnoreCase("headingSmall")) {
            TextView t1 = new TextView(mainActivity);

            if (Build.VERSION.SDK_INT < 23) {
                //noinspection deprecation
                t1.setTextAppearance(mainActivity, android.R.style.TextAppearance_Medium);
            } else {
                t1.setTextAppearance(android.R.style.TextAppearance_Medium);
            }

            t1.setText(s);
            if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
                t1.setTextColor(ResourceHelper.getColor(mainActivity, R.color.textColorDark));
            } else {
                t1.setTextColor(ResourceHelper.getColor(mainActivity, R.color.textColorLight));
            }

            return t1;
        }

        return null;
    }

    private View addSubmitButton(String s) {
        Button b1 = new Button(mainActivity);
        b1.setText(s);
        b1.setOnClickListener(new CustomOnClickListener(this, "submitGui"));

        return b1;
    }

    private View addButton(String s, String c, String lc) {
        Button b1 = new Button(mainActivity);
        b1.setText(s);

        if (!c.equals("")) {
            b1.setOnClickListener(new CustomOnClickListener(mainActivity, c));
        }

        if (!lc.equals("")) {
            b1.setOnLongClickListener(new CustomOnLongClickListener(mainActivity, lc));
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 10, 0, 10);

        b1.setLayoutParams(layoutParams);

        return b1;
    }

    private View addEditText(HashMap<Object, Object> hashMap, String pName) {
        View e1 = new EditTextView(mainActivity, hashMap);

        if(pName != null && !pName.isEmpty()) {
            editElements.put(pName, e1);

            return e1;
        }

        return null;
    }

    private View addListView(ArrayList<Object> pArray, ArrayList<String> pArrayActions, ArrayList<String> pArrayActionsLong) {
        LinearList li = new LinearList(mainActivity);

        for (int i = 0; i < pArray.size(); i++) {
            if(pArray.get(i) != null && // test if array is empty
                    pArrayActions != null && FormatHelper.containsKey(pArrayActions, i) && !pArrayActions.get(i).equals("") && // test if action exists
                    pArrayActionsLong != null && FormatHelper.containsKey(pArrayActionsLong, i) && !pArrayActionsLong.get(i).equals("")) { // test if action for long click exists

                li.addItem(pArray.get(i), pArrayActions.get(i), pArrayActionsLong.get(i));
            } else if(pArray.get(i) != null && // test if array is empty
                    pArrayActions != null && FormatHelper.containsKey(pArrayActions, i) && !pArrayActions.get(i).equals("")) { // test if action exists

                li.addItem(pArray.get(i), pArrayActions.get(i));
            } else if(pArray.get(i) != null && // test if array is empty
                    pArrayActionsLong != null && FormatHelper.containsKey(pArrayActionsLong, i) && !pArrayActionsLong.get(i).equals("")) { // test if action for long click exists

                li.addItem(pArray.get(i), null, pArrayActionsLong.get(i));
            } else if(pArray.get(i) != null) {
                li.addItem(pArray.get(i));
            }
        }

        return li;
    }

    private View addHorizontalLine() {
        View ruler;
        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            ruler = new View(mainActivity.getApplicationContext()); ruler.setBackgroundColor(ResourceHelper.getColor(mainActivity, R.color.textColorDark));
        } else {
            ruler = new View(mainActivity.getApplicationContext()); ruler.setBackgroundColor(0xFF000000);
        }

        linear.addView(ruler, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 2));

        return null;
    }

    @SuppressWarnings("unused") // used by invoke
    public void submitGui(View v) {
        if(mainActivity instanceof MainActivity) {
            HashMap<String,String> nameValuePair = new HashMap<>();
            HttpPostJsonHelper httpPost = new HttpPostJsonHelper(((MainActivity) mainActivity).getLoginHelper());

            for (Map.Entry<String, Object> entry : editElements.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();

                if (value instanceof EditText) {
                    nameValuePair.put(key, ((EditText) value).getText().toString());
                }

                if (value instanceof CheckBox) {
                    if(((CheckBox) value).isChecked()) {
                        nameValuePair.put(key, "1");
                    } else {
                        nameValuePair.put(key, "0");
                    }
                }

                if (value instanceof Spinner) {
                    nameValuePair.put(key, ((Spinner) value).getSelectedItem().toString() );
                }

                if(value instanceof UploadButton) {
                    httpPost.addDataName(key); // only there to tell the class that this is a data field
                    nameValuePair.put(key, ((UploadButton) value).getFile());
                }

                if(value instanceof Range) {
                    String valueString = Integer.toString( ((Range) value).getProgress() );
                    nameValuePair.put(key, valueString);
                }

                if(value instanceof ColorView) {
                    nameValuePair.put(key, ((ColorView) value).getValue());
                }

                if(value instanceof Editor) {
                    nameValuePair.put(key, ((Editor) value).getValue());
                }
            }

            String urlStr = ((MainActivity) mainActivity).getCurrentViewHost();
            httpPost.setPost(nameValuePair);
            httpPost.setOutput(mainActivity, "getContent");
            httpPost.execute(urlStr);

            Logs.toast(mainActivity, nameValuePair.toString(), Logs.LENGTH_LONG);
        }
    }

    public Activity getActivity() {
        return mainActivity;
    }

    public void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public View getCurrentView() {
        return currentView;
    }

    public LinearLayout.LayoutParams getLayoutParams() {
        return (LinearLayout.LayoutParams) linear.getLayoutParams();
    }

    public void toggleView(String id) {
        if(idElements.containsKey(id) && idElements.get(id) instanceof View) {
            View view = (View) idElements.get(id);

            if(view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.GONE);
            } else if(view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }
}
