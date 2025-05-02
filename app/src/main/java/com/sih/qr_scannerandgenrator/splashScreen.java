package com.sih.qr_scannerandgenrator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sih.qr_scannerandgenrator.authentication.LoginActivity;
import com.sih.qr_scannerandgenrator.authentication.SignupActivity;

public class splashScreen extends AppCompatActivity {


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

            if (isLoggedIn) {
                startActivity(new Intent(splashScreen.this, MainActivity.class));
            } else {
                startActivity(new Intent(splashScreen.this, LoginActivity.class));
            }

            finish();
        }
    }
