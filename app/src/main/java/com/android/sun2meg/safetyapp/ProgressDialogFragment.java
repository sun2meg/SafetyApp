package com.android.sun2meg.safetyapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    private ProgressBar progressBar;
    private TextView progressText;

    public static ProgressDialogFragment newInstance(int maxProgress) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putInt("maxProgress", maxProgress);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_dialog, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.progress_text);

        int maxProgress = getArguments().getInt("maxProgress");
        progressBar.setMax(maxProgress);

        return view;
    }
    public void dismissProgress() {
        progressBar.setProgress(100);
        progressText.setText("Progress: " + "100%");
        dismiss(); // Dismiss the dialog
    }
    public void updateProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
            progressText.setText("Progress: " + progress+"%");
        }
    }




}
