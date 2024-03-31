package com.example.alertify_main_admin.adapters;


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

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.activities.EditPoliceStationActivity;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.models.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
                deleteData(policeStation);
                notifyDataSetChanged();
            }
        });

    }

    private void deleteData(PoliceStationModel policeStation) {


        FirebaseDatabase
                .getInstance()
                .getReference(ALERTIFY_POLICE_STATIONS_REF)
                .child(policeStation.getId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "Police Station Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
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
