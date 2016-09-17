package net.michaelsoftware.android.jui.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.util.Log;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Michael on 27.11.2015.
 * Sends a POST request to a server and parses the result asynchronously
 */


public class HttpPostJsonHelper extends AsyncTask<String, Integer, Object> {
    private JuiParser juiParser;
    private Object obj;
    private ProgressDialog progressDialog;
    private java.lang.reflect.Method method;
    private HashMap<String, String> post = null;
    private String key;
    private String iv;
    private Object o = null;
    public static final int timeoutConnection = 12000;
    public static final int timeoutSocket = 20000;
    private boolean showDialog = true;
    private Boolean outputString = false;
    private ArrayList<String> dataNames = new ArrayList<>();
    private android.os.Handler timeout;


    public final static String boundary =  "*****";
    public final static String crlf = "\r\n";
    public final static String twoHyphens = "--";

    public HttpPostJsonHelper() {
    }

    public HttpPostJsonHelper(JuiParser juiParser) {
        this.juiParser = juiParser;
    }

    @Override
    protected void onPreExecute() {
        if(juiParser != null && juiParser.getActivity() != null && showDialog) {
            Activity activity = juiParser.getActivity();

            if(activity != null) {
                timeout = new android.os.Handler();
                timeout.postDelayed(new CustomRunnable(activity), 300);
            }
        }
    }

    public void setShowDialog(boolean pBoolen) {
        this.showDialog = pBoolen;
    }

    @SuppressWarnings("deprecation")
    protected Object doInBackground(String... urls) {
        int count = urls.length;
        HashMap<Object,Object> response = null;

        if (count > 0) {
            try {
                URL url = new URL(urls[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(timeoutConnection);
                conn.setConnectTimeout(timeoutSocket);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setUseCaches(false);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + boundary);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                if(post == null) {
                    this.post = new HashMap<>();
                }

                this.write(request, this.post);

                request.writeBytes(twoHyphens + boundary +
                        twoHyphens + crlf);

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

                    Log.d(this.toString(), "RESPONSE"+  responseString);

                    if(this.outputString) {
                        return responseString;
                    }

                    JsonParser jsonParser = new JsonParser(responseString);
                    response = jsonParser.getHashMap();
                }
                else {
                    response = null;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    private void write(DataOutputStream request, HashMap<String, String> post) throws IOException, URISyntaxException {
        for(Map.Entry<String, String> entry : post.entrySet()) {

            if(dataNames.contains(entry.getKey())) {
                Uri uri = Uri.parse( entry.getValue() );
                File file = new File(uri.getPath());

                if(uri.getScheme() != null && uri.getScheme().equals("content")) {
                    InputStream inputStream = juiParser.getActivity().getContentResolver().openInputStream(uri);
                    Cursor cursor = juiParser.getActivity().getContentResolver().query(uri, null, null, null, null);
                    String fileName = "";
                    if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }

                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" +
                            entry.getKey()+"[]" + "\";filename=\"" +
                            fileName + "\"" + crlf);
                    request.writeBytes(crlf);

                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1 * 1024 * 1024;

                    if(inputStream != null) {
                        bytesAvailable = inputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // read file and write it into form...
                        bytesRead = inputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {
                            request.write(buffer, 0, bufferSize);
                            bytesAvailable = inputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = inputStream.read(buffer, 0, bufferSize);
                        }

                        request.write(buffer);
                    }
                } else if(file.exists() && file.isFile()) {
                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "[]\";filename=\"" +
                            file.getName() + "\"" + crlf);
                    request.writeBytes(crlf);

                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1 * 1024 * 1024;

                    FileInputStream fileInputStream = new FileInputStream(file);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0)
                    {
                        request.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    request.write(buffer);
                }

            } else {
                request.writeBytes(twoHyphens + boundary + crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" +
                        entry.getKey() + "\"" + crlf);
                request.writeBytes(crlf);
                request.write(this.encodeFormData(entry.getValue()));
            }

            request.writeBytes(crlf);
        }
    }

    private byte[] encodeFormData(String value) {
        try {
            byte[] b = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                b = value.getBytes(StandardCharsets.UTF_8);
            } else {
                b = value.getBytes("UTF-8");
            }

            return b;

        } catch (UnsupportedEncodingException e) {
            return value.getBytes();
        }
    }

    public void setOutput(Object c, String methodName) {
        try {
            obj = c;

            Class resultClass;
            if(outputString) {
                resultClass = String.class;
            } else {
                resultClass = HashMap.class;
            }

            if(key != null && !key.isEmpty() && iv != null && !iv.isEmpty()) {
                method = c.getClass().getMethod(methodName, resultClass, String.class, String.class);
            } else {
                if(this.o != null) {
                    method = c.getClass().getMethod(methodName, resultClass, Object.class);
                } else {
                    method = c.getClass().getMethod(methodName, resultClass);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void setOutputString(Boolean bool) {
        this.outputString = bool;
    }

    protected void onProgressUpdate(Integer... progress) {
        //if(progressDialog != null && progressDialog.isShowing())
        //progressDialog.setProgress(progress[0]);
    }

    protected void onPostExecute(Object result) {
        if(juiParser != null && juiParser.getActivity() != null && showDialog) {
            dismissProgressDialog();
        }

        if (method != null && obj != null) {
            try {
                if (key != null && !key.isEmpty() && iv != null && !iv.isEmpty()) {
                    method.invoke(obj, result, key, iv);
                } else {
                    if (this.o != null) {
                        method.invoke(obj, result, this.o);
                    } else {
                        method.invoke(obj, result);
                    }
                }
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPost(HashMap<String, String> postDataParams) {
        this.post = postDataParams;
    }

    public void setKeyIv(String pKey, String pIv) {
        this.key = pKey;
        this.iv = pIv;
    }

    public void setSpecialData(Object pO) {
        this.o = pO;
    }

    public void addDataName(String name) {
        this.dataNames.add(name);
    }

    private class CustomRunnable implements Runnable {
        private Activity activity;

        public CustomRunnable(Activity activity) {
                this.activity = activity;
            }

        @Override
        public void run() {
            showProgressDialog();
            }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(juiParser.getActivity(), R.style.Dialog);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(juiParser.getActivity().getResources().getString(R.string.loading));
            progressDialog.setMessage(juiParser.getActivity().getResources().getString(R.string.loading_info));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(10);
            progressDialog.setProgress(0);
        }

        if(progressDialog.isIndeterminate())
            progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
