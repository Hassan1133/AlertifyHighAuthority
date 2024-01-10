package com.example.alertify_main_admin.activities;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;


import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivitySplashScreenBinding;


public class SplashScreen extends AppCompatActivity {

    private ActivitySplashScreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        goToNextActivity();
    }


    private void goToNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                boolean check = pref.getBoolean("flag", false);
                Intent intent;

                if (check) {
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, LoginSignupActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}