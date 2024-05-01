package com.example.alertify_main_admin.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.alertify_main_admin.R;
import com.example.alertify_main_admin.activities.SeeAnalyticsActivity;
import com.example.alertify_main_admin.adapters.DepAdminDropDown;
import com.example.alertify_main_admin.constants.Constants;
import com.example.alertify_main_admin.databinding.FragmentAnalyticsBinding;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.models.DepAdminModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private String selectedStatus = "";
    private String depAdminId = "";
    private DatabaseReference highAuthorityRef, depAdminRef;
    private AppSharedPreferences appSharedPreferences;

    private List<DepAdminModel> depAdmins;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {

        highAuthorityRef = FirebaseDatabase.getInstance().getReference().child(Constants.ALERTIFY_HIGH_AUTHORITY_REF);
        depAdminRef = FirebaseDatabase.getInstance().getReference().child(Constants.ALERTIFY_DEP_ADMIN_REF);
        appSharedPreferences = new AppSharedPreferences(requireActivity());
        depAdmins = new ArrayList<>();
        setupStatusAutoCompleteTextView();
        setupCalenderAutoCompleteTextView();
        getDepAdminList();

        binding.seeAnalyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    Intent intent = new Intent(requireActivity(), SeeAnalyticsActivity.class);
                    intent.putExtra("status", selectedStatus);
                    intent.putExtra("depAdminId", depAdminId);
                    intent.putExtra("date", binding.date.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }

    private void getDepAdminList() {
        highAuthorityRef.child(appSharedPreferences.getString("userProfileId")).child("depAdminList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    depAdmins.clear();
                    for (DataSnapshot snapshotData : snapshot.getChildren()) {
                        String depAdminId = snapshotData.getValue(String.class);
                        getDepAdminDetails(depAdminId);
                    }
                } else {
                    Toast.makeText(getActivity(), "DepAdmins not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDepAdminDetails(String depAdminId) {
        depAdminRef.child(depAdminId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DepAdminModel depAdmin = snapshot.getValue(DepAdminModel.class);
                    if (depAdmin != null) {
                            depAdmins.add(depAdmin);
                        getDepAdminNameFromList(depAdmins);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDepAdminNameFromList(List<DepAdminModel> list) {
        DepAdminDropDown adapter = new DepAdminDropDown(
                requireActivity(), list
        );

        binding.selectDepAdmin.setAdapter(adapter);

        binding.selectDepAdmin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DepAdminModel depAdminModel = adapter.getItem(position);
                if (depAdminModel != null) {
                    binding.selectDepAdmin.setText(depAdminModel.getDepAdminName());
                    depAdminId = depAdminModel.getDepAdminId();
                }
            }
        });
    }

    private void setupStatusAutoCompleteTextView() {

        // Create ArrayAdapter for AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.status_array)
        );
        // Set adapter to AutoCompleteTextView
        binding.status.setAdapter(adapter);

        // Set item click listener
        binding.status.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = parent.getItemAtPosition(position).toString();
            }
        });
    }

    private void setupCalenderAutoCompleteTextView() {

        // Create ArrayAdapter for AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.calender_type)
        );
        // Set adapter to AutoCompleteTextView
        binding.calender.setAdapter(adapter);

        // Set item click listener
        binding.calender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = adapter.getItem(position);
                switch (selectedItem) {
                    case "Date":
                        showDatePicker();
                        break;
                    case "Month":
                        showMonthPicker();
                        break;
                    case "Year":
                        showYearPicker();
                        break;
                }
            }
        });
    }

    private void showMonthPicker() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        final String[] monthPickerValues = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        // Inflate the custom layout
        View view = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);
        final NumberPicker monthPicker = view.findViewById(R.id.monthPicker);
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(monthPickerValues);
        monthPicker.setValue(currentMonth);

        final NumberPicker yearPicker = view.findViewById(R.id.yearPicker);
        yearPicker.setMinValue(currentYear - 100); // Set min year to 100 years before current year
        yearPicker.setMaxValue(currentYear + 100); // Set max year to 100 years after current year
        yearPicker.setValue(currentYear);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Select Month and Year");
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedMonth = monthPicker.getValue();
                int selectedYear = yearPicker.getValue();
                String formattedDate = monthPickerValues[selectedMonth] + " " + selectedYear;
                binding.date.setText(formattedDate);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showYearPicker() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a NumberPicker for selecting the year
        final NumberPicker yearPicker = new NumberPicker(requireActivity());
        yearPicker.setMinValue(currentYear - 100); // Set min year to 100 years before current year
        yearPicker.setMaxValue(currentYear + 100); // Set max year to 100 years after current year
        yearPicker.setValue(currentYear); // Set initial value to current year

        // Create an AlertDialog with the year picker
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Select Year");
        builder.setView(yearPicker);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedYear = yearPicker.getValue();
                binding.date.setText(String.valueOf(selectedYear));
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                binding.date.setText(dateFormat.format(cal.getTime()));
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireActivity(),
                datePickerListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private boolean isValid() {
        boolean valid = true;

        if (selectedStatus.isEmpty()) {
            binding.status.setError("select status please");
            valid = false;
        }
        if (binding.date.getText().toString().isEmpty()) {
            binding.date.setError("select date from calendar please");
            valid = false;
        }

        return valid;
    }

}