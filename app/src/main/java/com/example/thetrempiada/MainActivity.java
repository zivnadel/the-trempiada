package com.example.thetrempiada;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.thetrempiada.data.BaseActivity;
import com.example.thetrempiada.data.PathStorageHelper;
import com.example.thetrempiada.data.User;
import com.example.thetrempiada.data.UserHelper;
import com.example.thetrempiada.data.directionData.StorageHelper;
import com.example.thetrempiada.enums.Gender;
import com.example.thetrempiada.enums.UserType;
import com.example.thetrempiada.login.LoginActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * This activity is the gateway and the main page of the application.
 * It includes the map functionality.
 * It handles the side menu (using drawer navigation menu) and the sidebar menu (search button, for now).
 * More functionality of this activity will be added as more options will be added to the app.
 */

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    // need to be public static to be accessed from a different location
    public static GoogleMap mGoogleMap;
    public static LatLng mCurLatLng;
    public static List<Polyline> activePolylines = new ArrayList<>();
    public static Marker destinationOfDriveMarker = null;
    public static Marker openedMarker = null;

    private Menu mainMenu;
    private Marker mCurrLocationMarker;
    private AppBarConfiguration mAppBarConfiguration;
    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    // mapType = GoogleMap.MAP_TYPE_SOMETHING
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private final int MAP_TYPE_NIGHT = 16;
    private boolean zoomedIn;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener fireAuthListener;

    // user profile card view components
    private MaterialCardView userProfileCardView;
    private ImageView cardProfilePicture;
    private TextView cardTitle, cardSecondary, cardDescription;
    private Button cardTakeTrempBtn, cardMessageBtn;

    // a main shared preferences to save every data that needs to be loaded on activity/app launch
    // such as settings
    private SharedPreferences mainSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainSharedPref = this.getPreferences(Context.MODE_PRIVATE);

        // getting user data
        firebaseAuth = FirebaseAuth.getInstance();
        fireAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser tempUser = firebaseAuth.getCurrentUser();
                if(tempUser == null) {
                    // user isn't logged in
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

            }
        };

        // --- Setting up the side menu ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // inside AppBarConfigurationBuilder() is all items of the side menu.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // --- Setting up the map ---
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        this.zoomedIn = false;

        // handling changes to the main activity from other activities (such as settings)
        Intent intent = getIntent();
        assert intent != null;
        Bundle extras = intent.getExtras();
        if(extras != null) {
            // night mode
            if(intent.getBooleanExtra(INTENT_EXTRA_NIGHT_MAP, false)) {
                this.mapType = this.MAP_TYPE_NIGHT;
            }
            // satellite mode
            if(intent.getBooleanExtra(INTENT_EXTRA_SATELLITE, false)) {
                this.mapType = GoogleMap.MAP_TYPE_HYBRID;
            }
            // not night mode or satellite mode, AKA normal mode;
            if(!(intent.hasExtra(INTENT_EXTRA_NIGHT_MAP)) && !(intent.hasExtra(INTENT_EXTRA_SATELLITE))) {
                this.mapType = GoogleMap.MAP_TYPE_NORMAL;
            }
            saveMapTypeToPreference(this.mapType);
        }

        // Initialize the Places SDK
        Places.initialize(getApplicationContext(), GOOGLE_MAPS_API_KEY);

        // user profile card view components
        userProfileCardView = findViewById(R.id.user_profile_card_view);
        cardProfilePicture = findViewById(R.id.profile_card_picture);
        cardTitle = findViewById(R.id.profile_card_title);
        cardSecondary = findViewById(R.id.profile_card_secondary);
        cardDescription = findViewById(R.id.profile_card_description);
        cardTakeTrempBtn = findViewById(R.id.profile_card_order_tremp_btn);
        cardMessageBtn = findViewById(R.id.profile_card_message_btn);

        // close profile card view if clicked outside
        this.setOnTouchOutsideViewListener(findViewById(R.id.user_profile_card_view), new OnTouchOutsideViewListener() {
            @Override
            public void onTouchOutside(View view, MotionEvent event) {
                // used to prevent leaking of UID
                if(openedMarker != null) {
                    openedMarker.hideInfoWindow();
                }
                userProfileCardView.setVisibility(View.GONE);
                mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(fireAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(fireAuthListener != null) {
            firebaseAuth.removeAuthStateListener(fireAuthListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        // setting map type
        mGoogleMap = googleMap;
        if(!(this.mainSharedPref.getInt(SHARED_PREFERENCES_MAP_TYPE, 1) == 16)) {
            googleMap.setMapType(this.mainSharedPref.getInt(SHARED_PREFERENCES_MAP_TYPE, 1));
        }
        else {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_map));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // 5s interval
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                // Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }

        // raise a profile page on user marker click
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if(marker.getSnippet() != null && marker.getSnippet().length() == 28) {
                    // marker is a user marker
                    // pointer to use later, when dismissing the card view
                    openedMarker = marker;
                    if(userProfileCardView.getVisibility() != View.VISIBLE) {
                        String uid = marker.getSnippet();
                        UserHelper._userDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);

                                StorageHelper._storageReference.child("profilePictures").child(user.get_uid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(MainActivity.this)
                                                .load(uri)
                                                .placeholder(R.drawable.banner_profile)
                                                .error(R.drawable.banner_profile)
                                                .into(cardProfilePicture);
                                    }
                                });



                                cardTitle.setText(user.get_displayName());

                                final String CAR_EMOJI = "\uD83D\uDE97 ";
                                final String TREMPIST_EMOJI = "\uD83D\uDE4B\u200D♂️ ";
                                final String MALE_EMOJI = "♂️ ";
                                final String FEMALE_EMOJI = "♀️ ";

                                String userType = (user.get_userType() == UserType.DRIVER ? CAR_EMOJI : user.get_userType() == UserType.TREMPIST ? TREMPIST_EMOJI : "") +
                                        user.get_userType().toString().substring(0, 1) + user.get_userType().toString().substring(1).toLowerCase();
                                if (user.get_userType() == UserType.BOTH) {
                                    userType = CAR_EMOJI + TREMPIST_EMOJI + "Driver and Trempist";
                                }

                                String userGender = (user.get_gender() == Gender.MALE ? MALE_EMOJI : FEMALE_EMOJI)
                                        + user.get_gender().toString().substring(0, 1) + user.get_gender().toString().substring(1).toLowerCase();

                                cardSecondary.setText(userType + " | " + userGender);

                                cardDescription.setText(user.get_description());

                                if (user.get_userType() != UserType.TREMPIST) {
                                    cardTakeTrempBtn.setVisibility(View.VISIBLE);
                                }

                                // TODO : implement Message and OrderTremp buttons.
                                cardMessageBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // todo
                                    }
                                });

                                cardTakeTrempBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // todo
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("DatabaseError", error.getMessage());
                            }
                        });
                        userProfileCardView.setVisibility(View.VISIBLE);
                        // prevent the user from moving the map while profile card view visible
                        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
                    }
                }
                return false;
            }
        });

    }

    // function to quickly save map type to the shared preference
    public void saveMapTypeToPreference(int mapType) {
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putInt(SHARED_PREFERENCES_MAP_TYPE, mapType);
        editor.apply();
    }

    LocationCallback mLocationCallback = new LocationCallback() {

        /**
         * This section (callback) is being called every 5s, and will update every data connected
         * to user on the map, such as current location marker, other user's markers and more.
         */

        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                // The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                // Place current location marker and add the location to the user database
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                mCurLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                assert currentUser != null;
                UserHelper._userDatabase.child(currentUser.getUid()).child("_currentLocation").setValue(new com.example.thetrempiada.data.directionData.LatLng(mCurLatLng.latitude, mCurLatLng.longitude));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mCurLatLng);
                markerOptions.title(currentUser.getDisplayName());
                markerOptions.snippet("Current location");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(210));

                // just before the location updates, the map will clear all markers to make sure all
                // offline user markers are cleared
                mGoogleMap.clear();
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                /**
                 * this section will handle map changes according to a list of online users
                 * for example, marker to close online users and more
                 * all tasks will be done in realtime
                 */

                UserHelper._userDatabase.addValueEventListener(new ValueEventListener() {

                    // --- Anonymous class ---

                    // list to hold online users
                    List<User> onlineUsers;

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        onlineUsers = new ArrayList<User>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.child("_online").getValue(Boolean.class)) {
                                onlineUsers.add(ds.getValue(User.class));
                            }
                        }
                        // draw marker on the map, of all online users in 100km range - for now
                        for (User user : onlineUsers) {
                            if (firebaseAuth.getCurrentUser() != null &&
                                    // the user isn't the current user
                                    !user.get_uid().equals(firebaseAuth.getCurrentUser().getUid())  &&
                                    // user has current location
                                    user.get_currentLocation() != null &&
                                    // user is within a distance of 100km from current user's location
                                    distanceInKilometers(mCurLatLng.latitude, mCurLatLng.longitude, user.get_currentLocation().getLatitude(), user.get_currentLocation().getLongitude()) <= 100000.0) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new LatLng(user.get_currentLocation().getLatitude(), user.get_currentLocation().getLongitude()));
                                markerOptions.title(user.get_displayName());
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                markerOptions.snippet(user.get_uid());
                                mGoogleMap.addMarker(markerOptions);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("ErrorGettingUsersData", error.getMessage());
                    }
                });

                // move camera to current location and zoom in, only if not zoomed in already
                if(!zoomedIn) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurLatLng, 15));
                    zoomedIn = true;
                }

                mainMenu.findItem(R.id.action_new_drive).setVisible(true);
                mainMenu.findItem(R.id.action_take_tremp).setVisible(true);

            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    // result of location permission allowed / rejected
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);
                }

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // create the default menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.mainMenu = menu;
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    /**
     * default menu / action bar buttons functionality.
     * search button is presented but without functionality yet.
     * more may be added in the future.
     */

    private static final int AUTOCOMPLETE_REQUEST_CODE_NEW_DRIVE = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_TAKE_TREMP = 2;

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // launch autocomplete search view (with Places SDK) using an Intent

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        // create an autocomplete intent
        Intent intent = new Autocomplete
                .IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("IL")
                .build(this);

        // switch case for handling button clicks
        switch (item.getItemId()) {
        case R.id.action_take_tremp:
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_TAKE_TREMP);
            break;
        case R.id.action_new_drive:
            // Start the autocomplete intent.
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_NEW_DRIVE);
            break;
        case R.id.action_dismiss_route:
            item.setVisible(false);
            mainMenu.findItem(R.id.action_new_drive).setVisible(true);
            mainMenu.findItem(R.id.action_take_tremp).setVisible(true);
            // remove any existing polylines and destination marker
            for(Polyline polyline : activePolylines) {
                polyline.remove();
            }
            if(destinationOfDriveMarker != null) {
                destinationOfDriveMarker.remove();
                destinationOfDriveMarker = null;
            }
            // remove the path of the current user from the database
            PathStorageHelper._pathDatabase.child(firebaseAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("RemovedUserPath", String.valueOf(task.isSuccessful()));
                }
            });
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // if request code is one of the two buttons
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_NEW_DRIVE || requestCode == AUTOCOMPLETE_REQUEST_CODE_TAKE_TREMP) {

            if (resultCode == RESULT_OK) {

                // remove any existing polylines and destination marker
                for(Polyline polyline : activePolylines) {
                    polyline.remove();
                }
                if(destinationOfDriveMarker != null) {
                    destinationOfDriveMarker.remove();
                    destinationOfDriveMarker = null;
                }

                // set go back button visible
                MenuItem goBackBtn = mainMenu.findItem(R.id.action_dismiss_route);
                goBackBtn.setVisible(true);

                mainMenu.findItem(R.id.action_new_drive).setVisible(false);
                mainMenu.findItem(R.id.action_take_tremp).setVisible(false);

                assert data != null;
                Place place = Autocomplete.getPlaceFromIntent(data);

                // if action is new drive:
                if(requestCode == AUTOCOMPLETE_REQUEST_CODE_NEW_DRIVE && place.getLatLng() != null) {

                    // for now, draw a route on the map to the location and place a marker
                    List<LatLng> path = drawGeoPolyline(mCurLatLng, place.getLatLng());

                    // upload the list to firebase
                    PathStorageHelper._pathDatabase.child(firebaseAuth.getCurrentUser().getUid()).setValue(path).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("UploadedUserPath", String.valueOf(task.isSuccessful()));
                        }
                    });

                    // Draw the polyline, zoom in and draw destination marker.
                    if (path != null && path.size() > 0) {
                        PolylineOptions opts = new PolylineOptions().addAll(path).color(0xDF3480eb).width(15);
                        activePolylines.add(mGoogleMap.addPolyline(opts));
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurLatLng, 18));
                        destinationOfDriveMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .title(place.getName())
                                .snippet("Destination")
                                .icon(BitmapDescriptorFactory.defaultMarker(210)));
                    }

                   mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                }
                // if action is take tremp:
                // check all the active user's routes if they are clost to you
                if(requestCode == AUTOCOMPLETE_REQUEST_CODE_TAKE_TREMP) {

                    ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "Looking for a tremp", "Please wait...", true);

                    PathStorageHelper._pathDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot paths : snapshot.getChildren()) {
                                UserHelper._userDatabase.child(paths.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean isAhead = false;
                                        User user = snapshot.getValue(User.class);
                                        for(DataSnapshot latLng : paths.getChildren()) {
                                            LatLng value = latLng.getValue(LatLng.class);
                                            if(value.latitude == user.get_currentLocation().getLatitude()
                                            && value.longitude == user.get_currentLocation().getLongitude()) {
                                                isAhead = true;
                                                continue;
                                            }
                                            if(isAhead) {
                                                // we are now at the LatLngs that are ahead of the user in his path
                                                // check if any of those LatLngs are in 250m distance of the user
                                                // WIP
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Couldn't find a tremp for you!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }, 3000);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                Log.i("AutocompleteError", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                mainMenu.findItem(R.id.action_new_drive).setVisible(true);
                mainMenu.findItem(R.id.action_take_tremp).setVisible(true);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This function draws a route on the map (an actual drive route)
     * @param origin LatLng
     * @param destination LatLng
     * @return List of LatLngs as the path
     * @rtype List<LatLng>
     */

    public List<LatLng> drawGeoPolyline(LatLng origin, LatLng destination) {
        // Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();

        // Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(GOOGLE_MAPS_API_KEY)
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude),
                String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude));
        try {
            DirectionsResult res = req.await();

            // Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            // Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        // Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            if(ex.getMessage() != null) {
                Log.e(TAG, ex.getMessage());
            } else {
                Toast.makeText(this, "Cannot navigate to that location!", Toast.LENGTH_SHORT).show();
            }
            mainMenu.findItem(R.id.action_dismiss_route).setVisible(false);
            mainMenu.findItem(R.id.action_new_drive).setVisible(true);
            mainMenu.findItem(R.id.action_take_tremp).setVisible(true);
            return null;
        }
        return path;
    }

    @Override
    // settings up what clicking on hamburger (|||) button will actually do
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * This function finds the distance (on a sphere i.e. earth), between two points in kilometers.
     * The function calculations is based on Haversine formula.
     * @param lat1 : @type double : latitude of the first point
     * @param lon1 : @type double : longitude of the first point
     * @param lat2 : @type double : latitude of the second point
     * @param lon2 : @type double : longitude of the second point
     * @return : returns the distance between two points in kilometers
     * @rtype double
     */

    public double distanceInKilometers(double lat1, double lon1, double lat2, double lon2) {
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers.
        final double r = 6371;

        // calculate the result
        return(c * r);
    }
}