package com.example.alertify_main_admin.adapter;


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
import com.example.alertify_main_admin.police_station.EditPoliceStationActivity;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.police_station.PoliceStationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PoliceStationAdp extends RecyclerView.Adapter<PoliceStationAdp.Holder> {


    private Context context;

    private List<PoliceStationModel> policeStationsList;

    public PoliceStationAdp(Context context, List<PoliceStationModel> policeStations) {
        this.context = context;
        policeStationsList = policeStations;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.police_station_recycler_design, parent, false);
        Holder holder = new Holder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        PoliceStationModel policeStation = policeStationsList.get(position);
        Glide.with(context.getApplicationContext()).load(policeStation.getImgUrl()).into(holder.policeStationImg);
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
            @Override
            public void onClick(View v) {
                deleteImage(policeStation);
                notifyDataSetChanged();
            }
        });

    }

    private void deleteImage(PoliceStationModel policeStation)
    {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(policeStation.getImgUrl());
        storageReference.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deleteData(policeStation);
                    }
                })
               .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteData(PoliceStationModel policeStation) {


        FirebaseDatabase
                .getInstance()
                .getReference("AlertifyPoliceStations")
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

    class Holder extends RecyclerView.ViewHolder {
        private ShapeableImageView policeStationImg;
        private TextView policeStationName, policeStationLocation;

        private Button editBtn, deleteBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);

            policeStationImg = itemView.findViewById(R.id.policeStationRecyclerImg);
            policeStationName = itemView.findViewById(R.id.policeStationRecyclerName);
            policeStationLocation = itemView.findViewById(R.id.policeStationRecyclerLocation);
            editBtn = itemView.findViewById(R.id.recyclerEditBtn);
            deleteBtn = itemView.findViewById(R.id.recyclerDeleteBtn);
        }
    }
}
