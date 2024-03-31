package com.example.alertify_main_admin.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivityEditUserProfileBinding;
import com.example.alertify_main_admin.databinding.UserEditNameDialogBinding;
import com.example.alertify_main_admin.databinding.UserEditPasswordDialogBinding;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditUserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private Dialog userUpdateNameDialog, userUpdatePasswordDialog;
    private DatabaseReference highAuthorityRef;

    private FirebaseUser firebaseUser;

    private ActivityEditUserProfileBinding binding;

    private UserEditPasswordDialogBinding userEditPasswordDialogBinding;

    private UserEditNameDialogBinding userEditNameDialogBinding;

    private AppSharedPreferences appSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {

        binding.nameEditBtn.setOnClickListener(this);
        binding.passwordEditBtn.setOnClickListener(this);

        highAuthorityRef = FirebaseDatabase.getInstance().getReference("AlertifyHighAuthority");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        appSharedPreferences = new AppSharedPreferences(EditUserProfileActivity.this);

        getProfileData(); // set method for load user data to the profile

    }

    private void getProfileData() {

        binding.userName.setText(appSharedPreferences.getString("userProfileName"));

        binding.userEmail.setText(appSharedPreferences.getString("userProfileEmail"));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

        userEditNameDialogBinding.userDialogName.setText(appSharedPreferences.getString("userProfileName"));

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

        HashMap<String, Object> map = new HashMap<>();

        map.put("name", updatedName);

        highAuthorityRef.child(firebaseUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    LoadingDialog.hideLoadingDialog();
                    Toast.makeText(EditUserProfileActivity.this, "User Name Updated Successfully!", Toast.LENGTH_SHORT).show();
                    userUpdateNameDialog.dismiss();

                    binding.userName.setText(updatedName);
                    appSharedPreferences.put("userProfileName", updatedName);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}