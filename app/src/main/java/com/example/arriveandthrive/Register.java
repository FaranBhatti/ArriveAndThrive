package com.example.arriveandthrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Create object of DatabaseReference class to access firebase's Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // obtain field values
        final EditText fullname = findViewById(R.id.fullname);
        final EditText email = findViewById(R.id.email);
        final EditText phone = findViewById(R.id.phone);
        final EditText password = findViewById(R.id.password);
        final EditText conPassword = findViewById(R.id.conPassword);

        final Button registerBtn = findViewById(R.id.registerBtn);
        final TextView loginNowBtn = findViewById(R.id.loginNow);

        databaseReference.child("users").setValue("");


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // obtain field values
                final String fullnameTxt = fullname.getText().toString();
                final String emailTxt = email.getText().toString();
                final String phoneTxt = phone.getText().toString();
                final String passwordTxt = password.getText().toString();
                final String conPasswordTxt = conPassword.getText().toString();

                // create a new user object with the values you want to send
                User user = new User(fullnameTxt, emailTxt, passwordTxt);

                // set the user object to the Firebase Realtime Database
                //databaseReference.child("users").child(phoneTxt).setValue(user);

                // Create new user
                databaseReference.child("users").child(phoneTxt).child("fullname").setValue(fullnameTxt);
                databaseReference.child("users").child(phoneTxt).child("email").setValue(emailTxt);
                databaseReference.child("users").child(phoneTxt).child("password").setValue(passwordTxt);

                // Display success message
                Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();

                // Open login activity
                startActivity(new Intent(Register.this, Login.class));

                /*
                // Checking if user filled all fields before sending information to Firebase
                if (fullnameTxt.isEmpty() || emailTxt.isEmpty() || phoneTxt.isEmpty() || passwordTxt.isEmpty() || conPasswordTxt.isEmpty()) {
                    // Display error message
                    Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                // Checking if password and confirm password match
                else if (!passwordTxt.equals(conPasswordTxt)) {
                    // Display error of matching passwords
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
                // Passes all field and password checks, register the user
                else {
                    // Print toast saying im in else statement
                    Toast.makeText(Register.this, "In else statement", Toast.LENGTH_SHORT).show();
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Prompt with a toast we're in onDataChange function
                            Toast.makeText(Register.this, "In onDataChange", Toast.LENGTH_SHORT).show();
                            // Checking if user already exists
                            if (snapshot.hasChild(phoneTxt)) {
                                // Display error message
                                Toast.makeText(Register.this, "Phone is already registered.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                // Create new user
                                databaseReference.child("users").child(phoneTxt).child("fullname").setValue(fullnameTxt);
                                databaseReference.child("users").child(phoneTxt).child("email").setValue(emailTxt);
                                databaseReference.child("users").child(phoneTxt).child("password").setValue(passwordTxt);

                                // Display success message
                                Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                // Open login activity
                                startActivity(new Intent(Register.this, Login.class));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Display error message
                            Toast.makeText(Register.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } */
            }
        });
        loginNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}