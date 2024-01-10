package com.example.alertify_main_admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alertify_main_admin.databinding.ComplaintsBinding;

public class ComplaintsFragment extends Fragment {

    private ComplaintsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ComplaintsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
