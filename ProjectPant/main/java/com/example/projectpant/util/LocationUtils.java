package com.example.projectpant.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projectpant.callbackInterface.MapsCallbackInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Code from google documentation is used in this class.
// Class used for handling location-based operations.
public class LocationUtils {

    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;

    public LocationUtils(Context context){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.context = context;
    }

    // gets the latitude and longitude for the device.
    public void getDeviceLocation(MapsCallbackInterface mapsCallbackInterface) {
        try {
            if (!locationPermissionGranted)
                getLocationPermission();
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener((Activity) context, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mapsCallbackInterface.onSuccessfulAddress(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                            }
                        } else {
                            Log.e("LocationUtils", "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions((Activity)context,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // returns an address name for a given set of coordinates.
    public String getAddress(double lat, double lon) {
        Geocoder geocoder;
        List<Address> addressList = new ArrayList<>();
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressList.get(0).getThoroughfare() +" "+ addressList.get(0).getFeatureName();
    }
}
