package com.rol;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rol.adapter.ChatAdapter;
import com.rol.beans.LoginBean;
import com.rol.beans.MessageBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat extends Base {

    public static boolean active = false;
    ListView chat_listview;
    EditText write_msg;
    Button send_btn;
    TextView chat_date;
    DatabaseReference mychat_ref;
    String user_id = "";
    String datakeyname = "";
    String sender_id, reciver_id, message, reciver_name, reciver_profile;
    boolean is_receiver = false;
    DBHelper db;
    LoginBean bean;
    long valuee2 = 0;
    int i = 0;
    ChatAdapter adapter;
    List<MessageBean> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.chat, wrapper);

        initMember();

        base_title.setText(R.string.message);
        chat.setImageResource(R.mipmap.message);
        search_btn.setVisibility(View.GONE);
        setting_btn.setVisibility(View.GONE);

        db = DBHelper.getInstance(this);
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();
        user_id = bean.user_id;

        mychat_ref = FirebaseDatabase.getInstance().getReference().child("chat_rol");

        Intent intent = getIntent();
        if (intent.hasExtra("reciver_id")) {
            is_receiver = true;
            reciver_id = intent.getExtras().getString("reciver_id");
            reciver_name = intent.getStringExtra("reciver_name");
            reciver_profile = intent.getStringExtra("reciver_profile");
        } else {
            reciver_id = "38";
        }

        if (Integer.parseInt(user_id) < Integer.parseInt(reciver_id)) {
            datakeyname = user_id + "_" + reciver_id;
        } else {
            datakeyname = reciver_id + "_" + user_id;
        }

        DatabaseReference reference = mychat_ref.child(datakeyname);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list = new ArrayList<>();
                list.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    MessageBean data = snapshot.getValue(MessageBean.class);
                    Log.e("message =====>", data.message + "===>>>>>");
                    MessageBean paymentData = new MessageBean(data);
                    list.add(paymentData);
                }

                if (list != null && list.size() > 0) {
                    ChatAdapter adapter = new ChatAdapter(Chat.this, list);
                    chat_listview.setAdapter(adapter);

                    String date = getDate(list.get(list.size() - 1).timestamp);
                    chat_date.setText(date);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });



/*
        chat_listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (valuee2 < latscount) {
                } else {
                    i = 0;
                    latscount = latscount + 10;
                    list.clear();
                    adapter.notifyDataSetChanged();

                    getdatesdata();

                }


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }

        });
*/

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg = write_msg.getText().toString();

                if (msg != null && msg.length() > 0) {

                    Long tsLong = System.currentTimeMillis();
                    db.open();

                    Map<String, Object> newMessage = new HashMap<String, Object>();
                    newMessage.put("message", msg); // Message
                    newMessage.put("senderUserId", user_id);
                    newMessage.put("receiverUserId", reciver_id);
                    newMessage.put("timestamp", tsLong);
                    newMessage.put("sender", db.getRegisterData().email);
                    newMessage.put("sender_name", db.getRegisterData().first_name + " " + db.getRegisterData().last_name);
                    newMessage.put("sender_profile", db.getRegisterData().profile_picture);
                    newMessage.put("reciver_name", reciver_name);
                    newMessage.put("reciver_profile", reciver_profile);

                    db.close();

                    mychat_ref.child(datakeyname)
                            .push()
                            .setValue(newMessage)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                    write_msg.setText("");
                                    if (task.isSuccessful()) {
                                        //Toast.makeText(Chat.this, "Sent.......", Toast.LENGTH_SHORT).show();

                                        sendMessageNotification(user_id, reciver_id, msg);
                                    } else {
                                        Toast.makeText(Chat.this, "Failed.......", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(Chat.this, getString(R.string.toast_type_something), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;
    }

    private void initMember() {
        chat_listview = findViewById(R.id.chat_list);
        write_msg = findViewById(R.id.write_message);
        send_btn = findViewById(R.id.send_btn);
        chat_date = findViewById(R.id.chat_date);
    }

    private void sendMessageNotification(String user_id, String reciver_id, String msg) {
        String tag = "sendMessage";
        String url = AppConstants.url + "send_message_notification.php?" + "from_userid=" + user_id + "&to_userid=" + reciver_id + "&message=" + msg;

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
//                        Toast.makeText(Chat.this, message, Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(Chat.this, message, Toast.LENGTH_SHORT).show();

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

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}
