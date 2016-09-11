package de.michaelsoftware.android.Vision.activity.AbstractMainActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.HashMap;

import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.network.HttpPostJsonHelper;
import de.michaelsoftware.android.Vision.tools.network.JsonParserAsync;
import de.michaelsoftware.android.Vision.tools.storage.SharedPreferencesHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;

/**
 * Created by Michael on 12.05.2016.
 */
public abstract class SiteActionsActivity extends LoginActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void openHome() {
        String urlStr = this.loginHelper.getServer() + "ajax.php?show=plugins";
        String dataString = offlineHelper.getData(urlStr);

        if (dataString.equals("")) {
            offlineHelper.downloadOfflineData(urlStr);

            HttpPostJsonHelper httpPost = new HttpPostJsonHelper(loginHelper);
            httpPost.setOutput(this, "openHomePage");
            httpPost.execute(urlStr);
        } else {
            JsonParserAsync jsonParser = new JsonParserAsync();
            jsonParser.setOutput(this, "openHomePage");
            jsonParser.execute(dataString);
        }

        this.mDrawerLayout.closeDrawers();
    }

    @SuppressWarnings("unused") // used by invoke
    public void openHomePage(HashMap<Object, Object> hashMap) {
        String pString = "{\"data\":";
        pString += "[{\"type\":\"heading\",\"value\":\"Guten Tag " + loginHelper.getUsername() + "\"}";
        pString += ",{\"type\":\"text\", \"value\":\"sie sind angemeldet an: " + loginHelper.getServer() + "\"}";
        pString += ",{\"type\":\"hline\"}";
        pString += ",{\"type\":\"buttonlist\", \"value\":[";

        SharedPreferencesHelper pref = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()));
        String mainPlugins = pref.read("MAINPLUGINS");
        String[] plugins = mainPlugins.split("\\|");

        for (int i = 0; i < hashMap.size(); i++) {
            if(!hashMap.containsKey(i)) continue;

            Object value = hashMap.get(i);
            if(value instanceof HashMap) {
                Object valueName = ((HashMap) value).get("name");
                Object valueId = ((HashMap) value).get("id");
                Object valueIcon = ((HashMap) value).get("icon");
                Boolean addIcon = false;

                if(valueName instanceof String  && valueId instanceof String) {
                    if(!mainPlugins.equals("") && !Arrays.asList(plugins).contains(valueId)) {
                        continue;
                    }

                    if(((HashMap) value).containsKey("visible") && ((HashMap) value).get("visible") instanceof String) {

                        if(!((HashMap) value).get("visible").equals("no"))
                            addIcon = true;
                    } else {
                        addIcon = true;
                    }
                }

                if(addIcon) {
                    pString += "{\"value\":[\"" + valueIcon + "\",\"" + valueName + "\"], \"click\":\"openPlugin('" + valueId + "','','')\"},";
                }
            }
        }

        pString = FormatHelper.removeLast(pString);
        pString += "]}]";
        pString += ",\"head\":{\"refreshable\":\"FALSE\"}}";

        if(!pString.equals("")) {
            JsonParserAsync json = new JsonParserAsync();
            json.setOutput(this, "getContent");
            json.execute(pString);
        }
    }

    public void openSettings() {
        this.disableRefresh();

        String pString = "{\"data\":";

        pString += "[{\"type\":\"heading\",\"value\":\"Einstellungen\"}";
        pString += ",{\"type\":\"text\", \"value\":\"Aktueller Server : " + loginHelper.getServer() + "\"}";
        pString += ",{\"type\":\"text\", \"value\":\"Aktueller Benutzer : " + loginHelper.getUsername() + "\"}";

        SharedPreferencesHelper pref = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()));
        String mac = pref.read("MAC");
        String notification = pref.read("NOTIFICATION", "1");
        String developer = pref.read("DEVELOPER", "0");
        boolean externallyImages = pref.readBoolean("IMAGES_EXTERNALLY");
        boolean externallyVideos = pref.readBoolean("VIDEO_EXTERNALLY");

        pString += ",{\"type\":\"text\", \"value\":\"MAC Adresse des Servers : " + mac + "\"}";
        String wolserver = pref.read("WOLSERVER");
        pString += ",{\"type\":\"text\", \"value\":\"Adresse des WOL-Servers : " + wolserver + "\"}";

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;

            pString += ",{\"type\":\"text\", \"value\":\"Version : " + versionName + " (" + versionNumber + ")" + "\"}";

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String host = pref.read("HOST", null);
        String wsport = pref.read("WSPORT", null);

        if (host != null && !host.equals("") && wsport != null && !wsport.equals("")) {
            pString += ",{\"type\":\"text\", \"value\":\"WebSocket-Server : " + host + ":" + wsport + "\"}";
        }

        if (this.mService != null && this.mService.getBinder() != null) {
            pString += ",{\"type\":\"text\", \"value\":\"WebSocket-Service : aktiviert\"}";
        }

        pString += ",{\"type\":\"button\", \"value\":\"Benutzer wechseln\", \"click\":\"changeUser\"}";
        pString += ",{\"type\":\"button\", \"value\":\"Offline Daten neuladen\", \"click\":\"clearCache\"}";

        if(ThemeUtils.getTheme(this, loginHelper.getIdentifier()) == 1) {
            pString += ",{\"type\":\"button\", \"value\":\"Helles Design aktivieren\", \"click\":\"activateLightMode\"}";
        } else {
            pString += ",{\"type\":\"button\", \"value\":\"Dunkles Design aktivieren\", \"click\":\"activateDarkMode\"}";
        }

        pString += ",{\"type\":\"hline\"}";

        pString += ",{\"type\":\"headingSmall\", \"value\":\"Medienwiedergabe\"}";

        if(externallyImages) {
            pString += ",{\"type\":\"text\", \"value\":\"Aktuell werden Bilder in einer anderen App dargestellt.\"}";
            pString += ",{\"type\":\"button\",\"value\":\"Bilder in Vision darstellen\",\"click\":\"deactivateExternalImages\"}";
        } else {
            pString += ",{\"type\":\"text\", \"value\":\"Aktuell werden Bilder mit dem integrierten Bildbetrachter dargestellt.\"}";
            pString += ",{\"type\":\"button\",\"value\":\"Bilder in externer App darstellen\",\"click\":\"activateExternalImages\"}";
        }

        if(externallyVideos) {
            pString += ",{\"type\":\"text\", \"value\":\"Aktuell werden Videos in einer anderen App wiedergegeben.\"}";
            pString += ",{\"type\":\"button\",\"value\":\"Videos in Vision anschauen\",\"click\":\"deactivateExternalVideos\"}";
        } else {
            pString += ",{\"type\":\"text\", \"value\":\"Aktuell werden Videos mit dem integrierten Videoplayer abgespielt.\"}";
            pString += ",{\"type\":\"button\",\"value\":\"Videos in externer App anschauen\",\"click\":\"activateExternalVideos\"}";
        }

        pString += ",{\"type\":\"hline\"}";

        if (!notification.equals("0")) {
            pString += ",{\"type\":\"text\", \"value\":\"Benachrichtigungen : aktiviert\"}";
            pString += ",{\"type\":\"button\", \"value\":\"Benachrichtigungen deaktivieren\", \"click\":\"deactivateNotifications\"}";
        } else {
            pString += ",{\"type\":\"text\", \"value\":\"Benachrichtigungen : deaktiviert\", \"color\":\"#FF0000\"}";
            pString += ",{\"type\":\"button\", \"value\":\"Benachrichtigungen aktivieren\", \"click\":\"activateNotifications\"}";
        }

        pString += ",{\"type\":\"text\", \"value\":\"Hinweis: Eine Änderung an den Benachrichtigungseinstellungen kann einige Zeit in Anspruch nehmen, bis sie angewendet wird. Spätestens nach einem Neustart des Gerätes wird die Änderung allerdings angewendet sein.\"}";

        pString += ",{\"type\":\"hline\"}";

        if (developer.equals("1")) {
            pString += ",{\"type\":\"text\", \"value\":\"Entwicklermodus : aktiviert\"}";
            pString += ",{\"type\":\"text\", \"value\":\"Authtoken : " + loginHelper.getAuthtoken() + "\"}";
            pString += ",{\"type\":\"text\", \"value\":\"Die Weitergabe des Authtokens kann zu Sicherheitsproblemen führen.\", \"color\":\"#FF0000\"}";

            pString += ",{\"type\":\"button\", \"value\":\"Entwicklermodus deaktivieren\", \"click\":\"deactivateDeveloper\"}";
        } else {
            pString += ",{\"type\":\"text\", \"value\":\"Entwicklermodus : deaktiviert\", \"color\":\"#FF0000\"}";
            pString += ",{\"type\":\"button\", \"value\":\"Entwicklermodus aktivieren\", \"click\":\"activateDeveloper\"}";
        }

        pString += "]";
        pString += ",\"head\":{\"refreshable\":\"FALSE\"}}";

        if (!pString.equals("")) {
            JsonParserAsync json = new JsonParserAsync();
            json.setOutput(this, "getContent");
            json.execute(pString);
        }
    }

    public void openShare(String pParameter) {
        this.disableRefresh();

        HttpPostJsonHelper httpPost = new HttpPostJsonHelper(this.getLoginHelper());
        httpPost.setOutput(this, "getContent");

        HashMap<String, String> nameValuePair = new HashMap<>();

        if(receivedIntent.hasExtra(Intent.EXTRA_STREAM)) {
            Uri receivedUri = this.receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            httpPost.addDataName("data");
            nameValuePair.put("data", receivedUri.toString());
        } else if(receivedIntent.hasExtra(Intent.EXTRA_TEXT)) {
            String text = this.receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            nameValuePair.put("data", text);
        }

        httpPost.setPost(nameValuePair);

        httpPost.execute(loginHelper.getServer() + "ajax.php?plugin=" + pParameter + "&page=receiver&get=view");
    }

    protected void getMimeContent(String dataString) {
        JsonParserAsync jsonAsync = new JsonParserAsync();
        jsonAsync.setOutput(this, "getMimeContent");
        jsonAsync.execute(dataString);
    }

    @SuppressWarnings("unused")
    public void getMimeContent(HashMap hm) {
        String type = this.receivedIntent.getType();
        String upperType = FormatHelper.getUnspecifiedMimeType(type);
        String value = "";
        String valueClick = "";

        if(hm != null)
        for(int i=0; i<hm.size(); i++) {
            if(hm.containsKey(i) && hm.get(i) instanceof HashMap) {
                HashMap plugin = (HashMap) hm.get(i);

                if (plugin.containsKey("mime") && plugin.get("mime") instanceof HashMap) {
                    if (((HashMap) plugin.get("mime")).containsValue(type) || ((HashMap) plugin.get("mime")).containsValue(upperType)) {
                        if (plugin.containsKey("name") && plugin.containsKey("id") && plugin.get("name") instanceof String && plugin.get("id") instanceof String) {
                            value += "\"" + plugin.get("name")  + "\",";
                            valueClick += "\"openPlugin('android','share','" + plugin.get("id")  + "')\",";
                        }
                    }
                }
            }
        }


        value = "[" + FormatHelper.removeLast(value) + "]";
        valueClick = "[" + FormatHelper.removeLast(valueClick) + "]";

        if(value.equals("[]")) {
            value = "[\"Für die Verarbeitung des Datentypes " + type + " ist kein Plugin vorhanden.\"]";
            valueClick = "[]";
        }


        String pString = "{\"data\":[";
        pString += "{\"type\":\"heading\",\"value\":\"Plugin auswählen\"},";
        pString += "{\"type\":\"list\",\"value\":" + value + ",\"click\":" + valueClick + "}";
        pString += "]";
        pString += ",\"head\":{\"refreshable\":\"FALSE\"}}";

        if(!pString.equals("")) {
            JsonParserAsync json = new JsonParserAsync();
            json.setOutput(this, "getContent");
            json.execute(pString);
        }
    }

    @SuppressWarnings("unused") // used by invoke
    public void activateNotifications(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.store("NOTIFICATION", "1");

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void deactivateNotifications(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.store("NOTIFICATION", "0");

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void activateDeveloper(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.store("DEVELOPER", "1");

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void deactivateDeveloper(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.store("DEVELOPER", "0");

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void activateExternalImages(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.storeBoolean("IMAGES_EXTERNALLY", true);

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void deactivateExternalImages(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.storeBoolean("IMAGES_EXTERNALLY", false);

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void activateExternalVideos(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.storeBoolean("VIDEO_EXTERNALLY", true);

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void deactivateExternalVideos(View v) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, loginHelper.getUsername() + '@' + FormatHelper.getServerName(loginHelper.getServer()) );
        sharedPreferencesHelper.storeBoolean("VIDEO_EXTERNALLY", false);

        this.openPlugin("android","settings");
    }

    @SuppressWarnings("unused") // used by invoke
    public void activateLightMode(View v) {
        ThemeUtils.changeToTheme(this, ThemeUtils.LIGHT, loginHelper);
    }

    @SuppressWarnings("unused") // used by invoke
    public void activateDarkMode(View v) {
        ThemeUtils.changeToTheme(this, ThemeUtils.DARK, loginHelper);
    }

    @SuppressWarnings("unused") // used by invoke
    public void changeUser(View v) {
        loginHelper.openSelectUserAccount();
    }
}
