package com.aventum.yellowpages;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

//import static com.example.yellowpages.NotiChannel.CHANNEL_ID;

public class ScheduleReminderService extends JobIntentService {

    private static final String TAG = "ScheduleReminderService";
    public static final String TAG_NOTIFICATION = "NOTIFICATION_MESSAGE";
    public static final int NOTIFICATION_ID = 100;
    private Context context;

    static void enqueueWork(Context context, Intent intent){
        enqueueWork(context, ScheduleReminderService.class, 1, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate");
        context = this;

        /*Intent show = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, show, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reminder active")
                .setContentText("0")
                .setSmallIcon(R.drawable.ic_baseline_pages_24)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getResources().getString(R.string.reminderServiceName), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        manager.notify(NOTIFICATION_ID, builder.build());*/

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "onHandleWork");

        String input = intent.getStringExtra("inputExtra");
        for(int i = 0 ; i<5; ++i){
            Log.i(TAG, input + "-" + i);

            if(isStopped())
                return;

            SystemClock.sleep(1000);
        }

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

        /*mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_pages_24)
                .setContentTitle("Test")
                .setContentText("Testing new notification")
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

        mNotificationManager.notify(TAG_NOTIFICATION,NOTIFICATION_ID, mBuilder.build());*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public boolean onStopCurrentWork() {
        Log.i(TAG, "onStopCurrentWork");
        return super.onStopCurrentWork();
    }
}
