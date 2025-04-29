package com.example.closetapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FilterDialogFragment extends DialogFragment {

    private List<Cloth> clothList;
    private List<String> selectedTags = new ArrayList<>();
    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFilterApplied(List<String> selectedTags);
    }

    public FilterDialogFragment(List<Cloth> clothList, OnFilterAppliedListener listener) {
        this.clothList = clothList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);
        LinearLayout checkboxContainer = view.findViewById(R.id.checkboxContainer);
        Button applyButton = view.findViewById(R.id.applyFilterButton);

        // 해시태그별 개수 세기
        HashMap<String, Integer> tagCountMap = new HashMap<>();
        for (Cloth cloth : clothList) {
            if (cloth.getTagsList() != null) {
                for (String tag : cloth.getTagsList()) {
                    tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0) + 1);
                }
            }
        }

        // 옷 수 많은 순서대로 태그 정렬
        List<String> sortedTags = new ArrayList<>(tagCountMap.keySet());
        Collections.sort(sortedTags, (t1, t2) -> tagCountMap.get(t2) - tagCountMap.get(t1));

        // 체크박스 추가
        for (String tag : sortedTags) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(tag + " (" + tagCountMap.get(tag) + ")");
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTags.add(tag);
                } else {
                    selectedTags.remove(tag);
                }
            });
            checkboxContainer.addView(checkBox);
        }

        applyButton.setOnClickListener(v -> {
            listener.onFilterApplied(selectedTags);
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}
