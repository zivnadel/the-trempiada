package com.example.thetrempiada.ui.contact;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;


public class ContactFragment extends Fragment {

    private ImageButton email, phone;
    private final String appEmail = "zywn1414@gmail.com";
    private final String appPhone = "0528485980";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        Dialog contactDialog = new Dialog(getContext());
        contactDialog.setContentView(R.layout.fragment_contact);
        contactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        contactDialog.show();

        email = contactDialog.findViewById(R.id.btn_email);
        phone = contactDialog.findViewById(R.id.btn_phone);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + appEmail));
                startActivity(intent);
            }
        });

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + appPhone));
                startActivity(intent);
            }
        });

        contactDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requireActivity().onBackPressed();
            }
        });

    }
}