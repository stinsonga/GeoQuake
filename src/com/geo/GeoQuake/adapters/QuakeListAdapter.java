package com.geo.GeoQuake.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.geo.GeoQuake.models.Feature;
import com.geo.GeoQuake.R;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * Created by gstinson on 15-03-03.
 */
public class QuakeListAdapter extends ArrayAdapter<Feature> {

    private Context mContext;
    private ArrayList<Feature> mFeature;

    public QuakeListAdapter(Context context, ArrayList<Feature> features) {
        super(context, R.layout.quake_list_item_layout, features);
        this.mContext = context;
        this.mFeature = features;
    }

    private static class QuakeListViewHolder {
        TextView magnitudeTextView;
        TextView locationTextView;
        TextView timeTextViewHolder;
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
            quakeListViewHolder.timeTextViewHolder = (TextView) view.findViewById(R.id.list_time_text);
            view.setTag(quakeListViewHolder);
        }

        Feature feature = mFeature.get(position);

        if (feature != null) {
            QuakeListViewHolder vh = (QuakeListViewHolder) view.getTag();
            String magnitude = "";
            //deal with weird, negative magnitudes
            if(feature.getProperties().getMag() < 0.0) {
                magnitude += 0.0;
            } else {
                magnitude += feature.getProperties().getMag(); //String var to get AS to quit complaining about concatenation
            }
            Context context = view.getContext();
            vh.magnitudeTextView.setText(magnitude);
            vh.magnitudeTextView.setTextColor(Color.WHITE);
            vh.locationTextView.setText(feature.getProperties().getPlace());
            if (feature.getProperties().getMag() <= 1.00) {
                vh.magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_green));
            } else if (feature.getProperties().getMag() <= 2.50) {
                vh.magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_orange));
            } else if (feature.getProperties().getMag() <= 4.50) {
                vh.magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_deeporange));
            } else {
                vh.magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_red));
            }
            java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
            vh.timeTextViewHolder.setText(df.format(feature.getProperties().getTime()));
        }

        return view;
    }

}
