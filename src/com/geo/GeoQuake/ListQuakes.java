package com.geo.GeoQuake;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by gaius on 2014-08-25.
 */
public class ListQuakes extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_quakes_layout);
    }
}