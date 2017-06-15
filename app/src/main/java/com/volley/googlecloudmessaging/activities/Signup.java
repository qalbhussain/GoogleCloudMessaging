package com.volley.googlecloudmessaging.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volley.googlecloudmessaging.R;
import com.volley.googlecloudmessaging.helper.FirebaseDB;

/**
 * Created by qalbh on 6/12/2017.
 */

public class Signup extends AppCompatActivity {
    //to show ProgressBar while connecting with firebase
    ProgressBar progressBar;

    //firebase authentication
    FirebaseAuth firebaseAuth;
    //firebase database reference
    DatabaseReference databaseReference;

    //views
    EditText email, pass, username ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.password);
        username = (EditText) findViewById(R.id.username);

        //initialize the instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //initialize the instance of firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressBar.setVisibility(View.GONE);
    }

    public void Submit(View view) {

        final String sEmail = email.getText().toString();
        final String sPass = pass.getText().toString();
        final String sUsername = username.getText().toString();

        if (validateCredentials(sEmail, sPass)) {
            //progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(sEmail, sPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                //String userId = user.getUid();
                                //savedUserToFirebaseDB(databaseReference,sEmail, sUsername, user.getUid());
                                FirebaseDB.saveUserToFirebaseDB(databaseReference,sEmail, sUsername, user.getUid());
                               // progressBar.setVisibility(View.GONE);
                                Toast.makeText(Signup.this, "User Created Successful!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(getApplicationContext(), UserProfile.class));
                            } else {
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(Signup.this, "Unsuccessful!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //validate user credentials
    private boolean validateCredentials(String sEmail, String sPass) {
        //return false if email field is empty
        if (TextUtils.isEmpty(sEmail)){
            email.setError("enter email");
            return false ;
        }
        //return false if email is entered in wrong format
        if (sEmail.contains(" ")){
            email.setError("Please enter correct format e.g xxxx@gmail.com");
            return false;
        }
        //return false if password field is empty
        if (TextUtils.isEmpty(sPass)){
            email.setError("enter password");
            return false ;
        }
        return true ;
    }

    public void GoToSignin(View view) {
        finish();
        startActivity(new Intent(this, Login.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "onDestroy Called: Signup.class", Toast.LENGTH_SHORT).show();
    }
}
