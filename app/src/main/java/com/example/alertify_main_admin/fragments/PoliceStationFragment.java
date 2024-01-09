package com.example.alertify_main_admin.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.PoliceStationBoundaryMapsActivity;
import com.example.alertify_main_admin.adapters.BoundaryAdapter;
import com.example.alertify_main_admin.adapters.DepAdminAdp;
import com.example.alertify_main_admin.adapters.PoliceStationAdp;
import com.example.alertify_main_admin.activities.MapsActivity;
import com.example.alertify_main_admin.databinding.PoliceStationBinding;
import com.example.alertify_main_admin.databinding.PoliceStationDialogBinding;
import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.main_utils.NetworkUtils;
import com.example.alertify_main_admin.models.DepAdminModel;
import com.example.alertify_main_admin.models.PoliceStationModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class PoliceStationFragment extends Fragment implements View.OnClickListener {

    private PoliceStationBinding binding;
    private double selectedLatitude, selectedLongitude;
    private Dialog dialog;

    private PoliceStationModel policeStation;

    private DatabaseReference firebaseReference;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private List<PoliceStationModel> policeStations;

    private PoliceStationAdp adp;
    private String randomId;

    private String imageSize;

    private List<LatLngWrapper> boundaryPoints;

    private BoundaryAdapter boundaryAdp;

    private PoliceStationDialogBinding policeStationDialogBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PoliceStationBinding.inflate(inflater, container, false);
        init();
        fetchData();
        return binding.getRoot();
    }

    private void init() {
        binding.addBtn.setOnClickListener(this);

        firebaseReference = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations"); // firebase initialization

        firebaseStorageReference = FirebaseStorage.getInstance().getReference(); // firebase storage reference initialization

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
    }

    private void search(String newText) {
        ArrayList<PoliceStationModel> searchList = new ArrayList<>();
        for (PoliceStationModel i : policeStations) {
            if (i.getPoliceStationName().toLowerCase().contains(newText.toLowerCase()) || i.getPoliceStationLocation().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        adp = new PoliceStationAdp(getActivity(), searchList);
        binding.policeStationRecycler.setAdapter(adp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addBtn:
                if (NetworkUtils.isInternetAvailable(getActivity())) {
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
        dialog = new Dialog(getActivity());
        dialog.setContentView(policeStationDialogBinding.getRoot());
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        policeStationDialogBinding.boundariesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        policeStationDialogBinding.dialogPoliceStationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
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
                    policeStation.setPoliceStationNumber(policeStationDialogBinding.dialogPoliceStationNumber.getText().toString());
                    policeStation.setPoliceStationLocation(policeStationDialogBinding.dialogPoliceStationLocation.getText().toString());
                    policeStation.setPoliceStationLatitude(selectedLatitude);
                    policeStation.setPoliceStationLongitude(selectedLongitude);
                    policeStation.setBoundaries(boundaryPoints);

                    checkPoliceStationExists(policeStation.getPoliceStationNumber());
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

    private void checkPoliceStationExists(String number) {
        firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot policeStationSnapshot : snapshot.getChildren()) {

                        PoliceStationModel ps = policeStationSnapshot.getValue(PoliceStationModel.class);

                        count++;

                        if (ps.getPoliceStationNumber().toLowerCase().trim().equals(number.toLowerCase().trim())) {
                            policeStationDialogBinding.dialogPoliceStationNumber.setText("");
                            Toast.makeText(getActivity(), "Police Station number already exists. Please enter a different one", Toast.LENGTH_SHORT).show();
                            policeStationDialogBinding.dialogPoliceStationNumber.setError("Police Station number already exists. Please enter a different one");
                            LoadingDialog.hideLoadingDialog();
                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                uploadImage();
                            }
                        }
                    }
                } else {
                    uploadImage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadImage() // method for upload image
    {
        randomId = firebaseReference.push().getKey();
        StorageReference strRef = firebaseStorageReference.child("Alertify_Police_Station_Images/" + randomId);

        strRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                strRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        policeStation.setImgUrl(task.getResult().toString());
                        addToDb(policeStation);
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
                    policeStationDialogBinding.dialogPoliceStationImg.setImageURI(imageUri);
                } else {
                    Toast.makeText(getActivity(), imageSize + ". Please select an image smaller than 2 MB", Toast.LENGTH_SHORT).show();
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
        Cursor cursor = getActivity().getContentResolver().query(imageUri, null, null, null, null);
        if (cursor == null) {
            throw new Exception("Cursor is null");
        }
        cursor.moveToFirst();
        long sizeInBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
        cursor.close();
        return sizeInBytes;
    }

    private void addToDb(PoliceStationModel policeStation) {

        policeStation.setId(randomId);

        firebaseReference.child(policeStation.getId()).setValue(policeStation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(getActivity(), "Police Station added successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean isValid() {
        boolean valid = true;

        if (imageUri == null) {
            Toast.makeText(getActivity(), "Please select your image", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (policeStationDialogBinding.dialogPoliceStationName.getText().length() < 3) {
            policeStationDialogBinding.dialogPoliceStationName.setError("Please enter valid name");
            valid = false;
        }
        if (policeStationDialogBinding.dialogPoliceStationNumber.getText().length() == 0) {
            policeStationDialogBinding.dialogPoliceStationNumber.setError("Please enter valid number");
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

        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                policeStations.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    policeStations.add(dataSnapshot.getValue(PoliceStationModel.class));
                }

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
        adp = new PoliceStationAdp(getActivity(), policeStations);
        binding.policeStationRecycler.setAdapter(adp);
    }


    private ActivityResultLauncher<Intent> policeStationLocationResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                policeStationDialogBinding.dialogPoliceStationLocation.setText(result.getData().getStringExtra("address"));
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
        boundaryAdp = new BoundaryAdapter(getActivity(), boundaryList);
        policeStationDialogBinding.boundariesRecycler.setAdapter(boundaryAdp);
    }
}
