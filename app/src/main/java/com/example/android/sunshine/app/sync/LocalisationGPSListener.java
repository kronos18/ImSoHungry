package com.example.android.sunshine.app.sync;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * Created by hicker on 12/04/15.
 */
public class LocalisationGPSListener implements LocationListener
{


    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    private int temps = 5000; // milliseconds
    private int distance = 5; // meters

    public LocalisationGPSListener()
    {
    }

    @Override
    public void onLocationChanged(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();
        System.out.println("la latitude : " + latitude + " et la longitude : " + longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String newStatus = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }




    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

}
