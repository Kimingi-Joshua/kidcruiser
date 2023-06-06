package com.creativeapps.schoolbusdriver.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.creativeapps.schoolbusdriver.data.network.models.Child;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*Some utility functions used by many classes in the application*/
public class Util {

    public static final String WEB_SERVER_URL = "YOUR_WEB_SERVER_URL_HERE";

    public static final int CHECK_IN_FLAG = 3;
    public static final int CHECK_OUT_FLAG = 4;
    /*A function to serialize an object using json and save it to SharedPreference*/
    public static void saveObjectToSharedPreference(Context context, String preferenceFileName,
                                                    String serializedObjectKey, Object object) {
        //get the SharedPreference from context
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        //start the SharedPreference editor
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        //serialize the object to json
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        //save the serialized object with the provided key
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        //apply changes to the SharedPreference editor
        sharedPreferencesEditor.apply();
    }

    /*A function to read an object that is represented as json from SharedPreference and deserialize it*/
    public static <GenericClass> GenericClass getSavedObjectFromPreference(Context context,
                                                                           String preferenceFileName,
                                                                           String preferenceKey,
                                                                           Class<GenericClass> classType) {
        //get the SharedPreference from context
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        //check if the SharedPreference contains the provided key
        if (sharedPreferences.contains(preferenceKey)) {
            //read the object in json format and deserialize it
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        //object with provided key not found
        return null;
    }

    /*go to an activity*/
    public static void redirectToActivity(AppCompatActivity currentActivity, Class NextActivityClass) {
        Intent intent = new Intent(currentActivity, NextActivityClass);
        currentActivity.startActivity(intent);
    }

    /*display a message with ok button and optional app exit if the user presses the ok button*/
    public static void displayExitMessage(String message, final Activity current, final boolean exitWithOk)
    {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(current);

        // Set the message show for the Alert
        builder.setMessage(message);

        // Set Alert Title
        builder.setTitle("Alert !");

        // Set Cancelable false so when the user clicks on the outside the Dialog Box,
        // it will remain visible
        builder.setCancelable(false);

        // Set the positive button with ok name and set OnClickListener method (defined
        // in DialogInterface interface).

        builder.setPositiveButton(
                "Ok",
                new DialogInterface
                        .OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which)
                    {
                        if(exitWithOk)
                        {
                            // When the user click yes button, then app will close
                            current.finishAffinity();
                        }
                    }
                });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
    }

    public static  Integer getAbsentStatus(Child child){
        if(child.getChild_AbsentTill()!=null) {
            try {
                String pattern = "yyyy-MM-dd HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
                Date today = Calendar.getInstance().getTime();
                Date absent_till = df.parse(child.getChild_AbsentTill());
                if(!today.after(absent_till)){
                    return  1;
                }
            }
            catch (Exception e)
            {
                Log.d("getStatus", "getStatus: " + e.getMessage());
            }
        }
        return 0;
    }

    public static boolean isAfternoon(Context context)
    {
        Date afternoon = null;
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        String afternoon_time = getSavedObjectFromPreference(context,
                "mPreference", "afternoon_time", String.class);
        if(afternoon_time != null) {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            try {
                Date afternoon_parsed = format.parse(afternoon_time);
                calendar.set(Calendar.HOUR_OF_DAY, afternoon_parsed.getHours());
                calendar.set(Calendar.MINUTE, afternoon_parsed.getMinutes());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                afternoon = calendar.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
        {
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            afternoon = calendar.getTime();
        }
        return now.after(afternoon);
    }

    public static Integer getLastCheckStatus(Child child){
        if(child.lastCheckStatus != null && child.lastCheckStatus.getLastDate() != null) {
            try {
                String pattern = "yyyy-MM-dd";
                DateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date today = cal.getTime();
                Date lastCheckStatusDate = df.parse(child.lastCheckStatus.getLastDate());
                if(today.compareTo(lastCheckStatusDate) == 0){
                    return  child.lastCheckStatus.getCase();
                }
            }
            catch (Exception e)
            {
                Log.d("getStatus", "getStatus: " + e.getMessage());
            }
        }
        return null;
    }
}
