package com.example.alertify_main_admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_main_admin.databinding.ActivitySplashScreenBinding;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;


@SuppressLint("CustomSplashScreen")
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

        AppSharedPreferences appSharedPreferences = new AppSharedPreferences(SplashScreen.this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean check = appSharedPreferences.getBoolean("highAuthorityLoginFlag");
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