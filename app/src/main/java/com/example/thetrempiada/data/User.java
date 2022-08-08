package com.example.thetrempiada.data;

import com.example.thetrempiada.data.directionData.LatLng;
import com.example.thetrempiada.enums.Gender;
import com.example.thetrempiada.enums.UserType;

/**
 * This class will hold all the data about the user.
 * An object of this class will be uploaded to Firebase Realtime Database.
 * Display Name and Profile Image aren't included here since those are included in the default Firebase User Database
 */

public class User {

    // UID from Firebase Database
    private String _uid;
    private String _displayName;
    // value -1 = no age entered
    private int _age;
    private Gender _gender;
    private UserType _userType;
    // empty string ("") = no description
    private String _description;
    // current user location
    private LatLng _currentLocation;
    // user connectivity status
    private boolean _online;

    // empty constructor
    private User() {}

    // all members constructor
    public User(String uid, String displayName, int age, Gender gender, UserType userType, String description) {
        this._uid = uid;
        this._displayName = displayName;
        this._age = age;
        this._gender = gender;
        this._userType = userType;
        this._description = description;
        this._currentLocation = null;
        this._online = false;
    }

    // no age constructor
    public User(String uid, String displayName, Gender gender, UserType userType, String description) {
        this._uid = uid;
        this._displayName = displayName;
        this._age = -1;
        this._gender = gender;
        this._userType = userType;
        this._description = description;
        this._currentLocation = null;
        this._online = false;
    }

    // no description constructor
    public User(String uid, String displayName, int age, Gender gender, UserType userType) {
        this._uid = uid;
        this._displayName = displayName;
        this._age = age;
        this._gender = gender;
        this._userType = userType;
        this._description = "";
        this._currentLocation = null;
        this._online = false;
    }

    // no description and age constructor
    public User(String uid, String displayName, Gender gender, UserType userType) {
        this._uid = uid;
        this._displayName = displayName;
        this._age = -1;
        this._gender = gender;
        this._userType = userType;
        this._description = "";
        this._currentLocation = null;
        this._online = false;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_displayName() {
        return _displayName;
    }

    public void set_displayName(String _displayName) {
        this._displayName = _displayName;
    }

    public int get_age() {
        return _age;
    }

    public void set_age(int _age) {
        this._age = _age;
    }

    public Gender get_gender() {
        return _gender;
    }

    public void set_gender(Gender _gender) {
        this._gender = _gender;
    }

    public UserType get_userType() {
        return _userType;
    }

    public void set_userType(UserType _userType) {
        this._userType = _userType;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public void set_currentLocation(double latitude, double longitude) {
        this._currentLocation = new LatLng(latitude, longitude);
    }

    public void set_currentLocation(LatLng latLng) {
        this._currentLocation = new LatLng(latLng.getLatitude(), latLng.getLongitude());
    }

    public LatLng get_currentLocation() {
        return this._currentLocation;
    }

    public boolean is_online() {
        return _online;
    }

    public void set_online(boolean _online) {
        this._online = _online;
    }
}
