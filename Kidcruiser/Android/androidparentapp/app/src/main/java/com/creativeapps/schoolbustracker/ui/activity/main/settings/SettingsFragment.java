package com.creativeapps.schoolbustracker.ui.activity.main.settings;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.Child;
import com.creativeapps.schoolbustracker.data.network.models.ChildResponse;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivityModel;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import static android.content.Context.LOCATION_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat implements LocationListener,
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";
    private static final String ChildAbsencePrefix = "child_absence_";

    //view model of the main activity
    private MainActivityModel mViewModel;
    //parent object that holds parent information
    private Parent mParent;
    //last selection of zone alert distance in preference
    private Integer mLastAlertDistancePrefSelection;
    //boolean to indicate if the location access permission granted
    private boolean mLocationPermissionGranted;
    //parent's home location
    private LatLng mParentHomeLocation;

    private LocationManager mLocationManager;

    private boolean mShouldListenForLocationUpdate = true;

    private String mPattern;

    private DateFormat mDateFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //instantiate the view model object
        mViewModel = ((MainActivity) getActivity()).createViewModel();
        //get the last saved parent data
        mParent = mViewModel.getParent().getValue();
        if(mParent == null)
            mParent = Util.getSavedObjectFromPreference(getContext(),
                    "mPreference", "Parent", Parent.class);

        mPattern = "yyyy-MM-dd HH:mm:ss";
        mDateFormat = new SimpleDateFormat(mPattern, Locale.ENGLISH);

        for (Child c: mParent.getChildren()) {
            addAbsentSwitch(c);
        }
    }

    private void addAbsentSwitch(final Child child) {
        PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("absent_category");

        SwitchPreferenceCompat absent_preference = new SwitchPreferenceCompat(getPreferenceManager().getContext());
        absent_preference.setTitle(getString(R.string.absent_day1) + " " + child.getchildName() + " " + getString(R.string.absent_day2));

        absent_preference.setKey(ChildAbsencePrefix + child.getId());

        setStatusAbsentPreference(child, absent_preference);

        absent_preference.setOnPreferenceChangeListener(this);

        preferenceGroup.addPreference(absent_preference);

    }

    private void setStatusAbsentPreference(Child child, SwitchPreferenceCompat absent_preference) {
        if(absent_preference == null)
            absent_preference = findPreference(ChildAbsencePrefix + child.getId());

        if(absent_preference == null)
            return;

        if(child.getChild_AbsentTill() != null) {
            try {

                Date today = Calendar.getInstance().getTime();
                Date absent_till = mDateFormat.parse(child.getChild_AbsentTill());
                if (!today.after(absent_till)) {
                    absent_preference.setChecked(true);
                    absent_preference.setSummary("absent until " + child.getChild_AbsentTill());
                } else {
                    absent_preference.setChecked(false);
                    absent_preference.setSummary("");
                }
            } catch (Exception e) {
                Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            absent_preference.setChecked(false);
            absent_preference.setSummary("");
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if(preference.getKey().startsWith(ChildAbsencePrefix))
        {
            Integer child_id = extractChildId(preference.getKey());
            if(child_id == null)
                return true;

            if (newValue.equals(true)) {
                Date tomorow_date = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(tomorow_date);
                c.add(Calendar.DATE, 1);
                tomorow_date = c.getTime();
                String strDate = mDateFormat.format(tomorow_date);

                Log.d("onViewCreated", "strDate" + strDate);

                mViewModel.updateChildAbsentServer(child_id, strDate);

            } else {
                mViewModel.updateChildAbsentServer(child_id, null);
            }
        }
        return true;
    }

    private Integer extractChildId(String key) {
        String child_key_s = key.replaceFirst(ChildAbsencePrefix,"");
        try {
            return Integer.parseInt(child_key_s);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //instantiate the location manager object
        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        //check if the parent's home location is not null
        if (mParent.getAddress_latitude() != null && mParent.getAddress_longitude() != null)
            //save it to a variable to be used throughout
            mParentHomeLocation = new LatLng(mParent.getAddress_latitude(),
                    mParent.getAddress_longitude());

        //if the parent's home location is null
        if(mParentHomeLocation==null) {
            //disable the notification settings part
            enableDisableNotifications(false);
        }
        else
            //otherwise, write the current location in the summary of this preference
            findPreference(Util.set_home_location_preference).setSummary(mParent.getAddress_latitude() + ", " + mParent.getAddress_longitude());

        //get the last selection of zone alert distance in preference
        mLastAlertDistancePrefSelection = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Util.near_home_preference,"0"));

        // set parent's home location preference click listener
        final Preference pref = findPreference(Util.set_home_location_preference);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //set location of parent home
                SetMyHomeLocation();
                return true;
            }
        });



    }

    /*disable the notification settings part of the preference screen*/
    private void enableDisableNotifications(Boolean enable) {

        findPreference(Util.arrived_home_notify).setEnabled(enable);
        findPreference(Util.left_home_notify).setEnabled(enable);
        findPreference(Util.near_home_preference).setEnabled(enable);

        if(!enable) {
            ((SwitchPreferenceCompat)findPreference(Util.arrived_home_notify)).setChecked(enable);
            ((SwitchPreferenceCompat)findPreference(Util.left_home_notify)).setChecked(enable);
            ((ListPreference) findPreference(Util.near_home_preference)).setValueIndex(0);
        }
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey);
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        //observe changes for the response of setting the parent's alert distance
        mViewModel.getParentAlertZoneDistanceUpdateIsRunning().observe(this, new ParentAlertZoneUpdateIsRunning());

        //observe changes for the response of setting the parent's home location
        mViewModel.getParentLocationUpdateIsRunning().observe(this, new ParentLocationUpdateIsRunning());

        //observe changes for the response of updating absent
        mViewModel.getChildAbsentUpdateIsRunning().observe(this, new ChildAbsentUpdateIsRunning());
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        //remove observes
        mViewModel.getParentAlertZoneDistanceUpdateIsRunning().removeObservers(this);
        mViewModel.getParentLocationUpdateIsRunning().removeObservers(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(Util.near_home_preference)) {
            //if the selection of zone alert distance in preference is clicked
            try {
                //get the index of the selection
                Integer zoneAlertDistanceIndex = Integer.parseInt(sharedPreferences.getString(key,""));
                //calculate the distance from selection
                Integer zoneAlertDistance = Util.ZoneAlertDistanceStep * zoneAlertDistanceIndex;

                //if the selection of zone alert distance in preference is changed from previous selection
                if(zoneAlertDistanceIndex!= mLastAlertDistancePrefSelection)
                    //send the new alert distance to the server
                    mViewModel.setAlertZoneDistance(mParent.getSecretKey(), zoneAlertDistance);
            }
            catch (Exception e){}

        }
    }

    /*set the parent's home location according to the current location*/
    private void SetMyHomeLocation() {
        startLocationUpdates();
    }

    /*start listening for location change of the driver. Note that, this function is executed in
    the main activity so that the app track the driver location regardless the fragment displayed*/
    public void startLocationUpdates() {
        //check if GPS is enabled
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //if not, display an Alert message that may take the user to the settings to enable
            // the GPS
            buildAlertMessageNoGps();
        } else {
            try {
                //check if location access permission is granted
                if (mLocationPermissionGranted) {
                    //turn spinner on until receiving the location update
                    ((MainActivity)getActivity()).showHideProgressBar(true);
                    //if so, start listening for location change

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, (float) 0, this);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, (float) 0, this);

                    //mLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0, 0, this);
                } else {
                    //otherwise, request a location access permission
                    getLocationPermission();
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    }

    /*stop listening for location change*/
    public void stopLocationUpdates() {
        mLocationManager.removeUpdates(this);
    }

    /*display an alert that take the user to the settings of his device to turn the GPS on*/
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Util.displayExitMessage(getString(R.string.alert), getString(R.string.turn_on_gps), getActivity(), false);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*Request location permission, so that we can get the location of the device. The result of
    the permission request is handled by a callback, onRequestPermissionsResult.*/
    private void getLocationPermission() {

        //check if the location access permission is granted
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //if so, start listening for location change
            mLocationPermissionGranted = true;
            startLocationUpdates();
        } else {
            //otherwise, request a permission for location access
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Util.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        //The result of the permission request is handled here
        mLocationPermissionGranted = false;
        //check the type of the request
        switch (requestCode) {
            //location access permission request
            case Util.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission is granted, start listening for location updates
                    mLocationPermissionGranted = true;
                    startLocationUpdates();
                } else {
                    //otherwise, display message to the user to turn on his location and finish the app
                    Util.displayExitMessage(getString(R.string.alert), getString(R.string.grant_access_to_location_access),
                            getActivity(), false);
                }
                break;
            }
        }
    }

    //this is the listener for location updates
    @Override
    public void onLocationChanged(Location location) {
        if(mShouldListenForLocationUpdate)
        {
            //stop spinner
            ((MainActivity)getActivity()).showHideProgressBar(false);

            if (location == null)
                return;
            //get the location
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            //convert the location to LatLng object
            mParentHomeLocation = new LatLng(latitude, longitude);

            Log.d(TAG, "onLocationChanged " + latitude + "," + longitude);
            //display a confirmation dialog before setting the location of the parent
            if (mParent != null) {
                new AlertDialog.Builder(this.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.set_pick_drop_location_title))
                        .setMessage(getString(R.string.are_you_sure_set_location))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //send the new location to the server
                                mViewModel.updateParentPosition(mParent.getSecretKey(), mParentHomeLocation);
                                mShouldListenForLocationUpdate = true;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopLocationUpdates();
                                mShouldListenForLocationUpdate = true;
                            }
                        })
                        .show();
                mShouldListenForLocationUpdate = false;
            }
        }

    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    /*observe changes for the response of setting the parents alert zone distance*/
    private class ParentAlertZoneUpdateIsRunning implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean alertZoneDistanceUpdateIsRunning) {
            if (alertZoneDistanceUpdateIsRunning == null) return;

            if (alertZoneDistanceUpdateIsRunning) {
                //if setting the alert distance is running, display the progress spinner
                ((MainActivity)getActivity()).showHideProgressBar(true);
            } else {
                //otherwise, stop the spinner
                ((MainActivity)getActivity()).showHideProgressBar(false);
                //check if the server returns OK status
                if (mViewModel.getParentAlertZoneDistanceUpdateResp().getValue()) {
                    //if so, update the last selection of zone alert distance in preference
                    mLastAlertDistancePrefSelection = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Util.near_home_preference,""));
                } else {
                    //otherwise, revert selection and display alert message
                    ((ListPreference)findPreference(Util.near_home_preference)).setValueIndex(mLastAlertDistancePrefSelection);
                    Util.displayExitMessage(getString(R.string.alert), getString(R.string.unexpected_error), getActivity(), false);
                }
                mViewModel.setParentAlertZoneDistanceUpdateIsRunning(null);
            }
        }
    }

    /*observe changes for the response of setting the home location of the parent*/
    private class ParentLocationUpdateIsRunning implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean parentLocationUpdateIsRunning) {
            if (parentLocationUpdateIsRunning == null) return;

            if (parentLocationUpdateIsRunning) {
                //if setting the location is running, display the progress spinner
                ((MainActivity)getActivity()).showHideProgressBar(true);
            }
            else
            {
                //otherwise, stop the spinner
                ((MainActivity)getActivity()).showHideProgressBar(false);
                //check if the server returns OK status
                if (mViewModel.getParentLocationUpdateResp().getValue()) {
                    //if so, stop requesting location updates
                    stopLocationUpdates();
                    //update the parent's home location in the parent object
                    mParent.setAddress_latitude(mParentHomeLocation.latitude);
                    mParent.setAddress_longitude(mParentHomeLocation.longitude);
                    //save the updated parent object
                    Util.saveObjectToSharedPreference(getContext(),
                            "mPreference", "Parent", mParent);
                    //update the summary text in the preference
                    findPreference(Util.set_home_location_preference).setSummary(mParent.getAddress_latitude() + ", " + mParent.getAddress_longitude());
                    //enable the notification settings part
                    enableDisableNotifications(true);
                    //display a success message
                    Util.displayExitMessage(getString(R.string.success), getString(R.string.pickup_dropoff_ok), getActivity(), false);
                } else {
                    Util.displayExitMessage(getString(R.string.alert), getString(R.string.unexpected_error), getActivity(), false);
                }
                mViewModel.setParentLocationUpdateIsRunning(null);
            }
        }
    }


    /*observe changes for the response of changing absent date*/
    private class ChildAbsentUpdateIsRunning implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean ChildAbsentUpdateIsRunning) {
            if (ChildAbsentUpdateIsRunning == null) return;

            if (ChildAbsentUpdateIsRunning) {
                //if setting the location is running, display the progress spinner
                ((MainActivity)getActivity()).showHideProgressBar(true);
            }
            else
            {
                //otherwise, stop the spinner
                ((MainActivity)getActivity()).showHideProgressBar(false);

                ChildResponse childResponse = mViewModel.getChildAbsentUpdateResp().getValue();
                //check if the server returns OK status
                if (childResponse != null) {
                    setStatusAbsentPreference(childResponse.getChild(), null);
                } else {
                    //reset if errors
                    for (Child c: mParent.getChildren()) {
                        setStatusAbsentPreference(c, null);
                    }

                    Util.displayExitMessage(getString(R.string.alert), getString(R.string.unexpected_error), getActivity(), false);
                }
                mViewModel.setChildAbsentUpdateIsRunning(null);
            }
        }
    }
}
