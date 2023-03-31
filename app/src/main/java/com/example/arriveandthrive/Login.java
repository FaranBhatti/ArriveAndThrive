package com.example.arriveandthrive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fireapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    // Create variables
    private EditText edtUsername, edtPassword;
    private Button btnSignup, btnLogin, btnDelete;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get the instance of the Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // get the reference to the JSON tree
        databaseReference = firebaseDatabase.getReference();

        // Obtain username and password field values
        edtUsername = findViewById(R.id.editTextUsername);
        edtPassword = findViewById(R.id.editTextPassword);


        // Signup button
        btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(view -> {
            if(checkFields()) {
                addData();
            }
        });

        // Login button
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(view -> {
            if(checkFields()) {
                login();
            }
        });

        // Delete button
        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(view -> {
            if(checkFields()) {
                deleteData();
            }
        });
    }
    // Function for checking the fields
    private boolean checkFields() {
        username = edtUsername.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        if(TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show();
            return false;
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Function for adding data to the database
    private void addData() {
        // use push method to generate a unique key for a new child node
        String id = databaseReference.push().getKey();
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // create a task to set the value of the node as the new user
        Task setValueTask = databaseReference.child("Users").child(id).setValue(user);

        // add a success listener to the task
        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(Login.this, "Account created", Toast.LENGTH_LONG).show();
                edtUsername.setText("");
                edtPassword.setText("");
            }
        });
    }

    // Function for logging in
    private void login() {

        // get the reference to the JSON tree
        databaseReference = firebaseDatabase.getReference();

        // add a value event listener to the Users node
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            // called to read a static snapshot of the contents at a given path
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean match = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if(username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                        match = true;

                        Toast.makeText(Login.this, "Access granted", Toast.LENGTH_LONG).show();

                        // Go to home page
                        Intent intent = new Intent(Login.this, home_page.class);
                        startActivity(intent);
                    }
                }
                if(!match) {
                    Toast.makeText(Login.this, "Access denied", Toast.LENGTH_LONG).show();
                }
            }

            // called when the client doesn't have permission to access the data
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Function for deleting data from the database
    private void deleteData() {
        // get the reference to the JSON tree
        databaseReference = firebaseDatabase.getReference();

        // add a value event listener to the Users node
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {

            // called to read a static snapshot of the contents at a given path
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if(username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                        found = true;
                        snapshot.getRef().setValue(null);
                        Toast.makeText(Login.this, "Account deleted", Toast.LENGTH_LONG).show();
                    }
                }
                if(!found) {
                    Toast.makeText(Login.this, "No matching account found", Toast.LENGTH_LONG).show();
                }
            }

            // called when the client doesn't have permission to access the data
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}