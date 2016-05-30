package br.com.inngage.sample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;

/**
 * Created by viniciusdepaula on 17/05/16.
 */
public class GCMListenerService extends GcmListenerService {

    private static final String TAG = "InnGCMListenerService";

    Random random = new Random();
    int notifyID = random.nextInt(9999 - 1000) + 1000;
    String contentTitle = "";
    String contentText = "";


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    //Bundle[{gcm.notification.e=1, gcm.notification.badge=1, gcm.notification.title=Phonegap Demo, gcm.notification.body=Testes com Phonegap, collapse_key=br.com.inngage.sample}]

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String message = data.getString("gcm.notification.body");
        Log.d(TAG, "From: " + from);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        //sendNotification(message);
        sendNotification(data);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param data GCM message received.
     */
    private void sendNotification(Bundle data) {

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent notificationIntent = new Intent(this, NotificationActivity.class);

        if(data.getString("id") != null) {

            notificationIntent.putExtra("notifyID", data.getString("id"));
        }
        if(data.getString("body") != null) {

            notificationIntent.putExtra("message", data.getString("body"));
        }
        if(data.getString("url") != null) {

            notificationIntent.putExtra("url", data.getString("url"));
        }
        if(data.getString("image") != null) {

            notificationIntent.putExtra("image", data.getString("image"));
        }
        if(data.getString("additional_data") != null) {

            notificationIntent.putExtra("additional_data", data.getString("additional_data"));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        if(data.getString("title") == null) {

            contentTitle = getApplicationName(getApplicationContext());

        } else {

            contentTitle = data.getString("title");
        }
        if(data.getString("title") == null) {

            contentTitle = getApplicationName(getApplicationContext());

        } else {

            contentTitle = data.getString("title");
        }
        if(data.getString("body") == null) {

            contentText = "Nenhuma mensagem recebida.";

        } else {

            contentText = data.getString("body");
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)

                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notifyID, notificationBuilder.build());
    }

    public static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }
}

