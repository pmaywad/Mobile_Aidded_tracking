package com.example.mycovidapp.symptoms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import mycovidapp.R;

public class LoginScreen extends AppCompatActivity {

    Button login;
    EditText username;
    EditText password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        login=findViewById(R.id.loginbtn);
        username= findViewById(R.id.textUsername);
        password=findViewById(R.id.editPassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences = getSharedPreferences("Username", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                if(!preferences.contains("password")) {
                    editor.putString("username", username.getText().toString());
                    editor.putString("password", password.getText().toString());
                    editor.apply();
                }
                else{
                    Toast.makeText(LoginScreen.this,"A password was already there, so not updating database password",Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
