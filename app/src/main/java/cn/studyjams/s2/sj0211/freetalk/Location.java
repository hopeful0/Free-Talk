package cn.studyjams.s2.sj0211.freetalk;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hopeful on 17-6-2.
 */
public class Location {

    public double longitude;

    public double latitude;

    public Location(DataSnapshot dataSnapshot) {
        longitude = dataSnapshot.child("longitude").getValue(Double.class);
        latitude = dataSnapshot.child("latitude").getValue(Double.class);
    }

    private Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static void getLocation(final Activity activity, final OnLocationFinished callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 321);
                    callback.onFinished(null);
                } else {
                        Snackbar.make(activity.findViewById(R.id.button), "定位权限被禁用！",Snackbar.LENGTH_INDEFINITE).setAction("开启", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                                activity.startActivity(intent);
                            }
                        }).setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                System.out.println("a");
                                callback.onFinished(null);
                                super.onDismissed(snackbar, event);
                            }
                        }).show();
                }
                return;
            }
        }
        final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        String provider;
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
//        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
//            provider = LocationManager.GPS_PROVIDER;
        } else {
            callback.onFinished(null);
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
//                if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    callback.onFinished(null);
                    return;
                }
                Location l = new Location(location.getLongitude(), location.getLatitude());
                callback.onFinished(l);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }

    public static interface OnLocationFinished {
        public abstract void onFinished(Location location);
    }

    public static HashMap<String, Object> toValue(double longitude, double latitude) {
        HashMap<String, Object> value = new HashMap<>(2);
        value.put("longitude", longitude);
        value.put("latitude", latitude);
        return value;
    }

    public HashMap<String, Object> toValue() {
        HashMap<String, Object> value = new HashMap<>(2);
        value.put("longitude", longitude);
        value.put("latitude", latitude);
        return value;
    }

    public  double getDistance(Location other) {
        return getDistance(this, other);
    }

    public static double getDistance(Location l1, Location l2) {
        return getDistance(l1.latitude,l1.longitude,l2.latitude,l2.longitude);
    }

    private static final double EARTH_RADIUS = 6378.137;//地球半径,单位千米
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    private static double getDistance(double lat1,double lng1,double lat2,double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        return s;

    }
}
