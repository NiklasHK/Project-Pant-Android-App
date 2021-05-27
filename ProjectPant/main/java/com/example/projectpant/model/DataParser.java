package com.example.projectpant.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.example.projectpant.callbackInterface.ReadCallbackInterface;
import com.example.projectpant.data.FirebaseCloudStorageManager;
import com.example.projectpant.data.FirebaseFirestoreManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

// class for parsing database data into model objects
public class DataParser{
    private ReadCallbackInterface callbackInterface;
    private Context context;

    public DataParser (ReadCallbackInterface cbInterface, Context ctx){
        callbackInterface = cbInterface;
        context = ctx;
    }

    //Gets ads from db for a user
    public void getAdsForUser(String userID){
        FirebaseFirestoreManager.getPostsForUser(userID, this);
    }

    // gets ads based on area
    public void getAdsFromDB(DropDownOptions sortOption, double latitude, double longitude){
        FirebaseFirestoreManager.getData(sortOption, latitude, longitude, this);
    }

    // Parses data to model object when read is successful.
    public void onSuccessCallback(List<DocumentSnapshot> dataList) {
        List<PantAd> adList =PantAdList.getinstance();
        adList.clear();
        for (DocumentSnapshot document : dataList) {
            Log.d("firestore manager", document.getId() + " => " + document.getData());
            PantAd ad = new PantAd();
            ad.setId(document.getId());
            ad.setLatitude((double)document.get("latitude"));
            ad.setLongitude((double)document.get("longitude"));
            ad.setPhoneNr((String) document.get("phoneNr"));
            String nrCans = Long.toString((Long) document.get("nrOfCans"));
            ad.setNrOfCans(nrCans);
            adList.add(ad);
            FirebaseCloudStorageManager.getImageById(document.getId(), this);
        }
        callbackInterface.onSuccessfulReadCallback();
    }

    // associates image to correct post object.
    public void onSuccessfulImageCallback(byte[] byteArray, String Id){
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        PantAd ad = PantAdList.getAdById(Id);
        BitmapDrawable d = new BitmapDrawable(context.getResources(), bmp);
        ad.setImage(d);
        callbackInterface.onSuccessfulReadCallback();
    }
}
