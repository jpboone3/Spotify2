package com.example.administrator.spotify2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public setTracks mCallback;
    private String mArtist = null;
    private String mAlbum = null;
    private ArrayAdapter mArtistadApter = null;
    private ArrayList<StreamerArtist> artists = new ArrayList<StreamerArtist>();
    //service
    private Context mContext;
    private AudioService mAudioService;
    //binding
    private View rootView = null;
    private ServiceConnection mAudioConnection = null;
    private Intent playIntent = null;
    private ListView mListView;
    private ArrayList mStreamerList = null;

    public DetailActivityFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (setTracks) activity;
        } catch (ClassCastException e) {
            // this exception will happen when a tablet is not being used
            // because the DetailFragment is not loaded yet
            // Need a more elegant way to handle this
            mCallback = null;
            //throw new ClassCastException(activity.toString()
            //        + " must implement setTracks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mStreamerList = savedInstanceState.getParcelableArrayList("streamerlist");
            //mArtistadApter.addAll(mStreamerList);
        } else
            mStreamerList = null;

        mContext = getActivity().getApplicationContext();

        mAudioConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
                //get service
                mAudioService = binder.getService();
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

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ArrayList<String> ar = new ArrayList();
        ar.add("No Artist Tracks loaded");

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {

            mArtist = extras.getString("artist");
            mAlbum = extras.getString("album");
            // set the title/subtitle on the action bar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Top 10 Tracks");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(mAlbum);
        }

        // instantiate our ArtistAdapter class
        mArtistadApter = new StreamerAdapter(getActivity(), R.layout.list_item_artist, artists);

        mListView = (ListView) rootView.findViewById(R.id.listview_artist);

        mListView.setAdapter(mArtistadApter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isConnected() == false) {
                    Toast.makeText(getActivity(), R.id.no_network, Toast.LENGTH_LONG).show();
                    return;
                }

                //parent,getItemAtPosition(position);
                StreamerArtist sa = (StreamerArtist) parent.getItemAtPosition(position);
                int dur = (int) sa.getDuration() / 1000;
                int min = dur / 60;
                int sec = dur % 60;
                String text = String.format("%s  (Duration: %02d:%02d )",
                        sa.getName(), min, sec);


                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.putInt("pref_track_selected", position); //**syntax error on tokens**
                prefEditor.commit();

                Intent i = new Intent(getActivity(), AudioActivity.class);
                i.putExtra("artist_name", sa.getArtist());
                i.putExtra("album_name", mAlbum);
                i.putExtra("track_name", text);
                i.putExtra("track_number", sa.getTrackNumber());

                // create three liss, song url, track name, image url
                ArrayList<String> songs = new ArrayList<String>();
                ArrayList<String> tracks = new ArrayList<String>();
                ArrayList<String> images = new ArrayList<String>();

                int mCount = mListView.getAdapter().getCount();

                for (int j = 0; j < mCount; j++) {
                    StreamerArtist mSa = (StreamerArtist) mListView.getAdapter().getItem(j);
                    songs.add(mSa.getPreviewUrl());
                    String mTrack = String.format("%s  (Duration: %02d:%02d )",
                            mSa.getName(), min, sec);
                    tracks.add(mTrack);
                    images.add(mSa.getSongImageUtl());
                }
                if (mArtist == null)
                    mArtist = preferences.getString("pref_artist", "Unknown Artist");

                if (mCallback != null && mCallback.setTracks(mArtist, mAlbum, sa.getPreviewUrl(), songs, tracks, images))
                    return;


                i.putStringArrayListExtra("songs", (ArrayList<String>) songs);
                i.putStringArrayListExtra("tracks", (ArrayList<String>) tracks);
                i.putStringArrayListExtra("images", (ArrayList<String>) images);

                i.putExtra("track_url", sa.getPreviewUrl());
                i.putExtra("song_url", sa.getSongImageUtl());

                startActivity(i);

            }
        });

        return rootView;
    }

    // This method can be called by MainFragment to load
    // the top 10 tracks when the UI is initated
    // on a tablet
    public void startTrackList(String album) {
        if (album == null) {
            // need to clear the list view
            // this condition is because the artist
            // had no albums and we need to cleanup
            // our old list
            mStreamerList = null;
            if (mArtistadApter != null)
                mArtistadApter.clear();
            mListView.invalidate();
            return;
        }
        // if the album has changed, must read the tracks for the new album
        if (mAlbum != null && mAlbum.equals(album) == false)
            mStreamerList = null;
        mAlbum = album;
        updateTracks();
    }

    @Override
    public void onStart() {
        super.onStart();

        updateTracks();
    }

    @Override
    public void onDestroy() {
        super.onStop();
        // design decision to stop the service when the user
        // is finished with the album
        if (mAudioService != null)
            mAudioService.stop();
        mContext.unbindService(mAudioConnection);
        mContext.stopService(playIntent);
    }
    private boolean isConnected() {
        // This should never be false since the main activity will check this first.
        // If the user is on a phone and on the tracks UI, then set the
        // phone to airplane mode, then we will test for this situation.
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo an = cm.getActiveNetworkInfo();
        if (an != null && an.isConnectedOrConnecting())
            return true;
        else
            return false;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("streamerlist", mStreamerList);
        super.onSaveInstanceState(outState);
    }

    protected void updateTracks() {

        // Load the song image
        if (mStreamerList != null) {
            mArtistadApter.clear();
            mArtistadApter.addAll(mStreamerList);
            return;
        }
        if (isConnected()) {
            if (mAlbum != null && mAlbum.length() > 0) {
                //boolean metric = preferences.getBoolean("pref_metric", true);
                DetailActivityFragment.TracklisttaSk task = new DetailActivityFragment.TracklisttaSk();

                task.execute(mAlbum);
            }
        } else {
            Toast.makeText(getActivity(), R.id.no_network, Toast.LENGTH_LONG).show();

        }
    }

    public interface setTracks {
        public boolean setTracks(String artist, String album, String song, ArrayList songs, ArrayList tracks, ArrayList images);
    }

    public class TracklisttaSk extends AsyncTask<String, String, ArrayList> {

        static final String myLOGFILTER = "ArtisTlisttaSk";

        //static final String myLOGFILTER = ArtisTlisttaSk.getClass().getSimpleName();
        public TracklisttaSk() {
        }

        protected ArrayList doInBackground(String... params) {

            String mQuery = params[0]; //.trim().replaceAll(" ", "%20");

            ArrayList<StreamerArtist> mSlist = new ArrayList<StreamerArtist>();

            SpotifyApi mApi = new SpotifyApi();
            if (mApi == null) {
                return mSlist;
            }
            SpotifyService mService = mApi.getService();
            if (mService == null) {
                return mSlist;
            }

            TracksPager mTm = mService.searchTracks(mQuery);
            List listOfArtists = mTm.tracks.items;

            for (int i = 0; i < listOfArtists.size(); i++) {

                StreamerArtist sa = new StreamerArtist();
                Track a = (Track) listOfArtists.get(i);
                sa.setName(a.name);

                sa.setDuration((int) a.duration_ms);
                sa.setArtist(((ArtistSimple) a.artists.get(0)).name);
                sa.setTrackNumber(a.disc_number);
                sa.setPopularity(a.popularity);
                sa.setPreviewUrl(a.preview_url);
                sa.setSelected(false);

                if (a.album.images != null) {
                    // find the image that is 54 in height
                    String url = null;
                    int mLargest = 0;
                    for (int j = 0; j < a.album.images.size(); j++) {
                        if (a.album.images.get(j).height <= 64)
                            url = a.album.images.get(j).url;

                        if (mLargest < a.album.images.get(j).height) {
                            mLargest = a.album.images.get(j).height;
                            sa.setSongImageUtl(a.album.images.get(j).url);
                        }
                    }
                    if (url != null) {
                        try {
                            URL aURL = new URL(url);
                            URLConnection conn =
                                    aURL.openConnection();
                            conn.connect();

                            InputStream is = conn.getInputStream();
                            BufferedInputStream bis = new BufferedInputStream(is);
                            sa.setThumbnail(BitmapFactory.decodeStream(bis));
                            bis.close();
                            is.close();
                        } catch (IOException e) {
                            // TO DO
                            Log.e("ArtistAdapter", "Error getting bitmap", e);
                        }

                    }

                }

                mSlist.add(sa);

            }
            // Need to return the top ten tracks based on popularity
            // First bubble sort popularity in reverse order
            Collections.sort(mSlist);

            // only want to retturn the top 10 tracks
            while (mSlist.size() > 10) {
                mSlist.remove(10);
            }

            return mSlist;
        }

        protected void onPostExecute(ArrayList l) {
            mArtistadApter.clear();
            if (l == null || l.size() == 0) {
                Toast.makeText(getActivity(), R.id.no_tracks, Toast.LENGTH_LONG).show();
                return;
            }
            // update UI to show last track selected, if any
            int mTrackSelected = -1;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (preferences != null) {
                mTrackSelected = preferences.getInt("pref_track_selected", -1);
            }
            if (mTrackSelected >= 0) {
                StreamerArtist sa = (StreamerArtist) l.get(mTrackSelected);
                sa.setSelected(true);
            }
            mArtistadApter.addAll(l);
            mStreamerList = l;
            super.onPostExecute(l);
        }
    }
}
