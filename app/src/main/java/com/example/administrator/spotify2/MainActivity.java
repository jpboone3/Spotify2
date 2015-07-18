package com.example.administrator.spotify2;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.setAlbumSelected,
        DetailActivityFragment.setTracks {

    private FragmentManager fm;
    private boolean mTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fm = getSupportFragmentManager();

        if (fm.findFragmentById(R.id.fragment_detail) != null) {
            mTablet = true;
        }

    }

    public boolean setAlbumSelected(String artist, String album) {
        if (mTablet == false)
            return false;
        /**
         * Probably should do a different layout for portriat
         * mode on a tablet.
         * if (getResources().getConfiguration().orientation !=
         *      Configuration.ORIENTATION_LANDSCAPE)
         *  return false;
         */
        // Get Detail Fragment that displays the top 10
        // If not in this layout, we are using a device
        // less than 600dp in size
        if (fm.findFragmentById(R.id.fragment_detail) != null) {

            DetailActivityFragment fragDetail = (DetailActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_detail);
            fragDetail.startTrackList(album);
            getSupportActionBar().setSubtitle(album);
            getSupportActionBar().setTitle("Top Ten Tracks");

            return true;
        }
        return false;
    }

    public boolean setTracks(String artist, String album, String song, ArrayList songs, ArrayList tracks, ArrayList images) {
        // if not on tablet, use activity instead
        if (mTablet == false)
            return false;

        AudioFragment df = new AudioFragment();
        df.setTracks(artist, album, song, songs, tracks, images);

        // Show DialogFragment
        df.show(getFragmentManager(), song);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }}
