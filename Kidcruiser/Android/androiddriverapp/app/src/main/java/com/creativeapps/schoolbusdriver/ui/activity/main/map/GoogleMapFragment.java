package com.creativeapps.schoolbusdriver.ui.activity.main.map;

import static com.mapbox.turf.TurfMisc.nearestPointOnLine;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.akexorcist.googledirection.model.Direction;
import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.base.BaseMaps;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapFragment extends BaseMaps implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener{

    final String TAG = "GoogleMapFragment";
    //Google map object
    private GoogleMap mGoogleMap;
    //bus marker
    private Marker mBusMarker;
    //accuracy circle with a center of bus marker position
    private Circle mAccuracyCircle;
    //bound of all locations of the homes of parents
    private LatLngBounds mParentBounds;
    private Menu mMenu;
    private boolean firstLoad = true;
    private Polyline polyline;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return onCreateView(inflater, container, R.layout.fragment_googlemaps_map);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShowMyLocation.setOnClickListener(this);
        mShowHomes.setOnClickListener(this);
        mToggleSatt.setOnClickListener(this);
        //start Google map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //when the map is ready
        this.mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(this);
        if(mViewModel.getGoogleMapType() != null)
        {
            this.mGoogleMap.setMapType(mViewModel.getGoogleMapType());
            if(mViewModel.getGoogleMapType() == GoogleMap.MAP_TYPE_SATELLITE)
            {
                mToggleSatt.setImageResource(R.drawable.map);
            }
            else
            {
                mToggleSatt.setImageResource(R.drawable.satellite);
            }
        }
        //observe changes for the response of direction api
        mViewModel.getDirection().observe(this, new RouteObserver());
        //observe changes for the tracking status of the driver
        mViewModel.getLocationTrackingStatus().observe(this, new LocationTrackingStatusObserver());
        //observe changes for the position of the driver
        mViewModel.getPosDriver().observe(this, new PosDriverObserver());
        //observe changes for connectivity status
        mViewModel.getConnectivityStatus().observe(this, new ConnectivityStatusObserver());
        //observe changes for driver information
        mViewModel.getDriver().observe(this, new DriverObserver());
        //update the gui on the map after the map is ready. The function will make "show location"
        // and "show homes" image views visible to the user
        updateMapUI();
    }


    public void getRouteFromViewmodel(){
        if(mDriver == null)
            return;

        if(mBusMarker == null)
            return;

        //check if now if afternoon or morning
        boolean is_afternoon = Util.isAfternoon(getContext());
        //Get waypoints from parents lat lng
        ArrayList<Parent> parents = getParentsOnRoute(mDriver, is_afternoon);
        ArrayList<LatLng> waypoints = new ArrayList<>();
        if(parents != null) {
            for (Parent parent : parents) {
                waypoints.add(new LatLng(parent.getAddress_latitude(), parent.getAddress_longitude()));
            }
        }

        LatLng current_driver_loc = new LatLng(mBusMarker.getPosition().latitude, mBusMarker.getPosition().longitude);
        LatLng school_loc = new LatLng(mDriver.school.getLast_latitude(), mDriver.school.getLast_longitude());

        LatLng end_loc = school_loc;
        Parent last_parent = getLastRoutePoint(mDriver, is_afternoon);
        if(last_parent != null)
        {
            end_loc = new LatLng(last_parent.getAddress_latitude(), last_parent.getAddress_longitude());
        }

        @Nullable ApplicationInfo applicationInfo = null;
        @Nullable String apiKey = null;
        try {

            applicationInfo = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                // Get the value from the key
                apiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY");
                mViewModel.getRoute(current_driver_loc, waypoints, end_loc, apiKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //center the map on the current location of the bus
            case R.id.showLocation:
                if (mBusMarker != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            mBusMarker.getPosition()).zoom(DEFAULT_ZOOM).build();

                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                break;
            //change the zoom level of the map to fit the locations of all parent homes
            case R.id.showHomes:
                String tracking_service_preference = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                        "mPreference", "service", String.class);

                if (tracking_service_preference == null || tracking_service_preference.matches("track_off")) {
                    Util.displayExitMessage("You need to start your trip first to display your route", getActivity(), false);
                }
                else
                {
                    showHomesRoute();
                }

                break;
            case R.id.toggleSatt:
                if (mGoogleMap != null) {
                    //toggle SATELLITE map view
                    if(this.mGoogleMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE)
                    {
                        this.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mViewModel.setGoogleMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mToggleSatt.setImageResource(R.drawable.satellite);
                    }
                    else
                    {
                        this.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mViewModel.setGoogleMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mToggleSatt.setImageResource(R.drawable.map);
                    }
                }
                break;
        }
    }

    @Override
    protected void showHomesRoute() {
        //get the current width and height of the screen to detect the orientation of
        // the device. Then, set the margins around the parent homes bound appropriately
        DisplayMetrics dMetrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        int w = dMetrics.widthPixels;
        int h = dMetrics.heightPixels;
        //((MainActivity)getActivity()).showHideProgressBar(true);
        getRouteFromViewmodel();
        if(mParentBounds != null)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mParentBounds, w, h, w < h ? 100 : 300));
    }

    /*update the gui on the map after the map is ready. The function will make the "show location"
    and "show homes" image views visible to the user*/
    protected void updateMapUI() {
        if (mGoogleMap == null) {
            return;
        }
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        super.updateMapUI();
    }

    /*set bus icon marker location on Google map. The function initialize the bus marker if it is
    not initialized yet, or move the marker with animation to a new position*/
    private void setBusLocation(Location bus_location) {

        //verify that Google map is loaded correctly before proceed
        if (mGoogleMap == null) {
            return;
        }
        //if the bus marker is not initialized, initialize it and add it to the map on the
        // specified location
        LatLng pos = new LatLng(bus_location.getLatitude(), bus_location.getLongitude());
        if (mBusMarker == null) {
            //define marker options with a bus icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(pos)
                    .title("You are here")
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.school_bus));

            // adding marker
            mBusMarker = mGoogleMap.addMarker(marker_option);

            //move the camera to make the bus icon centered on the map
            CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(DEFAULT_ZOOM).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else //if the bus marker is already initialized before, move it to the new location
        {
            float zoom = firstLoad? DEFAULT_ZOOM : mGoogleMap.getCameraPosition().zoom;
            firstLoad = false;
            //Helper method for smooth animation
            animateMarker(mBusMarker, bus_location);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pos).zoom(zoom).bearing(bus_location.getBearing()).build();
            //mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


    }

    /*put the homes of the parents on the map*/
    protected void setParentsLocationUI() {

        if (mGoogleMap == null) {
            return;
        }

        //construct LatLngBounds that include locations of homes of all parents
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(mDriver.school != null && mDriver.school.getLast_latitude() != null && mDriver.school.getLast_longitude() != null) {
            LatLng schoolPos = new LatLng(mDriver.school.getLast_latitude(), mDriver.school.getLast_longitude());
            builder.include(schoolPos);

            //define marker options with a school icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(schoolPos)
                    .title(mDriver.school.getName())
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.school));

            // adding marker
            mGoogleMap.addMarker(marker_option);

        }

        Log.d(TAG, "setParentsLocationUI: " + mDriver.school.getName());
        for (int i = 0; i < mDriver.getParents().size(); i++) {
            Parent p = mDriver.getParents().get(i);
            //if the parent set his home location
            if (p.getAddress_latitude() != null && p.getAddress_longitude() != null) {
                LatLng pos = new LatLng(p.getAddress_latitude(), p.getAddress_longitude());

                //draw the home icon on a bitmap along with the parent name with the following 4 steps
                //1 - define an empty bitmap
                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(400, 200, conf);
                //2 - get a canvas of this bitmap
                Canvas bmpCanvas = new Canvas(bmp);

                // paint defines the text color, stroke width and size
                Paint color = new Paint();
                color.setTextSize(55);
                color.setColor(Color.BLACK);

                Bitmap homeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.home);

                //3- modify canvas by drawing the homeIcon and the parent name
                bmpCanvas.drawBitmap(homeIcon, (bmp.getWidth() - homeIcon.getWidth()) / 2, (bmp.getHeight() - homeIcon.getHeight()) / 2, color);
                bmpCanvas.drawText(p.getName(), 20, 50, color);
                //4 - add marker to Map
                Marker parentMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                        // Specifies the anchor to be at a particular point in the marker image.
                        .anchor(0.5f, 0.5f));
                parentMarker.setTag(p);
                builder.include(pos);
            }
        }
        try {
            //build the bound with included homes locations
            mParentBounds = builder.build();
        } catch (Exception e) {

        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        try {
            Parent p = (Parent) marker.getTag();
            Log.d(TAG, "onMarkerClick: " + p.getName());
            showCheckDialog(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*observe changes for the position of the driver*/
    private class PosDriverObserver implements Observer<Location> {

        @Override
        public void onChanged(@Nullable Location posDriver) {
            if (posDriver == null) return;
            if (mGoogleMap == null) return;

            Log.d(TAG,
                    "PosDriverObserver => onChanged: posDriver " + posDriver.getLatitude() + ", " +
                            posDriver.getLongitude() + ", " + mViewModel.getAccuracy().getValue());
            //the bus change its location so update it on Google map
            setBusLocation(posDriver);
            updatePolyLineOnMap(posDriver);
        }
    }

    public void drawPolyLineOnMap(List<LatLng> list) {
        if(polyline != null)
        {
            polyline.remove();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(15);
        polyOptions.addAll(list);

        polyline = mGoogleMap.addPolyline(polyOptions);
    }

    public void updatePolyLineOnMap(Location posDriver) {
        if(polyline == null)
            return;

        List<LatLng> polyline_points = polyline.getPoints();
        LatLng start_coordinates = new LatLng(posDriver.getLatitude(), posDriver.getLongitude());
        int start_idx = 0;
        boolean found = false;
        for (start_idx = 0; start_idx < polyline_points.size()-1; start_idx++) {
            LatLng start = polyline_points.get(start_idx);
            LatLng end = polyline_points.get(start_idx+1);
            List<LatLng> local_polyline = new ArrayList<>();
            local_polyline.add(start);
            local_polyline.add(end);
            found = PolyUtil.isLocationOnPath(start_coordinates, local_polyline, true, 10);
            if(found)
                break;
        }
        if(!found)
            start_idx = 0;
        if (polyline_points.size() > start_idx)
        {
            //update the polyline to start from the specified index with the specified start point
            List<LatLng> updated_polyline_points = new ArrayList<>();
            updated_polyline_points.add(start_coordinates);
            for (int i = start_idx + 1; i < polyline_points.size(); i++) {
                updated_polyline_points.add(polyline_points.get(i));
            }

            if(polyline != null)
            {
                polyline.remove();
            }

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.BLUE);
            polyOptions.width(15);
            polyOptions.addAll(updated_polyline_points);

            polyline = mGoogleMap.addPolyline(polyOptions);
            if(mViewModel.polyline == null)
            {
                mViewModel.polyline = new ArrayList<>();
            }
            mViewModel.polyline.clear();
            for(LatLng pp: updated_polyline_points)
            {
                mViewModel.polyline.add(pp.latitude);
                mViewModel.polyline.add(pp.longitude);
            }
        }

    }


    private class RouteObserver implements Observer<Direction> {

        @Override
        public void onChanged(@Nullable Direction direction) {
            List<LatLng> points=direction.getRouteList().get(0).getOverviewPolyline().getPointList();
            drawPolyLineOnMap(points);
            //Save points in shared preferences
            Util.saveObjectToSharedPreference(getContext(), "mPreference", "points", points);
            ((MainActivity)getActivity()).showHideProgressBar(false);
        }
    }
}