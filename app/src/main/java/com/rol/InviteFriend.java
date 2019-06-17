package com.rol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.beans.LoginBean;
import com.rol.beans.MyFriendBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InviteFriend extends Base {

    ListView frd_listView;
    DBHelper db;
    LoginBean bean;
    List<MyFriendBean> list;
    Button send_btn;
    String friends_id = "", page_type, taskId;
    CheckBox select_all_btn;
    List<String> friendId_list;
    List<MyFriendBean> myfriend_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.invite_friend, wrapper);
        base_title.setText(R.string.invite_friend);
        friends.setImageResource(R.mipmap.fri_blue);
        logout_btn.setVisibility(View.GONE);

        frd_listView = findViewById(R.id.friend_list);
        send_btn = findViewById(R.id.send_btn);
        select_all_btn = findViewById(R.id.select_all_btn);

        db = DBHelper.getInstance(this);
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        if (AppConstants.isNetworkAvailable(InviteFriend.this)) {
            getMyfriend();
        }

        final Intent intent = getIntent();
        if (intent.hasExtra("page_name")) {
            page_type = intent.getStringExtra("page_name");
        }
        if (intent.hasExtra("task_id")) {
            taskId = intent.getStringExtra("task_id");
        }

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < frd_listView.getChildCount(); i++) {
                    RelativeLayout itemLayout = (RelativeLayout) frd_listView.getChildAt(i);
                    CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.select_frd_btn);
                    if (cb.isChecked()) {
                        friends_id = friends_id + "," + myfriend_list.get(i).friend_id;
                    }
                }

                if (page_type.equalsIgnoreCase("task_detail")) {
                    if (friends_id != null && friends_id.length() > 0) {
                        InviteFriend(friends_id);
                    } else {
                        Toast.makeText(InviteFriend.this, activity.getString(R.string.select_one_frd), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("friends_id", friends_id);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        select_all_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    for (int i = 0; i < frd_listView.getChildCount(); i++) {
                        RelativeLayout itemLayout = (RelativeLayout) frd_listView.getChildAt(i);
                        CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.select_frd_btn);
                        cb.setChecked(true);
                    }
                } else {
                    for (int i = 0; i < frd_listView.getChildCount(); i++) {
                        RelativeLayout itemLayout = (RelativeLayout) frd_listView.getChildAt(i);
                        CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.select_frd_btn);
                        cb.setChecked(false);
                    }
                }
            }
        });
    }

    private void getMyfriend() {
        String tag = "myFriends";
        String url = AppConstants.url + "friend_list.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.has("status")) {
                        if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                            String message = jsonObject.getString("message");
                            Toast.makeText(InviteFriend.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        list = new ArrayList<>();
                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);

                            MyFriendBean fBean = new MyFriendBean();
                            fBean.friend_id = resObject.getString("id");
                            fBean.friend_name = resObject.getString("friend_name");
                            fBean.friend_image = resObject.getString("friend_image");

                            list.add(fBean);
                        }
                        InviteFriendAdapter adapter = new InviteFriendAdapter(InviteFriend.this, list);
                        frd_listView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }

    private void InviteFriend(String ids) {
        String tag = "inviteFriends";

        ids = ids.substring(1, ids.length());

        String url = AppConstants.url + "invite_friend.php?" + "task_id=" + taskId + "&user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&friend_id=" + ids;

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
                        Toast.makeText(InviteFriend.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(InviteFriend.this, "You invited your friends successfully to this task!", Toast.LENGTH_SHORT).show();
                        friendId_list.clear();
                        friends_id = "";
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }

    //invite friend adapter

    public class InviteFriendAdapter extends BaseAdapter {

        Activity activity;

        public InviteFriendAdapter(Activity activity, List<MyFriendBean> data_list) {
            this.activity = activity;
            myfriend_list = data_list;
            friendId_list = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return myfriend_list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(R.layout.invite_friend_item, null);

            TextView friend_name = view.findViewById(R.id.friend_name);
            final CheckBox select_frd_btn = view.findViewById(R.id.select_frd_btn);

            if (myfriend_list.get(position).friend_name != null && myfriend_list.get(position).friend_name.length() > 0) {
                friend_name.setText(myfriend_list.get(position).friend_name);
            }

            select_frd_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });

            return view;
        }
    }
}
