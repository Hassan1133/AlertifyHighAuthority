package com.example.alertify_main_admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_main_admin.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class BoundaryAdapter extends RecyclerView.Adapter<BoundaryAdapter.Holder> {

    private Context context;

    private List<LatLng> boundaryList;

    public BoundaryAdapter(Context context, List<LatLng> boundaryList) {
        this.context = context;
        this.boundaryList = boundaryList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.boundary_recycler_design, parent, false);
        BoundaryAdapter.Holder holder = new BoundaryAdapter.Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        LatLng latLng = boundaryList.get(position);
        holder.latLngText.setText(latLng.toString());
    }

    @Override
    public int getItemCount() {
        return boundaryList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private TextView latLngText;
        public Holder(@NonNull View itemView) {
            super(itemView);

            latLngText = itemView.findViewById(R.id.latLngTxt);
        }
    }
}
