package com.example.alertify_main_admin.activities;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_DEP_ADMIN_REF;
import static com.example.alertify_main_admin.constants.Constants.USERS_COMPLAINTS_REF;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.databinding.ActivitySeeAnalyticsBinding;
import com.example.alertify_main_admin.models.ComplaintModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SeeAnalyticsActivity extends AppCompatActivity {

    private ActivitySeeAnalyticsBinding binding;
    private String status = "";
    private String date = "";
    private String depAdminId = "";
    private List<ComplaintModel> complaints;
    private DatabaseReference depAdminRef, complaintsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        depAdminRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_DEP_ADMIN_REF);
        complaintsRef = FirebaseDatabase.getInstance().getReference(USERS_COMPLAINTS_REF); // firebase initialization
        complaints = new ArrayList<ComplaintModel>();
        getDataFromIntent();
    }

    private void getDataFromIntent() {
        Intent intent = SeeAnalyticsActivity.this.getIntent();
        status = intent.getStringExtra("status");
        depAdminId = intent.getStringExtra("depAdminId");
        date = intent.getStringExtra("date");
        getDepAdminComplaintsID();
    }

    private void getDepAdminComplaintsID() {
        depAdminRef.child(depAdminId).child("complaintList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    complaints.clear(); // Clear complaints to avoid duplication
                    for (DataSnapshot snapshotData : snapshot.getChildren()) {
                        String complaintID = snapshotData.getValue(String.class);
                        getComplaintDetails(complaintID);
                    }
                } else {
                    Toast.makeText(SeeAnalyticsActivity.this, "Complaints not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SeeAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getComplaintDetails(String complaintID) {
        complaintsRef.child(complaintID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ComplaintModel complaint = snapshot.getValue(ComplaintModel.class);
                    if (complaint != null) {
                        // Filter complaints based on date and status
                        if (complaint.getComplaintDateTime().contains(date) && (complaint.getInvestigationStatus().equals(status) || status.equals("All"))) {
                            complaints.add(complaint);
                        }
                        getFilteredList(complaints);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SeeAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFilteredList(List<ComplaintModel> updatedComplaintList) {
        if (!updatedComplaintList.isEmpty()) {
            List<BarEntry> entries = new ArrayList<>();
            ArrayList<String> complainLabelList = new ArrayList<>();
            for (ComplaintModel complaint : updatedComplaintList) {
                if (!complainLabelList.contains(complaint.getCrimeType())) {
                    complainLabelList.add(complaint.getCrimeType());
                }
            }

            for (int j = 0; j < complainLabelList.size(); j++) {
                int count = 0;
                for (ComplaintModel complaint : updatedComplaintList) {
                    if (complaint.getCrimeType().equals(complainLabelList.get(j))) {
                        count++;
                    }
                }
                entries.add(new BarEntry(j, count));
            }

            setBarChart(complainLabelList, entries);
        }
    }

    private void setBarChart(ArrayList<String> complainLabelList, List<BarEntry> entries) {
        BarChart barChart = findViewById(R.id.barChart);
        barChart.getAxisRight().setDrawLabels(false);

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);

        BarDataSet dataSet = new BarDataSet(entries, "Complaint Types");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(13f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(complainLabelList));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setTextSize(6f);

        barChart.animateY(5000);
    }

}