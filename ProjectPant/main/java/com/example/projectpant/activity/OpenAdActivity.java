package com.example.projectpant.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectpant.R;
import com.example.projectpant.util.LocationUtils;
import com.example.projectpant.model.PantAd;
import com.example.projectpant.model.PantAdList;

// activity for opening a ad
public class OpenAdActivity extends AppCompatActivity {

    private TextView address;
    private TextView phoneNr;
    private TextView nrOfCans;
    private ImageView picture;
    private String phoneNrString;
    private LocationUtils locationUtils;
    private PantAd pantAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_ad);

        address = findViewById(R.id.address_text);
        phoneNr = findViewById(R.id.phoneNr_text);
        nrOfCans = findViewById(R.id.nrOfCans_text);
        picture = findViewById(R.id.adImageView);
        locationUtils = new LocationUtils(this);
        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        pantAd = PantAdList.getAdById(id);
        if (pantAd != null){
            if (pantAd.getImage() != null)
                picture.setImageDrawable(pantAd.getImage());
            else
                picture.setImageDrawable(getResources().getDrawable(R.drawable.no_image));

            String addr = locationUtils.getAddress(pantAd.getLatitude(), pantAd.getLongitude());
            address.setText(addr);
            phoneNr.setText(String.format("%s %s", getString(R.string.phone_number_text), pantAd.getPhoneNr()));
            nrOfCans.setText(String.format("%s %s", getString(R.string.nr_of_cans_text), pantAd.getNrOfCans()));
            phoneNrString = pantAd.getPhoneNr();
        }
    }

    // sends intent to the phone to make a call
    public void callPhoneNumber(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNrString));
        startActivity(intent);
    }

    // Opens a google maps path to the ads location
    public void findLocation(View view) {

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+pantAd.getLatitude()+","+pantAd.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
