/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.android.sunshine.app.data.RestaurantContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class RestaurantFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = RestaurantFragment.class.getSimpleName();
    private RestaurantAdaptateur restaurantAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final String SELECTED_KEY = "selected_position";

    private static final int RESTAURANT_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.

    public static final String[] RESTAURANT_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            RestaurantContract.RestaurantEntry.TABLE_NAME + "." + RestaurantContract.RestaurantEntry._ID,
            RestaurantContract.RestaurantEntry.COLUMN_NAME,
            RestaurantContract.RestaurantEntry.COLUMN_ADRESSE,
            RestaurantContract.RestaurantEntry.COLUMN_VILLE,
            RestaurantContract.RestaurantEntry.COLUMN_CODEPOSTAL,
            RestaurantContract.RestaurantEntry.COLUMN_DESCRIPTION,
            RestaurantContract.RestaurantEntry.COLUMN_IMG_LIST,
            RestaurantContract.RestaurantEntry.COLUMN_IMAGE_FICHE
//            RestaurantContract.RestaurantEntry.COLUMN_LATITUDE,
//            RestaurantContract.RestaurantEntry.COLUMN_LONGITUDE
    };



    // on inscrit l'indice de chaque colonne pour la bdd
    static final int COL_RESTAURANT_ID = 0;
    static final int COL_RESTAURANT_NAME = 1;
    static final int COL_RESTAURANT_DESCRIPTION= 2;
    static final int COL_RESTAURANT_IMG_LIST = 3;
    //    static final int COL_WEATHER_MIN_TEMP = 4;
//    static final int COL_LOCATION_SETTING = 5;
//    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 3;
    static final int COL_COORD_LONG = 4;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public RestaurantFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            updateWeather();
//            return true;
//        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("On est dans onCreateView");
        // The RestaurantAdaptateur will take data from a source and
        // use it to populate the ListView it's attached to.
//        restaurantAdapter = new RestaurantAdaptateur(getActivity(), null, 0);
        restaurantAdapter = new RestaurantAdaptateur(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_restaurant);

//        on rattache l'adaptateur a celui des restaurants
        mListView.setAdapter(restaurantAdapter);

        // We'll call our MainActivity
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    ((Callback) getActivity())
//                            .onItemSelected(RestaurantContract.RestaurantEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                }
//                mPosition = position;
//            }
//        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        System.out.println("-----1-----");
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        System.out.println("-----2-----");

//        restaurantAdapter.setUseTodayLayout(mUseTodayLayout);
        System.out.println("-----3-----");

        System.out.println("On sort de onCreateView");
        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("On est dans onActivityCreated");
        getLoaderManager().initLoader(RESTAURANT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        System.out.println("On sort de onActivityCreated");
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(RESTAURANT_LOADER, null, this);
    }

    private void updateWeather()
    {
        System.out.println("On est dans updateWeather");
        SunshineSyncAdapter.syncImmediately(getActivity());
        System.out.println("On sort updateWeather");
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != restaurantAdapter) {
            Cursor c = restaurantAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);

                //Détermination des latitudes et longitudes
                String posLat = null; //c.getString(COL_COORD_LAT);
                String posLong =null; // c.getString(COL_COORD_LONG);

                //Création de l'objet de "géoLocalisation", qui determinera la position dans l'activity MAP
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        System.out.println("On est dans onCreateLoader");
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by id.
        String sortOrder = RestaurantContract.RestaurantEntry._ID + " ASC";

        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri restaurantUri = RestaurantContract.RestaurantEntry.CONTENT_URI;
        System.out.println("l'uri est : "+restaurantUri);


        return new CursorLoader(getActivity(),
                restaurantUri,
                RESTAURANT_COLUMNS,
                null,
                null,
                sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        restaurantAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        restaurantAdapter.swapCursor(null);
    }

}