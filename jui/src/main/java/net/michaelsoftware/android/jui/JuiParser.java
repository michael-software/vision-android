package net.michaelsoftware.android.jui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.michaelsoftware.android.jui.interfaces.Listener;
import net.michaelsoftware.android.jui.models.ActionModel;
import net.michaelsoftware.android.jui.models.ConfigModel;
import net.michaelsoftware.android.jui.models.ViewModel;
import net.michaelsoftware.android.jui.network.HttpPostJsonHelper;
import net.michaelsoftware.android.jui.network.JsonParserAsync;
import net.michaelsoftware.android.jui.views.Button;
import net.michaelsoftware.android.jui.views.Checkbox;
import net.michaelsoftware.android.jui.views.Container;
import net.michaelsoftware.android.jui.views.File;
import net.michaelsoftware.android.jui.views.FileView;
import net.michaelsoftware.android.jui.views.Frame;
import net.michaelsoftware.android.jui.views.Heading;
import net.michaelsoftware.android.jui.views.HorizontalLine;
import net.michaelsoftware.android.jui.views.Image;
import net.michaelsoftware.android.jui.views.Input;
import net.michaelsoftware.android.jui.views.InputColorView;
import net.michaelsoftware.android.jui.views.InputDateView;
import net.michaelsoftware.android.jui.views.JuiView;
import net.michaelsoftware.android.jui.views.Link;
import net.michaelsoftware.android.jui.views.List;
import net.michaelsoftware.android.jui.views.Range;
import net.michaelsoftware.android.jui.views.Select;
import net.michaelsoftware.android.jui.views.SpinnerView;
import net.michaelsoftware.android.jui.views.Table;
import net.michaelsoftware.android.jui.views.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael on 28.08.2016.
 */
public class JuiParser {
    Activity activity;
    ScrollView scrollView;
    LinearLayout linearLayout;
    HashMap<String, View> submitElements;
    private java.util.List<ActionModel> actions = new ArrayList<>();
    private ConfigModel config;
    private ArrayList<Listener.OnSubmitListener> submitListeners;
    private ArrayList<Listener.OnParseHeaderListener> parseHeaderListeners;
    private ArrayList<ViewModel> customSingleLineElements = new ArrayList<>();
    private ArrayList<ViewModel> customElements = new ArrayList<>();
    private HashMap<String, String> customHttpElements = new HashMap<>();
    private String lastUrl = "http://www.google.de";
    private Listener.OnBeforeParseListener onBeforeParse = null;

    public JuiParser(Activity activity, ScrollView scroll, LinearLayout linearLayout) {
        this.activity = activity;
        this.scrollView = scroll;
        this.linearLayout = linearLayout;

        this.submitElements = new HashMap<>();

        this.submitListeners = new ArrayList<>();
        this.parseHeaderListeners = new ArrayList<>();
    }

    public void parse(HashMap<Object, Object> hashMap) {

        boolean parse = true;

        if(onBeforeParse != null) {
            parse = onBeforeParse.onBeforeParse(hashMap);
        }

        if(parse) {
            this.clean();
            submitElements = new HashMap<>();

            if (Tools.isHashmap(hashMap.get("head"))) {
                HashMap<Object, Object> head = (HashMap<Object, Object>) hashMap.get("head");

                for (int i = 0, x = parseHeaderListeners.size(); i < x; i++) {
                    parseHeaderListeners.get(i).onParseHead(head);
                }
            }

            if (Tools.isHashmap(hashMap.get("data"))) {
                hashMap = (HashMap<Object, Object>) hashMap.get("data");
            }

            this.parseReturn(hashMap, true, true);
        }
    }

    public LinearLayout parseReturn(HashMap<Object, Object> hashMap) {
        return this.parseReturn(hashMap, false, true);
    }

    public LinearLayout parseReturn(HashMap<Object, Object> hashMap, boolean allElements) {
        return this.parseReturn(hashMap, false, allElements);
    }

    private LinearLayout parseReturn(HashMap<Object, Object> hashMap, boolean addToRoot, boolean allElements) {
        if(Tools.isHashmap(hashMap.get("data"))) {
            hashMap = (HashMap<Object, Object>) hashMap.get("data");
        }

        LinearLayout linear = null;
        if(!addToRoot) {
            linear = new LinearLayout(activity);
            linear.setOrientation(LinearLayout.VERTICAL);
        }

        for(int i = 0, x = hashMap.size(); i < x; i++) {
            if(Tools.isHashmap(hashMap.get(i))) {

                HashMap<Object, Object> element = (HashMap<Object, Object>) hashMap.get(i);

                if(Tools.isString(element.get("type"))) {
                    JuiView viewElement = this.parseElement((String) element.get("type"), element, allElements);

                    if(viewElement != null) {
                        View view = viewElement.getView(this);
                        if(view != null) {
                            if(addToRoot) {

                                this.linearLayout.addView(view);
                            } else {
                                linear.addView(view);
                            }
                        }
                    }
                }
            }
        }

        return linear;
    }

    private void clean() {
        this.linearLayout.removeAllViews();
    }

    public void parse(String json) {
        JsonParserAsync jsonParser = new JsonParserAsync();
        jsonParser.setOutput(this, "parse");
        jsonParser.execute(json);
    }

    private JuiView parseElement(String type, HashMap<Object, Object> element, boolean allElements) {
        JuiView view = null;

        //Log.d("element", type);

        if(allElements) {
            view = this.parseMultiLineElements(type, element);
        }

        if(view == null) {
            if (Tools.isEqual(type, "text")) {
                view = new Text(activity, element);
            } else if (Tools.isEqual(type, "heading")) {
                view = new Heading(activity, element);
            } else if (Tools.isEqual(type, "input")) {
                view = new Input(activity, element);
            } else if (Tools.isEqual(type, "file")) {
                view = new File(activity, element);
            } else if (Tools.isEqual(type, "button")) {
                view = new Button(activity, element);
            } else if (Tools.isEqual(type, "image")) {
                view = new Image(activity, element);
            } else if (Tools.isEqual(type, "link")) {
                view = new Link(activity, element);
            } else if (Tools.isEqual(type, "select")) {
                view = new Select(activity, element);
            } else if (Tools.isEqual(type, "checkbox")) {
                view = new Checkbox(activity, element);
            } else if (Tools.isEqual(type, "hline")) {
                view = new HorizontalLine(activity, element);
            } else {
                //Log.d("not in List", type);
                for(int i = 0, x = customSingleLineElements.size(); i < x; i++) {
                    ViewModel value = customSingleLineElements.get(i);

                    if(Tools.isEqual(type, value.name)) {
                        try {
                            Object obj = value.view.getConstructor(Context.class, HashMap.class).newInstance(this.getActivity(), element);

                            if(obj instanceof JuiView)
                                return (JuiView) obj;
                        } catch (InstantiationException ex) {
                            Log.d("Error parsing HashMap", ex.getMessage());
                        } catch (IllegalAccessException ex) {
                            Log.d("Error parsing HashMap", ex.getMessage());
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        return view;
    }

    private JuiView parseMultiLineElements(String type, HashMap<Object, Object> element) {
        JuiView view = null;

        if(Tools.isEqual(type, "list")) {
            view = new List(activity, element);
        } else if(Tools.isEqual(type, "table")) {
            view = new Table(activity, element);
        } else if(Tools.isEqual(type, "container")) {
            view = new Container(activity, element);
        } else if(Tools.isEqual(type, "range")) {
            view = new Range(activity, element);
        } else if(Tools.isEqual(type, "frame")) {
            view = new Frame(activity, element); /* TODO */
        } else {
            //Log.d("not in multiline List", type);
            for(int i = 0, x = this.customElements.size(); i < x; i++) {
                ViewModel value = this.customElements.get(i);

                if(Tools.isEqual(type, value.name)) {
                    try {
                        Object obj = value.view.getConstructor(Context.class, HashMap.class).newInstance(this.getActivity(), element);

                        if(obj instanceof JuiView)
                            return (JuiView) obj;
                    } catch (InstantiationException ex) {
                        Log.d("Error parsing HashMap", ex.getMessage());
                    } catch (IllegalAccessException ex) {
                        Log.d("Error parsing HashMap", ex.getMessage());
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        return view;
    }

    public void registerSubmitElement(String name, View view) {
        submitElements.put(name, view);
    }

    public static View addProperties(View view, HashMap<Object, Object> properties) {
        if(!Tools.empty(properties) && !Tools.empty(view)) {
            if (Tools.isString(properties.get("width"))) {
            /* TODO */
            }

            if (Tools.isString(properties.get("height"))) {
            /* TODO */
            }

            if (Tools.isString(properties.get("color"))) {
                if(view instanceof TextView) {
                    ((TextView) view).setTextColor( Tools.parseColor((String) properties.get("color")) );
                }
            }

            if (Tools.isString(properties.get("background"))) {
                view.setBackgroundColor(Tools.parseColor((String) properties.get("background")));
            }

            if(Tools.isBool(properties.get("autofocus")) && (boolean) properties.get("autofocus")) {
                if(view.isFocusable() || view.isFocusableInTouchMode()) {
                    view.requestFocus();
                    view.requestFocusFromTouch();
                }
            }


            /* MARGIN */
            int offsetY = 0;
            if(view instanceof android.widget.Button) {
                offsetY = 5;
            }

            int marginLeft = 0, marginTop, marginRight = 0, marginBottom;

            marginTop = marginBottom = offsetY;

            if (Tools.isInt(properties.get("margin"))) {
                marginLeft = marginRight = Tools.getInt(properties.get("margin"), 0);
                marginTop = marginBottom = Tools.getInt(properties.get("margin"), 0)+offsetY;
            }

            if (Tools.isInt(properties.get("marginTop"))) {
                marginTop = Tools.getInt(properties.get("marginTop"), marginTop)+offsetY;
            }

            if (Tools.isInt(properties.get("marginLeft"))) {
                marginLeft = Tools.getInt(properties.get("marginLeft"), marginLeft);
            }

            if (Tools.isInt(properties.get("marginRight"))) {
                marginRight = Tools.getInt(properties.get("marginRight"), marginRight);
            }

            if (Tools.isInt(properties.get("marginBottom"))) {
                marginBottom = Tools.getInt(properties.get("marginBottom"), marginBottom)+offsetY;
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(marginLeft*2, marginTop*2, marginRight*2, marginBottom*2);

            view.setLayoutParams(layoutParams);

            /* PADDING */
            int paddingLeft = view.getPaddingLeft(),
                    paddingTop = view.getPaddingTop(),
                    paddingRight = view.getPaddingRight(),
                    paddingBottom = view.getPaddingBottom();

            if (Tools.isInt(properties.get("padding"))) {
                paddingLeft = paddingTop = paddingRight = paddingBottom = Tools.getInt(properties.get("padding"), 0);
            }

            if (Tools.isInt(properties.get("paddingTop"))) {
                paddingTop = Tools.getInt(properties.get("paddingTop"), paddingTop);
            }

            if (Tools.isInt(properties.get("paddingLeft"))) {
                paddingLeft = Tools.getInt(properties.get("paddingLeft"), paddingLeft);
            }

            if (Tools.isInt(properties.get("paddingRight"))) {
                paddingRight = Tools.getInt(properties.get("paddingRight"), paddingRight);
            }

            if (Tools.isInt(properties.get("paddingBottom"))) {
                paddingBottom = Tools.getInt(properties.get("paddingBottom"), paddingBottom);
            }

            view.setPadding(paddingLeft*2, paddingTop*2, paddingRight*2, paddingBottom*2);


            if (Tools.isString(properties.get("visible"))) {
                if (properties.get("visible").equals("away")) {
                    view = null;
                } else if (properties.get("visible").equals("invisible")) {
                    view.setVisibility(View.INVISIBLE);
                }
                //retval.style.display = 'none';
            }
        }

        return view;
    }

    public View addInputProperties(View view, HashMap<Object, Object> properties) {
        if(!Tools.empty(view) && !Tools.empty(properties)) {
            if(Tools.isString(properties.get("change"))) {
                final String changeAction = (String) properties.get("change");

                if(view instanceof EditText) {
                    ((EditText) view).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            JuiAction.call(JuiParser.this, changeAction.replace("this.value", Tools.escape(charSequence.toString()) ));
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                } else if(view instanceof CheckBox) {
                    ((CheckBox) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if(b) {
                                JuiAction.call(JuiParser.this, changeAction.replace("this.value", 1+""));
                            } else {
                                JuiAction.call(JuiParser.this, changeAction.replace("this.value", 0+""));
                            }
                        }
                    });
                } else if(view instanceof SeekBar) {
                    ((SeekBar) view).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            JuiAction.call(JuiParser.this, changeAction.replace("this.value", i+""));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                } else if(view instanceof SpinnerView) {
                    ((SpinnerView) view).setOnValueChangeListener(new OnValueChangeListener() {
                        @Override
                        public void onValueChange(String value) {
                            JuiAction.call(JuiParser.this, changeAction.replace("this.value", value));
                        }
                    });
                }
            }

            if(Tools.isString(properties.get("label"))) {


                if(view instanceof CheckBox) {
                    ((CheckBox) view).setText((String) properties.get("label"));
                } else {
                    LinearLayout linearLayout = new LinearLayout(activity);

                    TextView tv = new TextView(activity);
                    tv.setText((String) properties.get("label"));

                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(tv);
                    linearLayout.addView(view);

                    view = linearLayout;
                }
            }
        }

        return view;
    }

    public void addAction(String name, int numParams, Object object) {
        actions.add(new ActionModel(name, numParams, object));
    }

    public java.util.List<ActionModel> getActions() {
        return actions;
    }

    public void openUrl(String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        activity.startActivity(intent);
    }

    public void submit() {
        this.submit(this.lastUrl);
    }

    public void submit(String url) {
        HashMap<String, String> formData = new HashMap<>();

        HttpPostJsonHelper httpPostJsonHelper = new HttpPostJsonHelper(this);

        for(Map.Entry<String, View> entry : submitElements.entrySet()) {
            String key = entry.getKey();
            View view = entry.getValue();

            if(view instanceof EditText) {
                formData.put(key, ((EditText) view).getText().toString());
            } else if(view instanceof InputDateView) {
                formData.put(key, ((InputDateView) view).getValue() + "");
            } else if(view instanceof InputColorView) {
                formData.put(key, ((InputColorView) view).getValue() + "");
            } else if(view instanceof SpinnerView) {
                formData.put(key, ((SpinnerView) view).getValue() + "");
            } else if(view instanceof FileView) {
                formData.put(key, ((FileView) view).getUri() + "");
                httpPostJsonHelper.addDataName(key);
            } else {
                for(int i = 0, x = submitListeners.size(); i < x; i++) {
                    String value = submitListeners.get(i).onSubmit(view);

                    if(Tools.isString(value)) {
                        formData.put(key, value);
                        break;
                    }
                }
            }
        }

        formData.putAll(customHttpElements);

        httpPostJsonHelper.setOutput(this, "submitOutput");
        httpPostJsonHelper.setPost(formData);
        httpPostJsonHelper.execute(url);
    }

    public void parseUrl(String url) {
        this.lastUrl = url;

        HashMap<String, String> formData = new HashMap<>();

        HttpPostJsonHelper httpPostJsonHelper = new HttpPostJsonHelper(this);

        formData.putAll(customHttpElements);

        httpPostJsonHelper.setOutput(this, "submitOutput");
        httpPostJsonHelper.setPost(formData);
        httpPostJsonHelper.execute(url);
    }

    public void submitOutput(HashMap<Object, Object> output) {
        if(output != null) {
            this.parse(output);
        }
    }

    public void addSingleLineElement(ViewModel model) {
        this.customSingleLineElements.add(model);
    }

    public void addElement(ViewModel model) {
        this.customElements.add(model);
    }

    public Activity getActivity() {
        return activity;
    }

    public ConfigModel getConfig() {
        if(this.config != null) {
            return this.config;
        }

        return new ConfigModel();
    }

    public void setConfig(ConfigModel config) {
        this.config = config;
    }

    public void requestUnfreshable() { /* TODO */
    }

    public void setOnSubmitListener(Listener.OnSubmitListener listener) {
        this.submitListeners.add(listener);
    }

    public void setOnParseHeaderListener(Listener.OnParseHeaderListener listener) {
        this.parseHeaderListeners.add(listener);
    }

    public void setCustomHttpElements(String name, String value) {
        this.customHttpElements.put(name, value);
    }

    public void setOnBeforeParse(Listener.OnBeforeParseListener onBeforeParse) {
        this.onBeforeParse = onBeforeParse;
    }

    public void reload() {
        this.parseUrl(this.lastUrl);
    }
}
