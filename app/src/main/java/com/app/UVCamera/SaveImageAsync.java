package com.app.UVCamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SaveImageAsync extends AsyncTask<Void, String, Void> {

    private Context mContext;
    private Bitmap bitmap;

    private ProgressDialog mProgressDialog;

    public SaveImageAsync(Context context, Bitmap bitmap) {
        mContext = context;
        this.bitmap = bitmap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Saving Image to SD Card");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... str) {
        try {

            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream);
            byte[] mbitmapdata = byteOutputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mbitmapdata);

            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(Environment.getExternalStorageDirectory(), "UVCamera");
            if (!file.exists())
                file.mkdir();
            baseDir = baseDir + File.separator + "UVCamera";
            String fileName = System.currentTimeMillis() + ".jpg";

            OutputStream outputStream = new FileOutputStream(baseDir + File.separator + fileName);
            byteOutputStream.writeTo(outputStream);

            byte[] buffer = new byte[1024]; //Use 1024 for better performance
            int lenghtOfFile = mbitmapdata.length;
            int totalWritten = 0;
            int bufferedBytes = 0;

            while ((bufferedBytes = inputStream.read(buffer)) > 0) {
                totalWritten += bufferedBytes;
                publishProgress(Integer.toString((int) ((totalWritten * 100) / lenghtOfFile)));
                outputStream.write(buffer, 0, bufferedBytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    protected void onProgressUpdate(String... progress) {
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(Void filename) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }
}