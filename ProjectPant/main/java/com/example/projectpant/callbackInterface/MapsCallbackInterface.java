package com.example.projectpant.callbackInterface;

// callback interface for handling accessing the users location via gps.
public interface MapsCallbackInterface {
    void onSuccessfulAddress(double lat, double lon);
}
