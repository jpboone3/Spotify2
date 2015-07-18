package com.example.administrator.spotify2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Fragment thst displays the artists albums
 */
public class MainActivityFragment extends Fragment {

    private final int MAX_MARTISTS_TO_LIST = 40;
    public setAlbumSelected mCallback = null;
    private ArrayAdapter mArtistadApter = null;
    private ArrayList<com.example.administrator.spotify2.StreamerArtist> martists = new ArrayList<com.example.administrator.spotify2.StreamerArtist>();
    private EditText mArtist_name;
    private String mArtist;
    private ListView mListView;
    private int mArtistSelected = -1;
    private ArrayList mStreamerList = null;

    public MainActivityFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (setAlbumSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement setAlbumSelected");
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayList<String> ar = new ArrayList();
        ar.add("No Artist loaded");

        mArtist_name = (EditText) rootView.findViewById(R.id.artist_name);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences != null) {

            mArtist = preferences.getString("pref_artist", null);
            if (mArtist != null)
                mArtist_name.setText(mArtist);
        }

        // prevent the soft keyboard from showing at startup
        mArtist_name.setInputType(0);

        mArtist_name.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                mArtist = preferences.getString("pref_artist", null);
                if (mArtist_name.getText().equals(mArtist) == false) {
                    SharedPreferences.Editor prefEditor = preferences.edit();
                    prefEditor.putString("pref_artist", mArtist_name.getText().toString()); //**syntax error on tokens**
                    prefEditor.putInt("pref_artist_selected", -1); //**syntax error on tokens**
                    prefEditor.commit();

                }
                mStreamerList = null;

                updateMartists();
                return false;
            }
        });

        // instantiate our ArtistAdapter class
        mArtistadApter = new StreamerAdapter(getActivity(), R.layout.list_item_artist, martists);

        mListView = (ListView) rootView.findViewById(R.id.listview_artist);

        mListView.setAdapter(mArtistadApter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // turn off previous selected album
                StreamerArtist sa;
                if (mArtistSelected >= 0) {
                    sa = (StreamerArtist) parent.getItemAtPosition(mArtistSelected);
                    sa.setSelected(false);
                    mArtistSelected = position;
                }

                // set the current album selected
                sa = (StreamerArtist) parent.getItemAtPosition(position);
                String text = sa.getName();
                sa.setSelected(true);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.putInt("pref_artist_selected", position); //**syntax error on tokens**
                prefEditor.putInt("pref_track_selected", -1); //**syntax error on tokens**
                prefEditor.commit();

                // invalidate to show highligted artist
                mListView.invalidateViews();
                if (mCallback.setAlbumSelected(mArtist, text) == true) {
                    // the view list may have a different selected album
                    // force a repaint to ensure the selection is correct
                    return;
                }
                Intent i = new Intent(getActivity(), com.example.administrator.spotify2.DetailActivity.class);
                i.putExtra("artist", mArtist);
                i.putExtra("album", text);

                startActivity(i);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMartists();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("streamerlist", mStreamerList);
        super.onSaveInstanceState(outState);
    }
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo an = cm.getActiveNetworkInfo();
        if (an != null && an.isConnectedOrConnecting())
            return true;
        else
            return false;

    }

    protected void updateMartists() {
        if (mStreamerList != null) {
            mArtistadApter.clear();
            mArtistadApter.addAll(mStreamerList);
            mArtistSelected = -1;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (preferences != null) {
                mArtistSelected = preferences.getInt("pref_artist_selected", -1);
            }
            // update the UI with the selected item highlighted
            if (mArtistSelected >= 0) {
                StreamerArtist sa = (StreamerArtist) mStreamerList.get(mArtistSelected);

                // may be on tablet UI, immediate populate
                // top 10 track list
                mCallback.setAlbumSelected(mArtist, sa.getName());

                sa.setSelected(true);
            }
            return;
        }
        if (isConnected()) {
            ArtisTlisttaSk task = new ArtisTlisttaSk();
            String mTemp = mArtist_name.getText().toString();
            if (mTemp != null && mTemp.length() > 0) {
                task.execute(mTemp);
            } else {
                mArtist_name.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        } else {
            Toast.makeText(getActivity(), R.id.no_network, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                mArtist_name.setInputType(InputType.TYPE_CLASS_TEXT);
                mArtist_name.requestFocus();

                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(mArtist_name, 0);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface setAlbumSelected {
        public boolean setAlbumSelected(String artist, String album);
    }

    public class ArtisTlisttaSk extends AsyncTask<String, String, ArrayList> {

        static final String myLOGFILTER = "ArtisTlisttaSk";

        public ArtisTlisttaSk() {
        }

        protected ArrayList doInBackground(String... params) {
            String q = params[0].trim();

            ArrayList<StreamerArtist> mSl = new ArrayList<StreamerArtist>();

            SpotifyApi api = new SpotifyApi();
            if (api == null)
                return mSl;
            SpotifyService spotify = api.getService();
            if (spotify == null)
                return mSl;

            ArtistsPager results = spotify.searchArtists(q);
            List listOfMartists = results.artists.items;

            for (int i = 0; i < listOfMartists.size(); i++) {
                StreamerArtist sa = new StreamerArtist();
                Artist a = (Artist) listOfMartists.get(i);
                sa.setName(a.name);
                sa.setSelected(false);

                if (a.images != null) {
                    // find the image that is 54 in height
                    String url = null;
                    for (int j = 0; j < a.images.size(); j++) {
                        if (a.images.get(j).height <= 64)
                            url = a.images.get(j).url;
                    }
                    if (url != null) {
                        //sa.thumbnail = null;
                        try {
                            URL aURL = new URL(url);
                            URLConnection conn =
                                    conn = aURL.openConnection();
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
                mSl.add(sa);

                // limit the list to MAX_MARTISTS_TO_LIST
                if (i >= MAX_MARTISTS_TO_LIST)
                    break;
            }


            return mSl;
        }

        @Override
        protected void onPostExecute(ArrayList l) {
            mArtistadApter.clear();
            mArtist_name.setInputType(InputType.TYPE_CLASS_TEXT);
            //Log.d("lengthg ", "length: " + s.length);
            if (l == null || l.size() == 0) {
                mStreamerList = null;
                mListView.invalidate();
                // if on a tablet, need to clear the track list
                if (mCallback != null)
                    mCallback.setAlbumSelected(mArtist, null);

                    Toast.makeText(getActivity(), R.id.no_artists, Toast.LENGTH_LONG).show();
                return;
            }


            mArtistSelected = -1;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (preferences != null) {
                mArtistSelected = preferences.getInt("pref_artist_selected", -1);
            }

            // update the UI with the selected item highlighted
            if (mArtistSelected >= 0) {
                StreamerArtist sa = (StreamerArtist) l.get(mArtistSelected);

                // may be on tablet UI, immediate populate
                // top 10 track list
                mCallback.setAlbumSelected(mArtist, sa.getName());

                sa.setSelected(true);
            }

            mArtistadApter.addAll(l);
            mStreamerList = l;
            if (l.size() >= MAX_MARTISTS_TO_LIST)
                Toast.makeText(getActivity(), R.id.max_artists, Toast.LENGTH_LONG).show();
        }
    }
}
