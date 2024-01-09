package com.example.alertify_main_admin.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivityEditUserProfileBinding;
import com.example.alertify_main_admin.databinding.UserEditPasswordDialogBinding;
import com.example.alertify_main_admin.databinding.UserEditNameDialogBinding;
import com.example.alertify_main_admin.databinding.UserEditImageDialogBinding;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditUserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private Dialog userUpdateImgDialog, userUpdateNameDialog, userUpdatePasswordDialog;
    private UserModel user;

    private String imageUrl;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private DatabaseReference highAuthorityRef;

    private FirebaseUser firebaseUser;

    private SharedPreferences userData;

    private SharedPreferences.Editor editor;

    private String imageSize;

    private ActivityEditUserProfileBinding binding;

    private UserEditPasswordDialogBinding userEditPasswordDialogBinding;

    private UserEditNameDialogBinding userEditNameDialogBinding;

    private UserEditImageDialogBinding userEditImageDialogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {

        LoadingDialog.showLoadingDialog(EditUserProfileActivity.this);

        binding.userImage.setOnClickListener(this);
        binding.nameEditBtn.setOnClickListener(this);
        binding.passwordEditBtn.setOnClickListener(this);

        highAuthorityRef = FirebaseDatabase.getInstance().getReference("AlertifyHighAuthority");

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getProfileData(firebaseUser); // set method for load user data to the profile

        userData = getSharedPreferences("profileData", MODE_PRIVATE);

        editor = userData.edit();
    }

    private void getProfileData(FirebaseUser firebaseUser) {
        highAuthorityRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user = snapshot.getValue(UserModel.class);

                if (user != null) {

                    Glide.with(getApplicationContext()).load(user.getImgUrl()).into(binding.userImage);

                    binding.userName.setText(user.getName());

                    binding.userEmail.setText(user.getEmail());

                    LoadingDialog.hideLoadingDialog();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(EditUserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userImage:
                createUserImageDialog();
                break;
            case R.id.nameEditBtn:
                createUserNameDialog();
                break;
            case R.id.passwordEditBtn:
                createUserPasswordDialog();
                break;
        }
    }
    private void createUserPasswordDialog() {
        userEditPasswordDialogBinding = UserEditPasswordDialogBinding.inflate(LayoutInflater.from(this));
        userUpdatePasswordDialog = new Dialog(EditUserProfileActivity.this);
        userUpdatePasswordDialog.setContentView(userEditPasswordDialogBinding.getRoot());
        userUpdatePasswordDialog.show();
        userUpdatePasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userEditPasswordDialogBinding.userProfilePasswordDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdatePasswordDialog.dismiss();
            }
        });

        userEditPasswordDialogBinding.userProfilePasswordDialogUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidPassword()) {
                    LoadingDialog.showLoadingDialog(EditUserProfileActivity.this);
                    verifyHighAuthorityCurrentPassword(firebaseUser.getEmail(), userEditPasswordDialogBinding.userCurrentPassword.getText().toString().trim());
                }
            }
        });
    }

    private void verifyHighAuthorityCurrentPassword(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateUserPassword(userEditPasswordDialogBinding.userNewPassword.getText().toString().trim());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LoadingDialog.hideLoadingDialog();
                        userEditPasswordDialogBinding.userCurrentPassword.setError("password is invalid");
                        Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserPassword(String newPassword) {
        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditUserProfileActivity.this, "User Password Updated Successfully", Toast.LENGTH_SHORT).show();
                            LoadingDialog.hideLoadingDialog();
                            userUpdatePasswordDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LoadingDialog.hideLoadingDialog();
                        Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidPassword() {
        boolean valid = true;

        if (userEditPasswordDialogBinding.userCurrentPassword.getText().length() < 6) {
            userEditPasswordDialogBinding.userCurrentPassword.setError("enter valid password");
            valid = false;
        }

        if (userEditPasswordDialogBinding.userNewPassword.getText().length() < 6) {
            userEditPasswordDialogBinding.userNewPassword.setError("enter valid password");
            valid = false;
        }

        return valid;
    }

    private void createUserNameDialog() {
        userEditNameDialogBinding = UserEditNameDialogBinding.inflate(LayoutInflater.from(this));
        userUpdateNameDialog = new Dialog(EditUserProfileActivity.this);
        userUpdateNameDialog.setContentView(userEditNameDialogBinding.getRoot());
        userUpdateNameDialog.show();
        userUpdateNameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userEditNameDialogBinding.userDialogName.setText(user.getName());

        userEditNameDialogBinding.userEditNameDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdateNameDialog.dismiss();
            }
        });

        userEditNameDialogBinding.userEditNameDialogUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userEditNameDialogBinding.userDialogName.getText().length() < 3) {
                    userEditNameDialogBinding.userDialogName.setError("Please enter valid name");
                } else {
                    LoadingDialog.showLoadingDialog(EditUserProfileActivity.this);
                    updateNameToDb(userEditNameDialogBinding.userDialogName.getText().toString().trim());
                }
            }
        });
    }

    private void updateNameToDb(String updatedName) {
        user.setName(updatedName);

        HashMap<String, Object> map = new HashMap<>();

        map.put("name", user.getName());

        highAuthorityRef.child(firebaseUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(EditUserProfileActivity.this, "User Name Updated Successfully!", Toast.LENGTH_SHORT).show();
                    userUpdateNameDialog.dismiss();

                    binding.userName.setText(user.getName());
                    editor.putString("userName", user.getName());
                    editor.apply();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserImageDialog() {
        userEditImageDialogBinding = UserEditImageDialogBinding.inflate(LayoutInflater.from(this));
        userUpdateImgDialog = new Dialog(EditUserProfileActivity.this);
        userUpdateImgDialog.setContentView(userEditImageDialogBinding.getRoot());
        userUpdateImgDialog.show();
        userUpdateImgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Glide.with(getApplicationContext()).load(user.getImgUrl()).into(userEditImageDialogBinding.userEditDialogImage);

        userEditImageDialogBinding.userEditDialogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickNewImage();
            }
        });

        userEditImageDialogBinding.userEditImgDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdateImgDialog.dismiss();
            }
        });

        userEditImageDialogBinding.userEditImgDialogUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingDialog.showLoadingDialog(EditUserProfileActivity.this);
                if (imageUri == null) {
                    updateImageUrlToDb();
                } else if (imageUri != null) {
                    uploadImage();
                }
            }
        });

    }

    private void uploadImage() {
        StorageReference strRef = firebaseStorageReference.child("Alertify_High_Authority_Images/" + firebaseUser.getUid());

        strRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                strRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        imageUrl = task.getResult().toString();
                        updateImageUrlToDb();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageUrlToDb() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imgUrl", imageUrl);

        highAuthorityRef.child(firebaseUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(EditUserProfileActivity.this, "User Image Updated Successfully!", Toast.LENGTH_SHORT).show();
                    userUpdateImgDialog.dismiss();

                    user.setImgUrl(imageUrl);
                    editor.putString("userImgUrl", imageUrl);
                    editor.apply();
                    Glide.with(getApplicationContext()).load(user.getImgUrl()).into(binding.userImage);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    userEditImageDialogBinding.userEditDialogImage.setImageURI(imageUri);
                } else {
                    Toast.makeText(EditUserProfileActivity.this, imageSize + ". Please select an image smaller than 2 MB", Toast.LENGTH_SHORT).show();
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
        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
        if (cursor == null) {
            throw new Exception("Cursor is null");
        }
        cursor.moveToFirst();
        long sizeInBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
        cursor.close();
        return sizeInBytes;
    }

}