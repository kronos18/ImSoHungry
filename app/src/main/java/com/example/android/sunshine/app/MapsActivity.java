package com.example.android.sunshine.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        setUpMapIfNeeded();

        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

//    private void getListRestaurants()
//    {
//        Cursor cursor = new Cursor();
//    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        //Affiche un marker à la position 0,0
        ShowMarkerAtPosition(new LatLng(0, 0), "Restaurant", "resume du restaurant");

        //Affiche la position actuelle de l'utilisateur
        ShowCircleAtPosition(new LatLng(0, 0));

        //MyLocationOverlay currentLocation = new MyLocationOverlay(getApplicationContext(), //)
    }

    private void ShowMarkerAtPosition(LatLng position, String sNomRestaurant, String sResume)
    {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(position);
        markerOption.draggable(false);
        markerOption.flat(false);//contre la carte ou face a la camero
        markerOption.visible(true);

        //Pour la bulle d'info du marker
        markerOption.title(sNomRestaurant);
        markerOption.snippet(sResume);

        mMap.addMarker(markerOption);
    }

    private void ShowCircleAtPosition(LatLng position)
    {
        //Configure le cercle
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(position);
        circleOptions.radius(5000);
        circleOptions.visible(true);

        //interieur du cercle
        circleOptions.fillColor(Color.BLUE);

        //contour du cercle
        circleOptions.strokeWidth(10);
        circleOptions.strokeColor(Color.CYAN);

        //Affiche le cercle
        mMap.addCircle(circleOptions);
    }
}
