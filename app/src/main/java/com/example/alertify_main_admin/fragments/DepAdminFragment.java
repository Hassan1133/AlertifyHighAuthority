package com.example.alertify_main_admin.fragments;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_DEP_ADMIN_REF;
import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_HIGH_AUTHORITY_REF;
import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_POLICE_STATIONS_REF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.adapters.DepAdminAdp;
import com.example.alertify_main_admin.adapters.DropDownAdapter;
import com.example.alertify_main_admin.databinding.DepAdminBinding;
import com.example.alertify_main_admin.databinding.DepAdminDialogBinding;
import com.example.alertify_main_admin.interfaces.OnDropDownItemClickListener;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.main_utils.NetworkUtils;
import com.example.alertify_main_admin.models.DepAdminModel;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DepAdminFragment extends Fragment implements View.OnClickListener {

    private Dialog dialog;
    private ArrayList<PoliceStationModel> policeStationList;
    private DepAdminModel depAdmin;

    private DatabaseReference depAdminRef, highAuthorityRef, policeStationRef;

    private List<DepAdminModel> depAdmins;
    private DepAdminAdp adp;

    private DepAdminBinding binding;

    private DepAdminDialogBinding depAdminDialogBinding;

    private AppSharedPreferences appSharedPreferences;

    private String selectedPoliceStationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DepAdminBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        binding.addBtn.setOnClickListener(this);

        highAuthorityRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_HIGH_AUTHORITY_REF);
        policeStationRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_POLICE_STATIONS_REF);
        policeStationList = new ArrayList<>();

        depAdmins = new ArrayList<DepAdminModel>();

        appSharedPreferences = new AppSharedPreferences(requireActivity());

        depAdminRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_DEP_ADMIN_REF);

        binding.depAdminRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.depAdminSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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
        ArrayList<DepAdminModel> searchList = new ArrayList<>();
        for (DepAdminModel i : depAdmins) {
            if (i.getDepAdminName().toLowerCase().contains(newText.toLowerCase()) || i.getDepAdminPoliceStation().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        adp = new DepAdminAdp(getActivity(), searchList);
        binding.depAdminRecycler.setAdapter(adp);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.addBtn) {
            if (NetworkUtils.isInternetAvailable(requireActivity())) {
                // Internet is available, call the method for creating dialog
                createDialog();
            } else {
                // Internet is not available, show a message to the user
                Toast.makeText(getActivity(), "Please turn on your internet.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createDialog() {

        fetchPoliceStationNameForDropDown();

        depAdminDialogBinding = DepAdminDialogBinding.inflate(LayoutInflater.from(getActivity()));
        dialog = new Dialog(requireActivity());
        dialog.setContentView(depAdminDialogBinding.getRoot());
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        showDropDown();

        depAdminDialogBinding.depAdminPoliceStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDown();
            }
        });

        depAdminDialogBinding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    LoadingDialog.showLoadingDialog(getActivity());

                    depAdmin = new DepAdminModel();
                    depAdmin.setDepAdminName(depAdminDialogBinding.depAdminName.getText().toString().trim());
                    depAdmin.setDepAdminPoliceStation(depAdminDialogBinding.depAdminPoliceStation.getText().toString());
                    depAdmin.setDepAdminEmail(depAdminDialogBinding.depAdminEmail.getText().toString().trim());
                    depAdmin.setDepAdminUid("");
                    depAdmin.setDepAdminFCMToken("");
                    depAdmin.setDepAdminStatus("unblock");
                    depAdmin.setHighAuthorityId(appSharedPreferences.getString("userProfileId"));
                    depAdminAlreadyExistsOrNot(depAdmin);
                }
            }
        });

        depAdminDialogBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showDropDown() {

        // Set up the adapter with the fetched list
        DropDownAdapter dropDownAdapter = new DropDownAdapter(getActivity(), policeStationList, new OnDropDownItemClickListener() {
            @Override
            public void onItemClick(String policeStationId, String policeStationName) {
                selectedPoliceStationId = policeStationId;
                depAdminDialogBinding.depAdminPoliceStation.setText(policeStationName);
                Toast.makeText(requireActivity(), policeStationId, Toast.LENGTH_SHORT).show();
            }
        });

        // Attach the adapter to the AutocompleteTextView
        depAdminDialogBinding.depAdminPoliceStation.setAdapter(dropDownAdapter);
    }

    private void depAdminAlreadyExistsOrNot(DepAdminModel depAdmin) {
        depAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {

            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot depAdminSnapshot : snapshot.getChildren()) {

                        DepAdminModel dep = depAdminSnapshot.getValue(DepAdminModel.class);

                        count++;

                        assert dep != null;
                        if (dep.getDepAdminEmail().equals(depAdminDialogBinding.depAdminEmail.getText().toString())) {
                            depAdminDialogBinding.depAdminEmail.setText("");
                            Toast.makeText(getActivity(), "Email already exists. Please choose a different one", Toast.LENGTH_SHORT).show();
                            depAdminDialogBinding.depAdminEmail.setError("Email already exists. Please choose a different one");

                            LoadingDialog.hideLoadingDialog();

                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                addToDb(depAdmin);
                            }
                        }
                    }
                } else {
                    addToDb(depAdmin);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToDb(DepAdminModel depAdmin) {
        depAdmin.setDepAdminId(UUID.randomUUID().toString());
        depAdminRef.child(depAdmin.getDepAdminId()).setValue(depAdmin).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    saveDepAdminIdToHighAuthorityProfile(depAdmin.getDepAdminId());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDepAdminIdToHighAuthorityProfile(String depAdminId) {
        String userProfileId = appSharedPreferences.getString("userProfileId");

        // Retrieve the current value of depAdminList
        highAuthorityRef.child(userProfileId).child("depAdminList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> depAdminList = new ArrayList<>();
                for (DataSnapshot snapshotData : dataSnapshot.getChildren()) {
                    depAdminList.add(snapshotData.getValue(String.class));
                }

                // Check if the new depAdminId already exists in the list
                if (!depAdminList.contains(depAdminId)) {
                    // If it doesn't exist, add it to the list
                    depAdminList.add(depAdminId);

                    // Update the value of depAdminList in the database
                    highAuthorityRef.child(userProfileId).child("depAdminList").setValue(depAdminList)
                            .addOnSuccessListener(aVoid -> {
                                // Handle success
                               assignDepAdminIdToPoliceStation(depAdminId);
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // depAdmin already exists in the list
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(requireActivity(), "depAdmin already exists in the list!", Toast.LENGTH_SHORT).show();
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

    private void assignDepAdminIdToPoliceStation(String depAdminId) {

        HashMap<String, Object> map = new HashMap<>();

        map.put("depAdminId", depAdminId);

        if (selectedPoliceStationId != null) {
            policeStationRef.child(selectedPoliceStationId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    updatePoliceStationStatusAssigned(selectedPoliceStationId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updatePoliceStationStatusAssigned(String policeStationId) {

        HashMap<String, Object> map = new HashMap<>();

        map.put("assigned", true);
        policeStationRef.child(policeStationId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                Toast.makeText(requireActivity(), "DepAdmin added successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (depAdminDialogBinding.depAdminName.getText().length() < 3) {
            depAdminDialogBinding.depAdminName.setError("Please enter valid name");
            valid = false;
        }
        if (depAdminDialogBinding.depAdminPoliceStation.getText().length() == 0) {
            depAdminDialogBinding.depAdminPoliceStation.setError("Please select police station");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(depAdminDialogBinding.depAdminEmail.getText()).matches()) {
            depAdminDialogBinding.depAdminEmail.setError("Please enter valid email");
            valid = false;
        }
        return valid;
    }

    private void fetchPoliceStationNameForDropDown() {

        String highAuthorityId = appSharedPreferences.getString("userProfileId");
        highAuthorityRef.child(highAuthorityId).child("policeStationList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<String> policeStationList = new ArrayList<>();
                for (DataSnapshot snapshotData : snapshot.getChildren()) {
                    policeStationList.add(snapshotData.getValue(String.class));
                }

                getPoliceStationFromPoliceStationList(policeStationList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPoliceStationFromPoliceStationList(List<String> policeStationIdList) {
        policeStationList.clear();
        for (String policeStationId : policeStationIdList) {
            policeStationRef.child(policeStationId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    PoliceStationModel ps = snapshot.getValue(PoliceStationModel.class);

                    assert ps != null;
                    if (!ps.isAssigned()) {
                        policeStationList.add(ps);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchData() {

        LoadingDialog.showLoadingDialog(getActivity());

        depAdminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                depAdmins.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    depAdmins.add(dataSnapshot.getValue(DepAdminModel.class));
                }

                LoadingDialog.hideLoadingDialog();

                setDataToRecycler(depAdmins);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataToRecycler(List<DepAdminModel> depAdmins) {
        adp = new DepAdminAdp(getActivity(), depAdmins);
        binding.depAdminRecycler.setAdapter(adp);
    }
}