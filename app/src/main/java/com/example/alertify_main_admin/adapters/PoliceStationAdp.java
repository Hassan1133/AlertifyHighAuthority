package com.example.alertify_main_admin.adapters;


import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_HIGH_AUTHORITY_REF;
import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_POLICE_STATIONS_REF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.EditPoliceStationActivity;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.main_utils.LoadingDialog;
import com.example.alertify_main_admin.models.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PoliceStationAdp extends RecyclerView.Adapter<PoliceStationAdp.Holder> {


    private final Context context;

    private final List<PoliceStationModel> policeStationsList;

    public PoliceStationAdp(Context context, List<PoliceStationModel> policeStations) {
        this.context = context;
        policeStationsList = policeStations;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.police_station_recycler_design, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        PoliceStationModel policeStation = policeStationsList.get(position);
        holder.policeStationName.setText(policeStation.getPoliceStationName());
        holder.policeStationLocation.setText(policeStation.getPoliceStationLocation());

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditPoliceStationActivity.class);
                intent.putExtra("policeStation", policeStation);
                context.startActivity(intent);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                deleteDataPoliceStation(policeStation);
                notifyDataSetChanged();
            }
        });

    }

    private void deleteDataPoliceStation(PoliceStationModel policeStation) {
        FirebaseDatabase
                .getInstance()
                .getReference(ALERTIFY_POLICE_STATIONS_REF)
                .child(policeStation.getId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deletePoliceStationFromHighAuthorityList(policeStation.getId());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletePoliceStationFromHighAuthorityList(String policeStationId) {
        AppSharedPreferences appSharedPreferences = new AppSharedPreferences(context);
        String highAuthorityProfileId = appSharedPreferences.getString("userProfileId");
        DatabaseReference highAuthorityRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_HIGH_AUTHORITY_REF);

        highAuthorityRef.child(highAuthorityProfileId)
                .child("policeStationList").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> policeStationList = new ArrayList<>();
                        if (snapshot.exists()) {

                            policeStationList = (List<String>) snapshot.getValue();

                        }
                        // Check if the new policeStationId already exists in the list
                        assert policeStationList != null;
                        if (policeStationList.contains(policeStationId)) {
                            // If it doesn't exist, add it to the list
                            policeStationList.remove(policeStationId);

                            // Update the value of policeStationList in the database
                            highAuthorityRef.child(highAuthorityProfileId).child("policeStationList").setValue(policeStationList)
                                    .addOnSuccessListener(aVoid -> {
                                        // Handle success
                                        Toast.makeText(context, "Police Station Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Toast.makeText(context, "Failed to delete police station: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Police station already exists in the list
                            Toast.makeText(context, "Police Station not exist in the list!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return policeStationsList.size();
    }

    class Holder extends RecyclerView.ViewHolder { ;
        private final TextView policeStationName;
        private final TextView policeStationLocation;

        private final Button editBtn, deleteBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);

            policeStationName = itemView.findViewById(R.id.policeStationRecyclerName);
            policeStationLocation = itemView.findViewById(R.id.policeStationRecyclerLocation);
            editBtn = itemView.findViewById(R.id.recyclerEditBtn);
            deleteBtn = itemView.findViewById(R.id.recyclerDeleteBtn);
        }
    }
}
