package com.rol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rol.adapter.FriendsPhotoAdapter;
import com.rol.beans.LoginBean;
import com.rol.beans.MyFriendBean;
import com.rol.fragments.CompletedTask;
import com.rol.fragments.UpcomingTask;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;
import com.rol.widgets.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendProfile extends Base {

    RecyclerView friends_photos;
    String friend_id = "", is_friend, page_name = "", is_blocked;
    DBHelper db;
    LoginBean bean;
    ImageView friend_profile;
    TextView gender, age, total_hour, about_me, total_friend_count, friend_text;
    List<MyFriendBean> friend_list;
    Button block_user_btn, remove_friend_btn, send_msg_btn, accept_btn, reject_btn, add_friend_btn, pending_btn, cancel_btn, unblock_user_btn;
    RelativeLayout upcoming_task, completed_task;
    String name, profile;
    ProgressBar progressBar;
    ViewPager viewPager;
    TextView one, two;
    TabLayout tabs;
    LinearLayout ll_friends, ll_about_me , ll_main;
    private Activity activity = this;

    CustomTextView no_data_available;

    //CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_bChanged = false;
        m_nFriends = 0;
        m_nBlockUser = 0;
        m_nAccept = 0;
        m_strFriendId = "";
        m_nRemoveFriend = 0;


        getLayoutInflater().inflate(R.layout.friend_profile, wrapper);
        base_title.setText(R.string.name);
        friends.setImageResource(R.mipmap.fri_blue);
        logout_btn.setVisibility(View.GONE);
        share_btn.setVisibility(View.VISIBLE);

        initMember();

        db = DBHelper.getInstance(this);
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        authToken = db.getRegisterData().auth_token;

        db.close();

        Intent intent = getIntent();

        if (intent.hasExtra("friend_id")) {
            friend_id = intent.getStringExtra("friend_id");
            m_strFriendId = friend_id;
            if (AppConstants.isNetworkAvailable(this)) {
                getFriendProfileData();
            }
        }

        if (intent.hasExtra("page_name")) {
            page_name = intent.getStringExtra("page_name");
        }

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("share in base", "clicked");
                final String appPackageName = activity.getPackageName();
                Intent share = new Intent(Intent.ACTION_SEND);

                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Check out the App at: https://play.google.com/store/apps/details?id=" + appPackageName);
                startActivity(Intent.createChooser(share, "Share App"));
            }
        });

        block_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    blockUser(friend_id);
                }
            }
        });

        unblock_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    UnblockUser(friend_id);
                }
            }
        });

        remove_friend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    removeFriend(friend_id);
                }
            }
        });

        send_msg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendProfile.this, Chat.class);
                intent.putExtra("reciver_id", friend_id);
                intent.putExtra("reciver_name", name);
                intent.putExtra("reciver_profile", profile);
                startActivity(intent);
            }
        });

        add_friend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    addNewFriend(friend_id);
                }
            }
        });

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    AcceptRejectRequest("accept", friend_id);
                }
            }
        });

        reject_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    AcceptRejectRequest("reject", friend_id);
                }
            }
        });
//cancel button
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppConstants.isNetworkAvailable(FriendProfile.this)) {
                    callCancelFriendRequest(bean.user_id, friend_id);
                }
            }
        });

        upcoming_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent uIntent = new Intent(FriendProfile.this, MyTaskList.class);
                uIntent.putExtra("friend_id", friend_id);
                uIntent.putExtra("page_name", "upcoming");
                startActivity(uIntent);
            }
        });

        completed_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cIntent = new Intent(FriendProfile.this, MyTaskList.class);
                cIntent.putExtra("friend_id", friend_id);
                cIntent.putExtra("page_name", "completed");
                startActivity(cIntent);
            }
        });

        setUpViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);

        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        one = view.findViewById(R.id.tab_title);
        one.setText(R.string.upcoming_task);
        one.setTextColor(getResources().getColor(R.color.base_header_color));
        tabs.getTabAt(0).setCustomView(view);

        View view1 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        two = view1.findViewById(R.id.tab_title);
        two.setText(R.string.completed_task);
        tabs.getTabAt(1).setCustomView(view1);

        viewPager.setCurrentItem(0, true);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 1) {
                    two.setTextColor(getResources().getColor(R.color.base_header_color));
                    one.setTextColor(getResources().getColor(R.color.gray));
                } else {
                    one.setTextColor(getResources().getColor(R.color.base_header_color));
                    two.setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void callCancelFriendRequest(String userId, String friend_id) {

        String tag = "cancelFriend";
        String url = AppConstants.url + "cancel_friend_req.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&friend_id=" + friend_id;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        add_friend_btn.setVisibility(View.VISIBLE);
                        cancel_btn.setVisibility(View.GONE);
                        m_bChanged = true;
                        m_nFriends = 2;
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

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    private void setUpViewPager(ViewPager viewpager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putString("id", friend_id);
        UpcomingTask fragobj = new UpcomingTask();
        fragobj.setArguments(bundle);

        adapter.addFragment(fragobj);

        CompletedTask object = new CompletedTask();
        object.setArguments(bundle);

        adapter.addFragment(object);

        viewpager.setAdapter(adapter);
        //viewPager.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    private void initMember() {

        friends_photos = findViewById(R.id.friends_list);
        friend_profile = findViewById(R.id.friend_profile);
        gender = findViewById(R.id.gender);
        age = findViewById(R.id.age_text);
        total_hour = findViewById(R.id.total_hour);
        about_me = findViewById(R.id.about_me);
        total_friend_count = findViewById(R.id.total_friend_count);
        block_user_btn = findViewById(R.id.block_user_btn);
        remove_friend_btn = findViewById(R.id.remove_friend_btn);
        send_msg_btn = findViewById(R.id.send_msg_btn);
        add_friend_btn = findViewById(R.id.add_friend);
        accept_btn = findViewById(R.id.accept_btn);
        reject_btn = findViewById(R.id.reject_btn);
        pending_btn = findViewById(R.id.pending_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        upcoming_task = findViewById(R.id.upcoming_task);
        completed_task = findViewById(R.id.completed_task);
        progressBar = findViewById(R.id.img_progress_bar);
        viewPager = findViewById(R.id.viewpager);
        tabs = findViewById(R.id.tabs);
        unblock_user_btn = findViewById(R.id.unblock_user_btn);
        friend_text = findViewById(R.id.friend_text);
        ll_friends = findViewById(R.id.ll_friends);
        ll_about_me = findViewById(R.id.ll_about_me);
        ll_main = findViewById(R.id.ll_main);
        no_data_available = findViewById(R.id.no_data_available);
    }

    private void getFriendProfileData() {
        final ProgressDialog progDialog = ProgressDialog.show(FriendProfile.this, null, null, false, true);
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progDialog.setContentView(R.layout.progress_dialog_layout);

        String tag = "friendProfile";
        String url = AppConstants.url + "friend_profile.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&friend_id=" + friend_id;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.has("status")) {
                        if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                            String message = jsonObject.getString("message");
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                            ll_main.setVisibility(View.GONE);
                            block_user_btn.setVisibility(View.GONE);
                            unblock_user_btn.setVisibility(View.VISIBLE);
                            tabs.setVisibility(View.GONE);
                            viewPager.setVisibility(View.GONE);
                            no_data_available.setVisibility(View.VISIBLE);


                        }
                    } else {


                        ll_main.setVisibility(View.VISIBLE);
                        block_user_btn.setVisibility(View.VISIBLE);
                        unblock_user_btn.setVisibility(View.GONE);
                        tabs.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.VISIBLE);
                        no_data_available.setVisibility(View.GONE);


                        JSONObject resObject = jsonObject.getJSONObject("responseData");
                        Log.e("second response", resObject.toString());

                        name = resObject.getString("username");
                        profile = resObject.getString("userimage");

                        base_title.setText(name);
                        gender.setText(resObject.getString("gender") + ",");
                        age.setText(resObject.getString("age") + " Year");
                        total_hour.setText(resObject.getString("totalhourspent"));
                        // about_me.setText(Html.fromHtml(resObject.getString("aboutme")));
                        about_me.setText(resObject.getString("aboutme"));

                        total_friend_count.setText(resObject.getString("totalfriend"));
                        is_friend = resObject.getString("is_friend");
                        if(resObject.has("is_blocked"))
                            is_blocked = resObject.getString("is_blocked");

                        Log.e("request_profile", is_friend + "");

                        String task_visible = resObject.getString("task_visible");
                        if (task_visible != null && task_visible.length() > 0 && task_visible.equalsIgnoreCase("everyone")) {

                            tabs.setVisibility(View.VISIBLE);
                            viewPager.setVisibility(View.VISIBLE);
                        } else if (task_visible != null && task_visible.length() > 0 && task_visible.equalsIgnoreCase("friend") && is_friend.equalsIgnoreCase("1")) {

                            tabs.setVisibility(View.VISIBLE);
                            viewPager.setVisibility(View.VISIBLE);
                        } else {

                            tabs.setVisibility(View.GONE);
                            viewPager.setVisibility(View.GONE);
                        }

                        String friend_visible = resObject.getString("friend_visible");
                        if (friend_visible != null && friend_visible.length() > 0 && friend_visible.equalsIgnoreCase("everyone")) {

                            ll_friends.setVisibility(View.VISIBLE);
                        } else if (friend_visible != null && friend_visible.length() > 0 && friend_visible.equalsIgnoreCase("friend") && is_friend.equalsIgnoreCase("1")) {

                            ll_friends.setVisibility(View.VISIBLE);
                        } else {

                            ll_friends.setVisibility(View.GONE);
                        }

                        String profile_info_visible = resObject.getString("profile_info_visible");
                        if (profile_info_visible != null && profile_info_visible.length() > 0 && profile_info_visible.equalsIgnoreCase("everyone")) {

                            ll_about_me.setVisibility(View.VISIBLE);
                        } else if (profile_info_visible != null && profile_info_visible.length() > 0 && profile_info_visible.equalsIgnoreCase("friend") && is_friend.equalsIgnoreCase("1")) {

                            ll_about_me.setVisibility(View.VISIBLE);
                        } else {

                            ll_about_me.setVisibility(View.GONE);
                        }


                      /*  if (is_friend.equalsIgnoreCase("0"))
                        {
                            add_friend_btn.setVisibility(View.VISIBLE);
                        }
                        if (is_friend.equalsIgnoreCase("1"))
                        {
                            remove_friend_btn.setVisibility(View.VISIBLE);
                            send_msg_btn.setVisibility(View.VISIBLE);

                        }
                        if (is_friend.equalsIgnoreCase("2"))
                        {
                            if(page_name.equalsIgnoreCase("discover"))
                            {
                                pending_btn.setVisibility(View.VISIBLE);

                            }
                            else
                            {
                                accept_btn.setVisibility(View.VISIBLE);
                                reject_btn.setVisibility(View.VISIBLE);
                            }
                        }*/

                        if (is_blocked!=null && is_blocked.length()> 0 && is_blocked.equalsIgnoreCase("1")) {
                            block_user_btn.setVisibility(View.GONE);
                            unblock_user_btn.setVisibility(View.VISIBLE);
                            add_friend_btn.setVisibility(View.GONE);
                            remove_friend_btn.setVisibility(View.GONE);
                            send_msg_btn.setVisibility(View.GONE);
                            accept_btn.setVisibility(View.GONE);
                            reject_btn.setVisibility(View.GONE);
                        } else {
                            if (is_friend.equalsIgnoreCase("0")) {
                                add_friend_btn.setVisibility(View.VISIBLE);
                            }
                            if (is_friend.equalsIgnoreCase("1")) {
                                remove_friend_btn.setVisibility(View.VISIBLE);
                                send_msg_btn.setVisibility(View.VISIBLE);
                            }
                            if (is_friend.equalsIgnoreCase("2")) {

                                /*if(page_name.equalsIgnoreCase("discover"))
                                {

                                    pending_btn.setVisibility(View.VISIBLE);

                                }
                                else
                                {
                                    accept_btn.setVisibility(View.VISIBLE);
                                    reject_btn.setVisibility(View.VISIBLE);
                                }*/

                                String friend_request_by = resObject.getString("friend_request_by");
                                if (friend_request_by != null && friend_request_by.length() > 0 && friend_request_by.equalsIgnoreCase(bean.user_id)) {

                                    cancel_btn.setVisibility(View.VISIBLE);
                                } else {

                                    accept_btn.setVisibility(View.VISIBLE);
                                    reject_btn.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        if (is_friend.equalsIgnoreCase("3")) {
                            block_user_btn.setVisibility(View.GONE);
                        }

                        Glide.with(activity)
                                .load(profile)
                                .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                                .into(friend_profile);

                        JSONArray friend_array = resObject.getJSONArray("friendlist");
                        friend_list = new ArrayList<>();

                        for (int i = 0; i < friend_array.length(); i++) {

                            if (friend_array.getJSONObject(i).has("status")) {
                                if (friend_array.getJSONObject(i).getString("status").equalsIgnoreCase("0")) {
                                    Toast.makeText(activity, friend_array.getJSONObject(i).getString("message"), Toast.LENGTH_SHORT).show();
                                    friend_text.setVisibility(View.GONE);
                                    total_friend_count.setText("You don't have any friend");
                                }
                            } else {
                                friend_text.setVisibility(View.VISIBLE);

                                MyFriendBean f_bean = new MyFriendBean();
                                JSONObject friendObject = friend_array.getJSONObject(i);
                                Log.e("friendObject", friendObject.toString() + "===");

                                f_bean.friend_id = friendObject.getString("friend_id");
                                f_bean.friend_name = friendObject.getString("friend_name");
                                f_bean.friend_image = friendObject.getString("friend_image");
                                Log.e("friend_name", f_bean.friend_name + "");

                                friend_list.add(f_bean);
                            }
                        }

                        Log.e("friend_list", friend_list.size() + "===");

                        if (friend_list != null && friend_list.size() > 0) {
                            friends_photos.setVisibility(View.VISIBLE);
                            FriendsPhotoAdapter adapter = new FriendsPhotoAdapter(FriendProfile.this, friend_list);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                            friends_photos.setLayoutManager(linearLayoutManager);
                            friends_photos.setAdapter(adapter);
                        } else {
                            friends_photos.setVisibility(View.GONE);
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

                    }
                });

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    private void blockUser(String block_id) {
        String tag = "blockUser";
        String url = AppConstants.url + "blockuser.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&blockuserid=" + block_id;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        // Intent intent = new Intent(FriendProfile.this, Friends.class);
                        // startActivity(intent);
                        // finish();
                        block_user_btn.setVisibility(View.GONE);
                        unblock_user_btn.setVisibility(View.VISIBLE);
                        getFriendProfileData();
                        m_nBlockUser = 1;
                        m_bChanged = true;
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

    //block user

    //unblock user
    private void UnblockUser(String block_id) {
        String tag = "unblockUser";
        String url = AppConstants.url + "unblockuser.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&blockuserid=" + block_id;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        // Intent intent = new Intent(FriendProfile.this, Friends.class);
                        //   startActivity(intent);
                        //  finish();
                        unblock_user_btn.setVisibility(View.GONE);
                        block_user_btn.setVisibility(View.VISIBLE);
                        getFriendProfileData();
                        m_nBlockUser = 2;
                        m_bChanged = true;
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

    private void removeFriend(String friend_id) {
        String tag = "removeFriend";
        String url = AppConstants.url + "remove_friend.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&friend_id=" + friend_id;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        /*Intent intent = new Intent(FriendProfile.this, Friends.class);
                        startActivity(intent);
                        finish();*/
                        remove_friend_btn.setVisibility(View.GONE);
                        send_msg_btn.setVisibility(View.GONE);
                        getFriendProfileData();

                        m_bChanged = true;
                        m_nRemoveFriend = 1;
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

    //remove friend

    private void addNewFriend(String friendId) {
        String tag = "addFriend";
        String url = AppConstants.url + "add_friend.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&friend_id=" + friendId;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        add_friend_btn.setVisibility(View.GONE);
                        /*pending_btn.setVisibility(View.VISIBLE);*/
                        cancel_btn.setVisibility(View.VISIBLE);
                        m_bChanged = true;
                        m_nFriends = 1;
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

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    //add friend

    private void AcceptRejectRequest(final String type, String friend_id) {
        String tag = "friendRequest";
        String url = AppConstants.url + "accept_reject_request.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&type=" + type + "&friend_id=" + friend_id;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        if (type.equalsIgnoreCase("accept")) {
                            accept_btn.setVisibility(View.GONE);
                            reject_btn.setVisibility(View.GONE);
                            remove_friend_btn.setVisibility(View.VISIBLE);
                            send_msg_btn.setVisibility(View.VISIBLE);
                            m_bChanged = true;
                            m_nAccept = 1;
                        }
                        else {
                            m_bChanged = true;
                            m_nAccept = 2;
//                            Intent intent = new Intent(FriendProfile.this, Friends.class);
//                            startActivity(intent);
//                            finish();
                        }
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    //accept reject friend request

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }
    }
}