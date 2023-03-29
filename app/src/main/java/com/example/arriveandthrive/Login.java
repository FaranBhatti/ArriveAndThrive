package com.example.arriveandthrive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // obtain field values
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        final Button loginBtn = findViewById(R.id.loginBtn);
        final TextView registerBtn = findViewById(R.id.registerNowBtn);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String phoneTxt = username.getText().toString();
                final String PasswordTxt = password.getText().toString();

                if (phoneTxt.isEmpty() || PasswordTxt.isEmpty()) {
                    // Display error message
                    Toast.makeText(Login.this, "Please enter your username or password", Toast.LENGTH_SHORT).show();
                } else {
                    // Login

                }
            }
        });
    }
}