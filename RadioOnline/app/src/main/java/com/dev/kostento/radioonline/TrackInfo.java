package com.dev.kostento.radioonline;

import java.io.Serializable;

public class TrackInfo extends RadioStation implements Serializable{

    private  String  songwriter;
    private String  songname;

    public TrackInfo(RadioStation current) {
        super(current);
    }
    public TrackInfo() {
        songwriter="Невідомий виконавець";
        songname=null;
    }

    public TrackInfo getLast() {
       stream="http://s12.myradiostream.com:15208/listen.mp3/;stream/1";
       stantion="Blues Radio";

        return this;
    }


    public String getSongwriter() {
        return songwriter;
    }

    public String getSongname() {
        return songname;
    }

    public static TrackInfo parseString(RadioStation current, String value) {

        TrackInfo trackInfo=new TrackInfo(current);
        int separate=value.indexOf('-');
        if(separate>0){
            trackInfo.songname=value.substring(separate+1).trim();
            trackInfo.songwriter=value.substring(0,separate).trim();
        }else {
            trackInfo.songname=value;
            trackInfo.songwriter=null;
        }
        return  trackInfo;
    }

}
