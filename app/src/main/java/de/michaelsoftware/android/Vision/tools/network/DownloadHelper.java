package de.michaelsoftware.android.Vision.tools.network;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Helps to download a file to the filesystem
 * Created by Michael on 04.02.2016.
 */
public class DownloadHelper extends AsyncTask<String, Integer, String> {
    ProgressDialog pDialog;
    MainActivity zActivity;
    private HashMap<String, String> headers = new HashMap<>();

    public DownloadHelper(MainActivity pActivity) {
        zActivity = pActivity;
    }

    private void showDialog() {
        pDialog = new ProgressDialog(zActivity);
        pDialog.setMessage("Downloading file. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    private void dismissDialog() {
        pDialog.dismiss();
    }

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.showDialog();
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);



            String fileNameTest = FormatHelper.getFileName(f_url[0]);


            File file;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Vision/" + zActivity.getLoginHelper().getIdentifier() + "/" + fileNameTest);
            } else {
                file = new File(this.zActivity.getFilesDir() + "/Vision/" + zActivity.getLoginHelper().getIdentifier() + "/" + fileNameTest);
            }

            Log.d("File", file.toString());

            File parentDir = file.getParentFile();
            if(!parentDir.mkdirs() && !parentDir.isDirectory()) {
                return null;
            }

            if(file.exists()) {
                return null;
            } else if(!file.exists() && !file.createNewFile()) {
                return null;
            }




            URLConnection urlConnection = url.openConnection();

            if(this.headers.size() > 0) {
                for(Map.Entry<String, String> entry : this.headers.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    urlConnection.setRequestProperty(key, value);
                }
            }

            // download the file
            int totalSize = urlConnection.getContentLength();

            BufferedInputStream inStream = new BufferedInputStream(urlConnection.getInputStream());



            FileOutputStream fileOutput = new FileOutputStream(file);
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            //Log.d("Test", inputStream.read(buffer)+"");
            while ((bufferLength = inStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;

                publishProgress( Math.round( (downloadedSize/totalSize)*100 ) );
                Log.d("Progress:", "downloadedSize:" + downloadedSize + "totalSize:" + totalSize);
            }

            // flushing output
            fileOutput.flush();

            // closing streams
            fileOutput.close();

            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileNameTest;
        } catch (Exception e) {

            Logs.e(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /* Updating progress bar */
    protected void onProgressUpdate(Integer... progress) {
        pDialog.setProgress(progress[0]);
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    /* After completing background task: Dismiss the progress dialog, Open the downloaded file */
    @Override
    protected void onPostExecute(String file_url) {
        dismissDialog();

        if(file_url != null) {
            File file = new File(file_url);
            if (file.exists()) {
                String mime = FormatHelper.getMimeTypeByExtension(file_url);

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), mime);
                this.zActivity.startActivityForResult(intent, 10);
            }
        } else {
            Toast.makeText(zActivity, "Die Datei konnte leider nicht heruntergeladen werden.", Toast.LENGTH_LONG).show();
        }
    }

}