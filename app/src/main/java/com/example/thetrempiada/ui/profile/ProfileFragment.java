package com.example.thetrempiada.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.thetrempiada.R;
import com.example.thetrempiada.data.User;
import com.example.thetrempiada.data.directionData.StorageHelper;
import com.example.thetrempiada.ui.settings.ProfileSettingsActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Objects;

/**
 * This fragment will show profile and profile data.
 * Work in progress.
 */

public class ProfileFragment extends Fragment {

    private ImageView profilePicture;
    private TextView usernameTitle, usernameType;
    private TextInputEditText usernameDescription;
    private ImageButton close, goToSettings;
    private FirebaseAuth firebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePicture = view.findViewById(R.id.profilePicture);
        usernameTitle = view.findViewById(R.id.usernameLine);
        usernameType = view.findViewById(R.id.userTypeLine);
        usernameDescription = view.findViewById(R.id.description_view);


        // getting users data
        firebaseAuth = FirebaseAuth.getInstance();
        // setting usernameTitle to user's display name
        usernameTitle.setText(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getDisplayName());
        // settings profilePicture to user's profile picture
        Glide.with(ProfileFragment.this)
                .load(firebaseAuth.getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(profilePicture);

        // getting user data from realtime database and updating profile at real time
        DatabaseReference database = FirebaseDatabase.getInstance("https://thetrempiada-5e41a-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        database.child("users").child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // getting current user at real time
                User user = snapshot.getValue(User.class);
                assert user != null;

                switch (user.get_userType().toString()) {
                    case "DRIVER":
                        usernameType.setText("Driver");
                        break;
                    case "TREMPIST":
                        usernameType.setText("Trempist");
                        break;
                    case "BOTH":
                        usernameType.setText("Driver and Trempist");
                        break;
                }
                usernameDescription.setText(user.get_description());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        close = view.findViewById(R.id.btn_x);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        goToSettings = view.findViewById(R.id.btn_go_to_profile_settings);
        goToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), ProfileSettingsActivity.class));
                requireActivity().finish();
            }
        });

        return view;

    }
}
