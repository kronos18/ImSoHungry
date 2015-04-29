package com.example.android.sunshine.app.sync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    private LocationManager locationManager;
    private LocalisationGPSListener localisationGPSListener;
    private int temps = 5000; // milliseconds
    private int distance = 10; // meters
    private Location derniereLocation;


    @Override
    public void onCreate()
    {

        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock)
        {
//            si on n'a pas de sync adapter, on en cree un
            if (sSyncAdapter == null)
            {
//                recuperation du manager de localisation
                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                //*************ecouteur ou listener*********************
                localisationGPSListener = new LocalisationGPSListener();

                this.derniereLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                //on met a jour toute les 5 secondes et qu'on est bouge de 10m avant de recuperer les coordonnees a nouveau
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        temps,
                        distance,
                        localisationGPSListener);

//                quand on le demande, on recupere la derniere localisation et on met a jour
                System.out.println("Derniere localisation connue :" +derniereLocation.toString());
//                on met a jour la derniere localisation
                SyncAdapter.miseAjourPositionCourante(derniereLocation);

                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }



    private Location miseEnPlaceGeolocalisation() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //*************ecouteur ou listener*********************
        localisationGPSListener = new LocalisationGPSListener();

        Location derniereLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //on met a jour toute les 5 secondes et qu'on est bouge de 10m avant de recuperer les coordonnees a nouveau
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                temps,
                distance,
                localisationGPSListener);

        return derniereLocation;

    }
}