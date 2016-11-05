
package com.reactlibrary;

import android.app.Activity;
import android.widget.Toast;
import android.content.Intent;


import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;


public class RNGooglePlacePickerModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext reactContext;
    private Callback mCallback;
    WritableMap response;
    private static final int REQUEST_PLACE_PICKER = 1;


    public RNGooglePlacePickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNGooglePlacePicker";
    }

    @ReactMethod
    public void show(final Callback callback) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            response.putString("error", "can't find current Activity");
            callback.invoke(response);
            return;
        }

        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(currentActivity);
            mCallback = callback;
            currentActivity.startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            response = Arguments.createMap();
            response.putString("error", "GooglePlayServicesRepairableException");
            callback.invoke(response);
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), currentActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            response = Arguments.createMap();
            response.putString("error", "Google Play Services is not available.");
            callback.invoke(response);
            Toast.makeText(currentActivity, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Deprecated
    public void onActivityResult(int requestCode, int resultCode, Intent data) { }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) { 
        if (mCallback == null || requestCode != REQUEST_PLACE_PICKER) {
            return;
        }
        response = Arguments.createMap();
        if (resultCode == Activity.RESULT_OK) {
            final Place place = PlacePicker.getPlace(data, reactContext);
            final CharSequence address = place.getAddress();
            final LatLng coordinate = place.getLatLng();
            response.putString("address", address.toString());
            response.putDouble("latitude", coordinate.latitude);
            response.putDouble("longitude", coordinate.longitude);
            response.putString("google_id", place.getId());
            mCallback.invoke(response);
        } else {
            response.putBoolean("didCancel", true);
            mCallback.invoke(response);
            return;
        }
    }
}
