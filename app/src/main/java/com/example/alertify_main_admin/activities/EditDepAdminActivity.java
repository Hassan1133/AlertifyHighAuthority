package com.example.alertify_main_admin.activities;

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
    private String imageUrl;
    private Dialog depAdminUpdateImgDialog, depAdminUpdateNameDialog, depAdminUpdatePoliceStationDialog;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private DatabaseReference depAdminRef, policeStationsRef;
    private DropDownAdapter dropDownAdapter;
    private ArrayList<String> policeStationNameList;
    private String imageSize;
    private DepAdminEditNameDialogBinding depAdminEditNameDialogBinding;

    private DepAdminEditImgDialogBinding depAdminEditImgDialogBinding;

    private DepAdminEditPoliceStationDialogBinding depAdminEditPoliceStationDialogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditDepAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getIntentData();
    }

    private void init() {

        binding.editDepAdminImage.setOnClickListener(this);

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        depAdminRef = FirebaseDatabase.getInstance().getReference("AlertifyDepAdmin");

        binding.depAdminNameEditBtn.setOnClickListener(this);

        policeStationsRef = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations");
        policeStationNameList = new ArrayList<>();

        binding.depAdminPoliceStationEditBtn.setOnClickListener(this);

        binding.depAdminStatusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus(depAdminModel);
            }
        });
    }

    private void getIntentData() {
        depAdminModel = (DepAdminModel) getIntent().getSerializableExtra("depAdminModel");
        imageUrl = depAdminModel.getDepAdminImageUrl();
        Glide.with(getApplicationContext()).load(imageUrl).into(binding.editDepAdminImage);
        binding.editDepAdminName.setText(depAdminModel.getDepAdminName());
        binding.editDepAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());
        binding.editDepAdminEmail.setText(depAdminModel.getDepAdminEmail());
        if (depAdminModel.getDepAdminStatus().equals("unblock")) {
            binding.depAdminStatusIcon.setImageResource(R.drawable.unlock);
        } else if (depAdminModel.getDepAdminStatus().equals("block")) {
            binding.depAdminStatusIcon.setImageResource(R.drawable.lock);
        }
    }

    private void createDepAdminImageDialog() {
        depAdminEditImgDialogBinding = DepAdminEditImgDialogBinding.inflate(LayoutInflater.from(this));
        depAdminUpdateImgDialog = new Dialog(EditDepAdminActivity.this);
        depAdminUpdateImgDialog.setContentView(depAdminEditImgDialogBinding.getRoot());
        depAdminUpdateImgDialog.show();
        depAdminUpdateImgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Glide.with(getApplicationContext()).load(imageUrl).into(depAdminEditImgDialogBinding.editDepAdminDialogImage);

        depAdminEditImgDialogBinding.editDepAdminDialogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickNewImage();
            }
        });

        depAdminEditImgDialogBinding.editDepAdminImgCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminUpdateImgDialog.dismiss();
            }
        });

        depAdminEditImgDialogBinding.editDepAdminImgUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingDialog.showLoadingDialog(EditDepAdminActivity.this);
                if (imageUri == null) {
                    updateImageUrlToDb(depAdminModel);
                } else if (imageUri != null) {
                    uploadImage();
                }
            }
        });

    }

    private void uploadImage() {
        StorageReference strRef = firebaseStorageReference.child("Alertify_Dep_Admin_Images/" + depAdminModel.getDepAdminId());

        strRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                strRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        imageUrl = task.getResult().toString();
                        depAdminModel.setDepAdminImageUrl(imageUrl);
                        updateImageUrlToDb(depAdminModel);
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageUrlToDb(DepAdminModel depAdminModel) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("depAdminImageUrl", depAdminModel.getDepAdminImageUrl());

        depAdminRef.child(depAdminModel.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(EditDepAdminActivity.this, "Department Admin Image Updated Successfully!", Toast.LENGTH_SHORT).show();
                    depAdminUpdateImgDialog.dismiss();

                    Glide.with(getApplicationContext()).load(imageUrl).into(binding.editDepAdminImage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditDepAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickNewImage() {
        getContent.launch("image/*");
    }

    ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            if (uri != null) {
                if (isImageSizeValid(uri)) {
                    imageUri = uri;
                    depAdminEditImgDialogBinding.editDepAdminDialogImage.setImageURI(imageUri);
                } else {
                    Toast.makeText(EditDepAdminActivity.this, imageSize + ". Please select an image smaller than 2 MB", Toast.LENGTH_SHORT).show();
                }
            }
        }
    });

    // Method to check if the selected image size is valid (less than 2MB)
    private boolean isImageSizeValid(Uri imageUri) {
        try {
            // Get the image file size in bytes
            long imageSizeInBytes = getImageSizeInBytes(imageUri);

            // Convert the size to MB
            double imageSizeInMB = imageSizeInBytes / (1024.0 * 1024.0);

            imageSize = String.format("Selected image size is %.2f MB", imageSizeInMB);

            // Compare with the 2MB limit
            return imageSizeInMB < 2.0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to get the image file size in bytes
    private long getImageSizeInBytes(Uri imageUri) throws Exception {
        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
        if (cursor == null) {
            throw new Exception("Cursor is null");
        }
        cursor.moveToFirst();
        long sizeInBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
        cursor.close();
        return sizeInBytes;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editDepAdminImage:
                createDepAdminImageDialog();
                break;
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

        fetchPoliceStationNameForDialogDropDown();
        dropDownAdapter = new DropDownAdapter(EditDepAdminActivity.this, policeStationNameList);
        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setAdapter(dropDownAdapter);
        depAdminEditPoliceStationDialogBinding.editDepAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());

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