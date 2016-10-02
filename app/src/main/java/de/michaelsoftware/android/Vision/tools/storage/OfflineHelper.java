package de.michaelsoftware.android.Vision.tools.storage;

import net.michaelsoftware.android.jui.network.HttpPostJsonHelper;

import java.sql.SQLException;
import java.util.HashMap;

import de.michaelsoftware.android.Vision.activity.AbstractMainActivity.BaseActivity;
import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Created by Michael on 20.12.2015.
 * Used for managing offline Data
 */
public class OfflineHelper {
    private BaseActivity mainActivity;
    private DatabaseHelper dbHelper;

    public OfflineHelper(BaseActivity pMainActivity) {
        mainActivity = pMainActivity;
        dbHelper = new DatabaseHelper(mainActivity);

        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void downloadOfflineData(Object valueId, String pView, String pParameter) {
        String url = mainActivity.getLoginHelper().getServer()+"ajax.php?plugin=" + valueId + "&page=" + pView + "&cmd=" + pParameter + "&get=view";

        this.downloadOfflineData(url);
    }

    public void downloadOfflineData(String url) {
        if(dbHelper != null && dbHelper.getData(DatabaseHelper.OFFLINE_DATA, url).equals("")) {

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "bearer " + mainActivity.getLoginHelper().getAuthtoken());

            HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
            httpPost.setSpecialData(url);
            httpPost.setOutputString(true);
            httpPost.setHeaders(headers);
            httpPost.setOutput(this, "getOfflineData");
            httpPost.execute(url);
        }
    }

    @SuppressWarnings("unused") // used by invoke from HttpJsonPost
    public void getOfflineData(String pResult, Object o) {
        Logs.d(this, pResult);
        if(o instanceof String) {
            DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
            try {
                dbHelper.open();
                dbHelper.createEntry(DatabaseHelper.OFFLINE_DATA, (String) o, pResult);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused") // used by invoke from HttpJsonPost
    public void getOfflineDataOpen(String pResult, Object o) {
        Logs.d(this, pResult);
        if(o instanceof String) {
            DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
            try {
                dbHelper.open();
                dbHelper.createEntry(DatabaseHelper.OFFLINE_DATA, (String) o, pResult);

                String view = FormatHelper.getView((String) o);
                String page = FormatHelper.getPage((String) o);
                String command = FormatHelper.getCommand((String) o);

                mainActivity.openPluginNoHistory(view, page, command);
                mainActivity.closeRefresh();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getData(String url) {
        if (!dbHelper.getDatabase().isOpen()) {
            try {
                dbHelper.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String offlineData = dbHelper.getData(DatabaseHelper.OFFLINE_DATA, url);

        if(offlineData == null || offlineData.equals("")) {
            url = url.replaceAll("&cmd=[^\"]*&", "&");
            url = url.replaceAll("cmd=[^\"]*&", "");
            url = url.replaceAll("&cmd=[^\"]*","");
            String paraCacheData = dbHelper.getData(DatabaseHelper.PARA_CACHE_DATA, url);

            if(paraCacheData != null && !paraCacheData.equals("")) {
                return paraCacheData;
            } else {
                return "";
            }

        } else {
            return offlineData;
        }
    }

    public void clear() {
        dbHelper.close();
        dbHelper.clear();
    }

    public void downloadParaCache(Object valueId, String pView) {
        String url = mainActivity.getLoginHelper().getServer()+"ajax.php?plugin=" + valueId + "&page=" + pView + "&get=view";

        this.downloadParaCache(url);
    }

    public void downloadParaCache(String url) {
        if(dbHelper.getData(DatabaseHelper.PARA_CACHE_DATA, url).equals("")) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "bearer " + mainActivity.getLoginHelper().getAuthtoken());

            HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
            httpPost.setSpecialData(url);
            httpPost.setOutputString(true);
            httpPost.setHeaders(headers);
            httpPost.setOutput(this, "getParaCacheData");
            httpPost.execute(url);
        }
    }

    @SuppressWarnings("unused") // used by invoke from HttpJsonPost
    public void getParaCacheData(String pResult, Object o) {
        if(o instanceof String) {
            DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
            try {
                dbHelper.open();
                dbHelper.createEntry(DatabaseHelper.PARA_CACHE_DATA, (String) o, pResult);
                dbHelper.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadOfflineDataAndOpen(String url) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + mainActivity.getLoginHelper().getAuthtoken());

        HttpPostJsonHelper httpPost = new HttpPostJsonHelper();
        httpPost.setSpecialData(url);
        httpPost.setOutputString(true);
        httpPost.setHeaders(headers);
        httpPost.setOutput(this, "getOfflineDataOpen");
        httpPost.execute(url);
    }
}
