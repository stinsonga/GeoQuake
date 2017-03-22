package com.geo.GeoQuake.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geo.GeoQuake.R;
import com.geo.GeoQuake.models.Feature;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by George Stinson on 2017-03-15.
 */
public class QuakeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Feature> mFeature;
    OnQuakeItemClickedListener listener;

    public QuakeAdapter(OnQuakeItemClickedListener listener) {
        this.listener = listener;
    }

    public void setQuakeList(ArrayList<Feature> features) {
        this.mFeature = features;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quake_list_item_layout, parent, false);
        return new QuakeViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((QuakeViewHolder)holder).bind(mFeature.get(position));
    }

    @Override
    public int getItemCount() {
        return mFeature.size();
    }

    public interface OnQuakeItemClickedListener {
        void onQuakeClicked(Feature feature);
        void onQuakeLongClick(Feature feature);
    }

    public class QuakeViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.list_magnitude_text)
        TextView magnitudeTextView;
        @Bind(R.id.list_location_text)
        TextView locationTextView;
        @Bind(R.id.list_time_text)
        TextView timeTextViewHolder;
        OnQuakeItemClickedListener listener;

        public QuakeViewHolder(View itemView, OnQuakeItemClickedListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }

        public void bind(final Feature feature) {
            String magnitude = "";
            //deal with weird, negative magnitudes
            if(magnitude.equals("")) {
                if (feature.getProperties().getMag() < 0.0) {
                    magnitude += 0.0;
                } else {
                    magnitude += feature.getProperties().getMag(); //String var to get AS to quit complaining about concatenation
                }
                Context context = itemView.getContext();
                magnitudeTextView.setText(magnitude);
                magnitudeTextView.setTextColor(Color.WHITE);
                locationTextView.setText(feature.getProperties().getPlace());
                if (feature.getProperties().getMag() <= 1.00) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_green));
                } else if (feature.getProperties().getMag() <= 2.50) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_orange));
                } else if (feature.getProperties().getMag() <= 4.50) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_deeporange));
                } else {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_red));
                }
                java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
                timeTextViewHolder.setText(df.format(feature.getProperties().getTime()));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onQuakeClicked(feature);
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        listener.onQuakeLongClick(feature);
                        return true;
                    }
                });
            }
        }
    }
}
