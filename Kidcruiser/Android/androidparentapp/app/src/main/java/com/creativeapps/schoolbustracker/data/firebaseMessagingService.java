package com.creativeapps.schoolbustracker.data;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.creativeapps.schoolbustracker.R;
import com.creativeapps.schoolbustracker.ui.activity.main.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class firebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public static int NOTIFICATION_ID = 101;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Check if message contains a data Payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data Payload: " + remoteMessage.getData());
            try
            {
                //get the notification message
                String message = remoteMessage.getData().get("message_content");
                //get the notification type
                String notificationType = remoteMessage.getData().get("notification_type");
                //ge the notification time from the server. Note that, the server time is different
                // from the device time. The following code convert from the server time to the
                // device time
                String time="";
                //define the format of the server (incoming) time. Note that this includes the time
                // zone of the server
                SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                //define the format of the time to be displayed on the device
                SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");
                try {
                    // get the time of the notification
                    time = remoteMessage.getData().get("time");
                    TimeZone tz = TimeZone.getDefault();
                    //set the time zone of the display format to be the current time zone of the
                    // device
                    displayFormat.setTimeZone(tz);
                    // get the time of the notification
                    Date date = inFormat.parse(time);
                    //display the notification time in the display format
                    time = displayFormat.format(date);
                }
                catch(ParseException pe) {

                }

                if(notificationType.compareTo(Util.near_home_preference) == 0)
                {
                    String near_me_pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Util.near_home_preference,"");
                    if(!near_me_pref.isEmpty() && near_me_pref.compareTo("0")!=0)
                    {
                        sendNotification(time, message);
                    }
                }
                else
                {
                    Boolean prefSetting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(notificationType,false);
                    //check if the settings of the app to determine if the notification should be showed
                    if(prefSetting || notificationType.compareTo("admin_message")==0)
                    {
                        sendNotification(time, message);
                    }
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, "onMessageReceived: " + e.getMessage());
            }

        }
    }
    // [END receive_message]


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, String title) {
        if(title=="")
            return;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "driverUpdate";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.bus_bar)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFICATION_ID++, notificationBuilder.build());
    }
}