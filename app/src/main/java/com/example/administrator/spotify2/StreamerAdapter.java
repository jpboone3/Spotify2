package com.example.administrator.spotify2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class StreamerAdapter extends ArrayAdapter<com.example.administrator.spotify2.StreamerArtist> {

    private final Context context;
    // declaring our ArrayList of artists
    private ArrayList<com.example.administrator.spotify2.StreamerArtist> objects = null;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Artist> objects,
    * because it is the list of objects we want to display.
    */
    public StreamerAdapter(Context context, int textViewResourceId, ArrayList<com.example.administrator.spotify2.StreamerArtist> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list artist will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View mView = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (mView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = inflater.inflate(R.layout.list_item_artist, null);
        }

        /*
         * Recall that the variable position is sent in as an argument to this method.
         * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
         * iterates through the list we sent it)
         *
         * Therefore, mSa refers to the current Artist object.
         */
        StreamerArtist mSa = objects.get(position);

        if (mSa != null) {
            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.
            ImageView mThumbnaul = (ImageView) mView.findViewById(R.id.list_item_icon);
            if (mThumbnaul != null && mSa.getThumbnail() != null) {
                mThumbnaul.setImageBitmap(mSa.getThumbnail());
            }

            TextView mName = (TextView) mView.findViewById(R.id.list_item_name);

            if (mSa.isSelected()) {
                // set yellow background
                mName.setBackgroundColor(0xffffff00);
            } else {
                // set white background
                // the "back" button does not always refresh the backgrounf in a viewlist
                mName.setBackground(mView.getBackground());
            }

            mName.setText(mSa.getName());

        }

        // the view must be returned to our
        return mView;

    }

}
