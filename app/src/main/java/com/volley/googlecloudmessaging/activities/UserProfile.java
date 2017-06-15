package com.volley.googlecloudmessaging.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.volley.googlecloudmessaging.R;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile extends AppCompatActivity {
    FirebaseAuth firebaseAuth ;
    FirebaseUser user;
    DatabaseReference databaseReference;

    TextView uid, pid, user_email, username, user_imageUrl;
    private static String _user = null, url = null;

    ImageView imageView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        firebaseAuth = FirebaseAuth.getInstance();

        //give references to views
        uid= (TextView) findViewById(R.id.user_id);
        pid= (TextView) findViewById(R.id.user_pid);
        user_email= (TextView) findViewById(R.id.user_email);
        username = (TextView) findViewById(R.id.user_name);
        user_imageUrl = (TextView) findViewById(R.id.user_imageUrl);
        imageView = (ImageView) findViewById(R.id.user_img);

        user = firebaseAuth.getCurrentUser();
        String sEmail = user.getEmail();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference childValue = databaseReference.child("Users").child(user.getUid()).child("Username");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                _user = (String) dataSnapshot.child("Users").child(user.getUid()).child("Username").getValue();
                url = (String) dataSnapshot.child("Users").child(user.getUid()).child("ProfileUrl").getValue();
                String userEmail = (String) dataSnapshot.child("Users").child(user.getUid()).child("Email").getValue();
                Log.d("VALUE","user value is: "+_user);
                Log.d("PROFILE PHOTO URL","uRL is: "+url);
                Log.d("PROFILE getPhotoUrl","uRL is: "+user.getPhotoUrl());
                username.setText("Username: "+_user);
                user_imageUrl.setText("User Photo Url: "+url);
                user_email.setText("User Email: "+userEmail);


                Picasso.Builder build = new Picasso.Builder(getApplicationContext());
                build.listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        exception.printStackTrace();
                        Log.d("EXCEPTION","ex: "+exception.getMessage());
                    }
                });

                //load user profile image into imageview
                build.build().load(Uri.parse(url))
                        .error(R.mipmap.ic_launcher_round)
                        .placeholder(R.mipmap.ic_launcher)
                        .fit()
                        .into(imageView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        uid.setText("User ID: "+user.getUid());
        pid.setText("User Provider ID: "+user.getProviderId());
        //username.setText("Username: "+ _user);

    }

    public void logout(View view) {
        firebaseAuth.signOut();
        try{
            LoginManager.getInstance().logOut();
        }catch (Exception ex){

        }

        finish();
        startActivity(new Intent(this, Login.class));
    }

    //Activity Methods

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.child("Users").child(user.getUid()).child("isLoggedin").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "userProfile onStop Called ", Toast.LENGTH_SHORT).show();
        databaseReference.child("Users").child(user.getUid()).child("isLoggedin").setValue(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "userProfile Destroy Called ", Toast.LENGTH_SHORT).show();
        }
}
