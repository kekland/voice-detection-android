package com.kekland.voicedetection;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by kkerz on 30-May-18.
 */

public class VoiceRecorderService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    VoiceRecorderThread thread;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i("VoiceService", "Received Start Foreground Intent");
            showNotification();
            thread = new VoiceRecorderThread(getApplicationContext());
            thread.start();
        }
        else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i("VoiceService", "Received Stop Foreground Intent");
            thread.close();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        RemoteViews notificationView = new RemoteViews(this.getPackageName(),R.layout.notification_main);


        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "voice_record_default")
                .setContentTitle("nkDroid Music Player")
                .setTicker("nkDroid Music Player")
                .setContentText("nkDroid Music")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(notificationView)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "VoiceRecorderChannel";
            String description = "This channel records voice and responds to it by decreasing music volume";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("voice_record_default", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("VoiceService", "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }
}
