package com.aventum.yellowpages;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderBroadcast extends BroadcastReceiver {
    public static final String TAG_NOTIFICATION = "NOTIFICATION_MESSAGE";
    public static final String CHANNEL_ID = "channel_1111";
    public static final int NOTIFICATION_ID = 111111;
    private static final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        try {

            // If android 10 or higher
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
            {

                startActivityNotification(context,NOTIFICATION_ID,context.getResources().getString(R.string.reminderServiceName), "test");

            }
            else
            {
                // If lower than Android 10, we use the normal method ever.
                Intent activity = new Intent(context, MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activity);
            }

        } catch (Exception e)
        {
            Log.d(TAG,e.getMessage()+"");
        }
    }


    // notification method to support opening activities on Android 10
    public static void startActivityNotification(Context context, int notificationID,
                                                 String title, String message) {

        NotificationManager mNotificationManager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Create GPSNotification builder
        NotificationCompat.Builder mBuilder;

        //Initialise ContentIntent
        Intent ContentIntent = new Intent(context, MainActivity.class);
        ContentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent ContentPendingIntent = PendingIntent.getActivity(context, 0, ContentIntent, 0);

        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_pages_24)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                .setContentIntent(ContentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    "Activity Opening Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setDescription("Activity opening notification");

            mBuilder.setChannelId(CHANNEL_ID);

            mNotificationManager.createNotificationChannel(mChannel);
        }

        mNotificationManager.notify(TAG_NOTIFICATION,notificationID, mBuilder.build());
    }
}
