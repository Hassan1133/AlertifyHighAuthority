package com.example.alertify_main_admin.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.main_utils.LatLngWrapper;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BoundaryAdapter extends RecyclerView.Adapter<BoundaryAdapter.Holder> {

    private Context context;

    private List<LatLngWrapper> boundaryList;

    public BoundaryAdapter(Context context, List<LatLngWrapper> boundaryList) {
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
        try {
            LatLngWrapper latLng = boundaryList.get(position);
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);

            if (addresses != null) {
                if (!addresses.isEmpty()) {
                    String pointAddress = addresses.get(0).getAddressLine(0);
                    holder.latLngText.setText(pointAddress);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
