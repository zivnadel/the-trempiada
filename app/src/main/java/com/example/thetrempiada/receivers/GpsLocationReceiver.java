package com.example.thetrempiada.receivers;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.data.UserHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GpsLocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if(!gpsEnabled) {
                Toast.makeText(context, "GPS Alert: Lost connection to the gps, the app won't be able to provide location services.", Toast.LENGTH_LONG).show();
                if(currentUser != null)
                    UserHelper.setOnline(currentUser.getUid(), false);
            } else {
                Toast.makeText(context, "GPS Alert: GPS and location services are back on", Toast.LENGTH_SHORT).show();
                if(currentUser != null)
                    UserHelper.setOnline(currentUser.getUid(), true);
            }
        }
    }
}
