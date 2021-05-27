package com.example.projectpant.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectpant.R;
import com.example.projectpant.callbackInterface.FirebaseCallbackInterface;
import com.example.projectpant.data.FirebaseCloudStorageManager;
import com.example.projectpant.data.FirebaseFirestoreManager;
import com.example.projectpant.callbackInterface.MapsCallbackInterface;
import com.example.projectpant.util.LocationUtils;
import com.example.projectpant.util.MessageUtils;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;

// class for creating a new ad
public class NewAdActivity extends AppCompatActivity implements FirebaseCallbackInterface, MapsCallbackInterface {

    private ImageView adImage;
    private EditText nrOfCans;
    private EditText phoneNr;
    private TextView address;
    private boolean photoAdded;
    private boolean addressAdded;
    private LocationUtils locationUtils;
    private double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ad);

        adImage = findViewById(R.id.testImageView);
        nrOfCans = findViewById(R.id.nrOfCans_EditText);
        phoneNr = findViewById(R.id.phoneNr_EditText);
        address = findViewById(R.id.address_EditText);
        photoAdded = false;
        addressAdded = false;

        FirebaseFirestoreManager.getPhoneNrByUUID(FirebaseAuth.getInstance().getCurrentUser().getUid(), this);
        locationUtils = new LocationUtils(this);
        locationUtils.getDeviceLocation(this);
    }

    // create a new post and add the data to firebase firestore, and the image (if any) to firebase cloud storage.
    public void CreatePost(View view) {
        String nrCans = nrOfCans.getText().toString();
        String phone = phoneNr.getText().toString();
        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (validateInput()) {
            FirebaseFirestoreManager.postData(nrCans, phone, latitude, longitude, hash, userID,  this);
            FirebaseFirestoreManager.postUser(phone, FirebaseAuth.getInstance().getUid());
        }
    }

    // access camera to take and store photo
    public void AddPhoto(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 0);
    }

    // handles result when a photo is received from the camera activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK)
        {
             if(data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap cropImg = null;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    int crop = (photo.getHeight() - photo.getWidth()) / 2;
                    cropImg = Bitmap.createBitmap(photo, 0, crop, photo.getWidth(), photo.getWidth());
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    int crop = (photo.getWidth() - photo.getHeight()) / 2;
                    cropImg = Bitmap.createBitmap(photo, crop, 0, photo.getHeight(), photo.getHeight());
                }
                Drawable imageFromCamera = new BitmapDrawable(getResources(), cropImg);
                adImage.setScaleType(ImageView.ScaleType.FIT_XY);
                adImage.setImageDrawable(imageFromCamera);
                photoAdded = true;
            }
        }
    }

    // Adds photo to the cloud storage when data has successfully been added to the firestore
    @Override
    public void onPostDataSuccess(String refId){
        if (photoAdded){
            FirebaseCloudStorageManager.postImage(adImage, refId, this);
        } else{
            resetUI();
            MessageUtils.showToast(this, "Inlägg skapat: utan bild");
        }
    }

    // failed upload of data
    @Override
    public void onPostDataFailure() {
        MessageUtils.showToast(this, "Misslyckad uppladdning.");
    }

    // successful image upload, resets ui
    @Override
    public void onPostImageSuccess() {
        resetUI();
        MessageUtils.showToast(this, "Inlägg skapat: med bild.");
    }

    // failed image upload
    @Override
    public void onPostImageFailure() {
        MessageUtils.showToast(this, "Inlägg skapat: Bild kunde ej laddas upp.");
    }

    // updates phone number field
    @Override
    public void onGetPhoneNrSuccess(String phoneNumber) {
        phoneNr.setText(phoneNumber);
    }

    private boolean validateInput(){
        boolean valid = true;
        if (nrOfCans.getText().toString() == null || nrOfCans.getText().toString().equals("")) {
            nrOfCans.setError("Fält måste fyllas i");
            valid = false;
        }
        if (phoneNr.getText().toString() == null || phoneNr.getText().toString().equals("")) {
            phoneNr.setError("Fält måste fyllas i");
            valid = false;
        }
        if (!addressAdded) {
            address.setError("Kunde inte hämta address");
            valid = false;
        }
        return valid;
    }

    private void resetUI(){
        nrOfCans.getText().clear();
        adImage.setImageResource(R.drawable.add_photo_button);
        photoAdded = false;
    }

    // updates address when users location is received
    @Override
    public void onSuccessfulAddress(double lat, double lon) {
        String addr = locationUtils.getAddress(lat,lon);
        longitude = lon;
        latitude = lat;
        Log.i("NewAdActivity", addr);
        address.setText(addr);
        addressAdded = true;
    }
}
