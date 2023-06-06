package com.creativeapps.schoolbusdriver.ui.activity.main.map.base;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Child;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivityModel;
import com.creativeapps.schoolbusdriver.ui.activity.main.childList.bottomsheet.BottomSheetCheckDialog;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.GoogleMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;

public abstract class BaseMaps extends Fragment {

    private static final String TAG = "BaseMaps";
    //default zoom level for the Google map
    protected static final int DEFAULT_ZOOM = 16;
    //view model of the main activity
    protected MainActivityModel mViewModel;

    protected ImageView mShowMyLocation, mShowHomes, mToggleSatt;
    //Text view that display the connectivity status of the app (online or offline)
    protected TextView mStatus;
    //Layout that contains the connectivity status
    protected Menu mMenu;
    protected RelativeLayout mStatusLayout;
    private NavController mNavigation;

    //driver object that holds driver information
    protected Driver mDriver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,  int layout) {
        //inflate the layout
        View root = inflater.inflate(layout, container, false);
        //instantiate the view model object
        mViewModel = ((MainActivity) getActivity()).createViewModel();
        mNavigation = ((MainActivity) getActivity()).navController;
        return root;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //show location image view is hidden until Google map is loaded correctly
        mShowMyLocation = view.findViewById(R.id.showLocation);
        mShowMyLocation.setVisibility(View.INVISIBLE);

        //show homes image view is hidden until Google map is loaded correctly
        mShowHomes = view.findViewById(R.id.showHomes);
        mShowHomes.setVisibility(View.INVISIBLE);

        //show homes image view is hidden until Google map is loaded correctly
        mToggleSatt = view.findViewById(R.id.toggleSatt);
        mToggleSatt.setVisibility(View.INVISIBLE);


        mStatus = view.findViewById(R.id.status);
        mStatusLayout = view.findViewById(R.id.statusLayout);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_tracking_fragment, menu);
        mMenu = menu;

        String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                "mPreference", "service", String.class);

        if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
            setTrackingOnOffMenuColor(1);
        }
        else
        {
            setTrackingOnOffMenuColor(0);
        }
    }
    void setTrackingOnOffMenuColor(int tracking_on_off)
    {
        int color = 0;
        if(tracking_on_off == 0) //tracking on
        {
            color = R.color.green;
        }
        else //tracking off
        {
            color = R.color.red;
        }

        for(int i = 0; i < mMenu.size(); i++){
            if(mMenu.getItem(i).getItemId() == R.id.start_stop_tracking)
            {
                Drawable drawable = mMenu.getItem(i).getIcon();
                if(drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.start_stop_tracking:
                String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                        "mPreference", "service", String.class);

                if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Start Tracking");
                    String warnMessage = "This app collects location data to enable parents to know the bus location. " +
                            "The app will collect the location even when the app is closed or not in use. " +
                            "However, you can stop the location access by turning the location icon on the top menu to be red, Continue?";
                    builder.setMessage(warnMessage);
                    builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: yes");
                            if(mDriver !=null)
                                //if so, update the driver information by getting the latest driver data from the server
                                //note that, the observer of this function is defined in the map fragment class
                                mViewModel.getDriverServer(mDriver.getCountry_code(), mDriver.getTel_number(), mDriver.getSecretKey());
                            //save to preference
                            Util.saveObjectToSharedPreference(getActivity().getApplicationContext(),
                                    "mPreference", "service", "track_on");
                            ((MainActivity) getActivity()).startLocationUpdates();

                            setTrackingOnOffMenuColor(0);
                            setOnlineTitle();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            setOnlineTitle();
                        }
                    });
                    builder.show();
                }
                else
                {
                    //save to preference
                    Util.saveObjectToSharedPreference(getActivity().getApplicationContext(),
                            "mPreference", "service", "track_off");
                    ((MainActivity) getActivity()).stopLocationUpdates();
                    setTrackingOnOffMenuColor(1);
                }
                setOnlineTitle();
                return true;

            case R.id.settings:
                mNavigation.navigate(R.id.nav_settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*make the status "online" */
    protected void setOnlineTitle() {
        //mStatusLayout.setVisibility(View.INVISIBLE);
        String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                "mPreference", "service", String.class);

        if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
            mStatus.setText(getString(R.string.no_tracking));
            mStatusLayout.setBackgroundColor(Color.RED);
        }
        else if (tracking_service_preference.matches("track_on"))
        {
            mStatus.setText(getString(R.string.tracking_on));
            mStatusLayout.setBackgroundColor(Color.GREEN);
        }

    }

    /*make the status "offline" with red background*/
    protected void setOfflineTitle() {
        //mStatusLayout.setVisibility(View.VISIBLE);
        mStatus.setText(getString(R.string.no_internet));
        mStatusLayout.setBackgroundColor(Color.RED);
    }


    protected abstract void showHomesRoute();

    /*update the gui on the map after the map is ready. The function will make the "show location"
        and "show homes" image views visible to the user*/
    protected void updateMapUI() {
        mShowMyLocation.setVisibility(View.VISIBLE);
        mShowHomes.setVisibility(View.VISIBLE);
        mToggleSatt.setVisibility(View.VISIBLE);
    }

    /*observer for connectivity status. If no connection with the backend web socket, the app will
    display a red bar at the bottom*/
    public class ConnectivityStatusObserver implements Observer<Boolean> {

        @Override
        public void onChanged(Boolean connectivityStatus) {
            if (connectivityStatus == null)
                return;

            if (connectivityStatus) {
                setOnlineTitle();
            }
            else
                setOfflineTitle();
        }
    }

    /*helper function to animate the change in position of the bus location so that it appears with smooth motion on
    the map*/
    protected void animateMarker(final Object marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        double my_lat = 0;
        double my_lng = 0;
        double my_rotation = 0;
        if(marker instanceof com.google.android.gms.maps.model.Marker)
        {
            my_lat = ((com.google.android.gms.maps.model.Marker)marker).getPosition().latitude;
            my_lng = ((com.google.android.gms.maps.model.Marker)marker).getPosition().longitude;
            my_rotation = ((com.google.android.gms.maps.model.Marker)marker).getRotation();
        }
        else
        {
            my_lat = ((com.mapbox.mapboxsdk.annotations.Marker)marker).getPosition().getLatitude();
            my_lng = ((com.mapbox.mapboxsdk.annotations.Marker)marker).getPosition().getLongitude();
        }
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();

        final double finalMy_lng = my_lng;
        final double finalMy_lat = my_lat;
        final double finalMy_rotation = my_rotation;
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * location.getLongitude() + (1 - t)
                        * finalMy_lng;
                double lat = t * location.getLatitude() + (1 - t)
                        * finalMy_lat;

                if(marker instanceof com.google.android.gms.maps.model.Marker)
                {
                    float rotation = (float) (t * location.getBearing() + (1 - t)
                            * finalMy_rotation);
                    ((com.google.android.gms.maps.model.Marker)marker).setRotation(rotation);
                    ((com.google.android.gms.maps.model.Marker)marker).setPosition(new com.google.android.gms.maps.model.LatLng(lat, lng));
                }
                else
                {
                    ((com.mapbox.mapboxsdk.annotations.Marker)marker).setPosition(new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng));
                }


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 200);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Util.isAfternoon(getContext()))
        {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.afternoon_way));
        }
        else
        {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.morning_way));
        }
    }

    protected ArrayList<Parent> getParentsOnRoute(Driver driver, boolean is_afternoon){
        ArrayList<Parent> waypoints = new ArrayList<>();
        for (Parent parent : driver.getParents()) {
            if(parent.getAddress_latitude() != null && parent.getAddress_longitude() != null) {
                if(parent.getChildren() != null)
                {
                    boolean allChildrenAbsent = true;
                    for (Child child : parent.getChildren())
                    {
                        if(Util.getAbsentStatus(child) == 0)
                        {
                            //not absent
                            allChildrenAbsent = false;
                            break;
                        }
                    }
                    if(!allChildrenAbsent)
                    {
                        boolean allChildrenChecked = true;
                        for (Child child : parent.getChildren())
                        {
                            Integer lastCheckStatus = Util.getLastCheckStatus(child);
                            boolean checkedIn = ((lastCheckStatus != null) && (lastCheckStatus == Util.CHECK_IN_FLAG));
                            boolean checkedOut = ((lastCheckStatus != null) && (lastCheckStatus == Util.CHECK_OUT_FLAG));
                            if(is_afternoon)
                            {
                                if(!checkedOut)
                                {
                                    //not checked
                                    allChildrenChecked = false;
                                    break;
                                }
                            }
                            else
                            {
                                if(!checkedIn)
                                {
                                    //not checked
                                    allChildrenChecked = false;
                                    break;
                                }
                            }
                        }
                        if(!allChildrenChecked) {
                            //check if a child is already checked in or out
                            waypoints.add(parent);
                        }
                    }
                }
            }
        }
        //If there is no waypoints, return
        if(waypoints.size() == 0){
            return null;
        }
        return waypoints;
    }


    protected Parent getLastRoutePoint(Driver driver, boolean is_afternoon) {
        if(is_afternoon)
        {
            String last_afternoon_parent = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                    "mPreference", "last_afternoon_parent", String.class);
            if(last_afternoon_parent != null && !last_afternoon_parent.equals("School"))
            {
                String[] parts = last_afternoon_parent.split(":");
                String parent_name = parts[0];
                String parent_tel_number = parts[1];
                boolean parent_found = false;
                for (Parent parent : driver.getParents()) {
                    if (parent.getAddress_latitude() != null && parent.getAddress_longitude() != null) {
                        if (parent.getName().equals(parent_name) && parent.getTel_number().equals(parent_tel_number))
                        {
                            return parent;
                        }
                    }
                }
                return null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    protected void showCheckDialog(Parent p) {
        BottomSheetCheckDialog dialog = new BottomSheetCheckDialog(p);
        assert getFragmentManager() != null;
        dialog.show(getFragmentManager(), "check_bottom_sheet");
    }

    public class LocationTrackingStatusObserver implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean trackingStatus) {
            if (trackingStatus == null) return;
            if (trackingStatus)
            {
                showHomesRoute();
            }
        }
    }

    //observer for driver data, which when changed, the status of the app becomes "online" and the
    // updated driver data is saved to SharedPreferences. If the driver is not verified, the user
    // is redirected to the login activity
    public class DriverObserver implements Observer<Driver> {

        @Override
        public void onChanged(@Nullable Driver driver) {
            //((MainActivity) getActivity()).showHideProgressBar(false);
            if (driver == null) {
                //get the last saved parent information from the SharedPreference
                mDriver = Util.getSavedObjectFromPreference(getContext(),
                        "mPreference", "Driver", Driver.class);
                //update the UI to show "offline" status
                mViewModel.setConnectivityStatus(false);
            } else {
                mDriver = driver;
                Log.d(TAG, "DriverObserver => onChanged: " + driver.getName());
                Util.saveObjectToSharedPreference(getContext(),
                        "mPreference", "Driver", driver);
                //update the UI to show "online" status
                mViewModel.setConnectivityStatus(true);
                showHomesRoute();
            }
            if(mDriver!=null && mDriver.getParents()!=null && mDriver.getParents().size()>0)
                //put the homes of the parents on the map
                setParentsLocationUI();
            //if the driver is not verified, go to the login activity
            if (mDriver.getVerified() != 1)
                //logout
                ((MainActivity) getActivity()).logout();
        }
    }

    protected abstract void setParentsLocationUI();
}
