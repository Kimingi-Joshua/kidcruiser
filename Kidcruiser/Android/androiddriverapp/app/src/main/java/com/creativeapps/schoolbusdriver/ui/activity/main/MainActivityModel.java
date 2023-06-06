package com.creativeapps.schoolbusdriver.ui.activity.main;

import android.location.Location;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.creativeapps.schoolbusdriver.data.DataManager;
import com.creativeapps.schoolbusdriver.data.network.models.Child;
import com.creativeapps.schoolbusdriver.data.network.models.ChildResponse;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.DriverResponse;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.data.network.services.DriverApiService;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityModel extends ViewModel {

    //region Member variables

    private MutableLiveData<Location> posDriver;
    private MutableLiveData<Float> accuracy;
    private MutableLiveData<String> status;
    private DriverApiService driverApiService;
    private MutableLiveData<Driver> driver;

    private MutableLiveData<Integer> showAds;

    private MutableLiveData<Boolean> mConnectivityStatus;

    private MutableLiveData<Integer> mCheckinStatus;
    private MutableLiveData<Boolean> mIsWaitRespCheck;

    private MutableLiveData<Integer> useMabBox;

    private MutableLiveData<Direction> direction;

    private static String mMapBoxType;
    private static Integer mGoogleMapType;

    private MutableLiveData<Boolean> mLocationTrackingStatus;

    public static List<Double> polyline;
    //endregion

    //region Getters and setters for member variables


    public MutableLiveData<String> getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status.postValue(status);
    }


    public MutableLiveData<Driver> getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver.postValue(driver);
    }


    public MutableLiveData<Location> getPosDriver() {
        return posDriver;
    }

    public void setPosDriver(Location posDriver) {
        this.posDriver.setValue(posDriver);
    }

    public MutableLiveData<Float> getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy.setValue(accuracy);
    }

    public MutableLiveData<Boolean> getConnectivityStatus() {
        return mConnectivityStatus;
    }

    public void setConnectivityStatus(Boolean mConnectivityStatus) {
        this.mConnectivityStatus.postValue(mConnectivityStatus);
    }


    public MutableLiveData<Integer> getCheckinStatus() {
        return mCheckinStatus;
    }

    public void setCheckinStatus(Integer mCheckinStatus) {
        this.mCheckinStatus.postValue(mCheckinStatus);
    }

    public MutableLiveData<Boolean> getIsWaitRespChecking() {
        return mIsWaitRespCheck;
    }

    public void SetIsWaitRespChecking(Boolean isWaitResp) {
        this.mIsWaitRespCheck.postValue(isWaitResp);
    }


    //endregion

    //region Constructor

    public MainActivityModel()
    {
        posDriver = new MutableLiveData<>();
        driver = new MutableLiveData<>();
        accuracy = new MutableLiveData<>();
        status = new MutableLiveData<>();
        mConnectivityStatus = new MutableLiveData<>();
        showAds = new MutableLiveData<>();
        useMabBox = new MutableLiveData<>();

        driverApiService = DataManager.getInstance().getDriverApiService();

        mCheckinStatus = new MutableLiveData<>();
        mIsWaitRespCheck = new MutableLiveData<>();

        direction = new MutableLiveData<>();
        mLocationTrackingStatus = new MutableLiveData<>();
    }
    //endregion

    /*get the driver data from the server with his telephone number and country code*/
    public void getDriverServer(final String countryCode, final String mobileNumber, final String SecretCode)
    {
        //define background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    getSchoolBusDriverTelNumber(countryCode, mobileNumber, SecretCode);
                } catch (Exception e) {
                    Log.d("getDriverServer", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*update the driver position in the backend */
    public void updateDriverPosition(final Integer id, final String SecretCode, final LatLng pos)
    {
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    //call a function to update the driver location in the server
                    updatePosition(id, SecretCode, pos.getLatitude(), pos.getLongitude());
                } catch (Exception e) {
                    Log.d("updateDriverPosition", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*update the driver position in the backend */
    public void updateDriverPositionSpeed(final String SecretCode,
                                          final LatLng pos, final Double speed)
    {
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    //call a function to update the driver location in the server
                    updatePositionSpeed(SecretCode, pos.getLatitude(), pos.getLongitude(), speed);
                } catch (Exception e) {
                    Log.d("updateDriverPosition", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }


    /*check in/out the child */
    public void checkInOutChildServer(final String secretKey,
                                      final Integer child_id, final Integer case_id,
                                      final String checked_in_string,final String checked_out_string)
    {
        SetIsWaitRespChecking(true);
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    //call a function to update the driver location in the server
                    checkInOutChild(secretKey, child_id, case_id, checked_in_string, checked_out_string);
                } catch (Exception e) {
                    SetIsWaitRespChecking(false);
                    Log.d("updateDriverPosition", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    private void checkInOutChild(String secretKey, Integer child_id,
                                 Integer case_id, String checked_in_string,
                                 String checked_out_string) {
        Call<ChildResponse> driverApiCall =  driverApiService.getDriverApi().checkInOutChild(
                secretKey, child_id, case_id, checked_in_string, checked_out_string);
        driverApiCall.enqueue(new MainActivityModel.checkInOutChildCallback(case_id));
    }

    private void updatePositionSpeed(String secretKey, Double last_latitude, Double last_longitude, Double speed) {
        JSONArray polylineJsonArray = new JSONArray(polyline);
        Call<ResponseBody> driverApiCall =  driverApiService.getDriverApi().updatePositionSpeed(secretKey, last_latitude, last_longitude, speed, polylineJsonArray);
        driverApiCall.enqueue(new MainActivityModel.updatePositionCallback());
    }
    /*call the Api function updatePosition and define a callback for this Api*/
    private void updatePosition(Integer id, String secretKey, Double last_latitude, Double last_longitude) {
        Call<ResponseBody> driverApiCall =  driverApiService.getDriverApi().updatePosition(id, secretKey, last_latitude, last_longitude);
        driverApiCall.enqueue(new MainActivityModel.updatePositionCallback());
    }

    /*call the Api function getSchoolBusDriverTelNumber and define a callback for this Api*/
    private void getSchoolBusDriverTelNumber(String countryCode, String mobileNumber, String SecretCode) {
        Call<DriverResponse> driverApiCall = driverApiService.getDriverApi().getSchoolBusDriverTelNumber(countryCode, mobileNumber, SecretCode);
        driverApiCall.enqueue(new MainActivityModel.DriverApiCallback());
    }

    public MutableLiveData<Integer> getShowAds() {
        return showAds;
    }

    public void setShowAds(Integer showAds) {
        if(showAds == null)
            showAds = 0;
        this.showAds.postValue(showAds);
    }

    public MutableLiveData<Integer> getUseMabBox() {
        return useMabBox;
    }

    public void setUseMabBox(Integer useMabBox) {
        if(useMabBox == null)
            useMabBox = 0;
        this.useMabBox.postValue(useMabBox);
    }

    public void setMabBoxType(String mapType) {
        this.mMapBoxType = mapType;
    }

    public String getMapBoxType() {
        return this.mMapBoxType;
    }

    public void setGoogleMapType(Integer mapType) {
        this.mGoogleMapType = mapType;
    }

    public Integer getGoogleMapType() {
        return this.mGoogleMapType;
    }

    //Get google map route between points
    public void getRoute(com.google.android.gms.maps.model.LatLng first_location,
                         ArrayList<com.google.android.gms.maps.model.LatLng> waypoints,
                         com.google.android.gms.maps.model.LatLng last_location,
                         String apiKey){
        GoogleDirection.withServerKey(apiKey)
                .from(first_location)
                .and(waypoints)
                .to(last_location)
                .transportMode(TransportMode.DRIVING)
                .execute(
                        new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(@Nullable Direction direction) {
                                assert direction != null;
                                if(direction.isOK()) {
                                    setDirection(direction);
                                }
                            }

                            @Override
                            public void onDirectionFailure(@NonNull Throwable t) {

                            }
                        }
                );
    }

    public void setDirection(Direction direction) {
        this.direction.postValue(direction);
    }

    public MutableLiveData<Direction> getDirection() {
        return direction;
    }

    public MutableLiveData<Boolean> getLocationTrackingStatus() {
        return mLocationTrackingStatus;
    }

    public void setLocationTrackingStatus(Boolean locationTrackingStatus) {
        this.mLocationTrackingStatus.postValue(locationTrackingStatus);
    }

    /*Callback for getSchoolBusDriverTelNumber Api function*/
    private class DriverApiCallback implements Callback<DriverResponse> {

        @Override
        public void onResponse( Call<DriverResponse> call, Response<DriverResponse> response) {
            int RetCode = response.code();
            //check the return code from response
            switch (RetCode)
            {
                //response is OK
                case 200:
                    setStatus("");
                    //get the driver data from the response
                    DriverResponse r = response.body();
                    if(r!=null) {
                        //save it to the live data variable
                        Driver d = r.getDriver();
                        setShowAds(d.showAds);
                        setUseMabBox(d.useMabBox);
                        setDriver(d);
                    }
                    break;
                //response has errors, so the driver live data is not set
                case 404:
                case 422:
                    Driver d = new Driver();
                    d.setVerified((byte) 0);
                    setDriver(d);
                    break;
                case 500:
                default:
                    setDriver(null);
                    Log.d("response.message", response.message()+"");
                    break;
            }
        }

        @Override
        public void onFailure(Call<DriverResponse> call, Throwable t) {
            setDriver(null);
            setStatus("Unable to connect to the server. Please try again later");
        }
    }

    /*Callback for updatePosition Api function*/
    private class updatePositionCallback implements Callback<ResponseBody> {

        @Override
        public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response) {
            int RetCode = response.code();
            switch (RetCode)
            {
                //if the response is error free, set the status to online
                case 200:
                    setConnectivityStatus(true);
                    break;
                //if errors, set the status to offline
                case 404:
                case 422:
                case 500:
                default:
                    setConnectivityStatus(false);
                    try {
                        Log.d("response.message", response.errorBody().string()+"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            setConnectivityStatus(false);
        }
    }


    /*Callback for checkInOutChild Api function*/
    private class checkInOutChildCallback implements Callback<ChildResponse> {
        Integer case_id;
        public checkInOutChildCallback(Integer case_id) {
            this.case_id = case_id;
        }

        @Override
        public void onResponse( Call<ChildResponse> call, Response<ChildResponse> response) {
            int RetCode = response.code();
            SetIsWaitRespChecking(false);
            switch (RetCode)
            {
                //if the response is error free, set the status to online
                case 200:
                    setCheckinStatus(case_id);
                    ChildResponse r = response.body();
                    if(r!=null) {
                        //save it to the live data variable
                        updateDriverData(r.getChild());
                    }
                    break;
                //if errors
                case 404:
                case 422:
                case 500:
                default:
                    setCheckinStatus(0);
                    try {
                        Log.d("response.message", response.errorBody().string()+"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void onFailure(Call<ChildResponse> call, Throwable t) {
            SetIsWaitRespChecking(false);
            setCheckinStatus(0);
        }
    }

    private void updateDriverData(Child child) {
        Driver d = driver.getValue();
        boolean found  = false;
        for (int i = 0; i < d.getParents().size(); i++) {
            Parent p = d.getParents().get(i);
            for (int j = 0; j < p.getChildren().size(); j++) {
                Child c = p.getChildren().get(j);
                if(c.getId().equals(child.getId()))
                {
                    p.getChildren().set(j, child);
                    d.getParents().set(i, p);
                    found = true;
                    break;
                }
            }
            if(found)
            {
                break;
            }
        }
        driver.postValue(d);
    }
}
