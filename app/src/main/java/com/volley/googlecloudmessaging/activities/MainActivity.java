package com.volley.googlecloudmessaging.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volley.googlecloudmessaging.R;

public class MainActivity extends AppCompatActivity {
    Button btn ;
    EditText username,pass, age ;
    int counter  ;

    private static String MYPREF ="MyPref";

    //TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);
        btn = (Button) findViewById(R.id.set);
        username = (EditText) findViewById(R.id.username);
        pass = (EditText) findViewById(R.id.password);
        age = (EditText) findViewById(R.id.age);

        //text = (TextView) findViewById(R.id.txt);

        //get counter value
        counter = getDataFromPref();


        //get the reference location of database
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send values to firebase database
                databaseReference.child("Users")
                        .child("user "+counter).child("username").setValue(username.getText().toString());
                databaseReference.child("Users")
                        .child("user "+counter).child("pass").setValue(pass.getText().toString());
                databaseReference.child("Users")
                        .child("user "+counter).child("age").setValue(age.getText().toString());
                Toast.makeText(MainActivity.this, "Counter="+counter, Toast.LENGTH_SHORT).show();
                counter++ ;

                databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String users = String.valueOf(dataSnapshot.getChildren());
                        Toast.makeText(MainActivity.this, users, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                saveCounterToSharedPref(counter);

            }
        });

    }

    void saveCounterToSharedPref(int c){
        SharedPreferences.Editor editor = getSharedPreferences(MYPREF, MODE_PRIVATE).edit();
        editor.putInt("counter", c);
        editor.commit();
    }

    int getDataFromPref(){
        int c = 0 ;
        SharedPreferences editor = getSharedPreferences(MYPREF, MODE_PRIVATE);
        c = editor.getInt("counter", 0);
        Toast.makeText(this, "Counter Previous value:"+c, Toast.LENGTH_SHORT).show();
        return c;
    }
}
