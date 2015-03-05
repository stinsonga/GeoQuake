package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

/**
 * Created by gaius on 2014-08-25.
 */
public class ListQuakes extends Activity {

    ListView quakeListView;
    SharedPreferences mSharedPreferences;
    Context mContext;
    Spinner mQuakeTypeSpinner;
    Spinner mDurationTypeSpinner;
    CheckBox mActionBarCheckbox;
    DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_quakes_layout);
        mSharedPreferences = getPreferences(0);      //multiple activites...probably need a more specific reference
        mContext = getApplicationContext();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        quakeListView = (ListView) findViewById(R.id.quakeListView);

        mActionBarCheckbox = (CheckBox) findViewById(R.id.actionbar_toggle_checkbox);
        mActionBarCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    getActionBar().hide();
                } else {
                    getActionBar().show();
                }
            }
        });
        mQuakeTypeSpinner = (Spinner) findViewById(R.id.quake_type_spinner);
        ArrayAdapter<CharSequence> quakeTypeAdapter = ArrayAdapter.createFromResource(this, R.array.quake_types, android.R.layout.simple_spinner_dropdown_item);
        mQuakeTypeSpinner.setAdapter(quakeTypeAdapter);
        mQuakeTypeSpinner.setSelection(4);

        mDurationTypeSpinner = (Spinner) findViewById(R.id.duration_type_spinner);
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this, R.array.duration_types, android.R.layout.simple_spinner_dropdown_item);
        mDurationTypeSpinner.setAdapter(durationAdapter);
        mDurationTypeSpinner.setSelection(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_map_view:
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                break;
            case R.id.action_refresh:
                if (GeoQuakeDB.checkRefreshLimit(Long.parseLong(GeoQuakeDB.getTime()),
                        mSharedPreferences.getLong(GeoQuakeDB.REFRESH_LIMITER, 0))) {
                    Log.i("ok to refresh?", "YES");
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putLong(GeoQuakeDB.REFRESH_LIMITER, Long.parseLong(GeoQuakeDB.getTime()));
                    editor.apply();
                    //initiateRefresh(true);
                } else {
                    Log.i("ok to refresh?", "NO");
                    Toast.makeText(mContext, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
                    Log.i("can refresh again at: ", ""+mSharedPreferences.getLong(GeoQuakeDB.REFRESH_LIMITER,
                            0)+GeoQuakeDB.REFRESH_LIMITER_TIME);
                    Log.i("current time: ", GeoQuakeDB.getTime());
                }
                break;
            case R.id.action_settings:
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else{
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            default:
                break;
        }
        return false;
    }
}