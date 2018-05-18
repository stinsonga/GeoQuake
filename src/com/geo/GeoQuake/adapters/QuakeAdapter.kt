package com.geo.GeoQuake.adapters

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.geo.GeoQuake.R
import com.geo.GeoQuake.models.Earthquake

import java.text.DateFormat
import java.util.ArrayList

/**
 * Created by George Stinson on 2017-03-15.
 */
class QuakeAdapter(internal var listener: OnQuakeItemClickedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mEarthquakes: ArrayList<Earthquake>? = null

    fun setQuakeList(earthquakes: ArrayList<Earthquake>) {
        this.mEarthquakes = earthquakes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quake_list_item_layout, parent, false)
        return QuakeViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as QuakeViewHolder).bind(mEarthquakes!![position])
    }

    override fun getItemCount(): Int {
        return if (mEarthquakes == null) 0 else mEarthquakes!!.size
    }

    interface OnQuakeItemClickedListener {
        fun onQuakeClicked(earthquake: Earthquake)
        fun onQuakeLongClick(earthquake: Earthquake)
    }

    inner class QuakeViewHolder(itemView: View, internal var listener: OnQuakeItemClickedListener) : RecyclerView.ViewHolder(itemView) {

        internal var magnitudeTextView: TextView
        internal var locationTextView: TextView
        internal var timeTextViewHolder: TextView

        init {
            this.magnitudeTextView = itemView.findViewById(R.id.list_magnitude_text)
            this.locationTextView = itemView.findViewById(R.id.list_location_text)
            this.timeTextViewHolder = itemView.findViewById(R.id.list_time_text)
        }

        fun bind(earthquake: Earthquake) {
            var magnitude = ""
            //deal with weird, negative magnitudes
            if (magnitude == "") {
                if (earthquake.mag < 0.0) {
                    magnitude += 0.0
                } else {
                    magnitude += earthquake.mag //String var to get AS to quit complaining about concatenation
                }
                val context = itemView.context
                magnitudeTextView.text = magnitude
                magnitudeTextView.setTextColor(Color.WHITE)
                locationTextView.text = earthquake.place
                if (earthquake.mag <= 1.00) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_green))
                } else if (earthquake.mag <= 2.50) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_orange))
                } else if (earthquake.mag <= 4.50) {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_deeporange))
                } else {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(context, R.color.material_red))
                }
                if (earthquake.time != 0L) {
                    val df = java.text.DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM)
                    timeTextViewHolder.text = df.format(earthquake.time)
                } else {
                    timeTextViewHolder.text = earthquake.timeString
                }
                itemView.setOnClickListener { listener.onQuakeClicked(earthquake) }
                itemView.setOnLongClickListener {
                    listener.onQuakeLongClick(earthquake)
                    true
                }
            }
        }
    }
}