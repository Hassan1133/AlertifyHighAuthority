package com.example.alertify_main_admin.activities;


import static com.example.alertify_main_admin.constants.Constants.USERS_COMPLAINTS_REF;
import static com.example.alertify_main_admin.constants.Constants.USERS_REF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivityComplaintsDetailsBinding;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.ComplaintModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class ComplaintsDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityComplaintsDetailsBinding binding;
    private String evidenceUrl, evidenceType;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComplaintsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    void init() {
        usersRef = FirebaseDatabase.getInstance().getReference(USERS_REF);
        binding.downloadEvidenceBtn.setOnClickListener(this);
        getDataFromIntent();
    }

    void getDataFromIntent() {
        ComplaintModel complaintModel = (ComplaintModel) getIntent().getSerializableExtra("complaintModel");
        assert complaintModel != null;
        LoadingDialog.showLoadingDialog(ComplaintsDetailsActivity.this);
        getUserData(complaintModel.getUserId());
        evidenceUrl = complaintModel.getEvidenceUrl();
        evidenceType = complaintModel.getEvidenceType();
        binding.detailsCrimeType.setText(complaintModel.getCrimeType());
        binding.detailsCrime.setText(complaintModel.getCrimeDetails());
        binding.detailsCrimeLocation.setText(complaintModel.getCrimeLocation());
        binding.detailsCrimeDateTime.setText(String.format("%s %s", complaintModel.getCrimeDate(), complaintModel.getCrimeTime()));
        binding.detailsComplaintPoliceStation.setText(complaintModel.getPoliceStation());
        binding.detailsComplaintDateTime.setText(complaintModel.getComplaintDateTime());
        binding.complaintInvestigationStatus.setText(complaintModel.getInvestigationStatus());
        binding.complaintFeedback.setText(complaintModel.getFeedback());
    }

    private void getUserData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setUserData(snapshot.child("name").getValue().toString(),
                        snapshot.child("email").getValue().toString(),
                        snapshot.child("cnicNo").getValue().toString(),
                        snapshot.child("phoneNo").getValue().toString()
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LoadingDialog.hideLoadingDialog();
                Toast.makeText(ComplaintsDetailsActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserData(String name, String email, String cnicNo, String phoneNo) {
        binding.userName.setText(name);
        binding.userPhoneNo.setText(phoneNo);
        binding.userCnicNo.setText(cnicNo);
        binding.userEmail.setText(email);
        LoadingDialog.hideLoadingDialog();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.downloadEvidenceBtn) {
            downloadEvidence();
        }
    }

    private void downloadEvidence() {

        if (evidenceUrl != null && !evidenceUrl.isEmpty() && evidenceType != null && !evidenceType.isEmpty()) {

            String fileName = "evidence_file." + getFileType();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(evidenceUrl)).setTitle("File Download") // Title of the notification during download
                    .setDescription("Downloading") // Description of the notification during download
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(ComplaintsDetailsActivity.this, "Download started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ComplaintsDetailsActivity.this, "Download Manager not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ComplaintsDetailsActivity.this, "Evidence not found", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileType() {
        String fileType = "";
        if (evidenceType.matches("application/msword")) {
            fileType = "docx";
        }
        if (evidenceType.matches("application/pdf")) {
            fileType = "pdf";
        }
        if (evidenceType.matches("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
            fileType = "pptx";
        }
        if (evidenceType.startsWith("video")) {
            fileType = "mp4";
        }
        if (evidenceType.startsWith("audio")) {
            fileType = "mp3";
        }
        if (evidenceType.startsWith("image")) {
            fileType = "jpg";
        }
        if (evidenceType.matches("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            fileType = "xlsx";
        }
        return fileType;
    }
}