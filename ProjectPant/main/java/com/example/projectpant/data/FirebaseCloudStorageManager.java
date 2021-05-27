package com.example.projectpant.data;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.projectpant.callbackInterface.FirebaseCallbackInterface;
import com.example.projectpant.model.DataParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

// Code from firebase documentation is used in this class
// Handles all access to the firebase cloud storage.
public class FirebaseCloudStorageManager {

    // Delete an image from the cloud storage
    public static void deleteImage(String id){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference desertRef = storageRef.child("images/"+id+".jpg");

        desertRef.delete();
    }

    // upload an image to the cloud storage
    // call the appropriate callback method based on outcome.
    public static void postImage(ImageView imageView, String idRef, FirebaseCallbackInterface callbackInterface){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference postImageRef = storageRef.child("images/"+idRef+".jpg");

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();

        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = postImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callbackInterface.onPostImageFailure();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callbackInterface.onPostImageSuccess();
            }
        });
    }

    // Retrieve image from the cloud storage, based on unique id
    public static void getImageById(String id, DataParser dataParser){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child("images/"+id+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                dataParser.onSuccessfulImageCallback(bytes, id);
            }
        });
    }
}
