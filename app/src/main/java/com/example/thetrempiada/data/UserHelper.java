package com.example.thetrempiada.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


// this class is used to handle some of the operations in using users


public class UserHelper {

    public static final DatabaseReference _userDatabase = FirebaseDatabase.getInstance("https://thetrempiada-5e41a-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users");

    // adds the user to the database

    public static void addUserToDatabase(User user, String uid, Context context) {
        _userDatabase.child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Successfully signed up!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // deletes the user from the database

    public static void deleteUserFromDatabase(String uid, Context context) {
        _userDatabase.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Successfully deleted user (UID: " + uid + ")", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "An error has occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // sets user state to online or offline
    // also adds/removes user from online users database

    public static void setOnline(String uid, boolean isOnline) {
        _userDatabase.child(uid).child("_online").setValue(isOnline).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("UserChangedState: ", String.valueOf(task.isSuccessful()));
                Log.d("UserState: ", String.valueOf(isOnline));
            }
        });
    }
}
