package de.michaelsoftware.android.Vision.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.network.HttpImageAsync;
import net.michaelsoftware.android.jui.network.HttpPostJsonHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.michaelsoftware.android.Vision.MyService;
import de.michaelsoftware.android.Vision.OfflineActivity;
import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.activity.AbstractMainActivity.SiteActionsActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;
import de.michaelsoftware.android.Vision.tools.SecurityHelper;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnActionExpandListener;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnClickListener;
import de.michaelsoftware.android.Vision.tools.network.DownloadHelper;
import de.michaelsoftware.android.Vision.tools.network.JsonParserAsync;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;

public class MainActivity extends SiteActionsActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SearchView searchView;
    private ProgressDialog progressDialog;

    private String urlStr = null;
    private String currentParameter = "";
    private String currentView = "home";
    private String currentName = "android";
    private CustomOnActionExpandListener customOnActionExpandListener;

    private HashMap<String, String> pluginList = new HashMap<>();
    private HashMap<String, String> pluginListImages = new HashMap<>();
    private ArrayList<String> pluginShareable = new ArrayList<>();
    private boolean isShareable = false;
    private String shareName;
    private String sharePage;
    private String shareCommand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(Looper.getMainLooper());
        CheckIfServiceIsRunning();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(searchTask);
    }


    /* Create Menus / Manage Menu touchs / Homebutton/up touches */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(de.michaelsoftware.android.Vision.R.menu.menu_main, menu);

        this.mMenu = menu;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(de.michaelsoftware.android.Vision.R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        customOnActionExpandListener = new CustomOnActionExpandListener(this);
        customOnActionExpandListener.setOpenHome(true);

        MenuItem item = this.mMenu.findItem(R.id.action_share);
        item.setVisible(this.isShareable);

        MenuItemCompat.setOnActionExpandListener((menu.findItem(de.michaelsoftware.android.Vision.R.id.action_search)), customOnActionExpandListener);

        super.onCreateOptionsMenu(menu);

        return true;
    }

    public void manageSearchInput(String query) {
        mHandler.removeCallbacks(searchTask);

        final String search = query;
        final MainActivity mainActivity = this;

        if(!query.equals("")) {
            searchTask = new Runnable() {
                @Override
                public void run() {

                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "bearer " + mainActivity.getLoginHelper().getAuthtoken());

                    if (loginHelper != null) {
                        HttpPostJsonHelper postHelper = new HttpPostJsonHelper();
                        postHelper.setSpecialData(search);
                        postHelper.setOutput(mainActivity, "insertSearch");
                        postHelper.setHeaders(headers);
                        postHelper.execute(loginHelper.getServer() + "api/search.php?query=" + HttpPostJsonHelper.urlEncode(search));
                    }
                }
            };

            mHandler.postDelayed(searchTask, 500);
        } else {
            this.insertSearch(null, "");
        }
    }

    @Override
    public void onRefresh() {
        this.gui.reload();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        if (id == de.michaelsoftware.android.Vision.R.id.action_settings) {
            this.openPlugin("android", "settings");
            return true;
        } else if (id == de.michaelsoftware.android.Vision.R.id.action_add_account) {
            Intent intent = new Intent(this, AddNewAccountActivity.class);
            this.startActivity(intent);

            return true;
        } else if (id == de.michaelsoftware.android.Vision.R.id.action_change_account) {
            loginHelper.openSelectUserAccount();

            return true;
        } else if(id == de.michaelsoftware.android.Vision.R.id.action_usersettings) {
            this.openPlugin("plg_user");
        } else if(id == de.michaelsoftware.android.Vision.R.id.action_add_shortcut) {
            this.addShortcut();
        } else if(id == de.michaelsoftware.android.Vision.R.id.action_share) {
            this.sharePlugin();
        } else if(id == R.id.action_feedback) {

            this.sendMail();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sharePlugin() {
        if(this.shareName != null && this.sharePage != null && this.shareCommand != null) {
            this.openPluginNoHistory(shareName, sharePage, shareCommand);
        } else {
            String url = loginHelper.getServer() + "ajax.php?action=share&plugin=" + this.currentName + "&page=" + this.currentView + "&cmd=" + this.currentParameter;

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "bearer " + this.getLoginHelper().getAuthtoken());

            HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
            httpPost.setOutput(this, "getShareUrl");
            httpPost.setHeaders(headers);
            httpPost.execute(url);
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void getShareUrl(HashMap<Object, Object> hashMap) {
        if(hashMap.containsKey("url") && hashMap.get("url") instanceof String) {
            String url = (String) hashMap.get("url");
            String pluginName = this.pluginList.get(this.currentName);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Vision - " + pluginName);
            this.startActivity(sharingIntent);
        }
    }

    @Override
    public void onBackPressed() {
        historyHelper.openLastEntry();
    }
    /* End Menu Section */


    // Manage Orientation to prevent recreation of the activity
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @SuppressWarnings("unused") // used by invoke
    public void insertSearch(HashMap<Object, Object> hashMap, Object query) {
        HashMap<Object, Object> hashmapUpper = new HashMap<>();
        HashMap<Object, Object> hashmapHeading = new HashMap<>();
            hashmapHeading.put("type","heading");
            if(!query.equals("")) {
                hashmapHeading.put("value", "Suche nach " + query);
            } else {
                hashmapHeading.put("value", "Bitte geben sie ein Suchwort ein.");
            }

        HashMap<Object, Object> hashmapListView = new HashMap<>();
        hashmapListView.put("type","list");
        HashMap<Object, Object> hashmapList = new HashMap<>();
        HashMap<Object, Object> hashmapClick = new HashMap<>();

        if(hashMap != null)
        for(int i=0; i<hashMap.size(); i++) {
            Object element = hashMap.get(i);

            if(element instanceof HashMap) {
                if(((HashMap) element).containsKey("title") && ((HashMap) element).get("title") instanceof String) {
                    String title = (String) ((HashMap) element).get("title");

                    hashmapList.put(i, title);
                }

                if(((HashMap) element).containsKey("click") && ((HashMap) element).get("click") instanceof String) {
                    String click = (String) ((HashMap) element).get("click");

                    hashmapClick.put(i, click);
                }

            }
        }

        hashmapListView.put("value", hashmapList);
        hashmapListView.put("click", hashmapClick);
        hashmapUpper.put(0, hashmapHeading);
        hashmapUpper.put(1, hashmapListView);

        gui.parse(hashmapUpper);

        this.closeDrawer();
    }

    public void loadMenu() {
        this.loadMenu(false);
    }

    public void loadMenu(Boolean pForceReload) {
        String urlStr = loginHelper.getServer() + "api/plugins.php";
        String dataString = offlineHelper.getData(urlStr);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + this.getLoginHelper().getAuthtoken());

        HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
        httpPost.setOutput(this, "getMenuContent");
        httpPost.setHeaders(headers);
        httpPost.execute(urlStr);
    }

    public void openPlugin(String pName, String pView) {
        this.openPlugin(pName, pView, "");
    }

    public void openPlugin(String pName) {
        this.openPlugin(pName, "home", "");
    }

    public void openPlugin(String pName, String pView, String pParameter) {
        pName      = FormatHelper.encodeURI(pName);
        pView      = FormatHelper.encodeURI(pView);
        pParameter = FormatHelper.encodeURI(pParameter);

        historyHelper.addHistory(pName, pView, pParameter);

        if(gui != null && gui.alertDialog != null && gui.alertDialog.isShowing()) {
            gui.alertDialog.dismiss();
        }

        this.openPluginNoHistory(pName, pView, pParameter);
    }

    public void openPluginNoHistory(String pName, String pView, String pParameter) {
        this.currentName      = pName;
        this.currentView      = pView;
        this.currentParameter = pParameter;

        if(loginHelper.getServer() != null) {
            urlStr = loginHelper.getServer() + "api/plugin.php?plugin=" + pName + "&page=" + pView + "&cmd=" + pParameter;
            Logs.toast(MainActivity.this, urlStr, Logs.LENGTH_LONG);

            if(this.pluginShareable.contains(pName)) {
                this.isShareable = true;
            } else {
                this.isShareable = false;
            }

            if(this.mMenu != null) {
                MenuItem item = this.mMenu.findItem(R.id.action_share);
                item.setVisible(this.isShareable);
                this.invalidateOptionsMenu();
            }

            if (pName.equals("android")) {
                if (pView.equals("home")) {
                    this.openHome();
                } else if (pView.equals("settings")) {
                    this.openSettings();
                } else if (pView.equals("share")) {
                    this.openShare(pParameter);
                }

                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
            } else {
                this.enableRefresh();

                String data = offlineHelper.getData(urlStr);
                if (data == null || data.equals("")) {
                    this.gui.parse(urlStr);
                } else {
                    getContent(data);
                }
            }

            if (searchView != null) {
                searchView.setIconified(true);
                searchView.clearFocus();
            }

            if (mMenu != null) {
                customOnActionExpandListener.setOpenHome(false);
                (mMenu.findItem(de.michaelsoftware.android.Vision.R.id.action_search)).collapseActionView();
            }
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void toggleView(String view) {
        if(gui != null) {
            gui.toggleView(view);
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void addViews(String jsonString) {
        JsonParserAsync jsonParserAsync = new JsonParserAsync();
        jsonParserAsync.setOutput(gui, "addViews");
        jsonParserAsync.execute(jsonString);
    }

    @SuppressWarnings("unused") // used by invoke
    public void addViews(String plugin, String jsonString) {
        if(plugin.equals(this.currentName)) {
            this.addViews(jsonString);
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void addViews(String plugin, String view, String jsonString) {
        if(plugin.equals(this.currentName) && (view.equals(this.currentView) || ( view.equals("") && this.currentView.equals("home") ) || ( view.equals("home") && this.currentView.equals("") ) ) ) {
            this.addViews(jsonString);
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void addViews(String plugin, String view, String parameter, String jsonString) {
        if(plugin.equals(this.currentName) && view.equals(this.currentView) && parameter.equals(this.currentParameter)) {
            this.addViews(jsonString);
        }
    }

    /*
    public void refreshPlugin(String pName, String pView, String pParameter) {
        if(pView == null) pView = "";
        if(pParameter == null) pParameter = "";

        urlStr = loginHelper.getServer() + "ajax.php?plugin=" + pName + "&page=" + pView + "&cmd=" + pParameter + "&get=view";

        String data = offlineHelper.getData(urlStr);

        if(pName != null && pName.equals("android")) {
            this.openPluginNoHistory(pName, pView, pParameter);
        } else if(pName != null) {
            if(data.equals("")) {
                HashMap<String, String> list = new HashMap<>();
                list.put("authtoken", SecurityHelper.encrypt(this.getLoginHelper().getAuthtoken()));

                HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
                httpPost.setShowDialog(false);
                httpPost.setPost(list);
                httpPost.setOutput(this, "closeRefresh");
                httpPost.execute(urlStr);
            } else {
                offlineHelper.downloadOfflineDataAndOpen(urlStr);
            }
        }
    }*/

    @SuppressWarnings("unused") // used by invoke
    public void refreshCurrentPlugin(String pName) {
        Logs.d(this, "Refresh by Service: " + pName);
        if(pName.equals(this.currentName)) {
            swipeRefreshLayout.setRefreshing(true);
            gui.reload();
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void refreshCurrentView(String pName, String pView) {
        if(pView.equals("")) {
            pView = "home";
        }

        if(this.currentView.equals("")) {
            this.currentView = "home";
        }

        if(pName.equals(this.currentName) && pView.equals(this.currentView)) {
            swipeRefreshLayout.setRefreshing(true);
            gui.reload();
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void refreshCurrentViewParameter(String pName, String pView, String pParameter) {
        if(pName.equals(this.currentName) && pView.equals(this.currentView) && pParameter.equals(this.currentParameter)) {
            swipeRefreshLayout.setRefreshing(true);
            gui.reload();
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void closeRefresh(HashMap<Object,Object> result) {
        swipeRefreshLayout.setRefreshing(false);
        this.getContent(result);
    }

    public void closeRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @SuppressWarnings("unused") // used by invoke
    public void openMedia(String pType, String pPath) {

        if(FormatHelper.isLast(pPath, "/")) {
            pPath = FormatHelper.removeLast(pPath);
        }

        switch (pType) {
            case "music":
                this.openMusic(pPath);
                break;
            case "video":
                this.openVideo(pPath);
                break;
            case "image":
                this.openImage(pPath);
                break;
            default:
                this.downloadFile(pPath);
                break;
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void sendAsync(String name, String value) {
        Bundle bundle = new Bundle();
        bundle.putString("value", value);
        bundle.putString("authtoken", this.getLoginHelper().getAuthtoken());
        bundle.putString("action", name);
        bundle.putString("plugin", this.getCurrentName());

        this.sendToService(bundle, MyService.MSG_ACTION_SEND);
    }

    private void openImage(String pPath) {
        SharedPreferencesHelper pref = new SharedPreferencesHelper(this, loginHelper.getIdentifier());


        String url = loginHelper.getServer() + "api/file.php?file=" + FormatHelper.encodeURI(pPath) + "&jwt=" + FormatHelper.encodeURI(loginHelper.getAuthtoken());


        Intent intentExternally = new Intent(Intent.ACTION_VIEW, Uri.parse(url) );
        intentExternally.setDataAndType(Uri.parse(url), "image/*");

        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intentExternally,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        if(pref.readBoolean("IMAGES_EXTERNALLY") && isIntentSafe) {
            startActivity(intentExternally);
        } else {
            Intent intent = new Intent(MainActivity.this, MediaActivity.class);
                Bundle b = new Bundle();
                b.putString("image", url);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void openUrl(String pUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pUrl));
        startActivity(intent);
    }

    private void downloadFile(String pPath) {
        String url = loginHelper.getServer() + "api/file.php?file=" + FormatHelper.encodeURI(pPath) + "&jwt=" + FormatHelper.encodeURI(loginHelper.getAuthtoken());

        DownloadHelper dl = new DownloadHelper(this);
        dl.execute(url);
    }

    private void openMusic(String pPath) {
        String url = loginHelper.getServer() + "api/file.php?file=" + FormatHelper.encodeURI(pPath) + "&jwt=" + FormatHelper.encodeURI(loginHelper.getAuthtoken());

        Bundle bundle = new Bundle();
        bundle.putString("URL", url);

        Message message = Message.obtain(null, MyService.MSG_ACTION_PLAY);
        message.setData(bundle);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void openVideo(String pPath) {
        SharedPreferencesHelper pref = new SharedPreferencesHelper(this, loginHelper.getIdentifier());

        String url = loginHelper.getServer() + "api/file.php?file=" + FormatHelper.encodeURI(pPath) + "&jwt=" + FormatHelper.encodeURI(loginHelper.getAuthtoken());


        Intent intentExternally = new Intent(Intent.ACTION_VIEW, Uri.parse(url) );
        intentExternally.setDataAndType(Uri.parse(url), "video/mp4");

        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intentExternally,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        if(pref.readBoolean("VIDEO_EXTERNALLY") && isIntentSafe) {
            startActivity(intentExternally);
        } else {
            Intent intent = new Intent(MainActivity.this, MediaActivity.class);
            Bundle b = new Bundle();
            b.putString("video", url);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    private void addDrawerItems() {
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, de.michaelsoftware.android.Vision.R.layout.list_item_menu, menuArray);
        mDrawerList.setAdapter(mAdapter);
    }

    protected void setupActivity() {
        ThemeUtils.onActivityCreateSetTheme(this, loginHelper.getIdentifier());
        setContentView(de.michaelsoftware.android.Vision.R.layout.activity_main);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDrawerTop = (LinearLayout) findViewById(de.michaelsoftware.android.Vision.R.id.left_drawer_top);
        mDrawerList = (ListView) findViewById(de.michaelsoftware.android.Vision.R.id.left_drawer_list);
        mDrawerLayout = (DrawerLayout)findViewById(de.michaelsoftware.android.Vision.R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        mDrawerTop.setOnClickListener(new CustomOnClickListener(this, "openPlugin('android','home','')"));

        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            int color = ResourceHelper.getColor(this, R.color.drawerBackgroundDark);

            mDrawerTop.setBackgroundColor(color);
            mDrawerList.setBackgroundColor(color);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(de.michaelsoftware.android.Vision.R.id.swipe_refresh_layout);
        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLACK);
        }
    }

    protected void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, de.michaelsoftware.android.Vision.R.string.drawer_open, de.michaelsoftware.android.Vision.R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);
                if(getSupportActionBar() != null)
                    getSupportActionBar().setTitle(de.michaelsoftware.android.Vision.R.string.drawer_title);

                showActionBar();

                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                if(getSupportActionBar() != null)
                    getSupportActionBar().setTitle(mActivityTitle);

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                showActionBar();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void addShortcut() {
        String methode = "openPlugin('" + this.currentName + "','" + this.currentView + "','" + this.currentParameter + "')";

        String shortcutName;
        if(pluginList != null && pluginList.containsKey(this.currentName)) {
            shortcutName = pluginList.get(this.currentName);
        } else {
            shortcutName = this.currentName;
        }

        Bitmap icon;
        if(pluginListImages != null && pluginListImages.containsKey(this.currentName)) {

            String urlStr = pluginListImages.get(this.currentName);

            if(Tools.isBase64(urlStr)) {
                icon = FormatHelper.baseToBitmap(urlStr, 192, 192);

                if (icon == null) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                }

                this.createShortcut(shortcutName, methode, icon);
            } else {

                HttpImageAsync httpImageAsync = new HttpImageAsync(this, "createShortcut");

                ArrayList<String> array = new ArrayList<>();
                array.add(shortcutName);
                array.add(methode);

                httpImageAsync.setSpecialData(array);
                httpImageAsync.execute( net.michaelsoftware.android.jui.Tools.getAbsoluteUrl(urlStr, this.loginHelper.getServer()) );
            }
        } else {
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            this.createShortcut(shortcutName, methode, icon);
        }

        //Logs.d(this, add.getExtras().toString());
    }

    public void createShortcut(Bitmap icon, ArrayList<String> array) {
        if(array.size() == 2) {
            this.createShortcut(array.get(0), array.get(1), icon);
        }
    }

    private void createShortcut(String shortcutName, String methode, Bitmap icon) {
        icon = Bitmap.createScaledBitmap(icon, 128, 128, true);

        Intent shortcutIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        shortcutIntent.putExtra("account", loginHelper.getAccount());
        shortcutIntent.putExtra("action", methode);
        shortcutIntent.putExtra("user", loginHelper.getAccount().name );

        Logs.d(this, shortcutIntent.getExtras().toString());

        Intent add = new Intent();
        add.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        add.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        //add.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));
        add.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        add.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(add);

        this.moveTaskToBack(true);
    }

    public void getMenuContent(String pResult) {  // Get JSON for the menu
        JsonParserAsync jsonAsync = new JsonParserAsync();
        jsonAsync.setOutput(this, "getMenuContent");
        jsonAsync.execute(pResult);
    }

    @SuppressWarnings("unused") // used by invoke
    public void getMenuContent(HashMap<Object, Object> hashMap) {
        this.menuArray = new ArrayList<>();
        this.menuArrayId = new ArrayList<>();

        this.addMenuHeader();

        for (int i = 0; i <= hashMap.size(); i++) {
            if(!hashMap.containsKey(i)) {
                continue;
            }

            Object value = hashMap.get(i);
            if(value instanceof HashMap) {
                Object valueName = ((HashMap) value).get("name");
                Object valueId = ((HashMap) value).get("id");
                Object valueIcon = ((HashMap) value).get("icon");
                Object valueIconColor = ((HashMap) value).get("icon-color");

                if(valueName instanceof String  && valueId instanceof String) {
                    if(((HashMap) value).containsKey("visible") && ((HashMap) value).get("visible") instanceof String) {

                        if(!((HashMap) value).get("visible").equals("no"))
                            addMenuItem((String) valueName, (String) valueId);
                    } else {
                        addMenuItem((String) valueName, (String) valueId);
                    }
                }

                if(valueId instanceof String && ((HashMap) value).containsKey("shareable") &&  ((HashMap) value).get("shareable").equals("TRUE")) {
                    pluginShareable.add((String) valueId);
                }

                if(valueId instanceof String && valueName instanceof String) {
                    pluginList.put((String) valueId, (String) valueName);
                }

                if(valueId instanceof String && valueIconColor instanceof String) {
                    pluginListImages.put((String) valueId, (String) valueIconColor);
                } else if(valueId instanceof String && valueIcon instanceof String) {
                    pluginListImages.put((String) valueId, (String) valueIcon);
                }

                if(((HashMap) value).containsKey("offline") && ((HashMap) value).get("offline") instanceof HashMap &&  valueId instanceof String) {
                    HashMap valueOffline = (HashMap) ((HashMap) value).get("offline");
                    for(int j = 0; j < valueOffline.size(); j++) {
                        if(valueOffline.containsKey(j) && valueOffline.get(j) instanceof HashMap) {
                            HashMap hmOfflineData = (HashMap) valueOffline.get(j);

                            if(hmOfflineData.containsKey(0) && hmOfflineData.get(0) instanceof String
                                    && hmOfflineData.containsKey(1) && hmOfflineData.get(1) instanceof String) {
                                String view = (String) hmOfflineData.get(0);
                                String params = (String) hmOfflineData.get(1);

                                offlineHelper.downloadOfflineData(valueId, view, params);
                            }
                        }
                    }
                }

                if(((HashMap) value).containsKey("viewCache") && ((HashMap) value).get("viewCache") instanceof HashMap &&  valueId instanceof String) {
                    HashMap valueOffline = (HashMap) ((HashMap) value).get("viewCache");
                    for(int j = 0; j < valueOffline.size(); j++) {
                        if(valueOffline.containsKey(j) && valueOffline.get(j) instanceof HashMap) {
                            HashMap hmOfflineData = (HashMap) valueOffline.get(j);

                            if(hmOfflineData.containsKey(0) && hmOfflineData.get(0) instanceof String) {
                                String view = (String) hmOfflineData.get(0);

                                offlineHelper.downloadParaCache(valueId, view);
                            }
                        }
                    }
                }
            }
        }

        addDrawerItems();
    }

    @SuppressLint("NewApi")
    private void addMenuHeader() {
        mDrawerTop.removeAllViews();

        //LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mDrawerTop.getLayoutParams();
        //layoutParams.gravity = Gravity.START;

        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            mDrawerTop.setBackgroundColor(ResourceHelper.getColor(this, R.color.headerBackgroundDark));
        }


        ImageView iv = new ImageView(this);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            iv.setImageDrawable(getDrawable(de.michaelsoftware.android.Vision.R.drawable.vision_transparent_nowhite));
        } else {
            iv.setImageDrawable(this.getResources().getDrawable(de.michaelsoftware.android.Vision.R.drawable.vision_transparent_nowhite));
        }

        iv.setPadding(0, 0, 0, 16);
        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (240 * 0.5), 0.5f));
        mDrawerTop.addView(iv);


        TextView tv1 = new TextView(this);
        tv1.setText(loginHelper.getUsername());
        tv1.setGravity(Gravity.END);
        mDrawerTop.addView(tv1);

        TextView tv2 = new TextView(this);
        tv2.setText(loginHelper.getServer());
        tv2.setGravity(Gravity.END);
        mDrawerTop.addView(tv2);

        /*
        View ruler = new View(this.getApplicationContext()); ruler.setBackgroundColor(0xFF000000);
        mDrawerTop.addView(ruler, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 2));*/
    }

    public void getContent(String pResult) {
        JsonParserAsync jsonAsync = new JsonParserAsync();
        jsonAsync.setOutput(this, "getContent");
        jsonAsync.execute(pResult);
    }

    public void getContent(HashMap<Object, Object> hashMap) {
        if(hashMap != null && hashMap.containsKey("action") && hashMap.get("action").equals("logout")) {
            LoginHelper.deleteAccount(this, loginHelper.getAccount());
            loginHelper.openSelectUserAccount();
        }
        gui.parse(hashMap);
    }

    public void addMenuItem(String pName, String pId) {
        menuArray.add(pName);
        menuArrayId.add(pId);
    }

    public String getCurrentViewHost() {
        if(urlStr != null && !urlStr.isEmpty()) {
            return urlStr;
        }

        return null;
    }

    public String getCurrentParameter() {
        if(this.currentParameter != null) {
            return this.currentParameter;
        }

        return "";
    }

    public String getCurrentName() {
        if(this.currentName != null) {
            return this.currentName;
        }

        return "android";
    }

    public String getCurrentView() {
        if(this.currentView != null) {
            return this.currentView;
        }

        return "home";
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
                progressDialog = new ProgressDialog(this, R.style.DialogDark);
            } else {
                progressDialog = new ProgressDialog(this, R.style.DialogLight);
            }

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(this.getResources().getString(R.string.loading));
            progressDialog.setMessage(this.getResources().getString(R.string.loading_info));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(10);
            progressDialog.setProgress(0);
        }

        if(progressDialog.isIndeterminate())
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    public void enableRefresh() {
        if(this.swipeRefreshLayout != null)
            this.swipeRefreshLayout.setEnabled(true);
    }

    public void disableRefresh() {
        this.swipeRefreshLayout.setEnabled(false);
    }

    public void historyRemoveLast() {
        historyHelper.removeLastEntry();
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    public void setShareable(String name, String page, String command) {
        MenuItem item = this.mMenu.findItem(R.id.action_share);
        item.setVisible(true);

        this.isShareable = true;

        this.shareName    = name;
        this.sharePage    = page;
        this.shareCommand = command;
    }

    public void setShareable(boolean b) {
        this.shareName    = null;
        this.sharePage    = null;
        this.shareCommand = null;

        this.isShareable = false;

        MenuItem item = this.mMenu.findItem(R.id.action_share);
        item.setVisible(false);
    }

    @SuppressWarnings("unused")
    public void openGallery(String galleryId, String pIndex) {
        HashMap<Object, Object> header = gui.getHeader();

        if(header.containsKey(galleryId) && Tools.isHashmap(header.get(galleryId))) {
            int index = Tools.getInt(pIndex, 0);
            HashMap<Object, Object> gallery = (HashMap<Object, Object>) header.get(galleryId);
            ArrayList<String> array = new ArrayList<>();

            for(int i  = 0, x = gallery.size(); i < x; i++) {
                if(gallery.containsKey(i) && Tools.isString(gallery.get(i)) ) {
                    String url = loginHelper.getServer() + "api/file.php?file=" + FormatHelper.encodeURI((String) gallery.get(i)) + "&jwt=" + FormatHelper.encodeURI(loginHelper.getAuthtoken());
                    array.add(url);
                }
            }

            if(array.size() > 0) {
                if(array.size()-1 < index) {
                    index = array.size()-1;
                }

                Intent intent = new Intent(MainActivity.this, MediaActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("images", array);
                b.putInt("index", index);
                intent.putExtras(b);
                startActivity(intent);
            }
        }
    }
}
