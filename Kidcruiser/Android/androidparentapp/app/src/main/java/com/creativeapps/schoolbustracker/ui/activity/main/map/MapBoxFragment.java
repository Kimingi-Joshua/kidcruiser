package com.creativeapps.schoolbustracker.ui.activity.main.map;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.data.network.models.Payload;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivityModel;

import com.creativeapps.schoolbustracker.ui.activity.main.map.base.BaseMaps;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapBoxFragment extends BaseMaps implements View.OnClickListener, OnMapReadyCallback {

    final String TAG = "MapBoxFragment";

    //bus marker
    private Marker mBusMarker;
    //home marker
    private Marker mParentHomeMarker;
    //school marker
    private Marker mSchoolMarker;

    private long last_time=0;
    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private Polyline polyline;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Mapbox.getInstance(((MainActivity)getActivity()), getString(R.string.mapbox_access_token));
        return onCreateView(inflater, container, R.layout.fragment_mabbox_map);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        mRefreshBusLocation.setOnClickListener(this);
        mToggleSatt.setOnClickListener(this);

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //center the map on the current location of the bus
            case R.id.refreshBusLocation:
                mViewMode = AUTO;
                adjustMapToPickDropBusLocations();
                break;

            case R.id.toggleSatt:
                if (mMapboxMap != null) {
                    //toggle SATELLITE map view
                    if(this.mMapboxMap.getStyle().getUri().equals(Style.SATELLITE_STREETS))
                    {
                        this.mMapboxMap.setStyle(Style.MAPBOX_STREETS);
                        mViewModel.setMabBoxType(Style.MAPBOX_STREETS);
                        mToggleSatt.setImageResource(R.drawable.satellite);
                    }
                    else
                    {
                        this.mMapboxMap.setStyle(Style.SATELLITE_STREETS);
                        mViewModel.setMabBoxType(Style.SATELLITE_STREETS);
                        mToggleSatt.setImageResource(R.drawable.map);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop observing live data when the fragment is paused
        mViewModel.getParent().removeObservers(this);
        mViewModel.getDriverRealTimeData().removeObservers(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //observe changes for the position of the driver
        mViewModel.getDriverRealTimeData().observe(this, new PosDriverObserver());
        //observe changes for connectivity status
        mViewModel.getConnectivityStatus().observe(this, new ConnectivityStatusObserver());
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        //when the map is ready
        this.mMapboxMap = mapboxMap;

        if(mViewModel.getMapBoxType() != null)
        {
            this.mMapboxMap.setStyle(new Style.Builder().fromUri(mViewModel.getMapBoxType()));
            if(mViewModel.getMapBoxType().equals(Style.SATELLITE_STREETS))
            {
                mToggleSatt.setImageResource(R.drawable.map);
            }
            else
            {
                mToggleSatt.setImageResource(R.drawable.satellite);
            }
        }
        else
        {
            this.mMapboxMap.setStyle(Style.MAPBOX_STREETS);
        }

        mMapboxMap.addOnCameraMoveListener(new MapboxMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mViewMode = MANUAL;
            }
        });

        //get the last saved parent information from the SharedPreference
        mParent = Util.getSavedObjectFromPreference(getContext(),
                "mPreference", "Parent", Parent.class);

        //check if there are already saved data for the parent
        if(mParent !=null) {
            //if so, update the parent information by getting the latest parent data from the server
            mViewModel.getParentServer(mParent.getCountry_code(), mParent.getTel_number(), mParent.getSecretKey());

            //observe changes for parent information
            mViewModel.getParent().observe(this, new ParentObserver());
        }
        if(mParent != null) {
            boolean tracking_on = checkTrackingOn();
            if(!tracking_on){
                //Toast to the user to recharge their wallet
                Toast.makeText(getContext(), "Please recharge your wallet", Toast.LENGTH_LONG).show();
            }
        }

        //update the gui on the map after the map is ready. The function will make the "refresh bus"
        // image view visible to the user
        updateMapUI();
    }

    /*put the pickup/drop-off location of the parent on the map*/
    private void updatePickupDropoffMarker(LatLng parentHomeLocation) {
        if (mMapboxMap == null)
            return;

        IconFactory iconFactory = IconFactory.getInstance(this.getContext());
        Icon icon = iconFactory.fromResource(R.drawable.home);
        //define marker options with a home icon
        MarkerOptions marker_option = new MarkerOptions()
                .position(parentHomeLocation)
                .title("Your pickup/drop-off location")
                .icon(icon);

        if (mParentHomeMarker == null) {
            // adding marker
            mParentHomeMarker = mMapboxMap.addMarker(marker_option);

        } else //update the marker position
            mParentHomeMarker.setPosition(parentHomeLocation);

    }

    /*adjust map to show both the parent's home and the bus locations*/
    private void adjustMapToPickDropBusLocations() {
        if (mMapboxMap == null) {
            return;
        }

        if(mViewMode==AUTO)
        {
            //construct LatLngBounds that include the parent's home and the bus locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();


            //check if the parent's home marker already on map
            if (mParentHomeMarker != null) {
                //if so, include it in the LatLngBounds object
                builder.include(mParentHomeMarker.getPosition());
                //check if the bus marker already on map
                if (mBusMarker != null)
                    //if so, include it in the LatLngBounds object
                    builder.include(mBusMarker.getPosition());

                try {
                    //build the bound with included homes locations
                    LatLngBounds homeBusLocationsBounds = builder.build();

                    //adjust map with animation to show both the parent's home and the bus locations
                    mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(homeBusLocationsBounds,200));

                } catch (Exception e) {

                }
            }
            else
            {
                //check if the bus marker already on map
                if (mBusMarker != null)
                {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            mBusMarker.getPosition()).zoom(DEFAULT_ZOOM).build();

                    mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }
        else if(mViewMode==MANUAL)
        {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    mBusMarker.getPosition()).zoom(mMapboxMap.getCameraPosition().zoom).build();

            mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


    }

    /*set bus icon marker location on Google map. The function initialize the bus marker if it is
    not initialized yet, or move the marker with animation to a new position*/
    private void setBusLocation(Payload bus_real_time_data) {

        //verify that Google map is loaded correctly before proceed
        if (mMapboxMap == null) {
            return;
        }
        //if the bus marker is not initialized, initialize it and add it to the map on the
        // specified location
        if (mBusMarker == null) {
            IconFactory iconFactory = IconFactory.getInstance(this.getContext());
            Icon icon = iconFactory.fromResource(R.drawable.school_bus);
            //define marker options with a bus icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(new LatLng(bus_real_time_data.lat, bus_real_time_data.lng))
                    .icon(icon);

            // adding marker
            mBusMarker = mMapboxMap.addMarker(marker_option);
            //mBusMarker.setTitle(bus_real_time_data.distance+"");
            mMapboxMap.selectMarker(mBusMarker);
        } else //if the bus marker is already initialized before, move it to the new location
        {
            //convert from LatLng to Location object
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(bus_real_time_data.lat);
            location.setLongitude(bus_real_time_data.lng);
            Log.d(TAG, "setBusLocation: " + bus_real_time_data.distance + "," + System.currentTimeMillis() + "," +last_time);

            double speed = bus_real_time_data.speed;

            mBusMarker.setTitle(String.format("%.2f", speed)+" km/h");

            last_time = System.currentTimeMillis();

            mMapboxMap.selectMarker(mBusMarker);
            //Helper method for smooth animation
            animateBusMarker(mBusMarker, location);
        }

    }

    /*set school icon marker location on Google map. The function initialize the school marker if it is
    not initialized yet, or move the marker to a new position*/
    private void setSchoolLocation(LatLng latLng) {
        if (mMapboxMap == null)
            return;

        IconFactory iconFactory = IconFactory.getInstance(this.getContext());
        Icon icon = iconFactory.fromResource(R.drawable.school);
        //define marker options with a home icon
        MarkerOptions marker_option = new MarkerOptions()
                .position(latLng)
                .title("School location")
                .icon(icon);

        if (mSchoolMarker == null) {
            // adding marker
            mSchoolMarker = mMapboxMap.addMarker(marker_option);
        } else
            mSchoolMarker.setPosition(latLng);
    }

    public void updatePolyLineOnMap(Payload posDriver) {
        if(posDriver.polylineValues == null)
            return;

        List<LatLng> polyline_points = new ArrayList<>();
        for (int i = 0; i < posDriver.polylineValues.size()-1; i+=2) {
            polyline_points.add(new LatLng(posDriver.polylineValues.get(i), posDriver.polylineValues.get(i+1)));
        }

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(15);
        polyOptions.addAll(polyline_points);

        if(polyline != null)
        {
            polyline.remove();
        }

        polyline = mMapboxMap.addPolyline(polyOptions);
    }
    /*observe changes for the position of the driver*/
    private class PosDriverObserver implements Observer<Payload> {

        @Override
        public void onChanged(@Nullable Payload posDriver) {
            if (posDriver == null) return;

            Log.d(TAG,
                    "PosDriverObserver => onChanged: posDriver " + posDriver.lat + ", " +
                            posDriver.lng);
            //the bus change its location so update it on Google map
            if(mParent != null) {
                boolean tracking_on = checkTrackingOn();
                if (tracking_on) {
                    setBusLocation(posDriver);
                    adjustMapToPickDropBusLocations();
                    updatePolyLineOnMap(posDriver);
                }
            }
        }
    }


    //observer for parent data, which when changed, the status of the app becomes "online" and the
    // updated parent data is saved to SharedPreferences. If the parent is not verified, the user
    // is redirected to the login activity
    private class ParentObserver implements Observer<Parent> {

        @Override
        public void onChanged(@Nullable Parent parent) {
            ((MainActivity)getActivity()).showHideProgressBar(false);
            if (parent == null) {

                //get the last saved parent information from the SharedPreference
                mParent = Util.getSavedObjectFromPreference(getContext(),
                        "mPreference", "Parent", Parent.class);
            } else {
                mParent = parent;
                Log.d(TAG, "ParentObserver => onChanged: " + parent.getName());
                Util.saveObjectToSharedPreference(getContext(),
                        "mPreference", "Parent", parent);
            }

            //if the parent is not verified, go to the login activity
            if (mParent.getVerified() != 1) {
                //logout
                ((MainActivity)getActivity()).logout();
            } else {
                //if the driver location information available
                if (mParent.getDriver() != null && mParent.getDriver().getLast_latitude() != null &&
                        mParent.getDriver().getLast_longitude() != null) {
                    //set bus icon marker location on Google map
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date renewDate = mParent.getNextRenewsAt()!= null ? format.parse(mParent.getNextRenewsAt()) : null;
                        if (!(renewDate == null || mParent.getSchool().getIsPayAsYouGo() == 1 && renewDate.before(new Date()))) {
                            setBusLocation(new Payload(mParent.getDriver().getLast_latitude(), mParent.getDriver().getLast_longitude()));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                //if the school location information available
                if (mParent.getSchool() != null && mParent.getSchool().getLast_latitude() != null &&
                        mParent.getSchool().getLast_longitude() != null) {
                    //set school icon marker location on Google map
                    setSchoolLocation(new LatLng(mParent.getSchool().getLast_latitude(),
                            mParent.getSchool().getLast_longitude()));
                }
                //if the parent location information available
                if (mParent.getAddress_latitude() != null && mParent.getAddress_longitude() != null) {
                    //set parent's home icon marker location on Google map

                    updatePickupDropoffMarker(new LatLng(mParent.getAddress_latitude(),
                            mParent.getAddress_longitude()));
                }
                //adjust map to show both the parent's home and the bus locations
                adjustMapToPickDropBusLocations();
            }
        }

    }
}