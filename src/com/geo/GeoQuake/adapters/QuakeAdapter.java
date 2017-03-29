package com.geo.GeoQuake.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geo.GeoQuake.R;
import com.geo.GeoQuake.models.Earthquake;

import java.text.DateFormat;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by George Stinson on 2017-03-15.
 */
public class QuakeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Earthquake> mEarthquakes;
    OnQuakeItemClickedListener listener;

    public QuakeAdapter(OnQuakeItemClickedListener listener) {
        this.listener = listener;
    }

    public void setQuakeList(ArrayList<Earthquake> earthquakes) {
        this.mEarthquakes = earthquakes;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quake_list_item_layout, parent, false);
        return new QuakeViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((QuakeViewHolder)holder).bind(mEarthquakes.get(position));
    }

    @Override
    public int getItemCount() {
        return mEarthquakes == null ? 0 : mEarthquakes.size();
    }

    public interface OnQuakeItemClickedListener {
        void onQuakeClicked(Earthquake earthquake);
        void onQuakeLongClick(Earthquake earthquake);
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

        public void bind(final Earthquake earthquake) {
            String magnitude = "";
            //deal with weird, negative magnitudes
            if(magnitude.equals("")) {
                if (earthquake.getMag() < 0.0) {
                    magnitude += 0.0;
                } else {
                    magnitude += earthquake.getMag(); //String var to get AS to quit complaining about concatenation
                }
                Context context = itemView.getContext();
                magnitudeTextView.setText(magnitude);
                magnitudeTextView.setTextColor(Color.WHITE);
                locationTextView.setText(earthquake.getPlace());
                if (earthquake.getMag() <= 1.00) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_green));
                } else if (earthquake.getMag() <= 2.50) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_orange));
                } else if (earthquake.getMag() <= 4.50) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_deeporange));
                } else {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_red));
                }
                if(earthquake.getTime() != 0) {
                    java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
                    timeTextViewHolder.setText(df.format(earthquake.getTime()));
                } else {
                    timeTextViewHolder.setText(earthquake.getTimeString());
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onQuakeClicked(earthquake);
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        listener.onQuakeLongClick(earthquake);
                        return true;
                    }
                });
            }
        }
    }
}
