package com.geo.GeoQuake;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.geo.GeoQuake.models.Earthquake;
import com.geo.GeoQuake.models.Feature;
import com.geo.GeoQuake.models.FeatureCollection;
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

import butterknife.ButterKnife;


public class QuakeMapFragment extends Fragment implements IDataCallback {
    private static final String TAG = QuakeMapFragment.class.getSimpleName();
    Bundle mBundle;
    HashMap<String, String> markerInfo = new HashMap<String, String>();

    private GoogleMap mMap;
    GeoQuakeDB mGeoQuakeDB;

    SupportMapFragment mMapFragment;

    FeatureCollection mFeatureCollection;
    ArrayList<Earthquake> mEarthquakes = new ArrayList<Earthquake>();

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
        mGeoQuakeDB = new GeoQuakeDB(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        ButterKnife.bind(this, view);


        return view;

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
        if (Utils.checkNetwork(getActivity())) {
            if (mMap == null) {
                setUpMap();
            } else {
                if (mFeatureCollection != null) {
                    placeMarkers();
                }
            }
        } else {
            Utils.connectToast(getActivity());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Contains the basics for setting up the map.
     */
    private void setUpMap() {
        if (Utils.checkNetwork(getActivity())) {
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
            Utils.connectToast(getActivity());
        }
    }

    /**
     * Called asynchronously. Initiates the map setup, and initial marker placement
     *
     */
    public void postSyncMapSetup() {
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            //check location permission
            PackageManager pm = getActivity().getPackageManager();
            int result = pm.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    getActivity().getPackageName());

            if (PackageManager.PERMISSION_GRANTED == result) {
                mMap.setMyLocationEnabled(true);
            }

            UiSettings settings = mMap.getUiSettings();
            settings.setCompassEnabled(true);
            settings.setMyLocationButtonEnabled(false);


            if (((MainActivity) getActivity()).getFeatures() != null && ((MainActivity) getActivity()).getFeatures().getFeatures().size() > 0) {
                mFeatureCollection = ((MainActivity) getActivity()).getFeatures();
                placeMarkers();
            } else {
                ((MainActivity) getActivity()).checkNetworkFetchData();
            }
        }
    }

    public void moveCameraToUserLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        if(mMap == null) {
            return;
        } try {
            MapsInitializer.initialize(getActivity());
        } catch(Exception e) {
            Log.i(TAG, "Problem initializing map");
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 4);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title(getActivity().getString(R.string.menu_my_location)));
    }

    public void userLocationMarker(double latitude, double longitude) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title(getActivity().getString(R.string.menu_my_location)));
        }

    }


    /**
     * The method that does the work of placing the markers on the map. Yes.
     */
    private void placeMarkers() {
        if (mFeatureCollection != null && mFeatureCollection.getFeatures() != null && mFeatureCollection.getFeatures().size() == 0) {
            if(getActivity() != null) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.empty_list)
                        , Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mMap != null) {
                mMap.clear();
            }
            try {
                if (mFeatureCollection != null && mMap != null) {
                    for (Feature feature : mFeatureCollection.getFeatures()) {
                        LatLng coords = new LatLng(feature.getLatitude(), feature.getLongitude());
                        BitmapDescriptor quakeIcon;
                        if (feature.getProperties().getMag() <= 1.00) {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake1_trans60);
                        } else if (feature.getProperties().getMag() <= 2.50) {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake2_trans60);
                        } else if (feature.getProperties().getMag() <= 4.50) {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake3_trans60);
                        } else {
                            quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake4_trans60);
                        }

                        Marker m = mMap.addMarker(new MarkerOptions().icon(quakeIcon).position(coords).title(feature.getProperties().getPlace()).snippet(getString(R.string.magnitude) + feature.getProperties().getMag()));
                        markerInfo.put(m.getId(), feature.getProperties().getUrl());

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Intent intent = new Intent(getActivity(), WebInfoActivity.class);
                                intent.putExtra("url", getURLFromMarker(marker.getId()));
                                startActivity(intent);
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

    /**
     * @param config value of Configuration object passed in by the system (as this is an overridden method)
     */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        //TODO: Possible actions for orientation change
    }

    /**
     * @param outState Bundle to be saved
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    @Override
    public void asyncUnderway() {
        //unused
    }

    /**
     *
     * @param featureCollection a FeatureCollection passed by the parent activity
     */
    @Override
    public void dataCallback(FeatureCollection featureCollection) {
        Log.i(TAG, "got callback in fragment, set data");
        mFeatureCollection = featureCollection;
        placeMarkers();
    }

    /**
     * Called from activity on refresh
     *
     * @param featureCollection a FeatureCollection object sent by the activity
     */
    public void onUpdateData(FeatureCollection featureCollection) {
        mFeatureCollection = featureCollection;
        if(mFeatureCollection != null) {
            placeMarkers();
        }
    }
}
