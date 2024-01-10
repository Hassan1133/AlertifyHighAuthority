package com.example.alertify_main_admin.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.alertify_main_admin.adapters.ViewPagerAdapter;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivityEditUserProfileBinding;
import com.example.alertify_main_admin.databinding.ActivityLoginSignupBinding;
import com.example.alertify_main_admin.fragments.LoginFragment;
import com.example.alertify_main_admin.fragments.SignupFragment;
import com.google.android.material.tabs.TabLayout;

public class LoginSignupActivity extends AppCompatActivity {

    private ActivityLoginSignupBinding binding;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    private void init() // initialization of widgets
    {

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // set fragments and titles to the adapter
        viewPagerAdapter.addFragment(new LoginFragment(), "LOGIN");
        viewPagerAdapter.addFragment(new SignupFragment(), "SIGNUP");

        // set adapter on viewpager
        binding.viewPager.setAdapter(viewPagerAdapter);

        // set tabLayout with viewpager
        binding.tabsLayout.setupWithViewPager(binding.viewPager);
    }

}