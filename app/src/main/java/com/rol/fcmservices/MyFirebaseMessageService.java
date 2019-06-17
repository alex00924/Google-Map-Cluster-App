package com.rol.fcmservices;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rol.Base;
import com.rol.FriendProfile;
import com.rol.R;
import com.rol.Splash;
import com.rol.TaskDetail;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessageService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    public static final String NOTIFICATION_CHANNEL_NAME = "ROL";
    String type;
    String message;
    DBHelper db;
    String user_id, auth_token;
    String task_id, sender_id, msg_from_id;
    private Context mContext = this;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("RESPONSE", remoteMessage.getData() + "##@#2432432");

        ringTone();
        try {
            Log.e("RESPONSETYPE", "" + remoteMessage.getData().get("type"));
            Log.e("RESPONSETYPE", "" + remoteMessage.getData().get("message"));

            Map<String, String> params = remoteMessage.getData();
            JSONObject object = new JSONObject(params);
            Log.e("JSON OBJECT", object.toString());
            type = object.getString("type");
            message = object.getString("message");
            if (object.has("task_id")) {
                task_id = object.getString("task_id");
            }
            if (object.has("sender_id")) {
                sender_id = object.getString("sender_id");
            }
            if (object.has("from_id")) {
                msg_from_id = object.getString("from_id");
            }

            if (message != null && message.length() > 0) {
                db = DBHelper.getInstance(this);
                db.open();
                user_id = db.getRegisterData().user_id;
                auth_token = db.getRegisterData().auth_token;
                db.close();
                getNotificationCount();
            }
           /* if(!Chat.active)
            {
                createNotification(type, message);
            }*/

            createNotification(type, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createNotification(String type, String message) {
        Intent resultIntent;
        if (task_id != null && task_id.length() > 0) {
            resultIntent = new Intent(this, TaskDetail.class);
            resultIntent.putExtra("task_id", task_id);
        } else if (sender_id != null && sender_id.length() > 0) {
            resultIntent = new Intent(this, FriendProfile.class);
            resultIntent.putExtra("friend_id", sender_id);
//        } else if (msg_from_id != null && msg_from_id.length() > 0) {
//            resultIntent = new Intent(this, Message.class);
        } else {
            resultIntent = new Intent(this, Splash.class);
        }
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("ROL")
                .setContentText(message)
                .setColor(101)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void ringTone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNotificationCount() {
        String tag = "getNotificationCount";
        String url = AppConstants.url + "get_message_counter.php?" + "user_id=" + user_id + "&auth_token=" + auth_token;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                    } else {
                        JSONObject object = jsonObject.getJSONObject("responsedata");
                        String counter = object.getString("counter");
                        if (counter != null && counter.length() > 0) {
                            Log.e("set counter", counter);
                            Base.notification_count.setVisibility(View.VISIBLE);
                            Base.notification_count.setText(counter);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e("reset pass", error.toString());
                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }
}