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
package com.example.android.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class RestaurantProvider extends ContentProvider
{


    private RestaurantDataBaseHelper baseDeDonnee;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    //
    static final int RESTAURANT = 100;
//    static final int WEATHER_WITH_LOCATION = 101;
//    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
//    static final int LOCATION = 300;

//  on surcharge la classe de creation de la bade de donnee
    public boolean onCreate() {
        baseDeDonnee = new RestaurantDataBaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
//        on cree un curseur sur notre base de donnee
        Cursor retCursor = baseDeDonnee.getReadableDatabase().query(
                RestaurantContract.RestaurantEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /*
    On implemente la fonction pour inserer dans la base de donnee
     */
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = baseDeDonnee.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        long _id = db.insert(RestaurantContract.RestaurantEntry.TABLE_NAME, null, values);
        if ( _id > 0 ) {
            returnUri = RestaurantContract.RestaurantEntry.buildRestaurantUri(_id);
        }
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //
        return 0;
    }

    public void close() {
        baseDeDonnee.close();
    }
//
//    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

//    static{
//        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
//
//        //This is an inner join which looks like
//        //weather INNER JOIN location ON weather.location_id = location._id
//        sWeatherByLocationSettingQueryBuilder.setTables(
//                RestaurantContract.RestaurantEntry.TABLE_NAME + " INNER JOIN " +
//                        RestaurantContract.LocationEntry.TABLE_NAME +
//                        " ON " + RestaurantContract.RestaurantEntry.TABLE_NAME +
//                        "." + RestaurantContract.RestaurantEntry.COLUMN_LOC_KEY +
//                        " = " + RestaurantContract.LocationEntry.TABLE_NAME +
//                        "." + RestaurantContract.LocationEntry._ID);
//    }
//
//    //location.location_setting = ?

//    //location.location_setting = ? AND date >= ?
//    private static final String sLocationSettingWithStartDateSelection =
//            RestaurantContract.LocationEntry.TABLE_NAME+
//                    "." + RestaurantContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
//                    RestaurantContract.RestaurantEntry.COLUMN_DATE + " >= ? ";
//
//    //location.location_setting = ? AND date = ?
//    private static final String sLocationSettingAndDaySelection =
//            RestaurantContract.LocationEntry.TABLE_NAME +
//                    "." + RestaurantContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
//                    RestaurantContract.RestaurantEntry.COLUMN_DATE + " = ? ";
//
//    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
//        String locationSetting = RestaurantContract.RestaurantEntry.getLocationSettingFromUri(uri);
//        long startDate = RestaurantContract.RestaurantEntry.getStartDateFromUri(uri);
//
//        String[] selectionArgs;
//        String selection;
//
//        if (startDate == 0) {
//            selection = sLocationSettingSelection;
//            selectionArgs = new String[]{locationSetting};
//        } else {
//            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
//            selection = sLocationSettingWithStartDateSelection;
//        }
//
//        return sWeatherByLocationSettingQueryBuilder.query(baseDeDonnee.getReadableDatabase(),
//                projection,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                sortOrder
//        );
//    }
//
//    private Cursor getWeatherByLocationSettingAndDate(
//            Uri uri, String[] projection, String sortOrder) {
//        String locationSetting = RestaurantContract.RestaurantEntry.getLocationSettingFromUri(uri);
//        long date = RestaurantContract.RestaurantEntry.getDateFromUri(uri);
//
//        return sWeatherByLocationSettingQueryBuilder.query(baseDeDonnee.getReadableDatabase(),
//                projection,
//                sLocationSettingAndDaySelection,
//                new String[]{locationSetting, Long.toString(date)},
//                null,
//                null,
//                sortOrder
//        );
//    }
//
//    /*
//        Students: Here is where you need to create the UriMatcher. This UriMatcher will
//        match each URI to the RESTAURANT, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
//        and LOCATION integer constants defined above.  You can test this by uncommenting the
//        testUriMatcher test within TestUriMatcher.
//     */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RestaurantContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RestaurantContract.PATH_RESTAURANT, RESTAURANT);

        return matcher;
    }
//
//    /*
//        Students: We've coded this for you.  We just create a new RestaurantDataBaseHelper for later use
//        here.
//     */
//    @Override
//    public boolean onCreate() {
//        baseDeDonnee = new RestaurantDataBaseHelper(getContext());
//        return true;
//    }
//
//    /*
//        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
//        test this by uncommenting testGetType in TestProvider.
//
//     */
//    @Override
//    public String getType(Uri uri) {
//
//        // Use the Uri Matcher to determine what kind of URI this is.
//        final int match = sUriMatcher.match(uri);
//
//        switch (match) {
//            // Student: Uncomment and fill out these two cases
//            case WEATHER_WITH_LOCATION_AND_DATE:
//                return RestaurantContract.RestaurantEntry.CONTENT_ITEM_TYPE;
//            case WEATHER_WITH_LOCATION:
//                return RestaurantContract.RestaurantEntry.CONTENT_TYPE;
//            case RESTAURANT:
//                return RestaurantContract.RestaurantEntry.CONTENT_TYPE;
//            case LOCATION:
//                return RestaurantContract.LocationEntry.CONTENT_TYPE;
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//    }
//
//    @Override
//    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
//                        String sortOrder) {
//        // Here's the switch statement that, given a URI, will determine what kind of request it is,
//        // and query the database accordingly.
//        Cursor retCursor;
//        switch (sUriMatcher.match(uri)) {
//            // "weather/*/*"
//            case WEATHER_WITH_LOCATION_AND_DATE:
//            {
//                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
//                break;
//            }
//            // "weather/*"
//            case WEATHER_WITH_LOCATION: {
//                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
//                break;
//            }
//            // "weather"
//            case RESTAURANT: {
//                retCursor = baseDeDonnee.getReadableDatabase().query(
//                        RestaurantContract.RestaurantEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
//                break;
//            }
//            // "location"
//            case LOCATION: {
//                retCursor = baseDeDonnee.getReadableDatabase().query(
//                        RestaurantContract.LocationEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
//                break;
//            }
//
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
//        return retCursor;
//    }
//
//    /*
//        Student: Add the ability to insert Locations to the implementation of this function.
//     */
//    @Override
//    public Uri insert(Uri uri, ContentValues values) {
//        final SQLiteDatabase db = baseDeDonnee.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//        Uri returnUri;
//
//        switch (match) {
//            case RESTAURANT: {
//                normalizeDate(values);
//                long _id = db.insert(RestaurantContract.RestaurantEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = RestaurantContract.RestaurantEntry.buildRestaurantUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
//                break;
//            }
//            case LOCATION: {
//                long _id = db.insert(RestaurantContract.LocationEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = RestaurantContract.LocationEntry.buildLocationUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
//                break;
//            }
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//        getContext().getContentResolver().notifyChange(uri, null);
//        return returnUri;
//    }
//
//    @Override
//    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        final SQLiteDatabase db = baseDeDonnee.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//        int rowsDeleted;
//        // this makes delete all rows return the number of rows deleted
//        if ( null == selection ) selection = "1";
//        switch (match) {
//            case RESTAURANT:
//                rowsDeleted = db.delete(
//                        RestaurantContract.RestaurantEntry.TABLE_NAME, selection, selectionArgs);
//                break;
//            case LOCATION:
//                rowsDeleted = db.delete(
//                        RestaurantContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
//                break;
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//        // Because a null deletes all rows
//        if (rowsDeleted != 0) {
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//        return rowsDeleted;
//    }
//
//    private void normalizeDate(ContentValues values) {
//        // normalize the date value
//        if (values.containsKey(RestaurantContract.RestaurantEntry.COLUMN_DATE)) {
//            long dateValue = values.getAsLong(RestaurantContract.RestaurantEntry.COLUMN_DATE);
//            values.put(RestaurantContract.RestaurantEntry.COLUMN_DATE, RestaurantContract.normalizeDate(dateValue));
//        }
//    }
//
//    @Override
//    public int update(
//            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        final SQLiteDatabase db = baseDeDonnee.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//        int rowsUpdated;
//
//        switch (match) {
//            case RESTAURANT:
//                normalizeDate(values);
//                rowsUpdated = db.update(RestaurantContract.RestaurantEntry.TABLE_NAME, values, selection,
//                        selectionArgs);
//                break;
//            case LOCATION:
//                rowsUpdated = db.update(RestaurantContract.LocationEntry.TABLE_NAME, values, selection,
//                        selectionArgs);
//                break;
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//        if (rowsUpdated != 0) {
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//        return rowsUpdated;
//    }
//
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        System.out.println("on est dans bulkInsert");
        final SQLiteDatabase db = baseDeDonnee.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RESTAURANT:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RestaurantContract.RestaurantEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
//
//    // You do not need to call this method. This is a method specifically to assist the testing
//    // framework in running smoothly. You can read more at:
//    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
//    @Override
//    @TargetApi(11)
//    public void shutdown() {
//        baseDeDonnee.close();
//        super.shutdown();
//    }
}