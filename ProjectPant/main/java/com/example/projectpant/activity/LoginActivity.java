package com.example.projectpant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectpant.R;
import com.example.projectpant.adapter.ItemAdapterPrivate;
import com.example.projectpant.callbackInterface.DeleteCallbackInterface;
import com.example.projectpant.callbackInterface.ReadCallbackInterface;
import com.example.projectpant.model.DataParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

// code from https://firebase.google.com/docs/auth/android/google-signin is used in this class
// handles the users options in the account page
public class LoginActivity extends AppCompatActivity implements ReadCallbackInterface, DeleteCallbackInterface {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView statusText;
    private ItemAdapterPrivate itemAdapterPrivate;
    private DataParser dataParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.oauth2clientid))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        statusText = findViewById(R.id.status_text);

        dataParser = new DataParser(this, this);
        mAuth = FirebaseAuth.getInstance();
        RecyclerView recyclerView = findViewById(R.id.recycler_view_private);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        itemAdapterPrivate = new ItemAdapterPrivate(this, this);
        recyclerView.setAdapter(itemAdapterPrivate);
        PantAdList.getinstance().clear();
        if (mAuth.getCurrentUser() != null){
            statusText.setText(String.format("Inloggad: %s", mAuth.getCurrentUser().getDisplayName()));
            FirebaseUser currentUser = mAuth.getCurrentUser();
            dataParser.getAdsForUser(currentUser.getUid());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    // handles the login process after a user has tried to log in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                finish();
            }
        }
    }

    // sign into app with google
    public void signIn(View view) {
        Log.d("LoginGoogle", "sign in pressed");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // sign out from app with google
    public void signOut(View view) {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });
    }

    // updates the list with ads that the logged in user has made
    @Override
    public void onSuccessfulReadCallback() {
        itemAdapterPrivate.notifyDataSetChanged();
    }

    //reloads the list when user has deleted ad
    @Override
    public void onSuccessfulDeleteCallback() {
        dataParser.getAdsForUser(mAuth.getCurrentUser().getUid());
    }
}
