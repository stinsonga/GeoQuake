package com.geo.GeoQuake;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by gstinson on 15-03-03.
 */
public class QuakeListAdapter extends ArrayAdapter<Feature> {

    private Context mContext;
    ArrayList<Feature> mFeature;

    public QuakeListAdapter(Context context, ArrayList<Feature> features) {
        super(context, R.layout.quake_list_item_layout, features);
        this.mContext = context;
        this.mFeature = features;
    }

    static class QuakeListViewHolder {
        TextView magnitudeTextView;
        TextView locationTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.quake_list_item_layout, null);
            QuakeListViewHolder quakeListViewHolder = new QuakeListViewHolder();
            quakeListViewHolder.magnitudeTextView = (TextView) view.findViewById(R.id.list_magnitude_text);
            quakeListViewHolder.locationTextView = (TextView) view.findViewById(R.id.list_location_text);
            view.setTag(quakeListViewHolder);
        }

        Feature feature = mFeature.get(position);
        
        if (feature != null) {
            QuakeListViewHolder vh = (QuakeListViewHolder) view.getTag();
            vh.magnitudeTextView.setText("" + feature.getProperties().getMag());
            vh.magnitudeTextView.setTextColor(Color.WHITE);
            vh.locationTextView.setText(feature.getProperties().getPlace());
            if (feature.getProperties().getMag() <= 1.00) {
                vh.magnitudeTextView.setTextColor(Color.GREEN);
            } else if (feature.getProperties().getMag() <= 2.50) {
                vh.magnitudeTextView.setTextColor(Color.YELLOW);
            } else if (feature.getProperties().getMag() <= 4.50) {
                vh.magnitudeTextView.setTextColor(mContext.getResources().getColor(R.color.orange));
            } else {
                vh.magnitudeTextView.setTextColor(Color.RED);
            }
        }

        return view;
    }

}
