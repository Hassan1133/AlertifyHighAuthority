package com.example.alertify_main_admin.fragments;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.adapters.DepAdminAdp;
import com.example.alertify_main_admin.adapters.DropDownAdapter;
import com.example.alertify_main_admin.databinding.DepAdminBinding;
import com.example.alertify_main_admin.databinding.DepAdminDialogBinding;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.main_utils.NetworkUtils;
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
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class DepAdminFragment extends Fragment implements View.OnClickListener {

    private Dialog dialog;
    private ArrayList<String> policeStationNameList;

    private DropDownAdapter dropDownAdapter;
    private Uri imageUri;

    private DepAdminModel depAdmin;

    private DatabaseReference depAdminRef, policeStationsRef;
    private StorageReference firebaseStorageReference;

    private List<DepAdminModel> depAdmins;
    private DepAdminAdp adp;
    private String imageSize;

    private DepAdminBinding binding;

    private DepAdminDialogBinding depAdminDialogBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DepAdminBinding.inflate(inflater, container, false);
        init();
        fetchData();
        return binding.getRoot();
    }

    private void init() {
        binding.addBtn.setOnClickListener(this);

        policeStationsRef = FirebaseDatabase.getInstance().getReference("AlertifyPoliceStations");
        policeStationNameList = new ArrayList<>();

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        depAdmins = new ArrayList<DepAdminModel>();

        depAdminRef = FirebaseDatabase.getInstance().getReference("AlertifyDepAdmin");

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
        depAdminDialogBinding = DepAdminDialogBinding.inflate(LayoutInflater.from(getActivity()));
        dialog = new Dialog(getActivity());
        dialog.setContentView(depAdminDialogBinding.getRoot());
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        fetchPoliceStationNameForDropDown();
        dropDownAdapter = new DropDownAdapter(getActivity(), policeStationNameList);
        depAdminDialogBinding.depAdminPoliceStation.setAdapter(dropDownAdapter);

        depAdminDialogBinding.depAdminImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
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
                    depAdmin.setUid("");
                    depAdmin.setDepAdminStatus("unblock");

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

                        if (dep.getDepAdminEmail().equals(depAdminDialogBinding.depAdminEmail.getText().toString())) {
                            depAdminDialogBinding.depAdminEmail.setText("");
                            Toast.makeText(getActivity(), "Email already exists. Please choose a different one", Toast.LENGTH_SHORT).show();
                            depAdminDialogBinding.depAdminEmail.setError("Email already exists. Please choose a different one");

                            LoadingDialog.hideLoadingDialog();

                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                policeStationAlreadyAssignedOrNot(depAdmin);
                            }
                        }
                    }
                } else {
                    uploadImage(depAdmin);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void policeStationAlreadyAssignedOrNot(DepAdminModel depAdmin) {
        depAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {

            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot depAdminSnapshot : snapshot.getChildren()) {

                        DepAdminModel dep = depAdminSnapshot.getValue(DepAdminModel.class);

                        count++;

                        if (dep.getDepAdminPoliceStation().equalsIgnoreCase(depAdminDialogBinding.depAdminPoliceStation.getText().toString())) {
                            depAdminDialogBinding.depAdminPoliceStation.setText("");
                            Toast.makeText(getActivity(), "Police Station already assigned. Please choose a different one", Toast.LENGTH_SHORT).show();
                            depAdminDialogBinding.depAdminPoliceStation.setError("Police Station already assigned. Please choose a different one");
                            LoadingDialog.hideLoadingDialog();
                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                uploadImage(depAdmin);
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


    private void uploadImage(DepAdminModel depAdmin) {

        depAdmin.setDepAdminId(UUID.randomUUID().toString());

        StorageReference strRef = firebaseStorageReference.child("Alertify_Dep_Admin_Images/" + depAdmin.getDepAdminId());

        strRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                strRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        depAdmin.setDepAdminImageUrl(task.getResult().toString());
                        addToDb(depAdmin);
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

    private void addToDb(DepAdminModel depAdmin) {


        depAdminRef.child(depAdmin.getDepAdminId()).setValue(depAdmin).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(getActivity(), "Department Admin added successfully!", Toast.LENGTH_SHORT).show();
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

    private void pickImage() {
        getContent.launch("image/*");
    }

    ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            if (uri != null) {
                if (isImageSizeValid(uri)) {
                    imageUri = uri;
                    depAdminDialogBinding.depAdminImage.setImageURI(imageUri);
                } else {
                    Toast.makeText(getActivity(), imageSize + ". Please select an image smaller than 2 MB", Toast.LENGTH_SHORT).show();
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

    private boolean isValid() {
        boolean valid = true;

        if (imageUri == null) {
            Toast.makeText(getActivity(), "Please select the image", Toast.LENGTH_SHORT).show();
            valid = false;
        }

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
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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