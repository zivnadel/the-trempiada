package com.example.thetrempiada.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * This is the home, practically main fragment.
 * It will be launched above the map with the app launching, and can be accessed via "Home" button in the side menu.
 * It contains layouts which will float above the map on the main page (such as the current "welcome" layout).
 */

public class HomeFragment extends Fragment {

    private FloatingActionButton btnCenterMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        this.btnCenterMap = view.findViewById(R.id.btn_center_map);
        btnCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.mCurLatLng != null)
                    MainActivity.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.mCurLatLng, 15));
            }
        });

        return view;
    }
}