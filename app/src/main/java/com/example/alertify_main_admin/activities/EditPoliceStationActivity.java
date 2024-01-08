package com.example.alertify_main_admin.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.model.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPoliceStationActivity extends AppCompatActivity implements View.OnClickListener {

    private PoliceStationModel policeStation;

    private CircleImageView policeStationImg;

    private EditText policeStationName, policeStationLocation, policeStationNumber;

    private Button updateBtn;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private DatabaseReference firebaseReference;

    private ImageView addLocation;

    private ProgressBar progressbar;
    private double selectedLatitude, selectedLongitude;

    private String imageUrl;

    private String imageSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_police_station);
        init();
        getIntentData();
    }

    private void init() {
        policeStationImg = findViewById(R.id.police_station_img);
        policeStationImg.setOnClickListener(this);

        policeStationName = findViewById(R.id.police_station_name);
        policeStationLocation = findViewById(R.id.police_station_location);
        policeStationNumber = findViewById(R.id.police_station_number);

        updateBtn = findViewById(R.id.update_btn);
        updateBtn.setOnClickListener(this);

        addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(this);

        firebaseStorageReference = FirebaseStorage.getInstance().getReference(); // firebase storage reference initialization

        firebaseReference = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations"); // firebase initialization

        progressbar = findViewById(R.id.progressbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.police_station_img:
                pickImage();
                break;

            case R.id.update_btn:
                if (isValid()) {
                    progressbar.setVisibility(View.VISIBLE);
                    if (imageUri == null) {
                        updateToDb();
                    } else if (imageUri != null) {
                        updateImg();
                    }
                }
                break;

            case R.id.add_location:
                selectLocation();
                break;
        }
    }

    private void selectLocation() {
        Intent intent = new Intent(EditPoliceStationActivity.this, MapsActivity.class);
        someActivityResultLauncher.launch(intent);
    }

    private void updateImg() {
        StorageReference strRef = firebaseStorageReference.child("Alertify_Police_Station_Images/" + policeStation.getId());

        strRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                strRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        imageUrl = task.getResult().toString();
                        updateToDb();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(EditPoliceStationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditPoliceStationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isValid() {
        boolean valid = true;

        if (policeStationName.getText().length() < 3) {
            policeStationName.setError("Please enter valid name");
            valid = false;
        }
        if (policeStationNumber.getText().length() == 0) {
            policeStationNumber.setError("Please enter valid number");
            valid = false;
        }
        if (policeStationLocation.getText().length() < 3) {
            policeStationLocation.setError("Please select valid location");
            valid = false;
        }

        return valid;
    }

    private void updateToDb() {
        policeStation.setImgUrl(imageUrl);
        policeStation.setPoliceStationName(policeStationName.getText().toString().trim());
        policeStation.setPoliceStationLocation(policeStationLocation.getText().toString().trim());
        policeStation.setPoliceStationNumber(policeStationNumber.getText().toString().trim());

        HashMap<String, Object> map = new HashMap<>();
        map.put("imgUrl", policeStation.getImgUrl());
        map.put("policeStationName", policeStation.getPoliceStationName());
        map.put("policeStationLocation", policeStation.getPoliceStationLocation());
        map.put("policeStationNumber", policeStation.getPoliceStationNumber());
        map.put("policeStationLatitude", selectedLatitude);
        map.put("policeStationLongitude", selectedLongitude);

        firebaseReference.child(policeStation.getId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressbar.setVisibility(View.INVISIBLE);
                Toast.makeText(EditPoliceStationActivity.this, "Data Updated Successfully", Toast.LENGTH_SHORT).show();

                Glide.with(getApplicationContext()).load(policeStation.getImgUrl()).into(policeStationImg);
                policeStationName.setText(policeStation.getPoliceStationName());
                policeStationNumber.setText(policeStation.getPoliceStationNumber());
                policeStationLocation.setText(policeStation.getPoliceStationLocation());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditPoliceStationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getIntentData() {

        policeStation = (PoliceStationModel) getIntent().getSerializableExtra("policeStation");
        imageUrl = policeStation.getImgUrl();
        Glide.with(getApplicationContext()).load(imageUrl).into(policeStationImg);
        policeStationName.setText(policeStation.getPoliceStationName());
        policeStationNumber.setText(policeStation.getPoliceStationNumber());
        policeStationLocation.setText(policeStation.getPoliceStationLocation());
        selectedLatitude = policeStation.getPoliceStationLatitude();
        selectedLongitude = policeStation.getPoliceStationLongitude();
    }

    private void pickImage() {
        getContent.launch("image/*");
    }

    ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            if (uri != null) {
                if (isImageSizeValid(uri)) {
                    imageUri = uri;
                    policeStationImg.setImageURI(imageUri);
                } else {
                    Toast.makeText(EditPoliceStationActivity.this, imageSize + ". Please select an image smaller than 2 MB", Toast.LENGTH_SHORT).show();
                }
            }
        }
    });

    // Method to check if the selected image size is valid (less than 3MB)
    private boolean isImageSizeValid(Uri imageUri) {
        try {
            // Get the image file size in bytes
            long imageSizeInBytes = getImageSizeInBytes(imageUri);

            // Convert the size to MB
            double imageSizeInMB = imageSizeInBytes / (1024.0 * 1024.0);

            imageSize = String.format("Selected image size is %.2f MB", imageSizeInMB);

            // Compare with the 3MB limit
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

    private ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                policeStationLocation.setText(result.getData().getStringExtra("address"));
                selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                selectedLongitude = result.getData().getDoubleExtra("longitude", 0);
            }
        }
    });
}