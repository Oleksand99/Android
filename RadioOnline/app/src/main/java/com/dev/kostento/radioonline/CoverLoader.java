package com.dev.kostento.radioonline;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;


public class CoverLoader extends Thread {
    private WeakReference<Context> context;
    private CoverLoaderCallBack callBack;
    private TrackInfo recordModel;
    private Handler handler;
    private String large;


    public CoverLoader(Context context, TrackInfo recordModel, CoverLoaderCallBack callBack, Handler handler) {
        this.context = new WeakReference<>(context);
        this.callBack = callBack;
        this.recordModel = recordModel;
        this.handler = handler;
    }

    @Override
    public void run() {

        try {
            String url = "http://ws.audioscrobbler.com/2.0/?method=track.search&api_key=b2416d38d2da13890d226d243eba598b&track="
                    + URLEncoder.encode(recordModel.getSongname().trim() + " ", "UTF-8")
                    + URLEncoder.encode(recordModel.getSongwriter(), "UTF-8").trim()
                    + "&format=json";
            url = url.replace("%00", "").replace("(", " ").replace(")", " ").replace("-", " ");
            url = url.replace("  ", " ");
            Log.d("CoverLoader", "url " + url);
            JSONObject jsonObject = new JSONObject(Jsoup.connect(url).ignoreContentType(true).execute().body());

            JSONArray jsonArray = jsonObject.getJSONObject("results").getJSONObject("trackmatches").getJSONArray("track");

            JSONObject object = jsonArray.getJSONObject(0);
            JSONArray images = object.getJSONArray("image");
            String large = images.getJSONObject(3).optString("#text");
            if (TextUtils.isEmpty(large)) throw new Resources.NotFoundException("Not found cover");

            if (context.get() == null) return;

            FutureTarget<Bitmap> futureTarget =
                    Glide.with(context.get())
                            .asBitmap()
                            .load(large)
                            .submit();

           final Bitmap bitmap = futureTarget.get();

            final Palette palette = Palette.from(bitmap).generate();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.CoverSuccessfully(bitmap,palette);
                }
            });




        } catch (Exception e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {

                    callBack.CoverSuccessfully(null,null);

                }
            });
        }

    }


    public interface CoverLoaderCallBack {
        void CoverSuccessfully(Bitmap resource, Palette palette);
    }

}
