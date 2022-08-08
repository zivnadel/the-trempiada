package com.example.thetrempiada.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;
import com.example.thetrempiada.data.BaseActivity;
import com.example.thetrempiada.data.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    private TextInputEditText logEmail, logPassword;
    private TextInputLayout passwordContainer, emailContainer;
    private TextView forgotPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private SwitchMaterial remember;
    private boolean emailValid = false, passwordValid = false;

    // required to be public static to be accessed from settings activity when signing out
    public static SharedPreferences sp;

    // used for reset password dialog
    private boolean resetPasswordEmailValid = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // disable night theme (launcher-activity)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // getting the instance of firebase

        firebaseAuth = FirebaseAuth.getInstance();
        sp = this.getPreferences(Context.MODE_PRIVATE);

        // checking if some sort of "cache" is still saved, if so - disconnecting
        if(!sp.getBoolean(SHARED_PREFERENCES_REMEMBER_ME, false) && FirebaseAuth.getInstance().getCurrentUser() != null) {
            UserHelper.setOnline(FirebaseAuth.getInstance().getCurrentUser().getUid(), false);
            firebaseAuth.signOut();
        }

        // auto login process
        // move to main activity if user already signed in

        if(sp.getBoolean(SHARED_PREFERENCES_REMEMBER_ME, false) && FirebaseAuth.getInstance().getCurrentUser() != null) {
            UserHelper.setOnline(FirebaseAuth.getInstance().getUid(), true);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        logEmail = findViewById(R.id.username_login);
        logPassword = findViewById(R.id.password_login);

        passwordContainer = findViewById(R.id.password_login_container);
        emailContainer = findViewById(R.id.username_login_container);

        btnLogin = findViewById(R.id.login);
        btnRegister = findViewById(R.id.go_to_register);

        progressBar = findViewById(R.id.loading_login);
        remember = findViewById(R.id.remember_switch);

        forgotPassword = findViewById(R.id.forgot_password);

        // go to register activity

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        // start login process if btnLogin is clicked

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = logEmail.getText().toString();
                String password = logPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this.getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this.getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // login user using signInWithEmailAndPassword function and handling errors
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if(!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Error! " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putBoolean(SHARED_PREFERENCES_REMEMBER_ME, remember.isChecked());
                                    editor.apply();
                                    // changing user status to online
                                    UserHelper.setOnline(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });

        // blocking and enabling login button according to patterns of correct email and password
        logEmail.addTextChangedListener(new TextWatcher() {
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
                btnLogin.setEnabled(emailValid && passwordValid);
            }
        });

        logPassword.addTextChangedListener(new TextWatcher() {
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
                    passwordContainer.setError("Password must be 6-16 numbers, letters or underscore, and contain at least 1 letter.");
                } else {
                    passwordContainer.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                btnLogin.setEnabled(emailValid && passwordValid);
            }
        });

        // go to reset password if clicked on forgot password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog resetPasswordDialog = new Dialog(LoginActivity.this);
                resetPasswordDialog.setContentView(R.layout.reset_password_dialog);
                resetPasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                resetPasswordDialog.show();

                TextInputEditText email = resetPasswordDialog.findViewById(R.id.reset_password_email);
                TextInputLayout emailContainer = resetPasswordDialog.findViewById(R.id.reset_password_email_container);
                Button reset = resetPasswordDialog.findViewById(R.id.btn_reset_password);

                // blocking and enabling reset button according to correct email pattern
                email.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        emailContainer.setError(null);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                            resetPasswordEmailValid = true;
                        } else if(s.length() > 0) {
                            resetPasswordEmailValid = false;
                            emailContainer.setError("Not a valid email!");
                        } else {
                            emailContainer.setError("This field cannot be blank!");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        reset.setEnabled(resetPasswordEmailValid);
                    }
                });

                // send reset email when reset button is enabled
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        firebaseAuth.sendPasswordResetEmail(email.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Instructions to reset your password have been sent to you by mail.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                        }
                                        progressBar.setVisibility(View.GONE);
                                        resetPasswordDialog.dismiss();
                                    }
                                });
                    }
                });
            }
        });
    }
}
