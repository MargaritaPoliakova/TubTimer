package com.application.tubtimer.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.application.tubtimer.R;

public class MyService extends Service {
    NotificationManager nm;
    Binder binder = new MyBinder();

    String NOTIF_ID = "timer_finished";
    int id = 124;
    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("my", "MyService onBind");
        return binder;
    }

    public void sendNotification(String s) {

        /*Notification(R.drawable.ic_notifications_black_24dp, "Text in status bar",
                System.currentTimeMillis());*/

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(MainActivity.FILE_NAME, "somefile");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(getApplicationContext(), NOTIF_ID)
                .setSmallIcon(R.drawable.icon1)
                .setContentTitle("Время вышло!")
                .setContentText(s)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pIntent)
                .setWhen(System.currentTimeMillis())
                .setVibrate(new long[]{0,400,200,400,200,400})
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();

//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(this);



        nm.notify(id++, notif);
    }




    class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}
