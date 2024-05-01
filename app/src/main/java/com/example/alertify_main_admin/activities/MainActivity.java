package com.example.alertify_main_admin.activities;

import static com.example.alertify_main_admin.constants.Constants.ERROR_DIALOG_REQUEST;
import static com.example.alertify_main_admin.constants.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.alertify_main_admin.constants.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.fragments.AnalyticsFragment;
import com.example.alertify_main_admin.fragments.ComplaintsFragment;
import com.example.alertify_main_admin.fragments.DepAdminFragment;
import com.example.alertify_main_admin.fragments.PoliceStationFragment;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView userName, userEmail;

    private boolean locationPermission = false;
    private BottomNavigationView bottom_navigation;

    private Intent intent;

    private AppSharedPreferences appSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        initialize(); // initialization method for initializing variables
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tool_bar_menu) {
            startDrawer(); // start drawer method for open or close navigation drawer
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
        ImageView toolBarBtn = findViewById(R.id.tool_bar_menu);
        toolBarBtn.setOnClickListener(this);

        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.user_name);
        userEmail = headerView.findViewById(R.id.user_email);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        appSharedPreferences = new AppSharedPreferences(MainActivity.this);

        loadFragment(new ComplaintsFragment());
        setProfileData();
        navigationSelection(); // selection method for navigation items
        bottomNavigationSelection();
        checkMapServices();
        getLocationPermission();
    }

    private void setProfileData() {
        userName.setText(appSharedPreferences.getString("userProfileName"));

        userEmail.setText(appSharedPreferences.getString("userProfileEmail"));
    }

    private void navigationSelection() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.logout:

                        appSharedPreferences.put("highAuthorityLoginFlag", false);

                        appSharedPreferences.clear();

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
                        loadFragment(new ComplaintsFragment());
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
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.complaints:
                        loadFragment(new ComplaintsFragment());
                        return true;
                    case R.id.dep_admin:
                        loadFragment(new DepAdminFragment());
                        return true;
                    case R.id.police_station:
                        if (isMapsEnabled()) {
                            getLocationPermission();
                            loadFragment(new PoliceStationFragment());
                        }
                        return true;
                    case R.id.analytics:
                        loadFragment(new AnalyticsFragment());
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

    private void checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
            }
        }
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
            assert dialog != null;
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