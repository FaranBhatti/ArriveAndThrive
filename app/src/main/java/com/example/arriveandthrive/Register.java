package com.example.arriveandthrive;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {

    // obtain field values
    final EditText fullname = findViewById(R.id.fullname);
    final EditText email = findViewById(R.id.email);
    final EditText phone = findViewById(R.id.phone);
    final EditText password = findViewById(R.id.password);
    final EditText conPassword = findViewById(R.id.conPassword);

    final Button registerBtn = findViewById(R.id.registerBtn);
    final TextView loginNowBtn = findViewById(R.id.loginNow);

    // Database variables
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // get the instance of the Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // get the reference to the JSON tree
        databaseReference = firebaseDatabase.getReference();

        // Register button
        registerBtn.setOnClickListener(view -> {
            if(checkFields()) {
                addData();
            }
        });
        // Login button
        loginNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addData() {
        // obtain field values
        final String fullnameTxt = fullname.getText().toString();
        final String emailTxt = email.getText().toString();
        final String phoneTxt = phone.getText().toString();
        final String passwordTxt = password.getText().toString();
        final String conPasswordTxt = conPassword.getText().toString();

        // use push method to generate a unique key for a new child node
        User user = new User(fullnameTxt, emailTxt, passwordTxt);

        // create a task to set the value of the node as the new user
        Task setValueTask = databaseReference.child("Users").child(phoneTxt).setValue(user);

        // add a success listener to the task
        setValueTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(Register.this, "Account created", Toast.LENGTH_LONG).show();

                // Open login activity
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        // add a failure listener to the task
        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkFields() {
        // obtain field values
        final String fullnameTxt = fullname.getText().toString();
        final String emailTxt = email.getText().toString();
        final String phoneTxt = phone.getText().toString();
        final String passwordTxt = password.getText().toString();
        final String conPasswordTxt = conPassword.getText().toString();

        // Checking if user filled all fields before sending information to Firebase
        if (fullnameTxt.isEmpty() || emailTxt.isEmpty() || phoneTxt.isEmpty() || passwordTxt.isEmpty() || conPasswordTxt.isEmpty()) {
            // Display error message
            Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Checking if password and confirm password match
        else if (!passwordTxt.equals(conPasswordTxt)) {
            // Display error of matching passwords
            Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Passes all field and password checks, register the user
        else {
            return true;
        }
    }
}

/*
public class MainActivity extends AppCompatActivity {
    private EditText edtUsername, edtPassword;
    private Button btnSignup, btnLogin, btnDelete;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the instance of the Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // get the reference to the JSON tree
        databaseReference = firebaseDatabase.getReference();

        edtUsername = findViewById(R.id.editTextUsername);
        edtPassword = findViewById(R.id.editTextPassword);

        btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(view -> {
            if(checkFields()) {
                addData();
            }
        });

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(view -> {
            if(checkFields()) {
                login();
            }
        });

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(view -> {
            if(checkFields()) {
                deleteData();
            }
        });
    }

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
                Toast.makeText(MainActivity.this, "Account created", Toast.LENGTH_LONG).show();
                edtUsername.setText("");
                edtPassword.setText("");
            }
        });

        // add a failure listener to the task
        setValueTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

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
                        Toast.makeText(MainActivity.this, "Access granted", Toast.LENGTH_LONG).show();
                    }
                }
                if(!match) {
                    Toast.makeText(MainActivity.this, "Access denied", Toast.LENGTH_LONG).show();
                }
            }

            // called when the client doesn't have permission to access the data
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

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
                        Toast.makeText(MainActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                    }
                }
                if(!found) {
                    Toast.makeText(MainActivity.this, "No matching account found", Toast.LENGTH_LONG).show();
                }
            }

            // called when the client doesn't have permission to access the data
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
 */