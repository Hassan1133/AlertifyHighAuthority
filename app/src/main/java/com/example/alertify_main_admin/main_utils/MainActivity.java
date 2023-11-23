package com.example.alertify_main_admin.main_utils;

import static com.example.alertify_main_admin.constants.Constants.ERROR_DIALOG_REQUEST;
import static com.example.alertify_main_admin.constants.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.alertify_main_admin.constants.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.authentication.LoginSignupActivity;
import com.example.alertify_main_admin.complaints.Complaints_Fragment;
import com.example.alertify_main_admin.dep_admin.Dep_Admin_Fragment;
import com.example.alertify_main_admin.police_station.PoliceStationFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View headerView;
    private ImageView toolBarBtn;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private CircleImageView userImage;
    private TextView userName, userEmail;

    private boolean locationPermission = false;
    private BottomNavigationView bottom_navigation;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        initialize(); // initialization method for initializing variables
        navigationSelection(); // selection method for navigation items
        bottomNavigationSelection();
        checkMapServices();
        getLocationPermission();
        loadFragment(new Complaints_Fragment());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tool_bar_menu:
                startDrawer(); // start drawer method for open or close navigation drawer
                break;
        }
    }

    private void startDrawer() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initialize() {
        toolBarBtn = findViewById(R.id.tool_bar_menu);
        toolBarBtn.setOnClickListener(this);

        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);
        headerView = navigationView.getHeaderView(0);
        userImage = headerView.findViewById(R.id.circle_img);
        userName = headerView.findViewById(R.id.user_name);
        userEmail = headerView.findViewById(R.id.user_email);

        firebaseAuth = FirebaseAuth.getInstance();

        bottom_navigation = findViewById(R.id.bottom_navigation);

        setProfileData();
    }

    private void setProfileData() {
        SharedPreferences userData = getSharedPreferences("profileData", MODE_PRIVATE);
        userName.setText(userData.getString("userName", ""));

        userEmail.setText(userData.getString("userEmail", ""));

        Glide.with(getApplicationContext()).load(userData.getString("userImgUrl", "")).into(userImage);
    }

    private void navigationSelection() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.logout:

                        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                        SharedPreferences.Editor logOutEditor = pref.edit();
                        logOutEditor.putBoolean("flag", false);
                        logOutEditor.apply();

                        SharedPreferences userData = getSharedPreferences("profileData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor profileDataEditor = userData.edit();
                        profileDataEditor.clear();
                        profileDataEditor.apply();

                        intent = new Intent(MainActivity.this, LoginSignupActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.profile:
                        intent = new Intent(MainActivity.this, EditUserProfileActivity.class);
                        startActivity(intent);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.home:
                        loadFragment(new Complaints_Fragment());
                        bottom_navigation.setSelectedItemId(R.id.complaints);
                        drawer.closeDrawer(GravityCompat.START);
                        break;

                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProfileData();
    }

    private void bottomNavigationSelection() {

        bottom_navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.complaints:
                        loadFragment(new Complaints_Fragment());
                        return true;
                    case R.id.dep_admin:
                        loadFragment(new Dep_Admin_Fragment());
                        return true;
                    case R.id.police_station:
                        if (isMapsEnabled()) {
                            getLocationPermission();
                            loadFragment(new PoliceStationFragment());
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
        }
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermission = true;
                    Toast.makeText(this, "fragment no", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (locationPermission) {
                } else {
                    getLocationPermission();
                }
            }
        }

    }

}