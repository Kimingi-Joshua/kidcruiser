package com.creativeapps.schoolbusdriver.ui.activity.main.map;

import static com.mapbox.core.constants.Constants.PRECISION_5;
import static com.mapbox.core.constants.Constants.PRECISION_6;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.map.base.BaseMaps;
import com.google.gson.JsonElement;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.api.optimization.v1.models.OptimizationWaypoint;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
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
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.MapboxNavigationProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapBoxFragment extends BaseMaps implements View.OnClickListener, OnMapReadyCallback, MapboxMap.OnMarkerClickListener {

    final String TAG = "MapBoxFragment";

    //bus marker
    private Marker mBusMarker;
    //bound of all locations of the homes of parents
    private LatLngBounds mParentBounds;

    private MapView mMapView;
    private MapboxMap mMapboxMap;

    private Polyline polyline;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(((MainActivity)getActivity()), getString(R.string.mapbox_access_token));
        //inflate the layout
        return onCreateView(inflater, container, R.layout.fragment_mabbox_map);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShowMyLocation.setOnClickListener(this);
        mShowHomes.setOnClickListener(this);
        mToggleSatt.setOnClickListener(this);
        //start MabBox
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.getMapAsync(this);
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
            mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mParentBounds, w, h, w < h ? 100 : 300));
    }

    public void getRouteFromViewmodel() {
        if (mDriver == null)
            return;

        if(mBusMarker == null)
            return;

        //check if now if afternoon or morning
        boolean is_afternoon = Util.isAfternoon(getContext());

        Point current_driver_loc = Point.fromLngLat(mBusMarker.getPosition().getLongitude(), mBusMarker.getPosition().getLatitude());

        //Get waypoints from parents lat lng
        ArrayList<Parent> parents = getParentsOnRoute(mDriver, is_afternoon);
        ArrayList<Point> waypoints = new ArrayList<>();
        waypoints.add(current_driver_loc);
        if(parents != null)
        {
            for (Parent parent:parents) {
                waypoints.add(Point.fromLngLat(parent.getAddress_longitude(), parent.getAddress_latitude()));
            }
        }

        Point school_loc = Point.fromLngLat(mDriver.school.getLast_longitude(), mDriver.school.getLast_latitude());
        Point end_loc = school_loc;
        Parent last_parent = getLastRoutePoint(mDriver, is_afternoon);
        if(last_parent != null)
        {
            end_loc = Point.fromLngLat(last_parent.getAddress_longitude(), last_parent.getAddress_latitude());
        }
        waypoints.add(end_loc);
        getRouteMapBox(waypoints);
    }

    private void getRouteMapBox(List<Point> waypoints) {
        MapboxOptimization optimizedClient = MapboxOptimization.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .source(DirectionsCriteria.SOURCE_FIRST)
                .destination(DirectionsCriteria.DESTINATION_LAST)
                .roundTrip(false)
                .coordinates(waypoints)
                .build();

        optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
            @Override
            public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                } else {
                    if (response.body() != null) {
                        List<DirectionsRoute> routes = response.body().trips();
                        if (routes != null) {
                            if (routes.isEmpty()) {
                                Toast.makeText(getContext(), R.string.successful_but_no_routes,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Get most optimized route from API response
                                drawOptimizedRoute(routes.get(0));
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.successful_but_no_routes,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.successful_but_no_routes,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<OptimizationResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(getContext(), R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawOptimizedRoute(DirectionsRoute route) {
        LineString lineString = LineString.fromPolyline(Objects.requireNonNull(route.geometry()), PRECISION_6);
        List<Point> waypoints = lineString.coordinates();
        drawPolyLineOnMap(waypoints);
    }
    public void drawPolyLineOnMap(List<Point> list) {
        ArrayList<LatLng> points = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Point w = list.get(i);
            points.add( new LatLng(w.latitude(), w.longitude()));
        }
        if(polyline != null)
        {
            polyline.remove();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(15);
        polyOptions.addAll(points);
        polyline = mMapboxMap.addPolyline(polyOptions);
    }

    public void updatePolyLineOnMap(Location posDriver) {
        if(polyline == null)
            return;

        List<LatLng> polyline_points = polyline.getPoints();
        List<Point> points = new ArrayList<>(polyline_points.size());
        for(LatLng p: polyline_points)
        {
            Point pp = Point.fromLngLat(p.getLongitude(), p.getLatitude());
            points.add(pp);
        }
        Point posDriverPoint = Point.fromLngLat(posDriver.getLongitude(), posDriver.getLatitude());
        Feature currentP = nearestPointOnLine(posDriverPoint, points);
        //currentP.properties().get("dist");
        Integer start_idx = currentP.properties().get("index").getAsInt();
        List<Double> start_coordinates = ((Point) currentP.geometry()).coordinates();

        if (polyline_points.size() > start_idx)
        {
            //update the polyline to start from the specified index with the specified start point
            List<LatLng> updated_polyline_points = new ArrayList<>();
            updated_polyline_points.add(new LatLng(start_coordinates.get(1), start_coordinates.get(0)));
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
            polyline = mMapboxMap.addPolyline(polyOptions);
            if(mViewModel.polyline == null)
            {
                mViewModel.polyline = new ArrayList<>();
            }
            mViewModel.polyline.clear();
            for(LatLng pp: updated_polyline_points)
            {
                mViewModel.polyline.add(pp.getLatitude());
                mViewModel.polyline.add(pp.getLongitude());
            }
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        //when the map is ready
        this.mMapboxMap = mapboxMap;
        this.mMapboxMap.setOnMarkerClickListener(this);
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
        //observe changes for the position of the driver
        mViewModel.getPosDriver().observe(this, new PosDriverObserver());
        //observe changes for the tracking status of the driver
        mViewModel.getLocationTrackingStatus().observe(this, new LocationTrackingStatusObserver());
        //observe changes for connectivity status
        mViewModel.getConnectivityStatus().observe(this, new ConnectivityStatusObserver());
        //observe changes for driver information
        mViewModel.getDriver().observe(this, new DriverObserver());
        //update the gui on the map after the map is ready. The function will make "show location"
        // and "show homes" image views visible to the user
        updateMapUI();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //center the map on the current location of the bus
            case R.id.showLocation:
                if (mBusMarker != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            mBusMarker.getPosition()).zoom(DEFAULT_ZOOM).build();

                    mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                break;
            //change the zoom level of the map to fit the locations of all parent homes
            case R.id.showHomes:
//                if (mParentBounds != null) {
//                    //get the current width and height of the screen to detect the orientation of
//                    // the device. Then, set the margins around the parent homes bound appropriately
//                    DisplayMetrics dMetrics = new DisplayMetrics();
//                    this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
//                    int w = dMetrics.widthPixels;
//                    int h = dMetrics.heightPixels;
//
//                    mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mParentBounds, w, h, w < h ? 100 : 300));
//                }
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
        }
    }

    protected void updateMapUI() {
        if (mMapboxMap == null) {
            return;
        }
        super.updateMapUI();
    }

    /*set bus icon marker location on Google map. The function initialize the bus marker if it is
    not initialized yet, or move the marker with animation to a new position*/
    private void setBusLocation(Location bus_location) {

        //verify that Google map is loaded correctly before proceed
        if (mMapboxMap == null) {
            return;
        }
        //if the bus marker is not initialized, initialize it and add it to the map on the
        // specified location
        LatLng pos = new LatLng(bus_location.getLatitude(), bus_location.getLongitude());
        if (mBusMarker == null) {
            //define marker options with a bus icon
            IconFactory iconFactory = IconFactory.getInstance(this.getContext());
            Icon icon = iconFactory.fromResource(R.drawable.school_bus);
            //define marker options with a bus icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(pos)
                    .title("You are here")
                    .icon(icon);
            // adding marker
            mBusMarker = mMapboxMap.addMarker(marker_option);

            //move the camera to make the bus icon centered on the map
            CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(DEFAULT_ZOOM).build();
            mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else //if the bus marker is already initialized before, move it to the new location
        {
            //Helper method for smooth animation
            animateMarker(mBusMarker, bus_location);
            Log.d(TAG, "setBusLocation: " + bus_location.getBearing());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pos).bearing(bus_location.getBearing()).build();
            //mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


    }

    /*put the homes of the parents on the map*/
    protected void setParentsLocationUI() {

        if (mMapboxMap == null) {
            return;
        }

        //construct LatLngBounds that include locations of homes of all parents
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(mDriver.school != null && mDriver.school.getLast_latitude() != null && mDriver.school.getLast_longitude() != null) {
            LatLng schoolPos = new LatLng(mDriver.school.getLast_latitude(), mDriver.school.getLast_longitude());
            builder.include(schoolPos);

            IconFactory iconFactory = IconFactory.getInstance(this.getContext());
            Icon icon = iconFactory.fromResource(R.drawable.school);
            //define marker options with a school icon
            MarkerOptions marker_option = new MarkerOptions()
                    .position(schoolPos)
                    .title(mDriver.school.getName())
                    .icon(icon);

            // adding marker
            mMapboxMap.addMarker(marker_option);

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

                IconFactory iconFactory = IconFactory.getInstance(this.getContext());
                Icon icon = iconFactory.fromBitmap(bmp);

                //4 - add marker to Map
                MarkerOptions marker_option = new MarkerOptions()
                        .position(pos)
                        .icon(icon);

                Marker parentMarker = mMapboxMap.addMarker(marker_option);
                parentMarker.setTitle(p.getName() + ":" + p.getTel_number());

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
    public boolean onMarkerClick(@NonNull Marker marker) {
        try {
            String markerTitle = marker.getTitle();
            Log.d(TAG, "onMarkerClick: " + marker.getTitle());
            String[] parts = markerTitle.split(":");
            String parent_name = parts[0];
            String parent_tel_number = parts[1];
            Parent p = null;
            for (Parent parent : mDriver.getParents()) {
                if (parent.getAddress_latitude() != null && parent.getAddress_longitude() != null) {
                    if (parent.getName().equals(parent_name) && parent.getTel_number().equals(parent_tel_number))
                    {
                        p = parent;
                        break;
                    }
                }
            }
            if(p != null)
            {
                showCheckDialog(p);
            }
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
            if (mMapboxMap == null) return;

            Log.d(TAG,
                    "PosDriverObserver => onChanged: posDriver " + posDriver.getLatitude() + ", " +
                            posDriver.getLongitude() + ", " + mViewModel.getAccuracy().getValue());
            //the bus change its location so update it on Google map
            setBusLocation(posDriver);
            updatePolyLineOnMap(posDriver);
        }
    }
}