package de.michaelsoftware.android.Vision.tools.network;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Helps to download a file to the filesystem
 * Created by Michael on 04.02.2016.
 */
public class DownloadHelper extends AsyncTask<String, String, String> {
    ProgressDialog pDialog;
    MainActivity zActivity;

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
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a tipical 0-100% progress bar
            int lengthOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream
            String fileNameTest = FormatHelper.getFileName(f_url[0]);

            File file;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Vision/" + zActivity.getLoginHelper().getIdentifier() + "/" + fileNameTest);
            } else {
                file = new File(this.zActivity.getFilesDir() + "/Vision/" + zActivity.getLoginHelper().getIdentifier() + "/" + fileNameTest);
            }

            File parentDir = file.getParentFile();
            if(!parentDir.mkdirs() && !parentDir.isDirectory()) {
                return null;
            }

            if(file.exists()) {
                return null;
            } else if(!file.exists() && !file.createNewFile()) {
                return null;
            }

            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;

                publishProgress(""+(int)((total*100)/lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileNameTest;
        } catch (Exception e) {

            Logs.e(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /* Updating progress bar */
    protected void onProgressUpdate(String... progress) {
        pDialog.setProgress(Integer.parseInt(progress[0]));
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