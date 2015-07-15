package com.example.administrator.spotify2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Methods in this mode should be shared with module
 * AudioFragment. TODO
 */
public class AudioActivity extends FragmentActivity
        implements
        MediaController.MediaPlayerControl {

    private static final String TAG = "AudioActivity";

    private MediaController mediaController;
    private String audioFile;
    private String trackName;
    private String albumName;
    private String artistName;
    private String songImageUrl;
    private ImageView mSongImage;
    private boolean mMmediaCleaned = false;
    private Handler handler = new Handler();
    private ArrayList<String> songs;
    private ArrayList<String> tracks;
    private ArrayList<String> images;
    private AudioService mAudioService;
    private ServiceConnection mAudioConnection = null;
    private Intent playIntent = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio);

        songImageUrl = this.getIntent().getStringExtra("song_url");
        audioFile = this.getIntent().getStringExtra("track_url");
        trackName = this.getIntent().getStringExtra("track_name");
        albumName = this.getIntent().getStringExtra("album_name");
        artistName = this.getIntent().getStringExtra("artist_name");

        songs = this.getIntent().getStringArrayListExtra("songs");
        images = this.getIntent().getStringArrayListExtra("images");
        tracks = this.getIntent().getStringArrayListExtra("tracks");


        ((TextView) findViewById(R.id.now_playing_artist)).setText(artistName);
        ((TextView) findViewById(R.id.now_playing_album)).setText(albumName);

        ((TextView) findViewById(R.id.now_playing_text)).setText(trackName);

        mSongImage = (ImageView) findViewById(R.id.song_image);

        mAudioConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
                //get service
                mAudioService = binder.getService();
                if (mAudioService != null) {
                    mAudioService.playSong(audioFile);
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

                mAudioService = null;
            }
        };
        playIntent = new Intent(getApplicationContext(), AudioService.class);
        getApplicationContext().bindService(playIntent, mAudioConnection, Context.BIND_AUTO_CREATE);

        mediaController = new MediaController(this);
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.main_audio_view));
        mediaController.setEnabled(true);

        mediaController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle next click here
                setNewSong(true);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle previous click here
                setNewSong(false);
            }
        });

        AudioActivity.SongimagetaSk task = new AudioActivity.SongimagetaSk();
        if (songImageUrl != null && songImageUrl.length() > 0) {

            task.execute(songImageUrl);
        }
    }

    @Override
    public void onAttachedToWindow() {
        mediaController.show();
    }

    private int findSong() {

        for (int i = 0; i < songs.size(); i++) {
            if (audioFile.equals(songs.get(i))) {
                return i;
            }
        }
        return 0;
    }

    private void setNewSong(boolean mNextTrack) {
        int mNext = findSong();

        if (mNextTrack) {
            if ((mNext + 1) < songs.size()) {
                mNext++;
            }
        } else {
            if ((mNext - 1) > 0) {
                mNext--;
            }
        }
        audioFile = songs.get(mNext);


        ((TextView) findViewById(R.id.now_playing_text)).
                setText(tracks.get(mNext));

        songImageUrl = images.get(mNext);
        if (songImageUrl != null && songImageUrl.length() > 0) {
            AudioActivity.SongimagetaSk task = new AudioActivity.SongimagetaSk();

            //Toast.makeText(getActivity(), "Seaching Spotify", Toast.LENGTH_LONG).show();
            task.execute(songImageUrl);
        }

        mAudioService.playSong(audioFile);

        mediaController.setMediaPlayer(this);
        mediaController.setEnabled(true);
        // lets have the mediacontroller hide after
        // two seconds
        mediaController.show(2);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        getApplicationContext().unbindService(mAudioConnection);
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        mediaController.show(0);
        return false;
    }

    @Override
    public void start() {
        if (mAudioService != null)
            mAudioService.start();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public void pause() {
        if (mAudioService != null)
            mAudioService.pause();
    }

    //--MediaPlayerControl methods----------------------------------------------------

    @Override
    public int getAudioSessionId() {
        if (mAudioService != null)
            return mAudioService.getAudioSessionId();
        return 0;
    }

    @Override
    public int getDuration() {
        if (mAudioService != null)
            return mAudioService.getDuration();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mAudioService != null)
            return mAudioService.getCurrentPosition();
        return 0;
    }

    @Override
    public void seekTo(int i) {
        if (mAudioService != null)
            mAudioService.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        if (mAudioService != null)
            return mAudioService.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }


    public class SongimagetaSk extends AsyncTask<String, String, Bitmap> {

        public SongimagetaSk() {
        }

        protected Bitmap doInBackground(String... params) {

            String mUrl = params[0];
            Bitmap mBitmap = null;
            try {
                URL aURL = new URL(mUrl);
                URLConnection conn =
                        aURL.openConnection();
                conn.connect();

                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                mBitmap = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                // TO DO logging of the bitmap load error
            }

            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {

            // set the song image
            if (bm != null && mSongImage != null)
                mSongImage.setImageBitmap(bm);
            super.onPostExecute(bm);
        }
    }
}