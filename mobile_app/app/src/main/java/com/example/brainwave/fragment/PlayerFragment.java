package com.example.brainwave.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.brainwave.R;

public class PlayerFragment extends Fragment {

    private float startY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_ntthanh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endY = event.getY();
                        if (startY - endY > 200) {
                            // Hiển thị BottomSheetDialogFragment
                            PlayerBottomSheetDialog bottomSheetDialog = new PlayerBottomSheetDialog();
                            bottomSheetDialog.show(getChildFragmentManager(), bottomSheetDialog.getTag());
                        }
                        break;
                }
                return true;
            }
        });
    }
}
