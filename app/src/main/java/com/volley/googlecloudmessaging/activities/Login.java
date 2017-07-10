package com.volley.googlecloudmessaging.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volley.googlecloudmessaging.R;
import com.volley.googlecloudmessaging.helper.FirebaseDB;

import static android.view.View.GONE;

public class Login extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    private static String FACEBOOK_TAG = "Facebook";

    //firebase variables
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseUser user;

    //views variables
    Button Signin_Gmail;
    LinearLayout linearLayout;

    //progressIndicator
    ProgressBar progressBar;

    //loginButton for facebook
    LoginButton Signin_Facebook;
    //facebook callback manager
    private CallbackManager callbackManager;

    private static final int RC_SIGN_IN = 007;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_login);
        //initialize firebase authentication instance
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        //pass reference to views

        Signin_Gmail = (Button) findViewById(R.id.signin_gmail);
        linearLayout = (LinearLayout) findViewById(R.id.ll);
        Signin_Facebook = (LoginButton) findViewById(R.id.login_button);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        //set click listener to button
        Signin_Gmail.setOnClickListener(this);

        //Configure Google signin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //google api client for using google play services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.connect();


        //setup and initialize facebook login
        setUpFacebook();
    }

    //setUp read permission and register callback for facebook
    private void setUpFacebook() {
        callbackManager = CallbackManager.Factory.create();
        Signin_Facebook.setReadPermissions("email", "public_profile");
        Signin_Facebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                linearLayout.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Log.d(FACEBOOK_TAG, "Facebook Result: " + loginResult);
                firebaseSigninWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void firebaseSigninWithFacebook(AccessToken accessToken) {
        Log.d(FACEBOOK_TAG, "Signin with facebook Token: " + accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Auth Successful", Toast.LENGTH_SHORT).show();
                            user = firebaseAuth.getCurrentUser();
                            if (user != null){
                                FirebaseDB.saveUserToFirebaseDBWithImage(databaseReference,
                                        user.getEmail(),
                                        user.getDisplayName(),
                                        user.getUid(),
                                        user.getPhotoUrl().toString());
                                startActivity(new Intent(getApplicationContext(), UserProfile.class));
                                finish();
                            }

                        } else {
                            linearLayout.setVisibility(GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(Login.this, "Facebook Auth Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        //progressBar.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "onDestroy Called: Login.class", Toast.LENGTH_SHORT).show();
    }

    //what happens when user click a button
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signin_gmail) {
            signIn();
        }
    }

    //on Connection Failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//connection failed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                //authenticate user with firebase
                firebaseSigninWithGoogle(account);
            }
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    //signin to firebase using gmail credentials
    private void firebaseSigninWithGoogle(final GoogleSignInAccount result) {
        AuthCredential credential = GoogleAuthProvider.getCredential(result.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, result.getDisplayName() + "--" + result.getEmail(), Toast.LENGTH_SHORT).show();
                            //user login successfull
                            user = firebaseAuth.getCurrentUser();
                            FirebaseDB.saveUserToFirebaseDBWithImage(databaseReference,
                                    user.getEmail(),
                                    user.getDisplayName(),
                                    user.getUid(),
                                    user.getPhotoUrl().toString());
                            startActivity(new Intent(getApplicationContext(), UserProfile.class));
                            finish();
                        } else {
                            linearLayout.setVisibility(GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            //progressBar.setVisibility(View.GONE);
                            //user login failed
                            Toast.makeText(Login.this, "login failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            Toast.makeText(this, "mGoogleApiClient is connected", Toast.LENGTH_SHORT).show();
        }
    }
}
