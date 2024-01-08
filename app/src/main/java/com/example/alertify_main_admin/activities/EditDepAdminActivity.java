package com.example.alertify_main_admin.activities;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.adapters.DropDownAdapter;
import com.example.alertify_main_admin.model.DepAdminModel;
import com.example.alertify_main_admin.model.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class EditDepAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private DepAdminModel depAdminModel;
    private String imageUrl;
    private CircleImageView depAdminImg;

    private TextView depAdminName, depAdminPoliceStation, depAdminEmail;

    private Dialog depAdminUpdateImgDialog, depAdminUpdateNameDialog, depAdminUpdatePoliceStationDialog;

    private ShapeableImageView depAdminDialogImg;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private ProgressBar depAdminImgProgressBar, depAdminNameProgressBar, depAdminPoliceStationProgressBar;

    private DatabaseReference depAdminRef, policeStationsRef;
    private ImageView depAdminNameEditBtn, depAdminPoliceStationEditBtn, statusBtn;
    private TextInputEditText depAdminDialogName;
    private DropDownAdapter dropDownAdapter;
    private ArrayList<String> policeStationNameList;
    private AutoCompleteTextView depAdminDialogPoliceStation;
    private String imageSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dep_admin);
        init();
        getIntentData();
    }

    private void init() {
        depAdminImg = findViewById(R.id.dep_admin_image);
        depAdminImg.setOnClickListener(this);
        depAdminName = findViewById(R.id.dep_admin_name);
        depAdminPoliceStation = findViewById(R.id.dep_admin_police_station);
        depAdminEmail = findViewById(R.id.dep_admin_email);

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        depAdminRef = FirebaseDatabase.getInstance().getReference("AlertifyDepAdmin");

        depAdminNameEditBtn = findViewById(R.id.name_edit_btn);
        depAdminNameEditBtn.setOnClickListener(this);

        policeStationsRef = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations");
        policeStationNameList = new ArrayList<>();

        depAdminPoliceStationEditBtn = findViewById(R.id.police_station_edit_btn);
        depAdminPoliceStationEditBtn.setOnClickListener(this);

        statusBtn = findViewById(R.id.status_icon);
        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus(depAdminModel);
            }
        });
    }

    private void getIntentData() {
        depAdminModel = (DepAdminModel) getIntent().getSerializableExtra("depAdminModel");
        imageUrl = depAdminModel.getDepAdminImageUrl();
        Glide.with(getApplicationContext()).load(imageUrl).into(depAdminImg);
        depAdminName.setText(depAdminModel.getDepAdminName());
        depAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());
        depAdminEmail.setText(depAdminModel.getDepAdminEmail());
        if (depAdminModel.getDepAdminStatus().equals("unblock")) {
            statusBtn.setImageResource(R.drawable.unlock);
        } else if (depAdminModel.getDepAdminStatus().equals("block")) {
            statusBtn.setImageResource(R.drawable.lock);
        }
    }

    private void createDepAdminImageDialog() {
        depAdminUpdateImgDialog = new Dialog(EditDepAdminActivity.this);
        depAdminUpdateImgDialog.setContentView(R.layout.dep_admin_edit_img_dialog);
        depAdminUpdateImgDialog.show();
        depAdminUpdateImgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        depAdminImgProgressBar = depAdminUpdateImgDialog.findViewById(R.id.dep_admin_img_progressbar);

        depAdminDialogImg = depAdminUpdateImgDialog.findViewById(R.id.dep_admin_dialog_image);
        Glide.with(getApplicationContext()).load(imageUrl).into(depAdminDialogImg);

        depAdminDialogImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickNewImage();
            }
        });

        depAdminUpdateImgDialog.findViewById(R.id.dep_admin_img_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminUpdateImgDialog.dismiss();
            }
        });

        depAdminUpdateImgDialog.findViewById(R.id.dep_admin_img_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminImgProgressBar.setVisibility(View.VISIBLE);
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
                    depAdminImgProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditDepAdminActivity.this, "Department Admin Image Updated Successfully!", Toast.LENGTH_SHORT).show();
                    depAdminUpdateImgDialog.dismiss();

                    Glide.with(getApplicationContext()).load(imageUrl).into(depAdminImg);
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
                    depAdminDialogImg.setImageURI(imageUri);
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
            case R.id.dep_admin_image:
                createDepAdminImageDialog();
                break;
            case R.id.name_edit_btn:
                createDepAdminNameDialog();
                break;
            case R.id.police_station_edit_btn:
                createDepAdminPoliceStationDialog();
                break;
        }
    }

    private void createDepAdminNameDialog() {
        depAdminUpdateNameDialog = new Dialog(EditDepAdminActivity.this);
        depAdminUpdateNameDialog.setContentView(R.layout.dep_admin_edit_name_dialog);
        depAdminUpdateNameDialog.show();
        depAdminUpdateNameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        depAdminNameProgressBar = depAdminUpdateNameDialog.findViewById(R.id.dep_admin_name_progressbar);

        depAdminDialogName = depAdminUpdateNameDialog.findViewById(R.id.dep_admin_dialog_name);
        depAdminDialogName.setText(depAdminModel.getDepAdminName());

        depAdminUpdateNameDialog.findViewById(R.id.dep_admin_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminUpdateNameDialog.dismiss();
            }
        });

        depAdminUpdateNameDialog.findViewById(R.id.dep_admin_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (depAdminDialogName.getText().length() < 3) {
                    depAdminDialogName.setError("Please enter valid name");
                } else {
                    depAdminNameProgressBar.setVisibility(View.VISIBLE);
                    updateNameToDb(depAdminDialogName.getText().toString().trim());
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
                    depAdminNameProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditDepAdminActivity.this, "Department Admin Name Updated Successfully!", Toast.LENGTH_SHORT).show();
                    depAdminUpdateNameDialog.dismiss();

                    depAdminName.setText(depAdminModel.getDepAdminName());

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

                    if (dep.getDepAdminPoliceStation().equalsIgnoreCase(depAdminDialogPoliceStation.getText().toString())) {
                        depAdminDialogPoliceStation.setText("");
                        depAdminDialogPoliceStation.setError("Police Station already assigned. Please choose a different one");
                        Toast.makeText(EditDepAdminActivity.this, "Police Station already assigned. Please choose a different one", Toast.LENGTH_SHORT).show();
                        depAdminPoliceStationProgressBar.setVisibility(View.INVISIBLE);
                        check = true;
                        return;
                    } else if (count == snapshot.getChildrenCount()) {
                        if (!check) {
                            if (depAdminDialogPoliceStation.length() == 0) {
                                depAdminPoliceStationProgressBar.setVisibility(View.INVISIBLE);
                                depAdminDialogPoliceStation.setError("Please enter valid name");
                            } else {
                                updatePoliceStationToDb(depAdminDialogPoliceStation.getText().toString());
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
        depAdminUpdatePoliceStationDialog = new Dialog(EditDepAdminActivity.this);
        depAdminUpdatePoliceStationDialog.setContentView(R.layout.dep_admin_edit_police_station_dialog);
        depAdminUpdatePoliceStationDialog.show();
        depAdminUpdatePoliceStationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        depAdminPoliceStationProgressBar = depAdminUpdatePoliceStationDialog.findViewById(R.id.dep_admin_police_station_progressbar);

        depAdminDialogPoliceStation = depAdminUpdatePoliceStationDialog.findViewById(R.id.dep_admin_police_station);

        fetchPoliceStationNameForDialogDropDown();
        dropDownAdapter = new DropDownAdapter(EditDepAdminActivity.this, policeStationNameList);
        depAdminDialogPoliceStation.setAdapter(dropDownAdapter);
        depAdminDialogPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());

        depAdminUpdatePoliceStationDialog.findViewById(R.id.dep_admin_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminUpdatePoliceStationDialog.dismiss();
            }
        });

        depAdminUpdatePoliceStationDialog.findViewById(R.id.dep_admin_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depAdminPoliceStationProgressBar.setVisibility(View.VISIBLE);
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
                    depAdminPoliceStationProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditDepAdminActivity.this, "Department Admin Police Station Updated Successfully!", Toast.LENGTH_SHORT).show();
                    depAdminUpdatePoliceStationDialog.dismiss();

                    depAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());

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
                        statusBtn.setImageResource(R.drawable.lock);
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
                        statusBtn.setImageResource(R.drawable.unlock);
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