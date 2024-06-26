package com.example.alertify_main_admin.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivityMapsBinding;
import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.example.alertify_main_admin.main_utils.NetworkUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.alertify_main_admin.databinding.ActivityPoliceStationBoundaryMapsBinding;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PoliceStationBoundaryMapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap googleMap;
    private ActivityPoliceStationBoundaryMapsBinding binding;
    private Geocoder geocoder;
    private SupportMapFragment mapFragment;

    private List<LatLngWrapper> polygonPoints;
    private PolygonOptions polygonOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPoliceStationBoundaryMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.police_station_boundary_map);
        mapFragment.getMapAsync(this);

        init();
    }

    private void init() {

        searchLocation();

        binding.drawPolygonBtn.setOnClickListener(this);

        binding.donePolygonBtn.setOnClickListener(this);

        polygonPoints = new ArrayList<>();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.drawPolygonBtn:
                drawPolygon();
                break;
            case R.id.donePolygonBtn:
                sendBoundaryPointsToPoliceStationDialog();
                break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                polygonPoints.add(new LatLngWrapper(point.latitude, point.longitude));
                googleMap.addMarker(new MarkerOptions().position(point));
            }
        });

        //        get location current location and customization of current location button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        googleMap.setMyLocationEnabled(true);
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 0, 20);
    }

    private void searchLocation() {
        binding.boundaryMapSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (NetworkUtils.isInternetAvailable(PoliceStationBoundaryMapsActivity.this)) {
                    String location = binding.boundaryMapSearchView.getQuery().toString();
                    List<Address> addressList = null;

                    if (!location.isEmpty()) {
                        geocoder = new Geocoder(PoliceStationBoundaryMapsActivity.this, Locale.getDefault());

                        try {
                            addressList = geocoder.getFromLocationName(location, 1);
                        } catch (IOException e) {
                            Toast.makeText(PoliceStationBoundaryMapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        if (!addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        } else {
                            Toast.makeText(PoliceStationBoundaryMapsActivity.this, "Please select a valid location", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(PoliceStationBoundaryMapsActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void drawPolygon() {
        if (googleMap != null && !polygonPoints.isEmpty()) {
            googleMap.clear();  // Clear existing markers and shapes
            polygonOptions = new PolygonOptions();

            for (LatLngWrapper point : polygonPoints) {
                polygonOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
            googleMap.addPolygon(polygonOptions);

        } else {
            Toast.makeText(this, "If map is loaded then please select boundary points if not then check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBoundaryPointsToPoliceStationDialog() {
        if (!polygonPoints.isEmpty()) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("latLngList", (Serializable) polygonPoints);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select boundary points", Toast.LENGTH_SHORT).show();
        }
    }

}