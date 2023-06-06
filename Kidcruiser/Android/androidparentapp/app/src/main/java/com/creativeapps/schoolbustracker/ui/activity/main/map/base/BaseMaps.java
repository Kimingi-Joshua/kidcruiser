package com.creativeapps.schoolbustracker.ui.activity.main.map.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivityModel;
import com.google.android.gms.maps.model.Marker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseMaps extends Fragment {
    //constants to set the mode of the map
    protected static final int AUTO = 0;//AUTO: map is adjusted automatically to view both the parent's home and driver locations
    protected static final int MANUAL = 1;//MANUAL: settings of the map (zoom level, position, ..) adjusted manually by the user
    //default zoom level for the map
    protected static final int DEFAULT_ZOOM = 13;

    private static final String TAG = "BaseMaps";
    //view model of the main activity
    protected MainActivityModel mViewModel;
    protected ImageView mRefreshBusLocation, mToggleSatt;
    //Text view that display the connectivity status of the app (online or offline)
    protected TextView mStatus;
    //Layout that contains the connectivity status
    protected Menu mMenu;
    protected RelativeLayout mStatusLayout;

    //parent object that holds parent information
    protected Parent mParent;

    protected int mViewMode = AUTO;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,  int layout) {
        //inflate the layout
        View root = inflater.inflate(layout, container, false);
        //instantiate the view model object
        mViewModel = ((MainActivity) getActivity()).createViewModel();

        return root;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mRefreshBusLocation = view.findViewById(R.id.refreshBusLocation);
        mRefreshBusLocation.setVisibility(View.INVISIBLE);

        mStatus = view.findViewById(R.id.status);
        mStatusLayout = view.findViewById(R.id.statusLayout);

        mToggleSatt = view.findViewById(R.id.toggleSatt);
        mToggleSatt.setVisibility(View.INVISIBLE);

        mStatus = view.findViewById(R.id.status);
        mStatusLayout = view.findViewById(R.id.statusLayout);
    }

    /*make the status "online" */
    protected void setOnlineTitle()
    {
        mStatusLayout.setVisibility(View.INVISIBLE);
    }

    /*make the status "offline" with red background*/
    protected void setOfflineTitle()
    {
        mStatusLayout.setVisibility(View.VISIBLE);
        mStatus.setText("offline");
        mStatusLayout.setBackgroundColor(Color.RED);
    }


    /*update the gui on the map after the map is ready. The function will make the "show location"
    and "show homes" image views visible to the user*/
    protected void updateMapUI() {
        mRefreshBusLocation.setVisibility(View.VISIBLE);
        mToggleSatt.setVisibility(View.VISIBLE);
    }


    protected boolean checkTrackingOn() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        boolean tracking_on = false;
        if(mParent.getSchool().getIsPayAsYouGo() == 0)
            tracking_on = true;
        else
        {
            if(mParent.getNextRenewsAt() != null)
            {
                try {
                    Date renewDate = null;
                    renewDate = format.parse(mParent.getNextRenewsAt());
                    if (renewDate == null ||
                            (mParent.getSchool().getIsPayAsYouGo() == 1 &&
                                    renewDate.before(new Date()))) {
                        tracking_on = false;
                    }
                    else
                    {
                        tracking_on = true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    tracking_on = false;
                }
            }
        }
        return tracking_on;
    }

    /*helper function to animate the change in position of the bus location so that it appears with smooth motion on
    the map*/
    protected void animateBusMarker(final Object marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        double my_lat = 0;
        double my_lng = 0;
        double my_rotation = 0;
        if(marker instanceof Marker)
        {
            my_lat = ((Marker)marker).getPosition().latitude;
            my_lng = ((Marker)marker).getPosition().longitude;
            my_rotation = ((Marker)marker).getRotation();
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

                if(marker instanceof Marker)
                {
                    float rotation = (float) (t * location.getBearing() + (1 - t)
                            * finalMy_rotation);
                    ((Marker)marker).setRotation(rotation);
                    ((Marker)marker).setPosition(new com.google.android.gms.maps.model.LatLng(lat, lng));
                }
                else
                {
                    ((com.mapbox.mapboxsdk.annotations.Marker)marker).setPosition(new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng));
                }


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 50);
                }
            }
        });
    }

    /*observer for connectivity status. If no connection with the backend web socket, the app will
    display a red bar at the bottom*/
    public class ConnectivityStatusObserver implements Observer<Boolean> {

        @Override
        public void onChanged(Boolean connectivityStatus) {
            if(connectivityStatus == null)
                return;

            if(connectivityStatus)
                setOnlineTitle();
            else
                setOfflineTitle();
        }
    }
}
