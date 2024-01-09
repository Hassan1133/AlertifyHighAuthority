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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.adapters.BoundaryAdapter;
import com.example.alertify_main_admin.databinding.ActivityEditPoliceStationBinding;
import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.PoliceStationModel;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPoliceStationActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityEditPoliceStationBinding binding;
    private PoliceStationModel policeStation;
    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private DatabaseReference firebaseReference;

    private double selectedLatitude, selectedLongitude;

    private String imageUrl;

    private String imageSize;

    private List<LatLngWrapper> boundaryPoints;

    private BoundaryAdapter boundaryAdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPoliceStationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getIntentData();
    }

    private void init() {
        binding.editPoliceStationImg.setOnClickListener(this);
        binding.editPoliceStationUpdateBtn.setOnClickListener(this);
        binding.editAddPoliceStationLocationBtn.setOnClickListener(this);
        binding.editAddPoliceStationBoundaryBtn.setOnClickListener(this);

        firebaseStorageReference = FirebaseStorage.getInstance().getReference(); // firebase storage reference initialization

        firebaseReference = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations"); // firebase initialization

        binding.editPoliceStationBoundariesRecycler.setLayoutManager(new LinearLayoutManager(EditPoliceStationActivity.this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.editPoliceStationImg:
                pickImage();
                break;

            case R.id.editPoliceStationUpdateBtn:
                if (isValid()) {
                    LoadingDialog.showLoadingDialog(EditPoliceStationActivity.this);
                    if (imageUri == null) {
                        updateToDb();
                    } else if (imageUri != null) {
                        updateImg();
                    }
                }
                break;

            case R.id.editAddPoliceStationLocationBtn:
                Intent locationIntent = new Intent(EditPoliceStationActivity.this, MapsActivity.class);
                someActivityResultLauncher.launch(locationIntent);
                break;
            case R.id.editAddPoliceStationBoundaryBtn:
                Intent boundaryIntent = new Intent(EditPoliceStationActivity.this, PoliceStationBoundaryMapsActivity.class);
                policeStationBoundaryResultLauncher.launch(boundaryIntent);
                break;
        }
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

        if (binding.editPoliceStationName.getText().length() < 3) {
            binding.editPoliceStationName.setError("Please enter valid name");
            valid = false;
        }
        if (binding.editPoliceStationNumber.getText().length() == 0) {
            binding.editPoliceStationNumber.setError("Please enter valid number");
            valid = false;
        }
        if (binding.editPoliceStationLocation.getText().length() < 3) {
            binding.editPoliceStationLocation.setError("Please select valid location");
            valid = false;
        }
        if(boundaryPoints.isEmpty())
        {
            Toast.makeText(this, "Please select boundary of police station", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private void updateToDb() {
        policeStation.setImgUrl(imageUrl);
        policeStation.setPoliceStationName(binding.editPoliceStationName.getText().toString().trim());
        policeStation.setPoliceStationLocation(binding.editPoliceStationLocation.getText().toString().trim());
        policeStation.setPoliceStationNumber(binding.editPoliceStationNumber.getText().toString().trim());
        policeStation.setBoundaries(boundaryPoints);

        HashMap<String, Object> map = new HashMap<>();
        map.put("imgUrl", policeStation.getImgUrl());
        map.put("policeStationName", policeStation.getPoliceStationName());
        map.put("policeStationLocation", policeStation.getPoliceStationLocation());
        map.put("policeStationNumber", policeStation.getPoliceStationNumber());
        map.put("policeStationLatitude", selectedLatitude);
        map.put("policeStationLongitude", selectedLongitude);
        map.put("boundaries",boundaryPoints);

        firebaseReference.child(policeStation.getId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Glide.with(getApplicationContext()).load(policeStation.getImgUrl()).into(binding.editPoliceStationImg);
                binding.editPoliceStationName.setText(policeStation.getPoliceStationName());
                binding.editPoliceStationNumber.setText(policeStation.getPoliceStationNumber());
                binding.editPoliceStationLocation.setText(policeStation.getPoliceStationLocation());
                setDataToBoundaryRecycler(policeStation.getBoundaries());
                LoadingDialog.hideLoadingDialog();
                Toast.makeText(EditPoliceStationActivity.this, "Data Updated Successfully", Toast.LENGTH_SHORT).show();
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
        Glide.with(getApplicationContext()).load(imageUrl).into(binding.editPoliceStationImg);
        binding.editPoliceStationName.setText(policeStation.getPoliceStationName());
        binding.editPoliceStationNumber.setText(policeStation.getPoliceStationNumber());
        binding.editPoliceStationLocation.setText(policeStation.getPoliceStationLocation());
        selectedLatitude = policeStation.getPoliceStationLatitude();
        selectedLongitude = policeStation.getPoliceStationLongitude();
        boundaryPoints = policeStation.getBoundaries();
        setDataToBoundaryRecycler(boundaryPoints);
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
                    binding.editPoliceStationImg.setImageURI(imageUri);
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
                binding.editPoliceStationLocation.setText(result.getData().getStringExtra("address"));
                selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                selectedLongitude = result.getData().getDoubleExtra("longitude", 0);
            }
        }
    });

    private ActivityResultLauncher<Intent> policeStationBoundaryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                boundaryPoints = (List<LatLngWrapper>) result.getData().getSerializableExtra("latLngList");
                setDataToBoundaryRecycler(boundaryPoints);
            }
        }
    });

    private void setDataToBoundaryRecycler(List<LatLngWrapper> boundaryList) {
        boundaryAdp = new BoundaryAdapter(EditPoliceStationActivity.this, boundaryList);
        binding.editPoliceStationBoundariesRecycler.setAdapter(boundaryAdp);
    }
}