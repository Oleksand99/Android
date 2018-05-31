package com.dev.kostento.radioonline;


import android.os.AsyncTask;
import android.os.Handler;

import com.un4seen.bass.BASS;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static com.un4seen.bass.BASS.BASS_CONFIG_BUFFER;


public class BassPlayer {
    private static BassPlayer instance;
     private Handler handler;
    private  int  chan;
    private  int  chanel_old;
    private int req;
    private  PlayerCallBack callBack;

    public int getChan() {
        return chan;
    }

    public void setChanel_old(int chanel_old) {
        this.chanel_old = chanel_old;
    }


    public static BassPlayer getInstance(Handler handler, PlayerCallBack callBack) {
        if(instance==null)instance=new BassPlayer(handler,callBack);
        return instance;
    }



    private   BassPlayer(Handler handler, PlayerCallBack callBack) {
        this.handler = handler;
        this.callBack = callBack;
    }


    public void preparate(int prg,int buf_size){
        if (!BASS.BASS_Init(-1, 44100, 0)){
            BASS.BASS_SetConfig(BASS.BASS_CONFIG_NET_PLAYLIST, 1);
            BASS.BASS_SetConfig(BASS.BASS_CONFIG_NET_PREBUF, 0);
            BASS.BASS_SetVolume(MusicService.volume/ 100.0f);
        }

        chan =  BASS.BASS_StreamCreateURL(MusicService.getCurrent().getStream(), 0, BASS.BASS_STREAM_BLOCK | BASS.BASS_STREAM_STATUS | BASS.BASS_STREAM_AUTOFREE, StatusProc, ++req);
        if(chan==0){

            return;

        }

        long progress=0;
        BASS.BASS_SetConfig(BASS_CONFIG_BUFFER, buf_size);
        while (progress < 100 ){
            progress = BASS.BASS_StreamGetFilePosition(chan, BASS.BASS_FILEPOS_BUFFER)
                    * 100 / BASS.BASS_StreamGetFilePosition(chan, BASS.BASS_FILEPOS_END);
            if(progress>prg){
                break;
            }

        }
    }


    public void start(){
        String[] icy = (String[]) BASS.BASS_ChannelGetTags(chan, BASS.BASS_TAG_ICY);
        if (icy == null)   BASS.BASS_ChannelGetTags(chan, BASS.BASS_TAG_HTTP);


        if(callBack!=null&&chan!=0){
            callBack.playerStarted();
        }else {
            if(callBack!=null) callBack.playerException();
            BASS.BASS_Free();
            return;
        }

        if(callBack!=null) callBack.playerMetadataReceiver(getMeta());
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_META, 0, MetaSync, 0);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_OGG_CHANGE, 0, MetaSync, 0);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);
        BASS.BASS_ChannelPlay(chan,false);
        if(chanel_old!=0) reset();

    }


    public void stop(){
        BASS.BASS_Stop();
        BASS.BASS_StreamFree(chan);
        BASS.BASS_Free();
        if(callBack!=null)callBack.playerStoped();
    }

    public void reset(){
        BASS.BASS_StreamFree(chanel_old);
    }



    public String getMeta() {
        return (String) BASS.BASS_ChannelGetTags(chan, BASS.BASS_TAG_META);
    }


    BASS.SYNCPROC MetaSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            handler.post(new Runnable() {
                public void run() {
                    if(callBack!=null)  callBack.playerMetadataReceiver(getMeta());
                }
            });
        }
    };


    BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            handler.post(new Runnable() {
                public void run() {

                }
            });
        }
    };

    BASS.DOWNLOADPROC StatusProc = new BASS.DOWNLOADPROC() {
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user) {
            if (buffer != null && length == 0 && (Integer) user == req) {
                String[] s;
                try {
                    CharsetDecoder dec = Charset.forName("utf8").newDecoder();
                    ByteBuffer temp = ByteBuffer.allocate(buffer.limit());
                    temp.put(buffer);
                    temp.position(0);
                    s = dec.decode(temp).toString().split("\0");
                } catch (Exception e) {
                    return;
                }
            }
        }
    };

    public void preparateAndStartAsync(int i, int i1) {

        AsyncPreparator   mt = new AsyncPreparator();
        mt.execute(i,i1);
    }


    void setVolume(float volume) {
        BASS.BASS_SetVolume(volume/ 100.0f);
    }

    class AsyncPreparator extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            preparate(params[0],params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            start();
            super.onPostExecute(result);
        }
    }

    @Override
    public void finalize() throws Throwable{
         BASS.BASS_Free();
        super.finalize();
    }

    public  interface PlayerCallBack{

        void playerMetadataReceiver(String meta);
        void playerStarted();
        void playerException();
        void playerStoped();
        void playerBuffering();

    }
}
