package com.example.thetrempiada.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thetrempiada.R;
import com.example.thetrempiada.data.BaseActivity;
import com.example.thetrempiada.data.UserHelper;
import com.example.thetrempiada.data.directionData.StorageHelper;
import com.example.thetrempiada.enums.Gender;
import com.example.thetrempiada.enums.UserType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

public class ProfileSettingsActivity extends BaseActivity {

    private TextView editProfileTitle;
    private ImageView editProfilePicture;
    private TextInputEditText editDisplayName, editAge, editDescription, confirmPassword;
    private TextInputLayout nameContainer, passwordContainer;
    private AutoCompleteTextView editGender, editUserType;
    private static final String[] GENDER_PATHS = {"Male", "Female"};
    private static final String[] TYPE_PATHS = {"Driver", "Trempist", "Both"};
    private Button confirmChanges;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    // variables used for uploading a photo
    private static Uri filePath;
    private static final StorageReference storageReference = StorageHelper._storageReference.child("profilePictures");
    private final int PICK_IMAGE_REQUEST = 22;

    private boolean displayNameValid = true, confirmPasswordValid = false, genderSelected = false, userTypeSelected = false;
    private boolean alrClickedConfirm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        databaseReference = FirebaseDatabase.getInstance("https://thetrempiada-5e41a-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // apply to custom toolbar from XML
        Toolbar toolbar = findViewById(R.id.toolbar_settings_profile);
        toolbar.setTitle("Profile Settings");
        setSupportActionBar(toolbar);

        // styling the title
        editProfileTitle = findViewById(R.id.edit_profile_title);
        TextPaint paint = editProfileTitle.getPaint();
        float width = paint.measureText(editProfileTitle.getText().toString());
        Shader textShader = new LinearGradient(0, 0, width, editProfileTitle.getTextSize(), new int[] {
                Color.parseColor("#FF9800"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#FF5722"),
        }, null, Shader.TileMode.CLAMP);
        editProfileTitle.getPaint().setShader(textShader);

        // uploading profile photo
        editProfilePicture = findViewById(R.id.edit_profile_picture);
        // on pressing the image, the user will be moved to gallery to select a file
        editProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Defining Implicit Intent to mobile gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(
                        Intent.createChooser(
                                intent,
                                "Select Image from here..."),
                        PICK_IMAGE_REQUEST);
            }
        });

        editDisplayName = findViewById(R.id.edit_display_name);
        editAge = findViewById(R.id.edit_age);
        editDescription = findViewById(R.id.edit_description);
        confirmPassword = findViewById(R.id.edit_confirm_password);

        nameContainer = findViewById(R.id.edit_name_container);
        passwordContainer = findViewById(R.id.confirm_password_to_edit_container);

        // blocking and enabling change button according to pattern of correct name and password confirmation
        editDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                nameContainer.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("^[\\w \\.]{3,16}")) {
                    displayNameValid = true;
                } else if(s.length() > 0) {
                    displayNameValid = false;
                    nameContainer.setError("Not a valid display name!");
                } else {
                    // display name is valid if empty because display name doesn't have to be changed
                    displayNameValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                confirmChanges.setEnabled(displayNameValid && confirmPasswordValid);
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                passwordContainer.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("^(?=.*[a-zA-Z])\\w{6,16}$")) {
                    confirmPasswordValid = true;
                } else if(s.length() > 0) {
                    confirmPasswordValid = false;
                    passwordContainer.setError("Invalid password!");
                } else {
                    passwordContainer.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                confirmChanges.setEnabled(displayNameValid && confirmPasswordValid);
            }
        });

        // creating the dropdown menus
        editGender = findViewById(R.id.edit_gender);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(ProfileSettingsActivity.this, R.layout.dropdownmenu_items, GENDER_PATHS);
        genderAdapter.setDropDownViewResource(R.layout.dropdownmenu_items);
        editGender.setAdapter(genderAdapter);
        editGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                genderSelected = true;
            }
        });

        editUserType = findViewById(R.id.edit_type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(ProfileSettingsActivity.this, R.layout.dropdownmenu_items, TYPE_PATHS);
        typeAdapter.setDropDownViewResource(R.layout.dropdownmenu_items);
        editUserType.setAdapter(typeAdapter);
        editUserType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                userTypeSelected = true;
            }
        });

        progressBar = findViewById(R.id.loading_change_profile);

        confirmChanges = findViewById(R.id.confirm_profile_changes);

        // attempting to change profile settings on button click
        confirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!alrClickedConfirm) {
                    alrClickedConfirm = true;

                    Toast.makeText(ProfileSettingsActivity.this, "Click again to confirm!", Toast.LENGTH_SHORT).show();

                    // slowmode
                    confirmChanges.setEnabled(false);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            confirmChanges.setEnabled(true);
                        }
                    }, 500);
                    return;
                }

                alrClickedConfirm = false;

                progressBar.setVisibility(View.VISIBLE);

                // using reauthenication to assure user has entered the old password correctly
                AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), confirmPassword.getText().toString());
                currentUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            // if task is successful, user has entered the right password, so we can proceed and edit the profile
                            // profile name
                            if(!editDisplayName.getText().toString().matches("")) {
                                UserProfileChangeRequest displayName = new UserProfileChangeRequest.Builder().setDisplayName(editDisplayName.getText().toString()).build();
                                Objects.requireNonNull(currentUser.updateProfile(displayName)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, display name is not updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            UserHelper._userDatabase.child(currentUser.getUid()).child("_displayName").setValue(editDisplayName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(!task.isSuccessful()) {
                                                        Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, display name is not updated", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            // age
                            if(!editAge.getText().toString().matches("")) {
                                UserHelper._userDatabase.child(currentUser.getUid()).child("_age").setValue(Integer.parseInt(editAge.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, age is not updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            // description
                            if(!editDescription.getText().toString().matches("")) {
                                UserHelper._userDatabase.child(currentUser.getUid()).child("_description").setValue(editDescription.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, description is not updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            // gender
                            if(genderSelected) {
                                Gender gender = null;
                                switch (editGender.getText().toString()) {
                                    case "Male":
                                        gender = Gender.MALE;
                                        break;
                                    case "Female":
                                        gender = Gender.FEMALE;
                                        break;
                                }
                                UserHelper._userDatabase.child(currentUser.getUid()).child("_gender").setValue(gender).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, gender is not updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            // userType
                            if(userTypeSelected) {
                                UserType userType = null;
                                switch (editUserType.getText().toString()) {
                                    case "Driver":
                                        userType = UserType.DRIVER;
                                        break;
                                    case "Trempist":
                                        userType = UserType.TREMPIST;
                                        break;
                                    case "Both":
                                        userType = UserType.BOTH;
                                        break;
                                }
                                UserHelper._userDatabase.child(currentUser.getUid()).child("_userType").setValue(userType).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, user type is not updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            // profile picture
                            if(filePath != null) {
                                // Upload the file to firebase storage
                                storageReference.child(currentUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // File already exists, delete and make a new one
                                        storageReference.child(currentUser.getUid()).delete();
                                    }
                                });
                                storageReference.child(currentUser.getUid()).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        Log.d("UploadedToStorage", String.valueOf(task.isSuccessful()));
                                        if(task.isSuccessful()) {
                                            // download file and put it in user auth database
                                            storageReference.child(currentUser.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if(task.isSuccessful()) {
                                                        UserProfileChangeRequest pictureChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(task.getResult()).build();
                                                        currentUser.updateProfile(pictureChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(!task.isSuccessful()) {
                                                                    Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, profile picture is not updated", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, profile picture is not updated", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(ProfileSettingsActivity.this, "An error has occurred, profile picture is not updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            progressBar.setVisibility(View.GONE);

                            startActivity(new Intent(ProfileSettingsActivity.this, SettingsActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ProfileSettingsActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                editProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    // create the menu which will store the back button (action button).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.go_back_menu, menu);
        return true;
    }

    // when the action button is clicked, go back.
    // used switch in case of future addons.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_back:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileSettingsActivity.this, SettingsActivity.class));
        finish();
    }
}