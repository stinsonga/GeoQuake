package com.geo.GeoQuake;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by gaius on 2014-08-25.
 */
public class ListQuakes extends Activity {

    ListView quakeListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_quakes_layout);

        quakeListView = (ListView) findViewById(R.id.quakeListView);

    }
}