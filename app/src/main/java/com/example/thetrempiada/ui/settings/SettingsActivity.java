package com.example.thetrempiada.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thetrempiada.MainActivity;
import com.example.thetrempiada.R;
import com.example.thetrempiada.data.BaseActivity;
import com.example.thetrempiada.data.UserHelper;
import com.example.thetrempiada.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class SettingsActivity extends BaseActivity {

    private SwitchMaterial satelliteSwitch, nightModeSwitch;
    private TextView mapSettingsTitle, profileSettingsTitle;
    private Button changeEmail, changePassword, editProfile, deleteUser;
    private Intent intent;

    // used for change password dialog
    private boolean newPasswordValid = false, oldPasswordValid = false, alrClickedChangePassword;

    // used for change email dialog
    private boolean newEmailValid = false, confirmPasswordValid = false, alrClickedChangeEmail;

    // used for delete user dialog
    private boolean passwordValid = false, alrClickedDeleteUser;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // getting current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        // apply to custom toolbar from XML
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        intent = new Intent(this, MainActivity.class);
        // this flags are added so when we exit SettingsActivity, old instance of MainActivity is deleted
        // only the new one with updated settings will be presented.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

        this.nightModeSwitch = findViewById(R.id.nightModeSwitch);
        this.satelliteSwitch = findViewById(R.id.satelliteSwitch);

        // saving the states of the switched to shared prefernces
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        this.nightModeSwitch.setChecked(sharedPref.getBoolean(SHARED_PREFERENCES_IS_NIGHT_MAP_SWITCH, false));
        this.satelliteSwitch.setChecked(sharedPref.getBoolean(SHARED_PREFERENCES_IS_SATELLITE_SWITCH, false));

        satelliteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                intent.putExtra(INTENT_EXTRA_SATELLITE, isChecked);
                editSwitchPreferences(SHARED_PREFERENCES_IS_SATELLITE_SWITCH, isChecked);
                if (isChecked) {
                    nightModeSwitch.setChecked(false);
                    editSwitchPreferences(SHARED_PREFERENCES_IS_NIGHT_MAP_SWITCH, false);
                }
            }
        });

        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                intent.putExtra(INTENT_EXTRA_NIGHT_MAP, isChecked);
                editSwitchPreferences(SHARED_PREFERENCES_IS_NIGHT_MAP_SWITCH, isChecked);
                if (isChecked) {
                    satelliteSwitch.setChecked(false);
                    editSwitchPreferences(SHARED_PREFERENCES_IS_SATELLITE_SWITCH, false);
                }
            }
        });

        // settings the gradient color of the titles
        // MUST add the first color of the gradient as textColor in the XML for it to work!
        mapSettingsTitle = findViewById(R.id.mapSettingsTitle);
        profileSettingsTitle = findViewById(R.id.profileSettingsTitle);

        TextPaint paint = mapSettingsTitle.getPaint();
        float width = paint.measureText(mapSettingsTitle.getText().toString());
        Shader textShader = new LinearGradient(0, 0, width, mapSettingsTitle.getTextSize(), new int[]{
                Color.parseColor("#FF9800"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#FF5722"),
        }, null, Shader.TileMode.CLAMP);
        mapSettingsTitle.getPaint().setShader(textShader);
        profileSettingsTitle.getPaint().setShader(textShader);

        changeEmail = findViewById(R.id.change_email);
        changePassword = findViewById(R.id.change_password);
        editProfile = findViewById(R.id.btn_edit_profile);
        deleteUser = findViewById(R.id.delete_user);

        // custom font for buttons
        Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        changePassword.setTypeface(font);
        editProfile.setTypeface(font);
        changeEmail.setTypeface(font);
        deleteUser.setTypeface(font);

        // initializing check booleans for already clicked on changed button
        alrClickedChangeEmail = false;
        alrClickedChangePassword = false;
        alrClickedDeleteUser = false;

        // dialogs for changing the profile settings
        // change email dialog
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog changeEmailDialog = new Dialog(SettingsActivity.this);
                changeEmailDialog.setContentView(R.layout.change_email_dialog);
                changeEmailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changeEmailDialog.show();

                TextInputEditText newEmail = changeEmailDialog.findViewById(R.id.new_email_edit);
                TextInputEditText confirmPassword = changeEmailDialog.findViewById(R.id.confirm_password_edit);
                TextInputLayout emailContainer = changeEmailDialog.findViewById(R.id.change_email_email_container);
                TextInputLayout passwordContainer = changeEmailDialog.findViewById(R.id.change_email_password_container);
                Button confirmChanges = changeEmailDialog.findViewById(R.id.confirm_email_change);
                ProgressBar progressBar = changeEmailDialog.findViewById(R.id.loading_change_email);

                // blocking and enabling change button according to patterns of correct email and password
                newEmail.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        emailContainer.setError(null);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                            newEmailValid = true;
                        } else if (s.length() > 0) {
                            newEmailValid = false;
                            emailContainer.setError("Not a valid email!");
                        } else {
                            emailContainer.setError("This field cannot be blank!");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmChanges.setEnabled(newEmailValid && confirmPasswordValid);
                    }
                });

                confirmPassword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        passwordContainer.setError(null);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().matches("^(?=.*[a-zA-Z])\\w{6,16}$")) {
                            confirmPasswordValid = true;
                        } else if (s.length() > 0) {
                            confirmPasswordValid = false;
                            passwordContainer.setError("Not a valid password!");
                        } else {
                            passwordContainer.setError("This field cannot be blank!");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmChanges.setEnabled(newEmailValid && confirmPasswordValid);
                    }
                });

                // button is enabled if newEmailValid && confirmPasswordValid
                confirmChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!alrClickedChangeEmail) {
                            alrClickedChangeEmail = true;

                            Toast.makeText(SettingsActivity.this, "Click again to confirm!", Toast.LENGTH_SHORT).show();

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

                        alrClickedChangeEmail = false;

                        progressBar.setVisibility(View.VISIBLE);
                        final String email = user.getEmail();
                        assert email != null;
                        AuthCredential credential = EmailAuthProvider.getCredential(email, confirmPassword.getText().toString());

                        // using reauthenicate to assure user has entered the right old password, if succeeded, update to new email
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updateEmail(newEmail.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SettingsActivity.this, "Email is updated. Please sign in with the new Email!", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);

                                                        // signing out
                                                        UserHelper.setOnline(FirebaseAuth.getInstance().getUid(), false);
                                                        FirebaseAuth.getInstance().signOut();
                                                        SharedPreferences.Editor editor = LoginActivity.sp.edit();
                                                        editor.putBoolean(SHARED_PREFERENCES_REMEMBER_ME, false);
                                                        editor.apply();
                                                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                        finish();

                                                    } else {
                                                        Toast.makeText(SettingsActivity.this, "Failed to update email!", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
            }
        });

        // change password dialog
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog changePasswordDialog = new Dialog(SettingsActivity.this);
                changePasswordDialog.setContentView(R.layout.change_password_dialog);
                changePasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changePasswordDialog.show();

                TextInputEditText newPassword = changePasswordDialog.findViewById(R.id.new_password_edit);
                TextInputEditText oldPassword = changePasswordDialog.findViewById(R.id.old_password_edit);
                TextInputLayout newPasswordContainer = changePasswordDialog.findViewById(R.id.change_password_new_password_container);
                TextInputLayout oldPasswordContainer = changePasswordDialog.findViewById(R.id.change_password_old_password_container);
                Button confirmChanges = changePasswordDialog.findViewById(R.id.confirm_password_change);
                ProgressBar progressBar = changePasswordDialog.findViewById(R.id.loading_change_password);

                // blocking and enabling change button according to patterns of correct email and password
                newPassword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        newPasswordContainer.setError(null);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().matches("^(?=.*[a-zA-Z])\\w{6,16}$")) {
                            newPasswordValid = true;
                        } else if (s.length() > 0) {
                            newPasswordValid = false;
                            newPasswordContainer.setError("Not a valid password!");
                        } else {
                            newPasswordContainer.setError("This field cannot be blank!");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmChanges.setEnabled(oldPasswordValid && newPasswordValid);
                    }
                });

                oldPassword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        oldPasswordContainer.setError(null);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().matches("^(?=.*[a-zA-Z])\\w{6,16}$")) {
                            oldPasswordValid = true;
                        } else if (s.length() > 0) {
                            oldPasswordValid = false;
                            oldPasswordContainer.setError("Not a valid password!");
                        } else {
                            oldPasswordContainer.setError("This field cannot be blank!");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmChanges.setEnabled(oldPasswordValid && newPasswordValid);
                    }
                });

                // button is enabled if oldPasswordValid && newPasswordValid
                confirmChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!alrClickedChangePassword) {
                            alrClickedChangePassword = true;

                            Toast.makeText(SettingsActivity.this, "Click again to confirm!", Toast.LENGTH_SHORT).show();

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

                        alrClickedChangePassword = false;

                        progressBar.setVisibility(View.VISIBLE);
                        final String email = user.getEmail();
                        assert email != null;
                        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword.getText().toString());

                        // using reauthenicate to assure user has entered the right old password, if succeeded, update to new password
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPassword.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SettingsActivity.this, "Password is updated. Please sign in with the new password!", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);

                                                        // signing out
                                                        UserHelper.setOnline(FirebaseAuth.getInstance().getUid(), false);
                                                        FirebaseAuth.getInstance().signOut();
                                                        SharedPreferences.Editor editor = LoginActivity.sp.edit();
                                                        editor.putBoolean(SHARED_PREFERENCES_REMEMBER_ME, false);
                                                        editor.apply();
                                                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(SettingsActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SettingsActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        // starting edit profile menu (activity)
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ProfileSettingsActivity.class));
                finish();
            }
        });

        // delete user dialog
        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog deleteUserDialog = new Dialog(SettingsActivity.this);
                deleteUserDialog.setContentView(R.layout.delete_user_dialog);
                deleteUserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                deleteUserDialog.show();

                TextInputEditText confirmPassword = deleteUserDialog.findViewById(R.id.confirm_password_delete_edit);
                TextInputLayout passwordContainer = deleteUserDialog.findViewById(R.id.delete_user_password_container);
                Button confirmChanges = deleteUserDialog.findViewById(R.id.confirm_delete_user);
                ProgressBar progressBar = deleteUserDialog.findViewById(R.id.loading_delete_user);

                // checking if password is valid in order to enable button
                confirmPassword.addTextChangedListener(new TextWatcher() {
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
                        confirmChanges.setEnabled(passwordValid);
                    }
                });

                confirmChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!alrClickedDeleteUser) {
                            alrClickedDeleteUser = true;

                            Toast.makeText(SettingsActivity.this, "Click again to confirm!", Toast.LENGTH_SHORT).show();

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

                        alrClickedDeleteUser = false;

                        progressBar.setVisibility(View.VISIBLE);
                        final String email = user.getEmail();
                        assert email != null;
                        AuthCredential credential = EmailAuthProvider.getCredential(email, confirmPassword.getText().toString());
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    String userUid = user.getUid();
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                // user successfully deleted, delete user data from database
                                                UserHelper.deleteUserFromDatabase(userUid, SettingsActivity.this);
                                                progressBar.setVisibility(View.GONE);

                                                // signing out
                                                FirebaseAuth.getInstance().signOut();
                                                SharedPreferences.Editor editor = LoginActivity.sp.edit();
                                                editor.putBoolean(SHARED_PREFERENCES_REMEMBER_ME, false);
                                                editor.apply();
                                                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(SettingsActivity.this, "Error deleting user!", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
            }
        });
    }


    public void editSwitchPreferences(String label, boolean isChecked) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(label, isChecked);
        editor.apply();
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
        Bundle extras = intent.getExtras();
        if(extras == null) {
            intent.putExtra(INTENT_EXTRA_NIGHT_MAP, nightModeSwitch.isChecked());
            intent.putExtra(INTENT_EXTRA_SATELLITE, satelliteSwitch.isChecked());
        }
        startActivity(intent);
        finish();
    }
}