package com.example.administrator.spotify2;

import android.app.DialogFragment;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
 * This fragment is used only on the tablet UI
 * to show the track info in a dialog
 * <p/>
 * This code and Audio Activity should share a class to
 * increase code reuse. TODO
 */
public class AudioFragment extends DialogFragment
        implements
        MediaController.MediaPlayerControl {

    private static String audioFile;
    private static String trackName;
    private static String albumName;
    private static String artistName;
    private static String songImageUrl = null;
    private static ArrayList<String> songs;
    private static ArrayList<String> tracks;
    private static ArrayList<String> images;
    private ImageView mSongImage = null;
    private Handler handler = new Handler();
    private static AudioService mAudioService;
    private static ServiceConnection mAudioConnection = null;
    private View rootView = null;
    private Intent playIntent;
    private MediaController mediaController = null;
    private Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        //connect to the service
        if (mAudioConnection == null)
            mAudioConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
                //get service
                mAudioService = binder.getService();
                mAudioService.playSong(audioFile);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mAudioService = null;
            }
        };
        /**
         *  Explict startservice to ensure the service stays
         *  activity while the ui transitions ri/from AudioActivity
         */

        playIntent = new Intent(getActivity(), AudioService.class);
        mContext.startService(playIntent);
        mContext.bindService(playIntent, mAudioConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.audio, container,
                false);

        ((TextView) rootView.findViewById(R.id.now_playing_artist)).setText(artistName);
        ((TextView) rootView.findViewById(R.id.now_playing_album)).setText(albumName);

        ((TextView) rootView.findViewById(R.id.now_playing_text)).setText(trackName);
        mSongImage = (ImageView) rootView.findViewById(R.id.song_image);

        mediaController = new MediaController(getActivity());

        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(rootView.findViewById(R.id.main_audio_view));
        mediaController.setEnabled(true);
        mediaController.requestFocus();

        ((ViewGroup) mediaController.getParent()).removeView(mediaController);
        ((FrameLayout) rootView.findViewById(R.id.controlsWrapper)).addView(mediaController);

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


        AudioFragment.SongimagetaSk task = new AudioFragment.SongimagetaSk();
        if (songImageUrl != null && songImageUrl.length() > 0) {

            task.execute(songImageUrl);
        }
        return rootView;


    }

    public void setTracks(String artist, String album, String song, ArrayList songs, ArrayList tracks, ArrayList images) {

        artistName = artist;
        albumName = album;
        this.songs = songs;
        this.images = images;
        this.tracks = tracks;
        audioFile = song;
        int mNext = findSong();
        songImageUrl = (String) images.get(mNext);
        trackName = (String) tracks.get(mNext);

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

        ((TextView) rootView.findViewById(R.id.now_playing_text)).
                setText(tracks.get(mNext));

        songImageUrl = images.get(mNext);
        if (songImageUrl != null && songImageUrl.length() > 0) {
            AudioFragment.SongimagetaSk task = new AudioFragment.SongimagetaSk();

            task.execute(songImageUrl);
        }

        mAudioService.playSong(audioFile);

        mediaController.setMediaPlayer(this);
        mediaController.setEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        mContext.unbindService(mAudioConnection);
        super.onDestroy();
    }

    @Override
    public void start() {
        if (mAudioService != null)
            mAudioService.start();
    }

    public void stop() {
        if (mAudioService != null)
            mAudioService.stop();
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

            String mUrl = params[0]; //.trim().replaceAll(" ", "%20");
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