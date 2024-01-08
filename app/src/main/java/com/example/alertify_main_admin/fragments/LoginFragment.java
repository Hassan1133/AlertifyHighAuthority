package com.example.alertify_main_admin.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.MainActivity;
import com.example.alertify_main_admin.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private TextInputEditText email, password;

    private Button loginBtn;

    private FirebaseAuth firebaseAuth;

    private ProgressBar loadingProgressBar;

    private DatabaseReference databaseReference;

    private Dialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);

        loginBtn = view.findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("AlertifyHighAuthority");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (isValid()) {
                    createLoadingDialog();
                    signIn();
                }
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
        loadingTxt.setText("Signing in....");

        loadingProgressBar = loadingDialog.findViewById(R.id.profile_progressbar);

        loadingProgressBar.setVisibility(View.VISIBLE);

        loadingDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dialog.dismiss();
                    getActivity().finish();
                }
                return true;
            }
        });
    }


    private void signIn() {
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    getProfileData();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
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

        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
            email.setError("enter valid email");
            valid = false;
        }
        if (password.getText().length() < 6) {
            password.setError("enter valid name");
            valid = false;
        }

        return valid;
    }

    private void getProfileData() {


        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel user = snapshot.getValue(UserModel.class);

                if (user != null) {

                    try {
                        SharedPreferences userData = getContext().getApplicationContext().getSharedPreferences("profileData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = userData.edit();
                        editor.putString("userName", user.getName());
                        editor.putString("userEmail", user.getEmail());
                        editor.putString("userImgUrl", user.getImgUrl());
                        editor.apply();
                    }
                    catch (Exception e)
                    {
                        System.out.println(e);
                    }

                    loadingDialog.dismiss();
                    Toast.makeText(getContext(), "Logged in Successfully", Toast.LENGTH_SHORT).show();
                    goToMainActivity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void goToMainActivity() {
        SharedPreferences pref = getActivity().getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("flag", true);
        editor.apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
