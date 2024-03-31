package com.example.alertify_main_admin.activities;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_POLICE_STATIONS_REF;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.adapters.BoundaryAdapter;
import com.example.alertify_main_admin.constants.Constants;
import com.example.alertify_main_admin.databinding.ActivityEditPoliceStationBinding;
import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPoliceStationActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityEditPoliceStationBinding binding;
    private PoliceStationModel policeStation;

    private DatabaseReference firebaseReference;

    private double selectedLatitude, selectedLongitude;

    private List<LatLngWrapper> boundaryPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPoliceStationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getIntentData();
    }

    private void init() {
        binding.editPoliceStationUpdateBtn.setOnClickListener(this);
        binding.editAddPoliceStationLocationBtn.setOnClickListener(this);
        binding.editAddPoliceStationBoundaryBtn.setOnClickListener(this);

        firebaseReference = FirebaseDatabase.getInstance().getReference(ALERTIFY_POLICE_STATIONS_REF); // firebase initialization

        binding.editPoliceStationBoundariesRecycler.setLayoutManager(new LinearLayoutManager(EditPoliceStationActivity.this));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.editPoliceStationUpdateBtn:
                if (isValid()) {
                    LoadingDialog.showLoadingDialog(EditPoliceStationActivity.this);
                    updateToDb();
                }
                break;

            case R.id.editAddPoliceStationLocationBtn:
                Intent locationIntent = new Intent(EditPoliceStationActivity.this, MapsActivity.class);
                someActivityResultLauncher.launch(locationIntent);
                break;
            case R.id.editAddPoliceStationBoundaryBtn:
                Intent boundaryIntent = new Intent(EditPoliceStationActivity.this, PoliceStationBoundaryMapsActivity.class);
                policeStationBoundaryResultLauncher.launch(boundaryIntent);
                break;
        }
    }
    private boolean isValid() {
        boolean valid = true;

        if (binding.editPoliceStationName.getText().length() < 3) {
            binding.editPoliceStationName.setError("Please enter valid name");
            valid = false;
        }
        if (binding.editPoliceStationLocation.getText().length() < 3) {
            binding.editPoliceStationLocation.setError("Please select valid location");
            valid = false;
        }
        if(boundaryPoints.isEmpty())
        {
            Toast.makeText(this, "Please select boundary of police station", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private void updateToDb() {
        policeStation.setPoliceStationName(binding.editPoliceStationName.getText().toString().trim());
        policeStation.setPoliceStationLocation(binding.editPoliceStationLocation.getText().toString().trim());
        policeStation.setBoundaries(boundaryPoints);

        HashMap<String, Object> map = new HashMap<>();
        map.put("policeStationName", policeStation.getPoliceStationName());
        map.put("policeStationLocation", policeStation.getPoliceStationLocation());
        map.put("policeStationLatitude", selectedLatitude);
        map.put("policeStationLongitude", selectedLongitude);
        map.put("boundaries",boundaryPoints);

        firebaseReference.child(policeStation.getId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                binding.editPoliceStationName.setText(policeStation.getPoliceStationName());
                binding.editPoliceStationLocation.setText(policeStation.getPoliceStationLocation());
                setDataToBoundaryRecycler(policeStation.getBoundaries());
                LoadingDialog.hideLoadingDialog();
                Toast.makeText(EditPoliceStationActivity.this, "Data Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditPoliceStationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getIntentData() {

        policeStation = (PoliceStationModel) getIntent().getSerializableExtra("policeStation");
        assert policeStation != null;
        binding.editPoliceStationName.setText(policeStation.getPoliceStationName());
        binding.editPoliceStationLocation.setText(policeStation.getPoliceStationLocation());
        selectedLatitude = policeStation.getPoliceStationLatitude();
        selectedLongitude = policeStation.getPoliceStationLongitude();
        boundaryPoints = policeStation.getBoundaries();
        setDataToBoundaryRecycler(boundaryPoints);
    }

    private final ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                binding.editPoliceStationLocation.setText(result.getData().getStringExtra("address"));
                selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                selectedLongitude = result.getData().getDoubleExtra("longitude", 0);
            }
        }
    });

    private final ActivityResultLauncher<Intent> policeStationBoundaryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                boundaryPoints = (List<LatLngWrapper>) result.getData().getSerializableExtra("latLngList");
                setDataToBoundaryRecycler(boundaryPoints);
            }
        }
    });

    private void setDataToBoundaryRecycler(List<LatLngWrapper> boundaryList) {
        BoundaryAdapter boundaryAdp = new BoundaryAdapter(EditPoliceStationActivity.this, boundaryList);
        binding.editPoliceStationBoundariesRecycler.setAdapter(boundaryAdp);
    }
}