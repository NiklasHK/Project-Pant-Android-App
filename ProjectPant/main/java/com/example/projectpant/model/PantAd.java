package com.example.projectpant.model;

import android.graphics.drawable.BitmapDrawable;

// Model for a ad object
public class PantAd {

    private String nrOfCans;
    private BitmapDrawable image;
    private String phoneNr;
    private String id;
    private double longitude;
    private double latitude;

    public String getPhoneNr() {
        return phoneNr;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
    }

    public String getNrOfCans() {
        return nrOfCans;
    }

    public void setNrOfCans(String nrOfCans) {
        this.nrOfCans = nrOfCans;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BitmapDrawable getImage() {
        return image;
    }

    public void setImage(BitmapDrawable image) {
        this.image = image;
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
}
