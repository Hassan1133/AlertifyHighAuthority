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

    private ImageView pick_img_icon, userImg;

    private TextInputEditText name, email, password;

    private Button signupBtn;

    private ProgressBar loadingProgressBar;
    private Uri imageUri;

    private UserModel user;

    private FirebaseAuth firebaseAuth;

    private StorageReference firebaseStorageReference;

    private DatabaseReference firebaseDatabaseReference;

    private Dialog loadingDialog;

    private String imageSize;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        init(view);
        return view;

    }

    private void init(@NonNull View view) // method for widgets or variables initialization
    {
        pick_img_icon = view.findViewById(R.id.pick_img_icon);
        pick_img_icon.setOnClickListener(this);

        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);

        signupBtn = view.findViewById(R.id.signup_btn);
        signupBtn.setOnClickListener(this);

        userImg = view.findViewById(R.id.image);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("AlertifyHighAuthority");
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.pick_img_icon:

                chooseImage();
                break;

            case R.id.signup_btn:
                createAccount();
                break;
        }
    }


    private void createLoadingDialog() {
        loadingDialog = new Dialog(getActivity());
        loadingDialog.setContentView(R.layout.profile_loading_dialog);
        loadingDialog.show();
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView loadingTxt = loadingDialog.findViewById(R.id.loading);
        loadingTxt.setText("Signing up....");

        loadingProgressBar = loadingDialog.findViewById(R.id.profile_progressbar);

        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void createAccount() // method for create account
    {
        if (isValid()) {

            createLoadingDialog();

            user = new UserModel();
            user.setName(name.getText().toString().trim());
            user.setEmail(email.getText().toString().trim());

            firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                    email.setText("");
                    loadingDialog.dismiss();
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
                        loadingDialog.dismiss();

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
                    loadingDialog.dismiss();
                    Toast.makeText(getContext(), "Signed up Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginSignupActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
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

        if (name.getText().length() < 3) {
            name.setError("enter valid name");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
            email.setError("enter valid email");
            valid = false;
        }
        if (password.getText().length() < 6) {
            password.setError("enter valid password");
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
                    userImg.setImageURI(imageUri);
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
