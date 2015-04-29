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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.sunshine.app.data.RestaurantContract.RestaurantEntry;

/**
 * Manages a local database for weather data.
 */
public class RestaurantDataBaseHelper extends SQLiteOpenHelper {

    // Pour mettre a jour la base de donnee, on change la version de la bdd
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "restaurant.db";

    public RestaurantDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("on cree la base de donnee en effectuant la requete :\n");

        final String SQL_CREATE_RESTAURANT_TABLE = "CREATE TABLE " + RestaurantEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                RestaurantEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // le nom du restaurant
                RestaurantEntry.COLUMN_NAME+ " VARCHAR2(512) NOT NULL, " +
                RestaurantEntry.COLUMN_DESCRIPTION + " VARCHAR2(512)  ," +
                RestaurantEntry.COLUMN_ADRESSE+ " VARCHAR2(512) NOT NULL," +
                RestaurantEntry.COLUMN_VILLE + " VARCHAR2(128) NOT NULL, " +
                RestaurantEntry.COLUMN_CODEPOSTAL + " INTEGER NOT NULL, " +
//                RestaurantEntry.COLUMN_TYPE_COMMU + " VARCHAR2(128) , " +
//                RestaurantEntry.COLUMN_TYPE_COMMU + " VARCHAR2(128) , " +
                RestaurantEntry.COLUMN_IMG_LIST + " BLOB ," +
                RestaurantEntry.COLUMN_IMAGE_FICHE + " BLOB , " +
                RestaurantEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                RestaurantEntry.COLUMN_LONGITUDE + " REAL NOT NULL " +
                ");";

        System.out.println(SQL_CREATE_RESTAURANT_TABLE);
        //la requete
        ////      CREATE TABLE Restaurant
        //        (
        //                id INTEGER PRIMARY KEY AUTOINCREMENT,
        //        nom VARCHAR2(128) NOT NULL,
        //        adresse VARCHAR2(128) NOT NULL,
        //        ville VARCHAR2(128) NOT NULL,
        //        code_Postal INTEGER NOT NULL,
        //        description VARCHAR2(3000) ,
        //        latitude REAL NOT NULL,
        //        longitude REAL NOT NULL,
        //        );



//        on execute la requete sql de creation de la table
        sqLiteDatabase.execSQL(SQL_CREATE_RESTAURANT_TABLE);
    }

    /**
     * Dans le cas ou on upgrade, on efface la base de donnee et on upgrade
     * @param sqLiteDatabase
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        System.out.println("on est dans onUpgrade");
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RestaurantEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        System.out.println("on sort de onUpgrade");
    }
}
