package com.example.alertify_main_admin.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
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
import com.example.alertify_main_admin.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView userImage;

    private Dialog userUpdateImgDialog, userUpdateNameDialog, userUpdatePasswordDialog, profileLoadingDialog;

    private ProgressBar userImgUpdateDialogProgressBar, userNameUpdateDialogProgressBar, userPasswordUpdateDialogProgressBar, loadingProfileProgressBar;

    private ShapeableImageView userDialogImg;

    private UserModel user;

    private String imageUrl;

    private Uri imageUri;

    private StorageReference firebaseStorageReference;

    private TextView userName, userEmail;

    private DatabaseReference highAuthorityRef;

    private FirebaseUser firebaseUser;

    private ImageView userNameEditBtn, userPasswordEditBtn;

    private EditText dialogUserName;

    private TextInputEditText userCurrentPassword, userNewPassword;

    private SharedPreferences userData;

    private SharedPreferences.Editor editor;

    private String imageSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);
        init();
        createProfileLoadingDialog();
    }

    private void init() {
        userImage = findViewById(R.id.user_image);
        userImage.setOnClickListener(this);

        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);

        userNameEditBtn = findViewById(R.id.name_edit_btn);
        userNameEditBtn.setOnClickListener(this);
        userPasswordEditBtn = findViewById(R.id.password_edit_btn);
        userPasswordEditBtn.setOnClickListener(this);

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

                    Glide.with(getApplicationContext()).load(user.getImgUrl()).into(userImage);

                    userName.setText(user.getName());

                    userEmail.setText(user.getEmail());

                    loadingProfileProgressBar.setVisibility(View.INVISIBLE);
                    profileLoadingDialog.dismiss();
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
            case R.id.user_image:
                createUserImageDialog();
                break;
            case R.id.name_edit_btn:
                createUserNameDialog();
                break;
            case R.id.password_edit_btn:
                createUserPasswordDialog();
                break;
        }
    }

    private void createProfileLoadingDialog()
    {
        profileLoadingDialog = new Dialog(EditUserProfileActivity.this);
        profileLoadingDialog.setContentView(R.layout.profile_loading_dialog);
        profileLoadingDialog.show();
        profileLoadingDialog.setCancelable(false);
        profileLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        loadingProfileProgressBar = profileLoadingDialog.findViewById(R.id.profile_progressbar);

        loadingProfileProgressBar.setVisibility(View.VISIBLE);

        profileLoadingDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dialog.dismiss();
                    finish();
                }
                return true;
            }
        });
    }
    private void createUserPasswordDialog() {
        userUpdatePasswordDialog = new Dialog(EditUserProfileActivity.this);
        userUpdatePasswordDialog.setContentView(R.layout.user_edit_password_dialog);
        userUpdatePasswordDialog.show();
        userUpdatePasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userCurrentPassword = userUpdatePasswordDialog.findViewById(R.id.user_current_password);
        userNewPassword = userUpdatePasswordDialog.findViewById(R.id.user_new_password);
        userPasswordUpdateDialogProgressBar = userUpdatePasswordDialog.findViewById(R.id.dep_admin_password_progressbar);

        userUpdatePasswordDialog.findViewById(R.id.dep_admin_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdatePasswordDialog.dismiss();
            }
        });

        userUpdatePasswordDialog.findViewById(R.id.dep_admin_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidPassword()) {
                    userPasswordUpdateDialogProgressBar.setVisibility(View.VISIBLE);
                    verifyHighAuthorityCurrentPassword(firebaseUser.getEmail(), userCurrentPassword.getText().toString().trim());
                }
            }
        });
    }

    private void verifyHighAuthorityCurrentPassword(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateUserPassword(userNewPassword.getText().toString().trim());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userPasswordUpdateDialogProgressBar.setVisibility(View.INVISIBLE);
                        userCurrentPassword.setError("password is invalid");
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
                            userPasswordUpdateDialogProgressBar.setVisibility(View.INVISIBLE);
                            userUpdatePasswordDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userPasswordUpdateDialogProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidPassword() {
        boolean valid = true;

        if (userCurrentPassword.getText().length() < 6) {
            userCurrentPassword.setError("enter valid password");
            valid = false;
        }

        if (userNewPassword.getText().length() < 6) {
            userNewPassword.setError("enter valid password");
            valid = false;
        }

        return valid;
    }

    private void createUserNameDialog() {
        userUpdateNameDialog = new Dialog(EditUserProfileActivity.this);
        userUpdateNameDialog.setContentView(R.layout.dep_admin_edit_name_dialog);
        userUpdateNameDialog.show();
        userUpdateNameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userNameUpdateDialogProgressBar = userUpdateNameDialog.findViewById(R.id.dep_admin_name_progressbar);

        dialogUserName = userUpdateNameDialog.findViewById(R.id.dep_admin_dialog_name);
        dialogUserName.setText(user.getName());

        userUpdateNameDialog.findViewById(R.id.dep_admin_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdateNameDialog.dismiss();
            }
        });

        userUpdateNameDialog.findViewById(R.id.dep_admin_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogUserName.getText().length() < 3) {
                    dialogUserName.setError("Please enter valid name");
                } else {
                    userNameUpdateDialogProgressBar.setVisibility(View.VISIBLE);
                    updateNameToDb(dialogUserName.getText().toString().trim());
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
                    userNameUpdateDialogProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditUserProfileActivity.this, "Department Admin Name Updated Successfully!", Toast.LENGTH_SHORT).show();
                    userUpdateNameDialog.dismiss();

                    dialogUserName.setText(user.getName());
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
        userUpdateImgDialog = new Dialog(EditUserProfileActivity.this);
        userUpdateImgDialog.setContentView(R.layout.dep_admin_edit_img_dialog);
        userUpdateImgDialog.show();
        userUpdateImgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userImgUpdateDialogProgressBar = userUpdateImgDialog.findViewById(R.id.dep_admin_img_progressbar);

        userDialogImg = userUpdateImgDialog.findViewById(R.id.dep_admin_dialog_image);
        Glide.with(getApplicationContext()).load(user.getImgUrl()).into(userDialogImg);

        userDialogImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickNewImage();
            }
        });

        userUpdateImgDialog.findViewById(R.id.dep_admin_img_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdateImgDialog.dismiss();
            }
        });

        userUpdateImgDialog.findViewById(R.id.dep_admin_img_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImgUpdateDialogProgressBar.setVisibility(View.VISIBLE);
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
                    userImgUpdateDialogProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditUserProfileActivity.this, "User Image Updated Successfully!", Toast.LENGTH_SHORT).show();
                    userUpdateImgDialog.dismiss();

                    user.setImgUrl(imageUrl);
                    editor.putString("userImgUrl", imageUrl);
                    editor.apply();
                    Glide.with(getApplicationContext()).load(user.getImgUrl()).into(userImage);

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
                    userDialogImg.setImageURI(imageUri);
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