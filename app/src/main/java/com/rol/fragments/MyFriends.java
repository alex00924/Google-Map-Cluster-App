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

/**
 * Created by keshav on 3/7/18.
 */

public class MyFriends extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ListView friend_listView;
    DBHelper db;
    LoginBean bean;
    List<MyFriendBean> myfriend_list;
    MyFriendsAdapter adapter;
    private int counter = 0;
    private View footer;
    private SwipeRefreshLayout swipe_layout;
    private boolean userScrolled = false;
    private boolean isFirstCall = true;
    private boolean is_getData = false;
    private List<MyFriendBean> new_data;

    public MyFriends() {

    }

    private Friends m_ParentActivity;

    public void setParent(Friends friend)
    {
        m_ParentActivity = friend;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list_view, container, false);

        friend_listView = view.findViewById(R.id.data_list);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        myfriend_list = new ArrayList<>();

        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);

        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);

        friend_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.e("Position", firstVisibleItem + "-----");

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

                if (userScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {
                    friend_listView.addFooterView(footer);
                    userScrolled = false;
                    counter += 10;
                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        getMyfriend();
                    }
                }
            }
        });

        if( isFirstCall)
        {
            counter = 0;
            myfriend_list.clear();
            getMyfriend();
        }
        else
        {
            adapter = new MyFriendsAdapter(getActivity(), myfriend_list, bean.user_id, bean.auth_token);
            friend_listView.setAdapter(adapter);
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
//                getMyfriend();
//            }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (!is_getData) {
//            isFirstCall = true;
//            counter = 0;
//            getMyfriend();
//        }
    }

    @Override
    public void onRefresh() {
        friend_listView.removeFooterView(footer);
        swipe_layout.setRefreshing(false);


//        isFirstCall = true;
//        is_getData = false;
//        counter = 0;
//        getMyfriend();

    }

    private void getMyfriend() {
        String tag = "myFriends";
        String url = AppConstants.url + "friend_list.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&start=" + counter;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                friend_listView.removeFooterView(footer);
                swipe_layout.setRefreshing(false);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.has("status")) {
                        if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                            String message = jsonObject.getString("message");
//                            if( getActivity() != null )
//                                Toast.makeText(getActivity(), message + "", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        new_data = new ArrayList<>();
                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            MyFriendBean fBean = new MyFriendBean();
                            fBean.friend_id = resObject.getString("id");
                            fBean.friend_name = resObject.getString("friend_name");
                            fBean.friend_image = resObject.getString("friend_image");

                            // myfriend_list.add(fBean);
                            new_data.add(fBean);
                        }
                        is_getData = true;

                        if (isFirstCall) {
                            myfriend_list.clear();
                            for (int i = 0; i < new_data.size(); i++) {
                                myfriend_list.add(new_data.get(i));
                            }
                            friend_listView.addFooterView(footer);
                            adapter = new MyFriendsAdapter(getActivity(), myfriend_list, bean.user_id, bean.auth_token);
                            friend_listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            isFirstCall = false;
                        } else {
                            for (int i = 0; i < new_data.size(); i++) {
                                myfriend_list.add(new_data.get(i));
                            }
                            adapter.notifyDataSetChanged();
                        }

                        userScrolled = true;
                        friend_listView.removeFooterView(footer);
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

    //friend adapter

    public class MyFriendsAdapter extends BaseAdapter {

        Activity activity;
        List<MyFriendBean> myfriend_list;
        String userId, authToken;

        public MyFriendsAdapter(Activity activity, List<MyFriendBean> myfriend_list, String userId, String authToken) {
            this.activity = activity;
            this.myfriend_list = myfriend_list;
            this.userId = userId;
            this.authToken = authToken;
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
            View view = inflater.inflate(R.layout.my_friends_item, null);

            ImageView friend_profile = view.findViewById(R.id.friend_profile);
            TextView friend_name = view.findViewById(R.id.friend_name);
            LinearLayout delete_layout = view.findViewById(R.id.delete_btn_layout);

            delete_layout.setVisibility(View.VISIBLE);

            if (myfriend_list.get(position).friend_name != null && myfriend_list.get(position).friend_name.length() > 0) {
                friend_name.setText(myfriend_list.get(position).friend_name);
            }
            if (myfriend_list.get(position).friend_image != null && myfriend_list.get(position).friend_image.length() > 0) {
                Glide.with(activity)
                        .load(myfriend_list.get(position).friend_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(friend_profile);
            } else {
                Glide.with(activity)
                        .load(R.mipmap.profile)
                        .apply(new RequestOptions().placeholder(R.mipmap.profile).error(R.mipmap.profile))
                        .into(friend_profile);
            }

            delete_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    removeFriend(myfriend_list.get(position).friend_id, position);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    is_getData = false;
                    Intent intent = new Intent(activity, FriendProfile.class);
                    intent.putExtra("friend_id", myfriend_list.get(position).friend_id);
                    //intent.putExtra("is_friend","1");
                    activity.startActivityForResult(intent, Global.FRIEND_MY);
                }
            });

            return view;
        }

        private void removeFriend(final String friend_id, final int pos) {
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

                            m_ParentActivity.m_nRemoveFriend = 1;
                            m_ParentActivity.m_strFriendId = friend_id;
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
        myfriend_list.clear();
        getMyfriend();
    }
    public void updateView(String strId, int nRemove, int nAccept)
    {
        if( nAccept == 1 )
        {
            refreshData();
            return;
        }

        if( nRemove != 1 )
            return;
        int i = 0, nCnt = myfriend_list.size(), nPos = -1;
        for( i = 0 ; i < nCnt ; i ++ )
        {
            if( !myfriend_list.get(i).friend_id.equals(strId))
                continue;
            nPos = i;
            break;
        }
        if( nPos < 0 )
            return;
        myfriend_list.remove(nPos);
        adapter.notifyDataSetChanged();
    }
}
