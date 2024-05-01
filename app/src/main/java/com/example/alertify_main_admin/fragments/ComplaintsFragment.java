package com.example.alertify_main_admin.fragments;

import static com.example.alertify_main_admin.constants.Constants.ALERTIFY_HIGH_AUTHORITY_REF;
import static com.example.alertify_main_admin.constants.Constants.USERS_COMPLAINTS_REF;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.alertify_main_admin.adapters.ComplaintsAdp;
import com.example.alertify_main_admin.databinding.ComplaintsBinding;
import com.example.alertify_main_admin.main_utils.AppSharedPreferences;
import com.example.alertify_main_admin.models.ComplaintModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ComplaintsFragment extends Fragment {

    private ComplaintsBinding binding;
    private List<ComplaintModel> complaints;
    private DatabaseReference complaintsRef, highAuthorityRef;
    private AppSharedPreferences appSharedPreferences;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ComplaintsBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        highAuthorityRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_HIGH_AUTHORITY_REF);
        complaintsRef = FirebaseDatabase.getInstance().getReference(USERS_COMPLAINTS_REF); // firebase initialization
        complaints = new ArrayList<ComplaintModel>();
        appSharedPreferences = new AppSharedPreferences(requireActivity());
        binding.complaintsRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isAdded()) {
            return;
        }
        fetchComplaintsData();
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });
    }

    private void search(String newText) {
        ArrayList<ComplaintModel> searchList = new ArrayList<>();
        for (ComplaintModel i : complaints) {
            if (i.getCrimeType().toLowerCase().contains(newText.toLowerCase()) || i.getComplaintDateTime().toLowerCase().contains(newText.toLowerCase()) || i.getCrimeDate().toLowerCase().contains(newText.toLowerCase()) || i.getCrimeTime().toLowerCase().contains(newText.toLowerCase()) || i.getPoliceStation().toLowerCase().contains(newText.toLowerCase()) || i.getCrimeLocation().toLowerCase().contains(newText.toLowerCase()) || i.getInvestigationStatus().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        setDataToRecycler(searchList);
    }

    private void fetchComplaintsData() {
        binding.complaintsProgressbar.setVisibility(View.VISIBLE);
        complaints.clear();

        // Listen for changes in complaintList
        highAuthorityRef.child(appSharedPreferences.getString("userProfileId")).child("complaintList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    complaints.clear(); // Clear complaints to avoid duplication
                    for (DataSnapshot snapshotData : snapshot.getChildren()) {
                        String complaintID = snapshotData.getValue(String.class);
                        listenForComplaintUpdates(complaintID);
                    }
                } else {
                    binding.complaintsProgressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.complaintsProgressbar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForComplaintUpdates(String complaintID) {
        complaintsRef.child(complaintID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ComplaintModel complaint = snapshot.getValue(ComplaintModel.class);
                    if (complaint != null) {
                        int index = findComplaintIndex(complaintID);
                        if (index == -1) {
                            // New complaint
                            complaints.add(complaint);
                        } else {
                            // Existing complaint, update it
                            complaints.set(index, complaint);
                        }

                        complaints.sort((complaint1, complaint2) -> {
                            return complaint2.getComplaintDateTime().compareTo(complaint1.getComplaintDateTime()); // Descending order
                        });
                        setDataToRecycler(complaints);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.complaintsProgressbar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int findComplaintIndex(String complaintID) {
        for (int i = 0; i < complaints.size(); i++) {
            if (complaints.get(i).getComplaintId().equals(complaintID)) {
                return i;
            }
        }
        return -1;
    }

    private void setDataToRecycler(List<ComplaintModel> complaints) {
        ComplaintsAdp complaintsAdapter = new ComplaintsAdp(getActivity(), complaints);
        binding.complaintsRecycler.setAdapter(complaintsAdapter);
        binding.complaintsProgressbar.setVisibility(View.GONE);
    }

}
