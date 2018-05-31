package com.dev.kostento.radioonline;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.Glide;



public class MusicService extends Service implements BassPlayer.PlayerCallBack {

    public static final String BROADCAST_ACTION = "displayevent";
    public static final byte MUSIC_PLAYING = 10;
    public static final byte MUSIC_STOP = 20;
    public static final byte MUSIC_BUFFERING = 30;
    private static byte status;
    private Handler handler;
    private static TrackInfo current;
    public static  float volume;
    public static BassPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        status=MUSIC_STOP;
        current= new TrackInfo().getLast();
        handler=new Handler();
        if(player==null) player=BassPlayer.getInstance(handler,this);
        player.setVolume(volume/ 100.0f);
    }


    public static TrackInfo getCurrent() {
        return current;
    }

    public void resetPlayer(RadioStation new_current){
        setCurrent(new_current);
        int id=player.getChan();
        setCurrent(new_current);
        player.setChanel_old(id);
        player.reset();
        player.preparateAndStartAsync(60,600);
    }

    public void updateCurrent(RadioStation new_current) {

        if(isPlaying()&&new_current.equals(current)) return;
        else  resetPlayer(new_current);
    }

    public void setCurrent(RadioStation current) {
        current= new TrackInfo(current);
    }

    public void playPlayer(){

        if(isPlaying()) return;

        if(player==null){
            player= BassPlayer.getInstance(handler,this);
        }
        status=MUSIC_BUFFERING;
        player.preparateAndStartAsync(60,600);
    }

    public void stopPlayer(){
        player.stop();
        status=MUSIC_STOP;
        LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(new Intent(BROADCAST_ACTION));


    }


    @Override
    public IBinder onBind(Intent intent) {
        return    new MusicBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        if(status==MUSIC_STOP){
            stopSelf();
        }
        return false;
    }

    @Override
    public void onTrimMemory(int level) {
        Glide.get(this).clearMemory();
        super.onTrimMemory(level);
    }

    public static boolean isPlaying() {
        return status==MusicService.MUSIC_PLAYING||status==MusicService.MUSIC_BUFFERING;
    }
    public static int getStatus() {
        return status;
    }

    public void setVolume(float v){
        player.setVolume(v);
        volume=v;

    }

    @Override
    public void playerMetadataReceiver(String meta) {

            if (meta != null&&!meta.isEmpty()) {

                int ti = meta.indexOf("StreamTitle=");
                if (ti >= 0) {
                    String value = meta.substring(ti + 13, meta.indexOf("'", ti + 13));
                    try {
                        Intent intent= new Intent(BROADCAST_ACTION);
                        current=TrackInfo.parseString(current,value);
                        intent.putExtra("trackInfo",current);
                        LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(meta.charAt(0)=='~'){
                    LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(new Intent(BROADCAST_ACTION));


                }

            }


    }




    @Override
    public void playerStarted() {
        status=MUSIC_PLAYING;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));

    }

    @Override
    public void playerStoped() {
        status=MUSIC_STOP;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));
    }
    @Override
    public void playerException() {
        status=MUSIC_STOP;
        stopPlayer();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));

    }

    @Override
    public void playerBuffering() {
        status=MUSIC_BUFFERING;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));

    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }




}


