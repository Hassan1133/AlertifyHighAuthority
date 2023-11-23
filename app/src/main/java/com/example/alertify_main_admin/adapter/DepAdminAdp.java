package com.example.alertify_main_admin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.model.DepAdminModel;
import com.example.alertify_main_admin.dep_admin.EditDepAdminActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class DepAdminAdp extends RecyclerView.Adapter<DepAdminAdp.Holder> {


    private Context context;

    private List<DepAdminModel> depAdminsList;

    public DepAdminAdp(Context context, List<DepAdminModel> depAdmins) {
        this.context = context;
        depAdminsList = depAdmins;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dep_admin_recycler_design, parent, false);
        DepAdminAdp.Holder holder = new DepAdminAdp.Holder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        DepAdminModel depAdminModel = depAdminsList.get(position);

        if (depAdminModel.getDepAdminStatus().equals("block")) {
            Glide.with(context.getApplicationContext()).load(depAdminModel.getDepAdminImageUrl()).into(holder.depAdminImg);
            holder.depAdminName.setText(depAdminModel.getDepAdminName());
            holder.depAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());
            holder.blockBtn.setVisibility(View.VISIBLE);
        } else {
            Glide.with(context.getApplicationContext()).load(depAdminModel.getDepAdminImageUrl()).into(holder.depAdminImg);
            holder.depAdminName.setText(depAdminModel.getDepAdminName());
            holder.depAdminPoliceStation.setText(depAdminModel.getDepAdminPoliceStation());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditDepAdminActivity.class);
                intent.putExtra("depAdminModel", depAdminModel);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return depAdminsList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private ShapeableImageView depAdminImg;
        private TextView depAdminName, depAdminPoliceStation;

        private ImageView blockBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);
            depAdminImg = itemView.findViewById(R.id.dep_admin_img);
            depAdminName = itemView.findViewById(R.id.dep_admin_name);
            depAdminPoliceStation = itemView.findViewById(R.id.dep_admin_police_station);
            blockBtn = itemView.findViewById(R.id.block_icon);
        }
    }
}
