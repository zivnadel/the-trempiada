package com.example.thetrempiada.ui.about;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;
import com.example.thetrempiada.ui.home.HomeFragment;

import java.util.Objects;

/**
 * This fragment will launch a dialog containing "about" information.
 */

public class AboutFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dialog aboutDialog = new Dialog(getActivity());
        aboutDialog.setContentView(R.layout.fragment_about);
        aboutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aboutDialog.show();

        // when dialog dismissed, go back to main page.
        aboutDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requireActivity().onBackPressed();
            }
        });
    }
}