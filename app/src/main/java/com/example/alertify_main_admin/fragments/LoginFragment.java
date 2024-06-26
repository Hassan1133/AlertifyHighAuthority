package com.example.alertify_main_admin.fragments;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_HIGH_AUTHORITY_REF;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.MainActivity;
import com.example.alertify_main_admin.databinding.LoginBinding;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.HighAuthorityModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private LoginBinding binding;
    private AppSharedPreferences appSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LoginBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        binding.loginBtn.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference(ALERTIFY_HIGH_AUTHORITY_REF);
        appSharedPreferences = new AppSharedPreferences(requireActivity());

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBtn:
                if (isValid()) {
                    LoadingDialog.showLoadingDialog(getActivity());
                    signIn();
                }
                break;
        }
    }

    private void signIn() {
        firebaseAuth.signInWithEmailAndPassword(binding.email.getText().toString().trim(), binding.password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getProfileData();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LoadingDialog.hideLoadingDialog();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(getContext(), "The Password is wrong", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValid() // method for data validation
    {
        boolean valid = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText()).matches()) {
            binding.email.setError("enter valid email");
            valid = false;
        }
        if (binding.password.getText().length() < 6) {
            binding.password.setError("enter valid name");
            valid = false;
        }

        return valid;
    }

    private void getProfileData() {


        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                HighAuthorityModel user = snapshot.getValue(HighAuthorityModel.class);

                if (user != null) {

                    try {
                        getFCMToken(user);

                    } catch (Exception e) {
                        System.out.println(e);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getFCMToken(HighAuthorityModel user) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    setFCMTokenToDb(task.getResult(), user);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFCMTokenToDb(String token, HighAuthorityModel user) {
        user.setHighAuthorityFCMToken(token);

        HashMap<String, Object> map = new HashMap<>();

        map.put("highAuthorityFCMToken", user.getHighAuthorityFCMToken());

        databaseReference.child(user.getId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    goToMainActivity(user);
                }
            }
        });
    }

    private void goToMainActivity(HighAuthorityModel user) {
        appSharedPreferences.put("userProfileId", user.getId());
        appSharedPreferences.put("userProfileName", user.getName());
        appSharedPreferences.put("userProfileEmail", user.getEmail());
        appSharedPreferences.put("highAuthorityLoginFlag", true);
        LoadingDialog.hideLoadingDialog();
        Toast.makeText(getActivity(), "Logged in Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}
