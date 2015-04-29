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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.IndexBDRestaurant;
import com.example.android.sunshine.app.data.RestaurantContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

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
            RestaurantContract.RestaurantEntry.COLUMN_IMAGE_FICHE,
            RestaurantContract.RestaurantEntry.COLUMN_LATITUDE,
            RestaurantContract.RestaurantEntry.COLUMN_LONGITUDE,
            RestaurantContract.RestaurantEntry.COLUMN_TELEPHONE


    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
//    public static final int COL_WEATHER_ID = 0;
//    public static final int COL_WEATHER_DATE = 1;
//    public static final int COL_WEATHER_DESC = 2;
//    public static final int COL_WEATHER_MAX_TEMP = 3;
//    public static final int COL_WEATHER_MIN_TEMP = 4;
//    public static final int COL_WEATHER_HUMIDITY = 5;
//    public static final int COL_WEATHER_PRESSURE = 6;
//    public static final int COL_WEATHER_WIND_SPEED = 7;
//    public static final int COL_WEATHER_DEGREES = 8;
//    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView  mAdresseView, mVilleView, moyenCommunicationView ,mFriendlyDateView;
    private TextView mDateView,mCodeView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private TextView mNomRestaurantView,mTelephoneView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        setRetainInstance(true);
        System.out.println("On est dans onCreateView de DetailFragment");
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            System.out.println("affichage mUri : "+mUri );
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        System.out.println("ma vue principal : " +rootView);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
//        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mNomRestaurantView = (TextView) rootView.findViewById(R.id.detail_nomRestaurant);
        System.out.println("onCreateView --- mNomRestaurantView : " + mNomRestaurantView);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_descriptionRestaurant_textview);
        mAdresseView = (TextView) rootView.findViewById(R.id.detail_adresse_textview);
        mVilleView = (TextView) rootView.findViewById(R.id.detail_ville_textview);
        mCodeView = (TextView) rootView.findViewById(R.id.detail_codeP_textview);
        mTelephoneView = (TextView) rootView.findViewById(R.id.detail_tel_textview);
//        this.setRetainInstance(true);
        System.out.println("On sort de onCreateView de DetailFragment");
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
//            long date = RestaurantContract.RestaurantEntry.getDateFromUri(uri);
//            Uri updatedUri = RestaurantEntry.buildWeatherLocationWithDate(newLocation, date);
//            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        System.out.println("On est dans onCreateLoader");
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    RESTAURANT_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        System.out.println("On sort de onCreateLoader");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        System.out.println("On est dans onLoadFinished ");
        String id;
        id = mUri.getLastPathSegment();
        System.out.println("Le getlastPast : "+Integer.valueOf(id));
        if (cursor != null && cursor.moveToPosition(Integer.valueOf(id)))
        {
            System.out.println("On est dans le si !");
            System.out.println("Le nom de la colonne est de "+cursor.getColumnName(IndexBDRestaurant.INDEX_RESTAURANT_ID));
            System.out.println("Le nb de colonne est de "+cursor.getColumnCount());
            // On recupere l'id du restaurant
            int restaurantId = cursor.getInt(IndexBDRestaurant.INDEX_RESTAURANT_ID);
            System.out.println("On a recuperer l'indice du restaurant qui est "+restaurantId+" !");

            // Use weather art image
//            mIconView.setImageResource(Utility.getImageRestaurant(restaurantId));
            System.out.println("on recupere l'image");
            //on recupere l'image dans la bdd et on l'applique a iconView
            byte[] image = cursor.getBlob(IndexBDRestaurant.INDEX_IMAGE_FICHE);
            Bitmap imageBitmap = null;
            if (image != null) {
                imageBitmap = Utility.getImage(image);

            }
            mIconView.setImageBitmap(imageBitmap);
            System.out.println("on a affecte l'image");

//            // Read date from cursor and update views for day of week and date
//            long date = cursor.getLong(COL_WEATHER_DATE);
//            String friendlyDateText = Utility.getDayName(getActivity(), date);
//            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
//            mFriendlyDateView.setText(friendlyDateText);
//            mDateView.setText(dateText);

            String nomRestaurant = cursor.getString(IndexBDRestaurant.INDEX_NOM);
            System.out.println("on a recupere le nom du restaurant qui est : "+nomRestaurant + " et mNomRestaurantView vaut "+mNomRestaurantView );
            System.out.println("mDescriptionView vaut "+mDescriptionView );
            System.out.println("mAdresseView vaut "+mAdresseView );
            System.out.println("mVilleView vaut "+mVilleView );
            System.out.println("mCodeView vaut "+mCodeView );
            mNomRestaurantView.setText(nomRestaurant);
            System.out.println("============1111111===========");
            // on lit la description et on met a jour la vue
            String description = cursor.getString(IndexBDRestaurant.INDEX_DESCRIPTION);
            mDescriptionView.setText(description);
            System.out.println("============22222222===========");

            mAdresseView.setText("Adresse : " +cursor.getString(IndexBDRestaurant.INDEX_ADRESSE));
            System.out.println("============3333333===========");
            mVilleView.setText("Ville : " + cursor.getString(IndexBDRestaurant.INDEX_VILLE));
            System.out.println("============4444444===========");

            mCodeView.setText("Code postal : " + cursor.getString(IndexBDRestaurant.INDEX_CODEPOSTAL));
            mCodeView.setText("Telephone : " + cursor.getString(IndexBDRestaurant.INDEX_TEL));
            System.out.println("============555555===========");

            // For accessibility, add a content description to the icon field
            mIconView.setContentDescription(description);
            System.out.println("============6666666===========");



            // We still need this for the share intent
            mForecast = String.format("%s - %s", nomRestaurant, description);
            System.out.println("============77777777===========");

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                System.out.println("dans le if");
                mShareActionProvider.setShareIntent(createShareForecastIntent());
                System.out.println("on sort du if");

            }
        }
        System.out.println("On sort onLoadFinished ");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}