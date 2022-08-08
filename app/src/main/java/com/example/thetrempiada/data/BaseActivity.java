package com.example.thetrempiada.data;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * This class is a base activity which all activities in the app inherit from.
 * This class measures the inactivity time of a user, and if it is above
 * DISCONNECT_TIMEOUT, the user's status will be set to "offline".
 * The user status will also be offline if the user closes the app from
 * the recent menu, this is implemented via service.
 * This class also overrides onCreate and adds inside it a callback for
 * onDisconnect on firebase, which firebase server's will change the status of the
 * user to offline if the app closed/something happened.
 *
 * This class will also save some important variables and constants that will be used
 * all over the app.
 */

 // There are practically 2 methods of setting the status of the user:
 // one manual, after a timeout of 5 minutes, and one using firebase
 // onDisconnect method.

public class BaseActivity extends AppCompatActivity {

    public static final long DISCONNECT_TIMEOUT = 180000; // 3 min = 5 * 60 * 1000 ms

    // string constants
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyDrNIlEtJ6tX_5loQL1KoaguQVwx4bpGtU";
    public static final String SHARED_PREFERENCES_REMEMBER_ME = "remember";
    public static final String SHARED_PREFERENCES_LAST_USER = "lastUser";
    public static final String SHARED_PREFERENCES_MAP_TYPE = "mapType";
    public static final String SHARED_PREFERENCES_IS_SATELLITE_SWITCH = "isSatelliteSwitchOn";
    public static final String SHARED_PREFERENCES_IS_NIGHT_MAP_SWITCH = "isNightModeSwitchOn";
    public static final String INTENT_EXTRA_SATELLITE = "Satellite";
    public static final String INTENT_EXTRA_NIGHT_MAP = "NightMode";

    private View mTouchOutsideView;
    private OnTouchOutsideViewListener mOnTouchOutsideViewListener;

    /**
     * Sets a listener that is being notified when the user has tapped outside a given view. To remove the listener,
     * @param view
     * @param onTouchOutsideViewListener
     */
    public void setOnTouchOutsideViewListener(View view, OnTouchOutsideViewListener onTouchOutsideViewListener) {
        mTouchOutsideView = view;
        mOnTouchOutsideViewListener = onTouchOutsideViewListener;
    }

    public OnTouchOutsideViewListener getOnTouchOutsideViewListener() {
        return mOnTouchOutsideViewListener;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Notify touch outside listener if user tapped outside a given view
            if (mOnTouchOutsideViewListener != null && mTouchOutsideView != null
                    && mTouchOutsideView.getVisibility() == View.VISIBLE) {
                Rect viewRect = new Rect();
                mTouchOutsideView.getGlobalVisibleRect(viewRect);
                if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    mOnTouchOutsideViewListener.onTouchOutside(mTouchOutsideView, ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Interface definition for a callback to be invoked when a touch event has occurred outside a formerly specified
     * view. See {@link #setOnTouchOutsideViewListener(View, OnTouchOutsideViewListener).}
     */
    public interface OnTouchOutsideViewListener {

        /**
         * Called when a touch event has occurred outside a given view.
         *
         * @param view  The view that has not been touched.
         * @param event The MotionEvent object containing full information about the event.
         */
        public void onTouchOutside(View view, MotionEvent event);
    }


    private static Handler disconnectHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // ignore
            return true;
        }
    });

    private static Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            // this callback is running when user has passed the inactivity time
            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                UserHelper.setOnline(FirebaseAuth.getInstance().getCurrentUser().getUid(), false);
            }
        }
    };

    public void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            UserHelper.setOnline(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);
    }

    @Override
    public void onUserInteraction() {
        // when user interacts with the app, he is no longer offline
        stopDisconnectTimer();
    }

    /**
     * this onCreate method will serve as a "global" onCreate to
     * prevent code duplication in activities
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stopDisconnectTimer();

        // when firebase detects that the user/the app has disconnected, it will change the value of
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            // write the last logged on UID to the sharedPreferences
           getPreferences(Context.MODE_PRIVATE).edit().putString(SHARED_PREFERENCES_LAST_USER, FirebaseAuth.getInstance().getCurrentUser().getUid()).apply();

            UserHelper._userDatabase
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("_online")
                    .onDisconnect()
                    .setValue(false)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("UserOffline: ", String.valueOf(task.isSuccessful()));
                        }
                    });
        }

        // check if "trash" entries to the database exists (as a result of cache), if so - delete
        // the cache will be the deleted as soon as any user opens the app
        // (if user doesn't have the field _uid, it means it is cache, and needs to be deleted)
        UserHelper._userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren())
                    if(!ds.hasChild("_uid")) {
                        ds.getRef().removeValue();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FailedRemovingCache", error.getMessage());
            }
        });
    }


     // measuring the time between onPause and onResume i.e. the inactivity time
    @Override
    protected void onPause() {
        super.onPause();
        resetDisconnectTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopDisconnectTimer();
    }
}