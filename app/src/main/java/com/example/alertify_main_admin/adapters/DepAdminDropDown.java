package com.example.alertify_main_admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.models.DepAdminModel;

import java.util.List;

public class DepAdminDropDown extends ArrayAdapter<DepAdminModel> {

    private final List<DepAdminModel> depAdminList;
    private final Context context;

    public DepAdminDropDown(Context context, List<DepAdminModel> depAdminList) {
        super(context, R.layout.drop_down_item, depAdminList);
        this.context = context;
        this.depAdminList = depAdminList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.drop_down_item, parent, false);
        }

        DepAdminModel depAdminModel = getItem(position);

        TextView textView = view.findViewById(R.id.itemName);
        textView.setText(depAdminModel != null ? depAdminModel.getDepAdminName() : "");

        return view;
    }

    @Override
    public DepAdminModel getItem(int position) {
        return depAdminList.get(position);
    }

    @Override
    public int getCount() {
        return depAdminList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

