package com.example.projectpant.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectpant.R;
import com.example.projectpant.adapter.ItemAdapter;
import com.example.projectpant.callbackInterface.MapsCallbackInterface;
import com.example.projectpant.callbackInterface.ReadCallbackInterface;
import com.example.projectpant.model.DataParser;
import com.example.projectpant.model.DropDownOptions;
import com.example.projectpant.util.LocationUtils;
import com.google.firebase.auth.FirebaseAuth;
//The main page of the app, lists all ads within selected area.
public class ListActivity extends AppCompatActivity implements ReadCallbackInterface, AdapterView.OnItemSelectedListener, MapsCallbackInterface {

    private ItemAdapter itemAdapter;
    private ImageView imageView;
    private Menu myMenu;
    private Spinner sortDropdown;
    private DropDownOptions chosenSortOption;
    private LocationUtils locationUtils;
    private DataParser dataParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        itemAdapter = new ItemAdapter(this);
        recyclerView.setAdapter(itemAdapter);

        imageView = new ImageView(this);
        sortDropdown = findViewById(R.id.spinner1);
        initSpinner();
        locationUtils = new LocationUtils(this);
        dataParser = new DataParser(this, this);
    }
    //dropdown menu for area
    private void initSpinner(){
        String[] items = new String[]{"1 Km", "5 Km", "10 Km"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        sortDropdown.setAdapter(adapter);
        sortDropdown.setOnItemSelectedListener(this);
        chosenSortOption = DropDownOptions.ONEKM;
    }
    //updates list based on new location
    @Override
    protected void onResume() {
        Log.i("Account", "ON RESUME");
        super.onResume();
        locationUtils.getDeviceLocation(this);
    }
    //Sends intent for creating a new ad
    public void createAd(View view) {
        Intent intent;
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            intent = new Intent(this, LoginActivity.class);
        else
            intent = new Intent(this, NewAdActivity.class);
        startActivity(intent);
    }

    //Updates the ui when the list has confirmed changes
    @Override
    public void onSuccessfulReadCallback() {
        itemAdapter.notifyDataSetChanged();
    }

    // Updates menu icon based on if the user is logged in
    @SuppressLint("UseCompatLoadingForDrawables")
    public void updateMenuIcon(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.i("Account", "SIGNING IN");
            myMenu.getItem(0).setIcon(getResources()
                    .getDrawable(R.drawable.account_icon));
        } else {
            Log.i("Account", "SIGNING OUT");
            myMenu.getItem(0).setIcon(getResources()
                    .getDrawable(R.drawable.login_button));
        }
    }

    //  create an action bar button for the account
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("Account", "MENU ONCREATE");
        getMenuInflater().inflate(R.menu.mymenu, menu);
        myMenu = menu;
        updateMenuIcon();
        return super.onCreateOptionsMenu(menu);
    }

    // Access account activity on click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.account_button) {
            Intent intent;
            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // updates the radius to retrieve ads within
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("DropdownMenu", "changed option");
        switch (position) {
            case 0:
                chosenSortOption = DropDownOptions.ONEKM;
                break;
            case 1:
                chosenSortOption = DropDownOptions.FIVEKM;
                break;
            case 2:
                chosenSortOption = DropDownOptions.TENKM;
                break;
        }
        locationUtils.getDeviceLocation(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // retrieves ads when user location is successfully found.
    @Override
    public void onSuccessfulAddress(double lat, double lon) {
        dataParser.getAdsFromDB(chosenSortOption, lat, lon);
        if (myMenu != null)
            updateMenuIcon();
    }
}