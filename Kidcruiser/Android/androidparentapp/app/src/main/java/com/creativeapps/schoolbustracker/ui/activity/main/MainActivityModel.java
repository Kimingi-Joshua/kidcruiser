package com.creativeapps.schoolbustracker.ui.activity.main;

import android.util.Log;

import com.creativeapps.schoolbustracker.data.DataManager;
import com.creativeapps.schoolbustracker.data.network.models.ChildResponse;
import com.creativeapps.schoolbustracker.data.network.models.EventLog;
import com.creativeapps.schoolbustracker.data.network.models.EventLogResponse;
import com.creativeapps.schoolbustracker.data.network.models.Parent;
import com.creativeapps.schoolbustracker.data.network.models.ParentResponse;
import com.creativeapps.schoolbustracker.data.network.models.Payload;
import com.creativeapps.schoolbustracker.data.network.services.ParentApiService;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityModel extends ViewModel {

    //region Member variables

    private ParentApiService mParentApiService;
    private MutableLiveData<Parent> mParent;

    private MutableLiveData<Boolean> mParentLocationUpdateIsRunning;
    private MutableLiveData<Boolean> mChildAbsentUpdateIsRunning;

    private MutableLiveData<Boolean> mParentAlertZoneDistanceUpdateIsRunning;



    private MutableLiveData<Boolean> mGetDriverLogIsRunning;

    private MutableLiveData<Boolean> mGetChildLogIsRunning;
    private MutableLiveData<EventLogResponse> mChildLogs;

    private MutableLiveData<Boolean> mParentLocationUpdateResp;
    private MutableLiveData<ChildResponse> mChildAbsentUpdateResp;

    private MutableLiveData<Boolean> mParentAlertZoneDistanceUpdateResp;

    private MutableLiveData<Boolean> mConnectivityStatus;



    private MutableLiveData<EventLogResponse> mDriverLogs;


    private MutableLiveData<Payload> mDriverRealTimeData;

    private MutableLiveData<Integer> showAds;

    private static String mMapBoxType;
    private static Integer mGoogleMapType;

    private MutableLiveData<Integer> useMabBox;

    //endregion

    //region Getters and setters for member variables

    public MutableLiveData<Integer> getUseMabBox() {
        return useMabBox;
    }

    public void setUseMabBox(Integer useMabBox) {
        if(useMabBox == null)
            useMabBox = 0;
        this.useMabBox.postValue(useMabBox);
    }

    public MutableLiveData<Parent> getParent() {
        return mParent;
    }

    public void setParent(Parent parent) {
        this.mParent.postValue(parent);
    }


    public MutableLiveData<Boolean> getParentLocationUpdateIsRunning() {
        return mParentLocationUpdateIsRunning;
    }

    public void setParentLocationUpdateIsRunning(Boolean mParentLocationSet) {
        this.mParentLocationUpdateIsRunning.postValue(mParentLocationSet);
    }


    public MutableLiveData<Boolean> getChildAbsentUpdateIsRunning() {
        return mChildAbsentUpdateIsRunning;
    }

    public void setChildAbsentUpdateIsRunning(Boolean mParentLocationSet) {
        this.mChildAbsentUpdateIsRunning.postValue(mParentLocationSet);
    }


    public MutableLiveData<Boolean> getParentLocationUpdateResp() {
        return mParentLocationUpdateResp;
    }

    public void setParentLocationUpdateResp(Boolean mParentLocationUpdateResp) {
        this.mParentLocationUpdateResp.postValue(mParentLocationUpdateResp);
    }

    public MutableLiveData<ChildResponse> getChildAbsentUpdateResp() {
        return mChildAbsentUpdateResp;
    }

    public void setChildAbsentUpdateResp(ChildResponse mChildAbsentUpdateResp) {
        this.mChildAbsentUpdateResp.postValue(mChildAbsentUpdateResp);
    }

    public MutableLiveData<Boolean> getParentAlertZoneDistanceUpdateIsRunning() {
        return mParentAlertZoneDistanceUpdateIsRunning;
    }

    public void setParentAlertZoneDistanceUpdateIsRunning(Boolean mParentAlertZoneDistanceUpdateIsRunning) {
        this.mParentAlertZoneDistanceUpdateIsRunning.postValue(mParentAlertZoneDistanceUpdateIsRunning);
    }

    public MutableLiveData<Boolean> getParentAlertZoneDistanceUpdateResp() {
        return mParentAlertZoneDistanceUpdateResp;
    }

    public void setParentAlertZoneDistanceUpdateResp(Boolean mParentAlertZoneDistanceUpdateResp) {
        this.mParentAlertZoneDistanceUpdateResp.postValue(mParentAlertZoneDistanceUpdateResp);
    }

    public MutableLiveData<Boolean> getConnectivityStatus() {
        return mConnectivityStatus;
    }

    public void setConnectivityStatus(Boolean mConnectivityStatus) {
        this.mConnectivityStatus.postValue(mConnectivityStatus);
    }

    public MutableLiveData<Boolean> getGetDriverLogIsRunning() {
        return mGetDriverLogIsRunning;
    }

    public void setGetDriverLogIsRunning(Boolean mGetDriverLogIsRunning) {
        this.mGetDriverLogIsRunning.postValue(mGetDriverLogIsRunning);
    }

    public MutableLiveData<EventLogResponse> getDriverLogs() {
        return mDriverLogs;
    }

    public void setDriverLogs(EventLogResponse mDriverLogs) {
        for (int i = 0; i < mDriverLogs.getEventLog().size(); i++) {
            EventLog driverLog = mDriverLogs.getEventLog().get(i);
            adjustTime(driverLog);
        }
        this.mDriverLogs.postValue(mDriverLogs);
    }

    private void adjustTime(EventLog eventLog) {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        //define the format of the time to be displayed on the device
        SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        TimeZone tz = TimeZone.getDefault();
        //set the time zone of the display format to be the current time zone of the
        // device
        displayFormat.setTimeZone(tz);
        try {
            // get the time of the notification
            String time = eventLog.getEvent_time();
            // get the time of the notification
            Date date = inFormat.parse(time);
            //display the notification time in the display format
            time = displayFormat.format(date);
            eventLog.setEvent_time(time);
        }
        catch(ParseException pe) {
            Log.d("MainModel", "setDriverLogs: " + pe.getMessage());
        }
    }

    public MutableLiveData<Payload> getDriverRealTimeData() {
        return mDriverRealTimeData;
    }

    public void setDriverRealTimeData(Payload mDriverRealTimeData) {
        this.mDriverRealTimeData.postValue(mDriverRealTimeData);
    }


    public MutableLiveData<Boolean> getGetChildLogIsRunning() {
        return mGetChildLogIsRunning;
    }

    public void setGetChildLogIsRunning(Boolean mGetChildLogRunning) {
        this.mGetChildLogIsRunning.postValue(mGetChildLogRunning);
    }


    public MutableLiveData<EventLogResponse> getChildLogs() {
        return mChildLogs;
    }

    public void setChildLogs(EventLogResponse mChildLogs) {
        for (int i = 0; i < mChildLogs.getEventLog().size(); i++) {
            EventLog childLog = mChildLogs.getEventLog().get(i);
            adjustTime(childLog);
        }
        this.mChildLogs.postValue(mChildLogs);
    }

    public MutableLiveData<Integer> getShowAds() {
        return showAds;
    }

    public void setShowAds(Integer showAds) {
        if(showAds == null)
            showAds = 0;
        this.showAds.postValue(showAds);
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

    //endregion

    //region Constructor

    public MainActivityModel()
    {
        mParent = new MutableLiveData<>();
        mParentLocationUpdateIsRunning = new MutableLiveData<>();
        mChildAbsentUpdateIsRunning = new MutableLiveData<>();
        mParentAlertZoneDistanceUpdateIsRunning = new MutableLiveData<>();
        mGetDriverLogIsRunning = new MutableLiveData<>();
        mParentLocationUpdateResp = new MutableLiveData<>();
        mChildAbsentUpdateResp = new MutableLiveData<>();
        mParentAlertZoneDistanceUpdateResp = new MutableLiveData<>();
        mConnectivityStatus = new MutableLiveData<>();
        mDriverLogs = new MutableLiveData<>();
        mDriverRealTimeData = new MutableLiveData<>();
        mParentApiService = DataManager.getInstance().getParentApiService();

        mGetChildLogIsRunning = new MutableLiveData<>();
        mChildLogs = new MutableLiveData<>();

        showAds = new MutableLiveData<>();
        useMabBox = new MutableLiveData<>();
    }
    //endregion

    /*get the parent data from the server with his telephone number and country code*/
    public void getParentServer(final String countryCode, final String mobileNumber, final String SecretCode)
    {
        //define background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    getParentTelNumber(countryCode, mobileNumber, SecretCode);
                } catch (Exception e) {
                    Log.d("getParentServer", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*update the parent location in the backend */
    public void updateChildAbsentServer(final Integer child_id, final String tomorrow_date)
    {
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    setChildAbsentUpdateIsRunning(true);
                    //call a function to update the mParent location in the server
                    Log.d("sdxz", "run: " + tomorrow_date);

                    updateAbsent(child_id, tomorrow_date);
                } catch (Exception e) {
                    setChildAbsentUpdateIsRunning(false);
                    Log.d("updateChildAbsent", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*update the parent location in the backend */
    public void updateParentPosition(final String SecretCode, final LatLng pos)
    {
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    setParentLocationUpdateIsRunning(true);
                    //call a function to update the mParent location in the server
                    updatePosition(SecretCode, pos.latitude, pos.longitude);
                } catch (Exception e) {
                    setParentLocationUpdateIsRunning(false);
                    Log.d("updateParentPosition", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*update the alert zone distance of the parent in the backend */
    public void setAlertZoneDistance(final String secretKey,final Integer zoneAlertDistance) {
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    setParentAlertZoneDistanceUpdateIsRunning(true);
                    //call a function to update the alert zone distance of the parent in the server
                    setZoneDistance(secretKey, zoneAlertDistance);
                } catch (Exception e) {
                    setParentAlertZoneDistanceUpdateIsRunning(false);
                    Log.d("updateZoneDistance", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }


    /*get the parent data from the server with his telephone number and country code*/
    public void getDriverLog(final Integer page, final String SecretCode)
    {
        //define background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    setGetDriverLogIsRunning(true);
                    getDriverLogServer(page, SecretCode);
                } catch (Exception e) {
                    setGetDriverLogIsRunning(false);
                    Log.d("getEventLog", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }


    /*get the getChildLog from the server */
    public void getChildLog(final Integer page, final String SecretCode)
    {
        //define background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    setGetChildLogIsRunning(true);
                    getChildLogServer(page, SecretCode);
                } catch (Exception e) {
                    setGetChildLogIsRunning(false);
                    Log.d("getEventLog", "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*call the Api function updateChildAbsent and define a callback for this Api*/
    private void updateAbsent(Integer child_id, String tomorrow_date) {
        Call<ChildResponse> parentApiCall =  mParentApiService.getParentApi().updateChildAbsent(child_id, tomorrow_date);
        parentApiCall.enqueue(new MainActivityModel.updateAbsentCallback());
    }

    /*call the Api function updatePosition and define a callback for this Api*/
    private void updatePosition(String secretKey, Double last_latitude, Double last_longitude) {
        Call<ResponseBody> parentApiCall =  mParentApiService.getParentApi().updatePosition(secretKey, last_latitude, last_longitude);
        parentApiCall.enqueue(new MainActivityModel.updatePositionCallback());
    }

    /*call the Api function getParentTelNumber and define a callback for this Api*/
    private void getParentTelNumber(String countryCode, String mobileNumber, String SecretCode) {
        Call<ParentResponse> parentApiCall = mParentApiService.getParentApi().getParentTelNumber(countryCode, mobileNumber, SecretCode);
        parentApiCall.enqueue(new MainActivityModel.ParentApiCallback());
    }

    /*call the Api function setParentZoneDistance and define a callback for this Api*/
    private void setZoneDistance(String secretKey, Integer zoneAlertDistance) {
        Call<ResponseBody> parentApiCall =  mParentApiService.getParentApi().setParentZoneDistance(secretKey, zoneAlertDistance);
        parentApiCall.enqueue(new MainActivityModel.updateZoneDistanceCallback());
    }

    /*call the Api function getParentTelNumber and define a callback for this Api*/
    private void getDriverLogServer(Integer page, String SecretCode) {
        Call<EventLogResponse> driverLogApiCall = mParentApiService.getParentApi().getDriverLog(page, SecretCode);
        driverLogApiCall.enqueue(new MainActivityModel.getDriverLogApiCallback());
    }


    /*call the Api function getChildLog and define a callback for this Api*/
    private void getChildLogServer(Integer page, String SecretCode) {
        Call<EventLogResponse> driverLogApiCall = mParentApiService.getParentApi().getChildLog(page, SecretCode);
        driverLogApiCall.enqueue(new MainActivityModel.getChildLogApiCallback());
    }

    /*Callback for getParentTelNumber Api function*/
    private class ParentApiCallback implements Callback<ParentResponse> {

        @Override
        public void onResponse( Call<ParentResponse> call, Response<ParentResponse> response) {
            int RetCode = response.code();
            //check the return code from response
            switch (RetCode)
            {
                //response is OK
                case 200:
                    //get the mParent data from the response
                    ParentResponse r = response.body();
                    if(r!=null) {
                        //save it to the live data variable
                        Parent p = r.getParent();
                        setShowAds(p.showAds);
                        setUseMabBox(p.useMabBox);
                        setParent(p);
                    }
                    break;
                //response has errors, so the mParent live data is not set
                case 404:
                case 422:
                    Parent p = new Parent();
                    p.setVerified((byte) 0);
                    setParent(p);
                    break;
                case 500:
                default:
                    setParent(null);
                    Log.d("response.message", response.message()+"");
                    break;
            }
        }

        @Override
        public void onFailure(Call<ParentResponse> call, Throwable t) {
            setParent(null);
        }
    }

    /*Callback for updateAbsent Api function*/
    private class updateAbsentCallback implements Callback<ChildResponse> {

        @Override
        public void onResponse( Call<ChildResponse> call, Response<ChildResponse> response) {
            int RetCode = response.code();
            switch (RetCode)
            {
                //return code is OK
                case 200:
                    //get the mParent data from the response
                    ChildResponse r = response.body();
                    if(r!=null) {
                        //save it to the live data variable
                        setChildAbsentUpdateResp(r);
                    }

                    break;
                //if the return code has error, set the status mChildAbsentUpdateIsRunning to false
                case 404: //resource not found
                case 422: //validation error
                case 500: //general server error
                default:
                    setChildAbsentUpdateResp(null);
                    break;
            }
            setChildAbsentUpdateIsRunning(false);
        }

        @Override
        public void onFailure(Call<ChildResponse> call, Throwable t) {
            setChildAbsentUpdateResp(null);
            setChildAbsentUpdateIsRunning(false);
        }
    }


    /*Callback for updatePosition Api function*/
    private class updatePositionCallback implements Callback<ResponseBody> {

        @Override
        public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response) {
            int RetCode = response.code();
            switch (RetCode)
            {
                //return code is OK
                case 200:
                    setParentLocationUpdateResp(true);
                    break;
                //if the return code has error, set the status mParentLocationUpdateIsRunning to false
                case 404: //resource not found
                case 422: //validation error
                case 500: //general server error
                default:
                    setParentLocationUpdateResp(false);
                    break;
            }
            setParentLocationUpdateIsRunning(false);
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            setParentLocationUpdateResp(false);
            setParentLocationUpdateIsRunning(false);
        }
    }


    /*Callback for setZoneDistance Api function*/
    private class updateZoneDistanceCallback implements Callback<ResponseBody> {

        @Override
        public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response) {
            int RetCode = response.code();
            switch (RetCode)
            {
                //return code is OK
                case 200:
                    setParentAlertZoneDistanceUpdateResp(true);
                    break;
                //if the return code has error, set the status mParentAlertZoneDistanceUpdateIsRunning to false
                case 404: //resource not found
                case 422: //validation error
                case 500: //general server error
                default:
                    setParentAlertZoneDistanceUpdateResp(false);
                    break;
            }
            setParentAlertZoneDistanceUpdateIsRunning(false);
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            setParentAlertZoneDistanceUpdateResp(false);
            setParentAlertZoneDistanceUpdateIsRunning(false);
        }
    }


    /*Callback for getParentTelNumber Api function*/
    private class getDriverLogApiCallback implements Callback<EventLogResponse> {

        @Override
        public void onResponse(Call<EventLogResponse> call, Response<EventLogResponse> response) {
            int RetCode = response.code();
            //check the return code from response
            switch (RetCode)
            {
                //response is OK
                case 200:
                    //get the mParent data from the response
                    EventLogResponse r = response.body();
                    if(r!=null) {
                        //save it to the live data variable
                        setDriverLogs(r);
                    }
                    break;
                //response has errors, so the mParent live data is not set
                case 404:
                case 422:
                case 500:
                default:
                    setDriverLogs(null);
                    Log.d("response.message", response.message()+"");
                    break;
            }
            setGetDriverLogIsRunning(false);
        }

        @Override
        public void onFailure(Call<EventLogResponse> call, Throwable t) {
            setDriverLogs(null);
            setGetDriverLogIsRunning(false);
        }
    }


    /*Callback for getParentTelNumber Api function*/
    private class getChildLogApiCallback implements Callback<EventLogResponse> {

        @Override
        public void onResponse(Call<EventLogResponse> call, Response<EventLogResponse> response) {
            int RetCode = response.code();
            //check the return code from response
            switch (RetCode)
            {
                //response is OK
                case 200:
                    //get the mParent data from the response
                    EventLogResponse r = response.body();
                    if(r!=null) {
                        //save it to the live data variable
                        setChildLogs(r);
                    }
                    break;
                //response has errors, so the mParent live data is not set
                case 404:
                case 422:
                case 500:
                default:
                    setChildLogs(null);
                    Log.d("response.message", response.message()+"");
                    try {
                        Log.d("response.message", response.errorBody().string()+"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            setGetChildLogIsRunning(false);
        }

        @Override
        public void onFailure(Call<EventLogResponse> call, Throwable t) {
            setChildLogs(null);
            setGetChildLogIsRunning(false);
        }
    }
}
