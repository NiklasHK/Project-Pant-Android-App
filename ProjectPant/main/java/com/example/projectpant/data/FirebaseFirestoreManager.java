package com.example.projectpant.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.projectpant.callbackInterface.DeleteCallbackInterface;
import com.example.projectpant.callbackInterface.FirebaseCallbackInterface;
import com.example.projectpant.model.DataParser;
import com.example.projectpant.model.DropDownOptions;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Code from firebase documentation is used in this class
// Handles all access to the firebase firestore.
public class FirebaseFirestoreManager {

    // Deletes a post based on id
    public static void deletePostById(String id, DeleteCallbackInterface callbackInterface){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("firestore manager", "DocumentSnapshot successfully deleted!");
                        callbackInterface.onSuccessfulDeleteCallback();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("firestore manager", "Error deleting document", e);
                    }
                });

    }

    // Retrieves phone number for user based on id
    public static void getPhoneNrByUUID(String uuid, FirebaseCallbackInterface callbackInterface){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("UUID", uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() > 0)
                                callbackInterface.onGetPhoneNrSuccess(task.getResult().getDocuments().get(0).getString("phoneNr"));
                        } else {
                            Log.w("firestore manager", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    // Create a new user and add them to to the firestore.
    public static void postUser(String phoneNr, String uuid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("phoneNr", phoneNr);
        user.put("UUID", uuid);

        db.collection("users").whereEqualTo("UUID", uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() > 0) {
                                String tmpUUID = task.getResult().getDocuments().get(0).getId();
                                db.collection("users").document(tmpUUID).set(user);
                            } else{
                                db.collection("users").add(user);
                            }
                        } else {
                            Log.w("firestore manager", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    // add a new post to the firestore.
    public static void postData(String nrOfCans, String phoneNr,double lat, double lon, String hash, String userID, FirebaseCallbackInterface callbackInterface){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> post = new HashMap<>();
        int NumberOfCans = Integer.parseInt(nrOfCans);
        post.put("nrOfCans", NumberOfCans);
        post.put("phoneNr", phoneNr);
        post.put("latitude", lat);
        post.put("longitude", lon);
        post.put("geohash", hash);
        post.put("userID", userID);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("firestore manager", "DocumentSnapshot added with ID: " + documentReference.getId());
                        callbackInterface.onPostDataSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("firestore manager", "Error adding document", e);
                        callbackInterface.onPostDataFailure();
                    }
                });
    }

    // Retrieves posts made by a specific user based on id.
    public static void getPostsForUser(String userID, DataParser dataParser){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts").whereEqualTo("userID", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> list = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                //Log.d("firestore manager", document.getId() + " => " + document.getData());
                                list.add(document);
                            }
                            dataParser.onSuccessCallback(list);
                        } else {
                            Log.w("firestore manager", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    // retrieves post in given area
    public static void getData(DropDownOptions sortOption, double latitude, double longitude, DataParser dataParser){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final double sortOptRadius;
        if (sortOption == DropDownOptions.ONEKM)
            sortOptRadius = 1000;
        else if (sortOption == DropDownOptions.FIVEKM)
            sortOptRadius = 5000;
        else
            sortOptRadius = 10000;

        final GeoLocation center = new GeoLocation(latitude, longitude);
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, sortOptRadius);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("posts")
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("latitude");
                                double lng = doc.getDouble("longitude");
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= sortOptRadius) {
                                    matchingDocs.add(doc);
                                }
                            }
                        }
                        dataParser.onSuccessCallback(matchingDocs);
                    }
                });
    }
}
