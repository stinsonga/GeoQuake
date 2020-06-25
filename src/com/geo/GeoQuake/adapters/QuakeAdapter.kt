package com.geo.GeoQuake.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.geo.GeoQuake.R
import com.geo.GeoQuake.models.Earthquake
import java.text.DateFormat
import java.util.*

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
                magnitude += if (earthquake.mag < 0.0) {
                    0.0
                } else {
                    earthquake.mag
                }

                magnitudeTextView.text = String.format("%.2f", magnitude.toDouble())
                magnitudeTextView.setTextColor(Color.WHITE)
                locationTextView.text = earthquake.place

                setMagnitudeColor(earthquake.mag)

                if (earthquake.time != 0L) {
                    val df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM)
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

        /**
         * Set text color based on magnitude
         */
        private fun setMagnitudeColor(mag: Double) {
            when {
                mag <= 1.00 -> {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.material_green))
                }
                mag <= 2.50 -> {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.material_orange))
                }
                mag <= 4.50 -> {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.material_deeporange))
                }
                else -> {
                    magnitudeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.material_red))
                }
            }
        }
    }
}
