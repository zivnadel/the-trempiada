package com.example.thetrempiada.ui.exit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;
import com.example.thetrempiada.data.BaseActivity;
import com.example.thetrempiada.data.UserHelper;
import com.example.thetrempiada.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class ExitFragment extends Fragment {

    private ImageButton yesExit, noExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dialog exitDialog = new Dialog(getActivity());
        exitDialog.setContentView(R.layout.fragment_exit);
        exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        exitDialog.show();

        yesExit = exitDialog.findViewById(R.id.yes_exit);
        noExit = exitDialog.findViewById(R.id.no_exit);

        yesExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserHelper.setOnline(FirebaseAuth.getInstance().getCurrentUser().getUid(), false);
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = LoginActivity.sp.edit();
                editor.putBoolean(BaseActivity.SHARED_PREFERENCES_REMEMBER_ME, false);
                editor.apply();
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        noExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
            }
        });

        exitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requireActivity().onBackPressed();
            }
        });
    }
}