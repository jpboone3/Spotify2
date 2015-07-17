package com.example.administrator.spotify2;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Object to hold album/track data for the ListViews
 */
public class StreamerArtist extends ArrayList<StreamerArtist> implements Comparable<StreamerArtist>, Parcelable {

    private String artist;                  //artist  name
    private String name;                  //artist or track name
    private String preview_url = null;    // preview song track
    private String song_image_url = null; // large image of song

    private Bitmap thumbnail = null;      // thumbnail of album/song image
    private int popularity = -1;          // popularity of the song (based on 0 - 100)
    private boolean selected = false;     // item selected
    private int track_number;             // track number
    private int duration;                 // track number

    public StreamerArtist() {}

    private StreamerArtist(Parcel in) {
        artist = in.readString();
        name = in.readString();
        preview_url = in.readString();
        song_image_url = in.readString();
        artist = in.readString();

        track_number = in.readInt();
        popularity = in.readInt();
        duration = in.readInt();
        //private Bitmap thumbnail = null;      // thumbnail of album/song image
        int i = in.readInt();
        if (i == 1)
            selected = true;     // item selected
        return;
    }



    @Override
    public int compareTo(StreamerArtist compareStreamerArtist) {
        // we want descending order

        if (compareStreamerArtist.popularity < popularity)
            return -1;
        if (compareStreamerArtist.popularity > popularity)
            return 1;
        return 0;

    }

    public int getDuration() {
        return this.duration;
    }


    public void setDuration(int duration) {
        this.duration = duration;
        return;
    }

    public int getTrackNumber() {
        return this.track_number;
    }

    public void setTrackNumber(int number) {
        this.track_number = number;
        return;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopularity() {
        return this.popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPreviewUrl() {
        return this.preview_url;
    }

    public void setPreviewUrl(String preview_url) {
        this.preview_url = preview_url;
    }

    public String getSongImageUtl() {
        return this.song_image_url;
    }

    public void setSongImageUtl(String song_image_url) {
        this.song_image_url = song_image_url;
    }

    public Bitmap getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean state) {
        this.selected = state;
        return;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

            dest.writeString(artist);
            dest.writeString(name);
            dest.writeString(preview_url);
            dest.writeString(song_image_url);
            dest.writeString(artist);

            dest.writeInt(track_number);
            dest.writeInt(popularity);
            dest.writeInt(duration);
            //private Bitmap thumbnail = null;      // thumbnail of album/song image
            if (selected)
                dest.writeLong(1);
            else
                dest.writeLong(0);
            //private boolean selected = false;     // item selected

    }

    public static final Parcelable.Creator<StreamerArtist> CREATOR = new Parcelable.Creator<StreamerArtist>() {
        public StreamerArtist createFromParcel(Parcel in) {
            return new StreamerArtist(in);
        }

        public StreamerArtist[] newArray(int size) {
            return new StreamerArtist[size];
        }
    };
}
