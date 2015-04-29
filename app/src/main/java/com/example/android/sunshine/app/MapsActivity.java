package com.example.android.sunshine.app;

import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.android.sunshine.app.data.IndexBDRestaurant;
import com.example.android.sunshine.app.data.RestaurantContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Uri mUri;
    static final String DETAIL_URI = "URI";

    private static final int DETAIL_LOADER_MAP = 0;
    public static final String[] RESTAURANT_COLUMNS = {
            RestaurantContract.RestaurantEntry.TABLE_NAME + "." + RestaurantContract.RestaurantEntry._ID,
            RestaurantContract.RestaurantEntry.COLUMN_NAME,
            RestaurantContract.RestaurantEntry.COLUMN_ADRESSE,
            RestaurantContract.RestaurantEntry.COLUMN_VILLE,
            RestaurantContract.RestaurantEntry.COLUMN_CODEPOSTAL,
            RestaurantContract.RestaurantEntry.COLUMN_DESCRIPTION,
            RestaurantContract.RestaurantEntry.COLUMN_IMG_LIST,
            RestaurantContract.RestaurantEntry.COLUMN_IMAGE_FICHE,
            RestaurantContract.RestaurantEntry.COLUMN_LATITUDE,
            RestaurantContract.RestaurantEntry.COLUMN_LONGITUDE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            System.out.println("onCreate");
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps2);
            setUpMapIfNeeded();

            // Enabling MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);


            mUri = RestaurantContract.RestaurantEntry.CONTENT_URI;
            System.out.println("mUri" + mUri);

            // Invoke LoaderCallbacks to retrieve and draw already saved locations in map
            getSupportLoaderManager().initLoader(DETAIL_LOADER_MAP, null, this);
        }
        catch(Exception e)
        {
        }
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

    @Override
    public Loader<Cursor> onCreateLoader(int arg0,
                                         Bundle arg1) {
        System.out.println("Entre dans onCreateLoader ");

        // Uri to the content provider LocationsContentProvider
        Uri uri = mUri;// LocationsContentProvider.CONTENT_URI;

        // Fetches all the rows from locations table
        System.out.println("On sort onCreateLoader ");
        System.out.println("muri :  "+mUri);
        if (uri != null)
            return new CursorLoader(this, uri, RESTAURANT_COLUMNS, null, null, null);

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        int locationCount = 0;
        String nom = "";
        String description = "";
        Double latitude = 0.0;
        Double longitude = 0.0;

        //Affiche un cercle autour de la position actuelle
        Location currentLocation = mMap.getMyLocation();
        if (currentLocation != null) {
            Double currentLatitude = currentLocation.getLatitude();
            Double currentLongitude = currentLocation.getLongitude();
            if (currentLatitude != null && currentLongitude != null)
                ShowCircleAtPosition(new LatLng(currentLatitude, currentLongitude));
        }

        if (cursor == null)
            return;

        // Number of locations available in the SQLite database table
        locationCount = cursor.getCount();

        // Move the current record pointer to the first row of the table
        cursor.moveToFirst();

        for(int i=0;i<locationCount;i++)
        {
            //Initialisation des variables
            nom = cursor.getString(IndexBDRestaurant.INDEX_NOM);
            description = cursor.getString(IndexBDRestaurant.INDEX_DESCRIPTION);
            latitude = cursor.getDouble(IndexBDRestaurant.INDEX_LATITUDE);
            longitude = cursor.getDouble(IndexBDRestaurant.INDEX_LONGITUDE);

            // Drawing the marker in the Google Maps
            ShowMarkerAtPosition(new LatLng(latitude, longitude), nom, description);

            // Traverse the pointer to the next row
            cursor.moveToNext();
        }

        if(locationCount>0){
            // Moving CameraPosition to last clicked position
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));

            // Setting the zoom level in the map on last position  is clicked
            mMap.animateCamera(CameraUpdateFactory.zoomTo(0));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
