package com.example.thetrempiada.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PathStorageHelper {

    public static final DatabaseReference _pathDatabase = FirebaseDatabase.getInstance("https://thetrempiada-5e41a-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("paths");

}
