package de.michaelsoftware.android.Vision.account;

import net.michaelsoftware.android.jui.network.HttpPostJsonHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.LoginHelper;
import de.michaelsoftware.android.Vision.tools.SecurityHelper;
import de.michaelsoftware.android.Vision.tools.network.JsonParser;

/**
 * Created by Michael on 12.06.2016.
 */
public class ParseComServerAuthenticate {
    public String userSignIn(String user, String pass, String authType) throws Exception {
        String username = LoginHelper.getUsernameFromAccountName(user);
        String server = LoginHelper.getServerNameFromAccountName(user);

        String password = SecurityHelper.decrypt(pass);

        URL url = new URL("http://" + server + "/ajax.php?action=login");

        String key = SecurityHelper.generateKey();
        String iv  = SecurityHelper.generateKey(16);

        HashMap<String,String> post = new HashMap<>();

        post.put("username", SecurityHelper.encrypt(username, key, iv));
        post.put("password", SecurityHelper.encrypt(password, key, iv));

        post.put("key", SecurityHelper.encrypt(key));
        post.put("iv", SecurityHelper.encrypt(iv, key));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(HttpPostJsonHelper.timeoutConnection);
        conn.setConnectTimeout(HttpPostJsonHelper.timeoutSocket);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.setUseCaches(false);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + HttpPostJsonHelper.boundary);


        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        DataOutputStream request = new DataOutputStream(conn.getOutputStream());

        this.write(request, post);

        request.writeBytes(HttpPostJsonHelper.twoHyphens + HttpPostJsonHelper.boundary +
                HttpPostJsonHelper.twoHyphens + HttpPostJsonHelper.crlf);

        writer.flush();
        writer.close();
        os.close();
        int responseCode = conn.getResponseCode();

        String responseString = "";
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line=br.readLine()) != null) {
                responseString+=line;
            }

            JsonParser jsonParser = new JsonParser(responseString);
            HashMap response = jsonParser.getHashMap();

            if(response != null && response.containsKey("authtoken") && response.get("authtoken") instanceof String) {
                return SecurityHelper.decrypt((String) response.get("authtoken"), key, iv);
            }

        }

        return null;
    }

    private void write(DataOutputStream request, HashMap<String, String> post) throws IOException, URISyntaxException {
        for(Map.Entry<String, String> entry : post.entrySet()) {
            request.writeBytes(HttpPostJsonHelper.twoHyphens + HttpPostJsonHelper.boundary + HttpPostJsonHelper.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    entry.getKey() + "\"" + HttpPostJsonHelper.crlf);
            request.writeBytes(HttpPostJsonHelper.crlf);
            request.write(FormatHelper.encodeFormData(entry.getValue()));

            request.writeBytes(HttpPostJsonHelper.crlf);
        }
    }


    private class ParseComError implements Serializable {
        int code;
        String error;
    }
    private class User implements Serializable {

        private String firstName;
        private String lastName;
        private String username;
        private String phone;
        private String objectId;
        public String sessionToken;
        private String gravatarId;
        private String avatarUrl;


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getGravatarId() {
            return gravatarId;
        }

        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
