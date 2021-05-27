package com.example.projectpant.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectpant.R;
import com.example.projectpant.data.FirebaseCloudStorageManager;
import com.example.projectpant.data.FirebaseFirestoreManager;
import com.example.projectpant.callbackInterface.DeleteCallbackInterface;
import com.example.projectpant.util.LocationUtils;
import com.example.projectpant.util.MessageUtils;
import com.example.projectpant.model.PantAd;
import com.example.projectpant.model.PantAdList;

import java.util.List;

//code from Anders Lindström is used in this class
//Class that handles the recyclerView in the login activity.
public class ItemAdapterPrivate extends RecyclerView.Adapter<ItemAdapterPrivate.ViewHolder> {

    private List<PantAd> pantAdList = PantAdList.getinstance();
    private Context context;
    private View itemView;
    private LocationUtils locationUtils;
    private DeleteCallbackInterface callbackInterface;

    public ItemAdapterPrivate(Context context, DeleteCallbackInterface callbackInterface){
        super();
        this.context = context;
        locationUtils = new LocationUtils(context);
        this.callbackInterface = callbackInterface;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView addressView;
        public TextView nrOfCansView;
        public ImageView adImageView;

        public ViewHolder(View v) {
            super(v);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ItemAdapterPrivate.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new item view
        itemView = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pant_item_private, parent, false);
        final ViewHolder vh = new ViewHolder(itemView);
        vh.addressView = itemView.findViewById(R.id.address_text);
        vh.nrOfCansView = itemView.findViewById(R.id.nrOfCans_text);
        vh.adImageView = itemView.findViewById(R.id.testImageView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        PantAd pantAd = pantAdList.get(position);
        String addr = locationUtils.getAddress(pantAd.getLatitude(), pantAd.getLongitude());
        vh.addressView.setText(addr);
        vh.nrOfCansView.setText(String.format("Antal burkar: ~ %s", pantAd.getNrOfCans()));
        if (pantAd.getImage()!= null)
            vh.adImageView.setImageDrawable(pantAd.getImage());
        else {
            vh.adImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.no_image));
        }

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageUtils.createMsgDialog("Radera annons?", "", context, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = pantAdList.get(position).getId();
                        FirebaseFirestoreManager.deletePostById(id, callbackInterface);
                        FirebaseCloudStorageManager.deleteImage(id);
                    }
                }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return pantAdList.size();
    }

}
