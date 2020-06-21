package com.geo.GeoQuake;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.geo.GeoQuake.models.Earthquake;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class QuakeMapFragment extends Fragment implements IDataCallback {
    private static final String TAG = QuakeMapFragment.class.getSimpleName();
    Bundle mBundle;
    HashMap<String, String> markerInfo = new HashMap<String, String>();

    private GoogleMap mMap;

    SupportMapFragment mMapFragment;

    ArrayList<Earthquake> mEarthquakes = new ArrayList<Earthquake>();

    MainActivity mActivity;

    public static QuakeMapFragment newInstance() {
        return new QuakeMapFragment();
    }

    public QuakeMapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mBundle = new Bundle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.gmap);
        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.gmap, mMapFragment).commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (Utils.checkNetwork(mActivity)) {
            if (mMap == null) {
                setUpMap();
            } else {
                if (mEarthquakes != null) {
                    placeMarkers();
                }
            }
        } else {
            Utils.connectToast(mActivity);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (MainActivity)getActivity();
    }

    /**
     * Contains the basics for setting up the map.
     */
    private void setUpMap() {
        if (Utils.checkNetwork(mActivity)) {
            if (mMap == null) {
                mMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        Log.i(TAG, "onMapReady");
                        mMap = googleMap;
                        postSyncMapSetup();
                    }
                });
            }

        } else {
            Utils.connectToast(mActivity);
        }
    }

    /**
     * Called asynchronously. Initiates the map setup, and initial marker placement
     */
    public void postSyncMapSetup() {
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            //check location permission
            PackageManager pm = mActivity.getPackageManager();
            int result = pm.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    mActivity.getPackageName());

            if (PackageManager.PERMISSION_GRANTED == result) {
                mMap.setMyLocationEnabled(true);
            }

            UiSettings settings = mMap.getUiSettings();
            settings.setCompassEnabled(true);
            settings.setMyLocationButtonEnabled(false);


            if (((MainActivity) Objects.requireNonNull(getActivity())).getEarthquakes().size() > 0) {
                mEarthquakes = ((MainActivity) getActivity()).getEarthquakes();
                placeMarkers();
            } else {
                mActivity.checkNetworkFetchData();
            }
        }
    }

    public void moveCameraToUserLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        if (mMap == null) {
            return;
        }
        try {
            MapsInitializer.initialize(mActivity);
        } catch (Exception e) {
            Log.i(TAG, "Problem initializing map");
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 4);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title(mActivity.getString(R.string.menu_my_location)));
    }

    public void userLocationMarker(double latitude, double longitude) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title(mActivity.getString(R.string.menu_my_location)));
        }

    }


    /**
     * The method that does the work of placing the markers on the map. Yes.
     */
    private void placeMarkers() {
        if (mEarthquakes != null && mEarthquakes.size() == 0) {
            if (mActivity != null) {
                Toast.makeText(mActivity, mActivity.getString(R.string.empty_list)
                        , Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mMap != null) {
                mMap.clear();
            }
            try {
                if (mEarthquakes != null && mMap != null) {
                    for (Earthquake earthquake : mEarthquakes) {
                        LatLng coords = new LatLng(earthquake.getLatitude(), earthquake.getLongitude());
                        BitmapDescriptor quakeIcon;
                        if (earthquake.getMag() <= 1.00) {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake1_trans60);
                        } else if (earthquake.getMag() <= 2.50) {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake2_trans60);
                        } else if (earthquake.getMag() <= 4.50) {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake3_trans60);
                        } else {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake4_trans60);
                        }

                        Marker m = mMap.addMarker(new MarkerOptions().icon(quakeIcon)
                                .position(coords).title(earthquake.getPlace()).snippet(getString(R.string.magnitude) + earthquake.getMag()));
                        markerInfo.put(m.getId(), earthquake.getUrl());

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                if(!TextUtils.isEmpty(getURLFromMarker(marker.getId()))) {
                                    Intent intent = new Intent(mActivity, WebInfoActivity.class);
                                    intent.putExtra("url", getURLFromMarker(marker.getId()));
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            } catch (Exception me) {
                me.printStackTrace();
            }
        }
    }


    /**
     * @param id The map marker id
     * @return URL value in hashmap
     */
    private String getURLFromMarker(String id) {
        return markerInfo.get(id);
    }

    @Override
    public void asyncUnderway() {
        //unused
    }

    @Override
    public void dataCallBack(@NonNull ArrayList<Earthquake> earthquakes) {
        //Log.i(TAG, "got callback in fragment, set data");
        mEarthquakes = earthquakes;
        placeMarkers();
    }

    /**
     * Called from activity on refresh
     */
    public void onUpdateData(ArrayList<Earthquake> earthquakes) {
        mEarthquakes = earthquakes;
        if (mEarthquakes != null) {
            placeMarkers();
        }
    }
}
