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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.rol.beans.DiscoverFriendBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;
import com.rol.widgets.CustomEditText;
import com.rol.widgets.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by keshav on 3/7/18.
 */

public class DiscoverFriends extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ListView friend_listView;
    CustomEditText search;
    DBHelper db;
    String userId = "", authToken = "", search_text = "";
    ImageView clearSearch;
    LinearLayout friend_layout;
    Button invite_friend;
    DiscoverFriendAdapter adapter;
    private List<DiscoverFriendBean> discoverFriendList;
    private SwipeRefreshLayout swipe_layout;
    private boolean userScrolled = false;
    private boolean isFirstCall = true;
    private boolean is_Data = false;
    private View footer;
    private int counter = 0;

    Friends         m_ParentActivity;

    public void setParent(Friends friend)
    {
        m_ParentActivity = friend;
    }
    public DiscoverFriends() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.discover_friends, container, false);

        friend_listView = view.findViewById(R.id.discover_friend_list);
        search = view.findViewById(R.id.search_friend);
        clearSearch = view.findViewById(R.id.clear_search);
        friend_layout = view.findViewById(R.id.invite_frd_layout);
        invite_friend = view.findViewById(R.id.invite_frds_btn);

        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);

        discoverFriendList = new ArrayList<>();

        db = DBHelper.getInstance(getActivity());
        db.open();
        userId = db.getRegisterData().user_id;
        authToken = db.getRegisterData().auth_token;
        db.close();

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
                        getDiscoverUserList(search_text);
                    }
                }
            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setText("");
                if (AppConstants.isNetworkAvailable(getActivity())) {
                    search_text = "";
                    is_Data = false;
                    isFirstCall = true;
                    counter = 0;
                    getDiscoverUserList(search_text);
                }
            }
        });

//        search.addTextChangedListener(new TextWatcher()
//        {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                if (AppConstants.isNetworkAvailable(getActivity()))
//                {
//                    if(charSequence.toString() != null && charSequence.toString().length()>0)
//                    {
//                        search_text=charSequence.toString();
//                        is_Data=false;
//                        counter=0;
//                        getDiscoverUserList(search_text);
//                    }
//
//
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e("keyboard", actionId + "==== action :: " + EditorInfo.IME_ACTION_DONE + "====");

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.e("Discover search", actionId + "====");

                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        search_text = search.getText().toString();
                        if (search_text != null && search_text.length() > 0) {
                            Log.e("search_text", search_text + "");
                            is_Data = false;
                            counter = 0;
                            getDiscoverUserList(search_text);
                        }
                    }

                    return true;
                }
                return false;
            }
        });
        invite_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appPackageName = getActivity().getPackageName();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://rolapplication.page.link/nM9x");
                sendIntent.setType("text/plain");
                getActivity().startActivity(sendIntent);
            }
        });

        if( isFirstCall ) {
            counter = 0;
            discoverFriendList.clear();
            getDiscoverUserList(search_text);
        }
        else
        {
            adapter = new DiscoverFriendAdapter(getActivity(), discoverFriendList, userId, authToken);
            friend_listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
/*
        if (isVisibleToUser && getActivity() != null) {
            counter = 0;
            isFirstCall = true;
            if (AppConstants.isNetworkAvailable(getActivity())) {
                Log.e("in user", "visible method");
                getDiscoverUserList(search_text);
            }
        }
*/
    }

    @Override
    public void onResume() {
        super.onResume();
//
//        if (!is_Data) {
//            isFirstCall = true;
//            counter = 0;
//            search_text = "";
//            Log.e("in", "onresume method");
//            if (AppConstants.isNetworkAvailable(getActivity())) {
//                getDiscoverUserList(search_text);
//            }
//        }

    }

    @Override
    public void onRefresh() {
        friend_listView.removeFooterView(footer);
        swipe_layout.setRefreshing(false);

//
//        isFirstCall = true;
//        is_Data = false;
//        counter = 0;
//        Log.e("in", "onrefresh method");
//
//        if (AppConstants.isNetworkAvailable(getActivity())) {
//            getDiscoverUserList(search_text);
//        }

    }

    private void getDiscoverUserList(String query) {

        String tag = "discoverUser";
        String url = AppConstants.url + "discover_user.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&search_text=" + query + "&start=" + counter;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.getString("status").equalsIgnoreCase("0") && counter <= 0) {
                        String message = jsonObject.getString("message");
                        if (message.equalsIgnoreCase("data not found")) {
                            friend_listView.setVisibility(View.GONE);
                            discoverFriendList.clear();
                            friend_layout.setVisibility(View.VISIBLE);
                            userScrolled = false;
                        }
                    } else {

                        friend_listView.removeFooterView(footer);
                        swipe_layout.setRefreshing(false);

                        friend_listView.setVisibility(View.VISIBLE);
                        friend_layout.setVisibility(View.GONE);

                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            DiscoverFriendBean bean = new DiscoverFriendBean();
                            bean.user_id = resObject.getString("user_id");
                            bean.user_name = resObject.getString("user_name");
                            bean.user_image = resObject.getString("user_image");
                            bean.is_friend = resObject.getString("is_friend");
                            bean.friend_request_by = resObject.getString("friend_request_by");
                            discoverFriendList.add(bean);
                        }
                        is_Data = true;

                        if (isFirstCall) {
                            friend_listView.addFooterView(footer);
                            if (discoverFriendList != null && discoverFriendList.size() > 0) {
                                Log.e("friend list size ", discoverFriendList.size() + "");
                                adapter = new DiscoverFriendAdapter(getActivity(), discoverFriendList, userId, authToken);
                                friend_listView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                isFirstCall = false;
                            } else {

                                Log.e("friend list size null", "");
                                friend_listView.setVisibility(View.GONE);
                                discoverFriendList.clear();
                                friend_layout.setVisibility(View.VISIBLE);
                            }
                        } else {
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

    //discover friend adapter

    public class DiscoverFriendAdapter extends BaseAdapter {

        Activity activity;

        List<DiscoverFriendBean> list;
        CircleImageView frdProfile;
        CustomTextView frdName, pending, friend, cancel, accept, reject;
        LinearLayout addFriend, accept_reject_layout;
        String userId, friendId = "", auth_token;

        public DiscoverFriendAdapter(Activity activity, List<DiscoverFriendBean> list, String userId, String auth_token) {
            this.activity = activity;
            this.list = list;
            this.userId = userId;
            this.auth_token = auth_token;

            Log.e("list size in adp", list.size() + "");
        }

        @Override
        public int getCount() {
            return list.size();
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
            final LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(R.layout.discover_friend_item, null);

            final DiscoverFriendBean bean = list.get(position);
            frdProfile = view.findViewById(R.id.friend_profile);
            frdName = view.findViewById(R.id.friend_name);
            addFriend = view.findViewById(R.id.add_btn_layout); //+ Add
            pending = view.findViewById(R.id.pending_text);     //Pending
            cancel = view.findViewById(R.id.cancel_text);       // Cancel Request
            friend = view.findViewById(R.id.friend_text);       //Friend
            accept_reject_layout = view.findViewById(R.id.accept_reject_layout);        //Accept , Reject
            accept = view.findViewById(R.id.accept);            //Accept
            reject = view.findViewById(R.id.reject);            //Reject

            if (bean.user_name != null && bean.user_name.length() > 0)
                frdName.setText(bean.user_name);

            if (bean.user_image != null && bean.user_image.length() > 0) {
                Glide.with(activity)
                        .load(bean.user_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(frdProfile);
            } else {
                Glide.with(activity)
                        .load(R.mipmap.profile)
                        .apply(new RequestOptions().placeholder(R.mipmap.profile).error(R.mipmap.profile))
                        .into(frdProfile);
            }

            if (list.get(position).is_friend.equalsIgnoreCase("2")) {

                addFriend.setVisibility(View.GONE);

                if (list.get(position).friend_request_by != null && list.get(position).friend_request_by.length() > 0
                        && list.get(position).friend_request_by.equalsIgnoreCase(userId)) {

                    cancel.setVisibility(View.VISIBLE);
                } else {

                    accept_reject_layout.setVisibility(View.VISIBLE);
                }

            } else if (list.get(position).is_friend.equalsIgnoreCase("1")) {
                addFriend.setVisibility(View.GONE);
                friend.setVisibility(View.VISIBLE);
            }

            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppConstants.isNetworkAvailable(activity)) {
                        addNewFriend(list.get(position).user_id, position);
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppConstants.isNetworkAvailable(activity)) {
                        callCancelFriendRequest(activity, userId, list.get(position).user_id, position);
                    }
                }
            });

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppConstants.isNetworkAvailable(activity)) {
                        callCancelFriendRequest(activity, userId, list.get(position).user_id, position);
                        AcceptRejectRequest(activity, "accept", list.get(position).user_id, position);
                    }
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppConstants.isNetworkAvailable(activity)) {
                        AcceptRejectRequest(activity, "reject", list.get(position).user_id, position);
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    is_Data = false;
                    Intent intent = new Intent(activity, FriendProfile.class);
                    intent.putExtra("friend_id", list.get(position).user_id);
                    intent.putExtra("page_name", "discover");
                    activity.startActivityForResult(intent, Global.FRIEND_DISCOVER);
                }
            });

            return view;
        }

        private void addNewFriend(final String friendId, final int nPos) {
            String tag = "addFriend";
            String url = AppConstants.url + "add_friend.php?" + "user_id=" + userId + "&auth_token=" + auth_token + "&friend_id=" + friendId;

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
                            m_ParentActivity.m_strFriendId = friendId;
                            m_ParentActivity.m_nFriends = 1;
                            m_ParentActivity.updateViews();
/*
                            //Cancel Request
                            discoverFriendList.get(nPos).is_friend = "2";
                            discoverFriendList.get(nPos).friend_request_by = userId;

                            int start = friend_listView.getFirstVisiblePosition();
                            if( start > nPos )
                                return;
                            View view = friend_listView.getChildAt(nPos-start);
                            friend_listView.getAdapter().getView(nPos, view, friend_listView);

                            view.findViewById(R.id.add_btn_layout).setVisibility(View.GONE);
                            view.findViewById(R.id.cancel_text).setVisibility(View.VISIBLE);
*/


/*
                            counter = 0;
                            isFirstCall = true;
                            is_Data = false;
                            onResume();
*/
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

        private void callCancelFriendRequest(final Activity activity, final String userId, final String friend_id, final int nPos) {

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

                            m_ParentActivity.m_strFriendId = friend_id;
                            m_ParentActivity.m_nFriends = 2;
                            m_ParentActivity.updateViews();
/*
                            //+ Add
                            //Cancel Request
                            discoverFriendList.get(nPos).is_friend = "0";


                            int start = friend_listView.getFirstVisiblePosition();
                            if( start > nPos )
                                return;
                            View view = friend_listView.getChildAt(nPos-start);
                            friend_listView.getAdapter().getView(nPos, view, friend_listView);
                            view.findViewById(R.id.add_btn_layout).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.cancel_text).setVisibility(View.GONE);
*/

/*
                            counter = 0;
                            isFirstCall = true;
                            is_Data = false;
                            onResume();
*/
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

        private void AcceptRejectRequest(final Activity activity, String type, final String friend_id, final int pos) {
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
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                            m_ParentActivity.m_strFriendId = friend_id;
                            if( message.contains("accept") )
                                m_ParentActivity.m_nAccept = 1;
                            else
                                m_ParentActivity.m_nAccept = 2;
                            m_ParentActivity.updateViews();

                            /*request_list.remove(pos);*/
/*
                            adapter.notifyDataSetChanged();
                            counter = 0;
                            isFirstCall = true;
                            is_Data = false;
                            onResume();
*/
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
        search_text = "";
        discoverFriendList.clear();
        getDiscoverUserList(search_text);
    }

    public void updateView(String strId,  int nFriend, int nBlock, int nAccept, int nRemove)
    {
        int i = 0, nCnt = discoverFriendList.size(), nPos = -1;
        for( i = 0 ; i < nCnt ; i ++ )
        {
            if( !discoverFriendList.get(i).user_id.equals(strId))
                continue;
            nPos = i;
            break;
        }
        if( nPos < 0 )
            return;

        if( nBlock == 1 )
        {
            discoverFriendList.remove(nPos);
            adapter.notifyDataSetChanged();
            return;
        }

        int start = friend_listView.getFirstVisiblePosition();
        if( start > nPos )
            return;
        View view = friend_listView.getChildAt(nPos-start);
        friend_listView.getAdapter().getView(nPos, view, friend_listView);

        if( nRemove == 1 )          //Remove Friend
        {
            discoverFriendList.get(nPos).is_friend = "0";
            discoverFriendList.get(nPos).friend_request_by = "";
            if( view != null ) {
                view.findViewById(R.id.add_btn_layout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cancel_text).setVisibility(View.GONE);
                view.findViewById(R.id.friend_text).setVisibility(View.GONE);
            }
        }

        if( nFriend == 1)           //Request Friend -> Cancel
        {
            discoverFriendList.get(nPos).is_friend = "2";
            discoverFriendList.get(nPos).friend_request_by = userId;
            if( view != null ) {
                view.findViewById(R.id.add_btn_layout).setVisibility(View.GONE);
                view.findViewById(R.id.cancel_text).setVisibility(View.VISIBLE);
            }
        }
        else if( nFriend == 2 )     //No Friend -> Add
        {
            discoverFriendList.get(nPos).is_friend = "0";
            discoverFriendList.get(nPos).friend_request_by = "";
            if( view != null ) {
                view.findViewById(R.id.add_btn_layout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cancel_text).setVisibility(View.GONE);
            }
        }

        if( nAccept == 1 )
        {
            discoverFriendList.get(nPos).is_friend = "1";
            discoverFriendList.get(nPos).friend_request_by = discoverFriendList.get(nPos).user_id;
            if( view != null ) {
                view.findViewById(R.id.add_btn_layout).setVisibility(View.GONE);
                view.findViewById(R.id.cancel_text).setVisibility(View.GONE);
                view.findViewById(R.id.accept_reject_layout).setVisibility(View.GONE);
                view.findViewById(R.id.friend_text).setVisibility(View.VISIBLE);
            }
        }
        if( nAccept == 2 )
        {
            discoverFriendList.get(nPos).is_friend = "0";
            discoverFriendList.get(nPos).friend_request_by = "";
            if( view != null ) {
                view.findViewById(R.id.add_btn_layout).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cancel_text).setVisibility(View.GONE);
                view.findViewById(R.id.accept_reject_layout).setVisibility(View.GONE);
            }
        }
    }
}
