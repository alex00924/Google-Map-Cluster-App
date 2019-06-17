package com.rol.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rol.FriendProfile;
import com.rol.Friends;
import com.rol.Global;
import com.rol.R;
import com.rol.beans.FriendRequestBean;
import com.rol.beans.LoginBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class RequestFriends extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ListView request_listView;
    DBHelper db;
    LoginBean bean;
    List<FriendRequestBean> request_list;

    private int counter = 0;
    private View footer;
    private SwipeRefreshLayout swipe_layout;
    private boolean userScrolled = false;
    private boolean isFirstCall = true;
    private boolean is_getData = false;
    private RequestFriendsAdapter adapter;
    private List<FriendRequestBean> new_data;

    private Friends         m_ParentActivity;

    public void setParent(Friends friend)
    {
        m_ParentActivity = friend;
    }

    public RequestFriends() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list_view, container, false);

        request_listView = view.findViewById(R.id.data_list);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        request_list = new ArrayList<>();

        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);

        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);

        request_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.e("onscroll", "onScrollStateChanged");
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.e("onscroll", userScrolled + " :: " + firstVisibleItem + ":" + visibleItemCount + ":" + totalItemCount);
                int totalrandom = firstVisibleItem + visibleItemCount;

                if (firstVisibleItem == 0) {
                    swipe_layout.setEnabled(true);
                }
                if (userScrolled) {
                    if (firstVisibleItem == 0) {
                        swipe_layout.setEnabled(true);
                    } else {
                        swipe_layout.setEnabled(false);
                    }
                }

                if (userScrolled && totalrandom == totalItemCount) {
                    request_listView.addFooterView(footer);
                    userScrolled = false;
                    counter += 10;
                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        getRequestFriendData();
                    }
                } else {
                    Log.e("userScrolled", "false=");
                }
            }
        });

        if( isFirstCall )
        {
            counter = 0;
            request_list.clear();
            getRequestFriendData();
        }
        else
        {
            adapter = new RequestFriendsAdapter(getActivity(), request_list, bean.user_id, bean.auth_token, request_listView);
            request_listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && getActivity() != null) {
//            counter = 0;
//            isFirstCall = true;
//            if (AppConstants.isNetworkAvailable(getActivity())) {
//                getRequestFriendData();
//            }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (!is_getData) {
//            isFirstCall = true;
//            counter = 0;
//            getRequestFriendData();
//        }
    }

    @Override
    public void onRefresh() {
        request_listView.removeFooterView(footer);
        swipe_layout.setRefreshing(false);

//        isFirstCall = true;
//        is_getData = false;
//        counter = 0;
//        getRequestFriendData();

    }

    private void getRequestFriendData() {
        String tag = "requestFriend";
        String url = AppConstants.url + "requested_friendlist.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&start=" + counter;

        url = url.replaceAll(" ", "%20");
        Log.e("request url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    request_listView.removeFooterView(footer);
                    swipe_layout.setRefreshing(false);

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject object = jsonObject.getJSONObject("responseData");
                        JSONArray resArray = object.getJSONArray("receive_request");

                        new_data = new ArrayList<>();
                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            FriendRequestBean reqBean = new FriendRequestBean();
                            reqBean.request_id = resObject.getString("user_id");
                            reqBean.user_name = resObject.getString("user_name");
                            reqBean.user_image = resObject.getString("user_image");

                            // request_list.add(reqBean);
                            new_data.add(reqBean);
                            is_getData = true;
                        }

                        if (isFirstCall) {
                            request_list.clear();
                            for (int i = 0; i < new_data.size(); i++) {
                                request_list.add(new_data.get(i));
                            }
                            request_listView.addFooterView(footer);
                            adapter = new RequestFriendsAdapter(getActivity(), request_list, bean.user_id, bean.auth_token, request_listView);
                            request_listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            isFirstCall = false;
                        } else {
                            for (int i = 0; i < new_data.size(); i++) {
                                request_list.add(new_data.get(i));
                            }
                            adapter.notifyDataSetChanged();
                        }

                        userScrolled = true;
                        request_listView.removeFooterView(footer);
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

        AppSingleton.getInstance(getActivity()).addToRequestQueue(request, tag);
    }

    //friend request adapter

    public class RequestFriendsAdapter extends BaseAdapter {

        Activity activity;
        String user_id, auth_token;
        ListView request_listView;
        private List<FriendRequestBean> request_list;

        public RequestFriendsAdapter(Activity activity, List<FriendRequestBean> request_list, String user_id, String auth_token, ListView request_listView) {

            this.activity = activity;
            this.request_list = request_list;
            this.user_id = user_id;
            this.auth_token = auth_token;
            this.request_listView = request_listView;
        }

        @Override
        public int getCount() {
            return request_list.size();
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
            View view = inflater.inflate(R.layout.my_friends_item, null);
            LinearLayout delete_btn_layout = view.findViewById(R.id.delete_btn_layout);
            LinearLayout accept_reje_btn_layout = view.findViewById(R.id.accept_reject_layout);

            ImageView friend_profile = view.findViewById(R.id.friend_profile);
            TextView friend_name = view.findViewById(R.id.friend_name);
            TextView accept_btn = view.findViewById(R.id.accept);
            TextView reject_btn = view.findViewById(R.id.reject);

            delete_btn_layout.setVisibility(View.GONE);
            accept_reje_btn_layout.setVisibility(View.VISIBLE);

            if (request_list.get(position).user_name != null && request_list.get(position).user_name.length() > 0) {
                friend_name.setText(request_list.get(position).user_name);
            }
            if (request_list.get(position).user_image != null && request_list.get(position).user_image.length() > 0) {
                Glide.with(activity)
                        .load(request_list.get(position).user_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(friend_profile);
            } else {
                Glide.with(activity)
                        .load(R.mipmap.profile)
                        .apply(new RequestOptions().placeholder(R.mipmap.profile).error(R.mipmap.profile))
                        .into(friend_profile);
            }

            accept_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AcceptRejectRequest("accept", request_list.get(position).request_id, position);
                }
            });

            reject_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AcceptRejectRequest("reject", request_list.get(position).request_id, position);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    is_getData = false;
                    Intent intent = new Intent(activity, FriendProfile.class);
                    intent.putExtra("friend_id", request_list.get(position).request_id);
                    activity.startActivityForResult(intent, Global.FRIEND_REQUEST);
                }
            });

            return view;
        }

        private void AcceptRejectRequest(String type, final String friend_id, final int pos) {
            String tag = "friendRequest";
            String url = AppConstants.url + "accept_reject_request.php?" + "user_id=" + user_id + "&auth_token=" + auth_token + "&type=" + type + "&friend_id=" + friend_id;

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
                            adapter.notifyDataSetChanged();
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                            m_ParentActivity.m_strFriendId = friend_id;
                            if( message.contains("accept"))
                                m_ParentActivity.m_nAccept = 1;
                            else
                                m_ParentActivity.m_nAccept = 2;
                            m_ParentActivity.updateViews();

//                            refreshData();
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
    }

    public void refreshData()
    {
        counter = 0;
        request_list.clear();
        getRequestFriendData();
    }

    public void updateView(String strId, int nAccept)
    {
        if( nAccept == 0 )
            return;
        int i = 0, nCnt = request_list.size(), nPos = -1;
        for( i = 0 ; i < nCnt ; i ++ )
        {
            if( !request_list.get(i).request_id.equals(strId))
                continue;
            nPos = i;
            break;
        }
        if( nPos < 0 )
            return;
        request_list.remove(nPos);
        adapter.notifyDataSetChanged();
    }
}
