package com.volley.googlecloudmessaging.helper;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by qalbh on 6/14/2017.
 */

public class FirebaseDB {

    //get a list of parameters and save data to Firebase Database
    public static void saveUserToFirebaseDB(DatabaseReference databaseReference,String sEmail, String sUsername, String userId) {
        databaseReference.child("Users").child(userId).child("Email").setValue(sEmail);
        databaseReference.child("Users").child(userId).child("Username").setValue(sUsername);
        databaseReference.child("Users").child(userId).child("isLoggedin").setValue(false);
    }

    public static void saveUserToFirebaseDBWithImage(DatabaseReference databaseReference,String sEmail, String sUsername, String userId, String userImageUrl) {
        databaseReference.child("Users").child(userId).child("Email").setValue(sEmail);
        databaseReference.child("Users").child(userId).child("Username").setValue(sUsername);
        databaseReference.child("Users").child(userId).child("ProfileUrl").setValue(userImageUrl);
        databaseReference.child("Users").child(userId).child("isLoggedin").setValue(false);
    }
}
