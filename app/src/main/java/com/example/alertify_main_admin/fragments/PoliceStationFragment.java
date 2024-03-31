package com.example.alertify_main_admin.fragments;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_HIGH_AUTHORITY_REF;
import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_POLICE_STATIONS_REF;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.MapsActivity;
import com.example.alertify_main_admin.activities.PoliceStationBoundaryMapsActivity;
import com.example.alertify_main_admin.adapters.BoundaryAdapter;
import com.example.alertify_main_admin.adapters.PoliceStationAdp;
import com.example.alertify_main_admin.databinding.PoliceStationBinding;
import com.example.alertify_main_admin.databinding.PoliceStationDialogBinding;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.main_utils.NetworkUtils;
import com.example.alertify_main_admin.models.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PoliceStationFragment extends Fragment implements View.OnClickListener {

    private PoliceStationBinding binding;
    private double selectedLatitude, selectedLongitude;
    private Dialog dialog;

    private PoliceStationModel policeStation;

    private DatabaseReference policeStationRef, highAuthorityRef;
    private List<PoliceStationModel> policeStations;

    private List<LatLngWrapper> boundaryPoints;

    private PoliceStationDialogBinding policeStationDialogBinding;

    private AppSharedPreferences appSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PoliceStationBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {

        binding.addBtn.setOnClickListener(this);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        policeStationRef = firebaseDatabase.getReference(ALERTIFY_POLICE_STATIONS_REF); // firebase initialization
        highAuthorityRef = firebaseDatabase.getReference(ALERTIFY_HIGH_AUTHORITY_REF); // firebase initialization

        appSharedPreferences = new AppSharedPreferences(requireActivity());

        policeStations = new ArrayList<PoliceStationModel>();

        boundaryPoints = new ArrayList<LatLngWrapper>();

        binding.policeStationRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.policeStationSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });

        fetchData();
    }

    private void search(String newText) {
        ArrayList<PoliceStationModel> searchList = new ArrayList<>();
        for (PoliceStationModel i : policeStations) {
            if (i.getPoliceStationName().toLowerCase().contains(newText.toLowerCase()) || i.getPoliceStationLocation().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        setDataToRecycler(searchList);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addBtn:
                if (NetworkUtils.isInternetAvailable(requireActivity())) {
                    // Internet is available, call the method for creating dialog
                    createDialog();
                } else {
                    // Internet is not available, show a message to the user
                    Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //    this method for create add police station dialog
    private void createDialog() {
        policeStationDialogBinding = PoliceStationDialogBinding.inflate(LayoutInflater.from(getActivity()));
        dialog = new Dialog(requireActivity());
        dialog.setContentView(policeStationDialogBinding.getRoot());
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        policeStationDialogBinding.boundariesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        policeStationDialogBinding.dialogAddPoliceStationLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                policeStationLocationResultLauncher.launch(intent);
            }
        });

        policeStationDialogBinding.dialogAddPoliceStationBoundaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PoliceStationBoundaryMapsActivity.class);
                policeStationBoundaryResultLauncher.launch(intent);
            }
        });
        policeStationDialogBinding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValid()) {
                    LoadingDialog.showLoadingDialog(getActivity());

                    policeStation = new PoliceStationModel();

                    policeStation.setPoliceStationName(policeStationDialogBinding.dialogPoliceStationName.getText().toString());
                    policeStation.setPoliceStationLocation(policeStationDialogBinding.dialogPoliceStationLocation.getText().toString());
                    policeStation.setPoliceStationLatitude(selectedLatitude);
                    policeStation.setPoliceStationLongitude(selectedLongitude);
                    policeStation.setBoundaries(boundaryPoints);

                    checkPoliceStationExists(policeStation);
                }
            }
        });

        policeStationDialogBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void checkPoliceStationExists(PoliceStationModel policeStation) {
        policeStationRef.addListenerForSingleValueEvent(new ValueEventListener() {

            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot policeStationSnapshot : snapshot.getChildren()) {

                        PoliceStationModel ps = policeStationSnapshot.getValue(PoliceStationModel.class);

                        count++;

                        assert ps != null;
                        if (ps.getPoliceStationName().toLowerCase().trim().equals(policeStation.getPoliceStationName().toLowerCase().trim())) {
                            Toast.makeText(getActivity(), "Police Station already exists. Please enter a different one", Toast.LENGTH_SHORT).show();
                            policeStationDialogBinding.dialogPoliceStationName.setError("Police Station already exists. Please enter a different one");
                            LoadingDialog.hideLoadingDialog();
                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                addToDb(policeStation);
                            }
                        }
                    }
                } else {
                    addToDb(policeStation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addToDb(PoliceStationModel policeStation) {


        policeStation.setId(UUID.randomUUID().toString());

        policeStationRef.child(policeStation.getId()).setValue(policeStation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    savePoliceStationIdToHighAuthorityProfile(policeStation.getId());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void savePoliceStationIdToHighAuthorityProfile(String policeStationId) {
        String userProfileId = appSharedPreferences.getString("userProfileId");

        // Retrieve the current value of policeStationList
        highAuthorityRef.child(userProfileId).child("policeStationList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> policeStationList = new ArrayList<>();
                if (dataSnapshot.exists()) {

                    policeStationList = (List<String>) dataSnapshot.getValue();
                }

                // Check if the new policeStationId already exists in the list
                assert policeStationList != null;
                if (!policeStationList.contains(policeStationId)) {
                    // If it doesn't exist, add it to the list
                    policeStationList.add(policeStationId);

                    // Update the value of policeStationList in the database
                    highAuthorityRef.child(userProfileId).child("policeStationList").setValue(policeStationList)
                            .addOnSuccessListener(aVoid -> {
                                // Handle success
                                LoadingDialog.hideLoadingDialog();
                                Toast.makeText(getActivity(), "Police Station added successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                Toast.makeText(requireActivity(), "Failed to add police station: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Police station already exists in the list
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(requireActivity(), "Police Station already exists in the list!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
                LoadingDialog.hideLoadingDialog();
                Toast.makeText(requireActivity(), "Failed to retrieve police station list: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isValid() {
        boolean valid = true;

        if (policeStationDialogBinding.dialogPoliceStationName.getText().length() < 3) {
            policeStationDialogBinding.dialogPoliceStationName.setError("Please enter valid name");
            valid = false;
        }
        if (policeStationDialogBinding.dialogPoliceStationLocation.getText().length() < 3) {
            policeStationDialogBinding.dialogPoliceStationLocation.setError("Please select valid location");
            valid = false;
        }
        if (selectedLatitude == 0 || selectedLongitude == 0) {
            Toast.makeText(getActivity(), "Please select location again", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (boundaryPoints.isEmpty()) {
            Toast.makeText(getActivity(), "Please add boundary points", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private void fetchData() {

        LoadingDialog.showLoadingDialog(getActivity());

        policeStationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                policeStations.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    policeStations.add(dataSnapshot.getValue(PoliceStationModel.class));
                }

                Collections.reverse(policeStations);

                LoadingDialog.hideLoadingDialog();

                setDataToRecycler(policeStations);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataToRecycler(List<PoliceStationModel> policeStations) {
        PoliceStationAdp adp = new PoliceStationAdp(getActivity(), policeStations);
        binding.policeStationRecycler.setAdapter(adp);
    }


    private final ActivityResultLauncher<Intent> policeStationLocationResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                policeStationDialogBinding.dialogPoliceStationLocation.setText(result.getData().getStringExtra("address"));
                selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                selectedLongitude = result.getData().getDoubleExtra("longitude", 0);
            }
        }
    });

    private final ActivityResultLauncher<Intent> policeStationBoundaryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                boundaryPoints = (List<LatLngWrapper>) result.getData().getSerializableExtra("latLngList");
                setDataToBoundaryRecycler(boundaryPoints);
            }
        }
    });

    private void setDataToBoundaryRecycler(List<LatLngWrapper> boundaryList) {
        BoundaryAdapter boundaryAdp = new BoundaryAdapter(getActivity(), boundaryList);
        policeStationDialogBinding.boundariesRecycler.setAdapter(boundaryAdp);
    }
}
