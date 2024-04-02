package com.example.alertify_main_admin.activities;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_DEP_ADMIN_REF;
import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_POLICE_STATIONS_REF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.adapters.DropDownAdapter;
import com.example.alertify_main_admin.constants.Constants;
import com.example.alertify_main_admin.databinding.ActivityEditDepAdminBinding;
import com.example.alertify_main_admin.databinding.DepAdminEditImgDialogBinding;
import com.example.alertify_main_admin.databinding.DepAdminEditNameDialogBinding;
import com.example.alertify_main_admin.databinding.DepAdminEditPoliceStationDialogBinding;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.DepAdminModel;
import com.example.alertify_main_admin.models.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
public class EditDepAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityEditDepAdminBinding binding;
    private DepAdminModel depAdminModel;
    private Dialog depAdminUpdateNameDialog, depAdminUpdatePoliceStationDialog;
    private DatabaseReference depAdminRef, policeStationsRef;
    private ArrayList<String> policeStationNameList;
    private DepAdminEditNameDialogBinding depAdminEditNameDialogBinding;
    private DepAdminEditPoliceStationDialogBinding depAdminEditPoliceStationDialogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditDepAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {

        depAdminRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_DEP_ADMIN_REF);

        binding.depAdminNameEditBtn.setOnClickListener(this);

        policeStationsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_POLICE_STATIONS_REF);
        policeStationNameList = new ArrayList<>();

        binding.depAdminPoliceStationEditBtn.setOnClickListener(this);

        binding.depAdminStatusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus(depAdminModel);
            }
        });

        getIntentData();
    }

    private void getIntentData() {
        depAdminModel = (DepAdminModel) getIntent().getSerializableExtra("depAdminModel");
        binding.editDepAdminName.setText(depAdminModel.getDepAdminName());
        binding.editDepAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());
        binding.editDepAdminEmail.setText(depAdminModel.getDepAdminEmail());
        if (depAdminModel.getDepAdminStatus().equals("unblock")) {
            binding.depAdminStatusIcon.setImageResource(R.drawable.unlock);
        } else if (depAdminModel.getDepAdminStatus().equals("block")) {
            binding.depAdminStatusIcon.setImageResource(R.drawable.lock);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.depAdminNameEditBtn:
                createDepAdminNameDialog();
                break;
            case R.id.depAdminPoliceStationEditBtn:
                createDepAdminPoliceStationDialog();
                break;
        }
    }
    private void createDepAdminNameDialog() {
        depAdminEditNameDialogBinding = DepAdminEditNameDialogBinding.inflate(LayoutInflater.from(this));
        depAdminUpdateNameDialog = new Dialog(EditDepAdminActivity.this);
        depAdminUpdateNameDialog.setContentView(depAdminEditNameDialogBinding.getRoot());
        depAdminUpdateNameDialog.show();
        depAdminUpdateNameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        depAdminEditNameDialogBinding.editDepAdminDialogName.setText(depAdminModel.getDepAdminName());

        depAdminEditNameDialogBinding.editDepAdminNameDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminUpdateNameDialog.dismiss();
            }
        });

        depAdminEditNameDialogBinding.editDepAdminUpdateNameDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (depAdminEditNameDialogBinding.editDepAdminDialogName.getText().length() < 3) {
                    depAdminEditNameDialogBinding.editDepAdminDialogName.setError("Please enter valid name");
                } else {
                    LoadingDialog.showLoadingDialog(EditDepAdminActivity.this);
                    updateNameToDb(depAdminEditNameDialogBinding.editDepAdminDialogName.getText().toString().trim());
                }
            }
        });
    }

    private void updateNameToDb(String updatedName) {
        depAdminModel.setDepAdminName(updatedName);

        HashMap<String, Object> map = new HashMap<>();

        map.put("depAdminName", depAdminModel.getDepAdminName());

        depAdminRef.child(depAdminModel.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(EditDepAdminActivity.this, "Department Admin Name Updated Successfully!", Toast.LENGTH_SHORT).show();
                    depAdminUpdateNameDialog.dismiss();

                    binding.editDepAdminName.setText(depAdminModel.getDepAdminName());

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void policeAlreadyAssignedOrNot(DepAdminModel depAdmin) {
        depAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {

            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot depAdminSnapshot : snapshot.getChildren()) {

                    DepAdminModel dep = depAdminSnapshot.getValue(DepAdminModel.class);

                    count++;

                    if (dep.getDepAdminPoliceStation().equalsIgnoreCase(depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.getText().toString())) {
                        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setText("");
                        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setError("Police Station already assigned. Please choose a different one");
                        Toast.makeText(EditDepAdminActivity.this, "Police Station already assigned. Please choose a different one", Toast.LENGTH_SHORT).show();
                        LoadingDialog.hideLoadingDialog();
                        check = true;
                        return;
                    } else if (count == snapshot.getChildrenCount()) {
                        if (!check) {
                            if (depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.length() == 0) {
                                LoadingDialog.hideLoadingDialog();
                                depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setError("Please enter valid name");
                            } else {
                                updatePoliceStationToDb(depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.getText().toString());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createDepAdminPoliceStationDialog() {
        depAdminEditPoliceStationDialogBinding = DepAdminEditPoliceStationDialogBinding.inflate(LayoutInflater.from(this));
        depAdminUpdatePoliceStationDialog = new Dialog(EditDepAdminActivity.this);
        depAdminUpdatePoliceStationDialog.setContentView(depAdminEditPoliceStationDialogBinding.getRoot());
        depAdminUpdatePoliceStationDialog.show();
        depAdminUpdatePoliceStationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//        fetchPoliceStationNameForDialogDropDown();
//        DropDownAdapter dropDownAdapter = new DropDownAdapter(EditDepAdminActivity.this, policeStationNameList);
//        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setAdapter(dropDownAdapter);
//        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());

        depAdminEditPoliceStationDialogBinding.editDepAdminCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminUpdatePoliceStationDialog.dismiss();
            }
        });

        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStationDialogUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingDialog.showLoadingDialog(EditDepAdminActivity.this);
                policeAlreadyAssignedOrNot(depAdminModel);
            }
        });
    }

    private void updatePoliceStationToDb(String policeStation) {
        depAdminModel.setDepAdminPoliceStation(policeStation);

        HashMap<String, Object> map = new HashMap<>();

        map.put("depAdminPoliceStation", depAdminModel.getDepAdminPoliceStation());

        depAdminRef.child(depAdminModel.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(EditDepAdminActivity.this, "Department Admin Police Station Updated Successfully!", Toast.LENGTH_SHORT).show();
                    depAdminUpdatePoliceStationDialog.dismiss();

                    binding.editDepAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPoliceStationNameForDialogDropDown() {

        policeStationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                policeStationNameList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    PoliceStationModel ps = dataSnapshot.getValue(PoliceStationModel.class);

                    policeStationNameList.add(ps.getPoliceStationName());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditDepAdminActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(DepAdminModel depAdminModel) {
        if (depAdminModel.getDepAdminStatus().equals("unblock")) {

            depAdminModel.setDepAdminStatus("block");

            HashMap<String, Object> map = new HashMap<>();

            map.put("depAdminStatus", depAdminModel.getDepAdminStatus());

            depAdminRef.child(depAdminModel.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        binding.depAdminStatusIcon.setImageResource(R.drawable.lock);
                        Toast.makeText(EditDepAdminActivity.this, "Department Admin Blocked Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (depAdminModel.getDepAdminStatus().equals("block")) {

            depAdminModel.setDepAdminStatus("unblock");

            HashMap<String, Object> map = new HashMap<>();

            map.put("depAdminStatus", depAdminModel.getDepAdminStatus());

            depAdminRef.child(depAdminModel.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        binding.depAdminStatusIcon.setImageResource(R.drawable.unlock);
                        Toast.makeText(EditDepAdminActivity.this, "Department Admin UnBlocked Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}