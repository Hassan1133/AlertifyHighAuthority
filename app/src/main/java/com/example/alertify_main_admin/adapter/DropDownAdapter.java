package com.example.alertify_main_admin.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.alertify_main_admin.R;

import java.util.ArrayList;

public class DropDownAdapter extends ArrayAdapter<String> {
    private ArrayList<String> policeStationList;

    public DropDownAdapter(Context context, ArrayList<String> policeStationList) {
        super(context, R.layout.drop_down_item);
        this.policeStationList = policeStationList;
    }


    @Override
    public int getCount() {
        return policeStationList.size();
    }

    @Override
    public String getItem(int position) {
        return policeStationList.get(position);
    }
}
