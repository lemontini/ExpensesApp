package com.montini.expensesapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    String projName,uid,email, msg;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e("ON_msgRec", "============================================");
            Log.e("MSG Recv", "Message DATA payload: " + remoteMessage.getData().toString());


            Map<String, String> dataReceived = remoteMessage.getData();


            uid = dataReceived.get("id");// id of 2nd person
            projName = dataReceived.get("proj");
            email = dataReceived.get("email");// email of 2nd person
            msg = dataReceived.get("msg");

            Log.e("NOTIFICATION", email + ": " + msg);
            }

        if(Messaging.isChatWindowOpen==false){
            makeMyNotification();  //if it is open it is working on the Listener, so no need for a Notification
        }

    }

    private void makeMyNotification() {

        //for Opening an activity
        Intent i = new Intent(this, Messaging.class);
        i.putExtra("proj",projName);
        i.putExtra("id",uid);
        i.putExtra("email", email);

        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//multiple instances without this
        PendingIntent openActivityIntent = PendingIntent.getActivity(this,0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, AppStart.CHANNEL_AppInfo)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle(email)
                .setContentText(msg)
                .setDefaults(Notification.DEFAULT_SOUND)//not needed +Android v8(Oreo)... picks sound from Priority
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)

                .setContentIntent(openActivityIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();

        //to avoid notification over writing each other need a unique id
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int uniqueId = (int)((new Date().getTime()/1000L) % Integer.MAX_VALUE);
        notificationManager.notify(uniqueId, notification);

    }

}


