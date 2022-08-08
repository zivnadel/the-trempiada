package com.example.thetrempiada.data.directionData;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * this class will handle anything connected with Firebase Storage
 * pretty empty for now.
 */

public class StorageHelper {

    public static final StorageReference _storageReference = FirebaseStorage.getInstance("gs://thetrempiada-5e41a.appspot.com/").getReference();

}
