package com.example.alertify_main_admin.fragments;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.LoginSignupActivity;
import com.example.alertify_main_admin.databinding.SignupBinding;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignupFragment extends Fragment implements View.OnClickListener {

    private Uri imageUri;

    private UserModel user;

    private FirebaseAuth firebaseAuth;

    private StorageReference firebaseStorageReference;

    private DatabaseReference firebaseDatabaseReference;

    private String imageSize;

    private SignupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SignupBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() // method for widgets or variables initialization
    {
        binding.pickImgIcon.setOnClickListener(this);
        binding.signupBtn.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("AlertifyHighAuthority");
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.pickImgIcon:

                chooseImage();
                break;

            case R.id.signupBtn:
                createAccount();
                break;
        }
    }
    private void createAccount() // method for create account
    {
        if (isValid()) {
            LoadingDialog.showLoadingDialog(getActivity());
            user = new UserModel();
            user.setName(binding.name.getText().toString().trim());
            user.setEmail(binding.email.getText().toString().trim());

            firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), binding.password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        user.setId(firebaseAuth.getUid());
                        uploadImage(user);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    binding.email.setText("");
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void uploadImage(UserModel user) // method for upload image
    {

        StorageReference strRef = firebaseStorageReference.child("Alertify_High_Authority_Images/" + user.getId());


        strRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                strRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        user.setImgUrl(task.getResult().toString());
                        addToDB(user);

                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LoadingDialog.hideLoadingDialog();
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

    private void addToDB(@NonNull UserModel user) // method for add data to the database
    {
        firebaseDatabaseReference.child(user.getId()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(getContext(), "Signed up Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginSignupActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LoadingDialog.hideLoadingDialog();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid() // method for data validation
    {
        boolean valid = true;

        if (imageUri == null) {
            Toast.makeText(getContext(), "select your image", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (binding.name.getText().length() < 3) {
            binding.name.setError("enter valid name");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText()).matches()) {
            binding.email.setError("enter valid email");
            valid = false;
        }
        if (binding.password.getText().length() < 6) {
            binding.password.setError("enter valid password");
            valid = false;
        }

        return valid;
    }

    private void chooseImage() // method for get image from gallery
    {
        getContent.launch("image/*");
    }

    ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            if (uri != null) {

                if (isImageSizeValid(uri)) {
                    imageUri = uri;
                    binding.image.setImageURI(imageUri);
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

}
