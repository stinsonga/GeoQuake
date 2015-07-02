package com.geo.GeoQuake;

import android.content.Context;
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

    public QuakeListAdapter(Context context, ArrayList<Feature> features){
        super(context, R.layout.quake_list_item_layout, features);
        this.mContext = context;
        this.mFeature = features;
    }

    static class ViewHolder {
        TextView magnitudeTextView;
        TextView locationTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.quake_list_item_layout, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.magnitudeTextView = (TextView) view.findViewById(R.id.list_magnitude_text);
            viewHolder.locationTextView = (TextView) view.findViewById(R.id.list_location_text);
            view.setTag(viewHolder);
        }
        Feature feature = mFeature.get(position);
        if(feature != null){
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.magnitudeTextView.setText(""+feature.getProperties().getMag());
            vh.locationTextView.setText(feature.getProperties().getPlace());
        }

        return view;
    }

}
