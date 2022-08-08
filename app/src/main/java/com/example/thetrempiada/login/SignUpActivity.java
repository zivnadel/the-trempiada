package com.example.thetrempiada.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;
import com.example.thetrempiada.data.BaseActivity;
import com.example.thetrempiada.data.User;
import com.example.thetrempiada.data.UserHelper;
import com.example.thetrempiada.enums.Gender;
import com.example.thetrempiada.enums.UserType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class SignUpActivity extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    private static final String TAG = "SignUpActivity";
    private TextInputEditText regEmail, regPassword, regConfirmPassword, regName, regAge;
    private TextInputLayout emailContainer, passwordContainer, confirmPasswordContainer, nameContainer;
    private AutoCompleteTextView regGender, regType;
    private static final String[] GENDER_PATHS = {"Male", "Female"};
    private static final String[] TYPE_PATHS = {"Driver", "Trempist", "Both"};
    private Button register;
    private ProgressBar progressBar;
    private boolean emailValid = false, passwordValid = false, nameValid = false, confirmPasswordValid = false, genderSelected = false, typeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        regEmail = findViewById(R.id.register_username);
        regPassword = findViewById(R.id.register_password);
        regConfirmPassword = findViewById(R.id.register_confirm_password);
        regAge = findViewById(R.id.register_age);
        regName = findViewById(R.id.register_display_name);

        emailContainer = findViewById(R.id.email_container);
        passwordContainer = findViewById(R.id.password_container);
        confirmPasswordContainer = findViewById(R.id.confirm_password_container);
        nameContainer = findViewById(R.id.name_container);

        register = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.loading_register);

        // setting up the gender dropdown menu
        regGender = findViewById(R.id.reg_gender);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(SignUpActivity.this, R.layout.dropdownmenu_items, GENDER_PATHS);
        genderAdapter.setDropDownViewResource(R.layout.dropdownmenu_items);
        regGender.setAdapter(genderAdapter);
        regGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                genderSelected = true;
                register.setEnabled(emailValid && passwordValid && nameValid && confirmPasswordValid && genderSelected && typeSelected);
            }
        });

        // setting up the user type dropdown menu
        regType = findViewById(R.id.reg_type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(SignUpActivity.this, R.layout.dropdownmenu_items, TYPE_PATHS);
        typeAdapter.setDropDownViewResource(R.layout.dropdownmenu_items);
        regType.setAdapter(typeAdapter);
        regType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                typeSelected = true;
                register.setEnabled(emailValid && passwordValid && nameValid && confirmPasswordValid && genderSelected && typeSelected);
            }
        });

        // registering user on click of sign up button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // checking validity of fields in order to activate the button
        regEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                emailContainer.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    emailValid = true;
                } else if(s.length() > 0) {
                    emailValid = false;
                    emailContainer.setError("Not a valid email!");
                } else {
                    emailContainer.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                register.setEnabled(emailValid && passwordValid && nameValid && confirmPasswordValid && genderSelected && typeSelected);
            }
        });

        regPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                passwordContainer.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("^(?=.*[a-zA-Z])\\w{6,16}$")) {
                    passwordValid = true;
                } else if(s.length() > 0) {
                    passwordValid = false;
                    passwordContainer.setError("Password must be 6-16 numbers, letters or underscores, and contain at least 1 letter.");
                } else {
                    passwordContainer.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                register.setEnabled(emailValid && passwordValid && nameValid && confirmPasswordValid && genderSelected && typeSelected);
            }
        });

        regConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                confirmPasswordContainer.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals(regPassword.getText().toString())) {
                    confirmPasswordValid = true;
                } else if(s.length() > 0) {
                    confirmPasswordValid = false;
                    confirmPasswordContainer.setError("Confirmation doesn't match!");
                } else {
                    confirmPasswordContainer.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                register.setEnabled(emailValid && passwordValid && nameValid && confirmPasswordValid && genderSelected && typeSelected);
            }
        });

        regName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                nameContainer.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("^[\\w \\.]{3,16}")) {
                    nameValid = true;
                } else if(s.length() > 0) {
                    nameValid = false;
                    nameContainer.setError("Display name must be 3-16 numbers, letters, spaces, underscores or dots.");
                } else {
                    nameContainer.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                register.setEnabled(emailValid && passwordValid && nameValid && confirmPasswordValid && genderSelected && typeSelected);
            }
        });

    }

    private void registerUser() {
        String userEmail = regEmail.getText().toString();
        String userPassword = regPassword.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        // register user
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "New user registration: " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            String userDisplayName = regName.getText().toString();

                            int userAge = 0;
                            if(!regAge.getText().toString().matches(""))
                                userAge = Integer.parseInt(regAge.getText().toString());
                            else userAge = -1;

                            Gender userGender = null;
                            switch (regGender.getText().toString()) {
                                case "Male":
                                    userGender = Gender.MALE;
                                    break;
                                case "Female":
                                    userGender = Gender.FEMALE;
                                    break;
                            }

                            UserType userType = null;
                            switch(regType.getText().toString()) {
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

                            UserProfileChangeRequest displayName = new UserProfileChangeRequest.Builder().setDisplayName(userDisplayName).build();
                            Objects.requireNonNull(firebaseAuth.getCurrentUser()).updateProfile(displayName);

                            //adding user to database
                            User user = new User(firebaseAuth.getCurrentUser().getUid(), userDisplayName, userAge, userGender, userType);
                            user.set_online(true);
                            UserHelper.addUserToDatabase(user, user.get_uid(), SignUpActivity.this);

                            progressBar.setVisibility(View.GONE);

                            //starting the app
                            SignUpActivity.this.startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            SignUpActivity.this.finish();
                        }
                    }
                });

    }
}