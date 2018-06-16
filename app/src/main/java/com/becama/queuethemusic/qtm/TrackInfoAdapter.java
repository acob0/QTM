package com.becama.queuethemusic.qtm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by jacob on 01/02/2018.
 */

public class TrackInfoAdapter extends ArrayAdapter<TrackInfo> {

    Context mContext;

    public TrackInfoAdapter(Context context, ArrayList<TrackInfo> users) {
        super(context, 0, users);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final TrackInfo trackInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_info_item, parent, false);
        }
        // Lookup view for data population
        TextView songName = convertView.findViewById(R.id.adapter_song);
        TextView artistName = convertView.findViewById(R.id.adapter_artist);
        ImageView albumImage = convertView.findViewById(R.id.adapter_album);
        // Populate the data into the template view using the data object
        songName.setText(trackInfo.trackName);
        artistName.setText(trackInfo.artistName);
        if (!trackInfo.imageURL.equals("")) {
            Picasso.with(mContext).load(trackInfo.imageURL).into(albumImage);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
