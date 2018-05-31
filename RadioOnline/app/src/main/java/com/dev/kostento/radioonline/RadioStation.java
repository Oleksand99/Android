package com.dev.kostento.radioonline;

import java.io.Serializable;

class RadioStation  implements Serializable{

    protected   String stream;
    protected  String stantion;

    public RadioStation(){

    }

    public RadioStation(RadioStation radioStation) {
        this.stream = radioStation.stream;
        this.stantion = radioStation.stantion;
    }

    public RadioStation(String stantion, String stream) {
        this.stream = stream;
        this.stantion = stantion;
    }

    public String getStream() {
        return stream;
    }

    public String getStantion() {
        return stantion;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  RadioStation){
            if(((RadioStation) obj).stream.equals(this.stream)) return true;
        }
        return super.equals(obj);
    }
}

