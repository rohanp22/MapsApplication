package com.example.mapsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.mapsapplication.Others.SharedPrefManager;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPrefManager.getInstance(Splash.this).isLoggedIn()) {
                    finish();
                    startActivity(new Intent(Splash.this, MainActivity.class));
                } else {
                    finish();
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                }
            }
        }, 3000);
    }
}