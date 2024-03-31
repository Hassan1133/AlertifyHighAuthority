package com.example.alertify_main_admin.fragments;

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
import com.example.alertify_main_admin.activities.LoginSignupActivity;
import com.example.alertify_main_admin.databinding.SignupBinding;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SignupFragment extends Fragment implements View.OnClickListener {

    private UserModel user;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference firebaseDatabaseReference;

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
        binding.signupBtn.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("AlertifyHighAuthority");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.signupBtn) {
            createAccount();
        }
    }
    private void createAccount() // method for create account
    {
        if (isValid()) {
            LoadingDialog.showLoadingDialog(getActivity());
            user = new UserModel();
            user.setName(binding.name.getText().toString().trim());
            user.setEmail(binding.email.getText().toString().trim());
            user.setDepAdminList(new ArrayList<>());
            user.setPoliceStationList(new ArrayList<>());

            firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), binding.password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        user.setId(firebaseAuth.getUid());
                        addToDB(user);
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
                    requireActivity().finish();
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
}
