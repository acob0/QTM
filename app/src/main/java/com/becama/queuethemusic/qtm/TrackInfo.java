package com.becama.queuethemusic.qtm;

/**
 * Created by jacob on 01/02/2018.
 */

public class TrackInfo {

    public String trackName;
    public String artistName;
    public String uri;
    public int length;
    public String imageURL;

    public void NameArtist(){
        this.trackName = "";
        this.artistName = "";
        this.uri = "";
        this.length = 0;
        this.imageURL = "";
    }

    public void NameArtist(String trackNames, String artistNames){
        this.trackName = trackNames;
        this.artistName = artistNames;
        this.uri = "";
        this.length = 0;
        this.imageURL = "";
    }
}
