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
import com.example.alertify_main_admin.adapters.PoliceStationAdp;
import com.example.alertify_main_admin.activities.MapsActivity;
import com.example.alertify_main_admin.main_utils.NetworkUtils;
import com.example.alertify_main_admin.model.PoliceStationModel;
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

    private FloatingActionButton addPoliceStationBtn;

    private EditText policeStationName, policeStationLocation, policeStationNumber;

    private double selectedLatitude, selectedLongitude;

    private Dialog dialog;

    private PoliceStationModel policeStation;

    private DatabaseReference firebaseReference;

    private ImageView police_station_img;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private RecyclerView recyclerView;

    private List<PoliceStationModel> policeStations;

    private PoliceStationAdp adp;

    private ProgressBar dialogProgressBar, fragmentProgressBar;

    private String randomId;

    private SearchView searchView;

    private String imageSize;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.police_station, container, false);
        init(view);
        fetchData();
        return view;
    }

    private void init(View view) {
        addPoliceStationBtn = view.findViewById(R.id.addBtn);
        addPoliceStationBtn.setOnClickListener(this);

        firebaseReference = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations"); // firebase initialization

        firebaseStorageReference = FirebaseStorage.getInstance().getReference(); // firebase storage reference initialization

        policeStations = new ArrayList<PoliceStationModel>();

        recyclerView = view.findViewById(R.id.policeStationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fragmentProgressBar = view.findViewById(R.id.progressbar);

        searchView = view.findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    }

    private void search(String newText) {
        ArrayList<PoliceStationModel> searchList = new ArrayList<>();
        for (PoliceStationModel i : policeStations) {
            if (i.getPoliceStationName().toLowerCase().contains(newText.toLowerCase()) || i.getPoliceStationLocation().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        adp = new PoliceStationAdp(getActivity(), searchList);
        recyclerView.setAdapter(adp);
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
                    Toast.makeText(getActivity(), "Please turn on your internet.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void createDialog() {
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.police_station_dialog);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        policeStationName = dialog.findViewById(R.id.police_station_name);
        policeStationNumber = dialog.findViewById(R.id.police_station_number);
        policeStationLocation = dialog.findViewById(R.id.police_station_location);
        police_station_img = dialog.findViewById(R.id.police_station_img);
        dialogProgressBar = dialog.findViewById(R.id.progressbar);

        police_station_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        dialog.findViewById(R.id.add_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                mapsActivityResultLauncher.launch(intent);
            }
        });

        dialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValid()) {
                    dialogProgressBar.setVisibility(View.VISIBLE);

                    policeStation = new PoliceStationModel();

                    policeStation.setPoliceStationName(policeStationName.getText().toString());
                    policeStation.setPoliceStationNumber(policeStationNumber.getText().toString());
                    policeStation.setPoliceStationLocation(policeStationLocation.getText().toString());
                    policeStation.setPoliceStationLatitude(selectedLatitude);
                    policeStation.setPoliceStationLongitude(selectedLongitude);

                    checkPoliceStationExists(policeStation.getPoliceStationNumber());

                }
            }
        });

        dialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
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
                            policeStationNumber.setText("");
                            Toast.makeText(getActivity(), "Police Station number already exists. Please enter a different one", Toast.LENGTH_SHORT).show();
                            policeStationNumber.setError("Police Station number already exists. Please enter a different one");
                            dialogProgressBar.setVisibility(View.INVISIBLE);
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
                    police_station_img.setImageURI(imageUri);
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
                    dialogProgressBar.setVisibility(View.INVISIBLE);
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
        if (selectedLatitude == 0 || selectedLongitude == 0) {
            Toast.makeText(getActivity(), "Please select location again", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void fetchData() {

        fragmentProgressBar.setVisibility(View.VISIBLE);

        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                policeStations.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    policeStations.add(dataSnapshot.getValue(PoliceStationModel.class));
                }

                fragmentProgressBar.setVisibility(View.INVISIBLE);

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
        recyclerView.setAdapter(adp);
    }


    private ActivityResultLauncher<Intent> mapsActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
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
