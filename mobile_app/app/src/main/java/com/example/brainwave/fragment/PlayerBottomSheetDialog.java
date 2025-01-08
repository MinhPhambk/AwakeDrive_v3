package com.example.brainwave.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.brainwave.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PlayerBottomSheetDialog extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music2_ntthanh, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Lấy BottomSheet
        View view = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(view);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

}
