package com.example.android.sunshine.app.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

// AsyckTask to download image (url given )

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    ImageView bmImage;

    //constructor
    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    /**
     * La fonction va telecharger l'image en fond
      * @param urls
     * @return
     */
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    //after downloading
    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }

}