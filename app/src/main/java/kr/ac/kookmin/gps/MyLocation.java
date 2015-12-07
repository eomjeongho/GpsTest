package kr.ac.kookmin.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MyLocation {

    Timer timer;
    LocationManager locationManager;
    LocationResult locationResult;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    String provide = null;

    public boolean getLocation(Context context, LocationResult result){

        locationResult = result;

        if(locationManager == null){
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex) {
        }
        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception ex){
        }

        if(!gps_enabled && !network_enabled)
            return false;

        if(gps_enabled){
            try{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListenerGps);
            }catch (SecurityException e){
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

            provide = "gps";
        }

        if(network_enabled){
            try{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListenerNetwork);
            }catch (SecurityException e){
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

            provide = "network";
        }

        timer = new Timer();
        timer.schedule(new GetLastLocation(), 20000);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);

            try{
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
            }catch (SecurityException e){
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);

            try{
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
            }catch (SecurityException e){
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run(){
            try {
                locationManager.removeUpdates(locationListenerGps);
                locationManager.removeUpdates(locationListenerNetwork);
            }catch (SecurityException e){
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

            Location net_loc = null, gps_loc = null;

            try{
                if(gps_enabled)
                    gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(network_enabled)
                    net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }catch (SecurityException e){
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

            if (gps_loc != null){
                locationResult.gotLocation(gps_loc);
                return;
            }
            if (net_loc != null){
                locationResult.gotLocation(net_loc);
                return;
            }
            locationResult.gotLocation(null);
        }

    }

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }
}
