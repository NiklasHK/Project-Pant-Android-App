package com.example.projectpant.callbackInterface;

// callback interface for handling access with the firebase storage for data and images.
public interface FirebaseCallbackInterface {

    void onPostDataSuccess(String refId);
    void onPostDataFailure();
    void onPostImageSuccess();
    void onPostImageFailure();
    void onGetPhoneNrSuccess(String number);
}
