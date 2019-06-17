package com.rol.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.Base;
import com.rol.R;
import com.rol.adapter.NotificationAdapter;
import com.rol.beans.LoginBean;
import com.rol.beans.NotificationBean;
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

public class NotificationList extends Fragment {

    ListView noti_listview;
    DBHelper db;
    LoginBean bean;
    List<NotificationBean> notification_list;
    private boolean is_dataLoad = false;

    public NotificationList() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_list, container, false);

        noti_listview = view.findViewById(R.id.message_list);

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        if (AppConstants.isNetworkAvailable(getActivity())) {
            getNotificationList();
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();

          /*  if(AppConstants.isNetworkAvailable(getActivity()))
            {
                getNotificationList();
            }
*/

        }
    }

    @Override
    public void onResume() {
        super.onResume();

       /* if(!is_dataLoad)
        {

            if(AppConstants.isNetworkAvailable(getActivity()))
            {
                getNotificationList();
            }

        }*/
    }

    private void getNotificationList() {
        String tag = "notificationList";

        String url = AppConstants.url + "notification_list.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token;

        // String url = AppConstants.url + "notification_list.php?" + "user_id=" +2+ "&auth_token="+"12f8a9f4dab2PM603987db2a9536fb0c";

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responsedata");
                        notification_list = new ArrayList<>();

                        for (int i = 0; i < resArray.length(); i++) {
                            NotificationBean nBean = new NotificationBean();
                            JSONObject resObject = resArray.getJSONObject(i);
                            nBean.message = resObject.getString("message");
                            nBean.user_image = resObject.getString("userimage");
                            nBean.userid = resObject.getString("userid");
                            nBean.type = resObject.getString("type");
                            nBean.notification_id = resObject.getString("id");

                            if (resObject.has("task_id")) {
                                nBean.task_id = resObject.getString("task_id");
                            }

                            if (resObject.has("sender_id")) {
                                nBean.sender_id = resObject.getString("sender_id");
                            }
                            if (resObject.has("sender_name")) {
                                nBean.sender_name = resObject.getString("sender_name");
                            }
                            notification_list.add(nBean);
                        }

                        is_dataLoad = true;

                        //  Log.e("noti list size",notification_list.size()+"");
                        NotificationAdapter adapter = new NotificationAdapter(getActivity(), notification_list, bean.user_id, bean.auth_token);
                        noti_listview.setAdapter(adapter);
                        Base.notification_count.setVisibility(View.GONE);

                        if (notification_list.size() > 0 && notification_list != null) {
                            int j = notification_list.size() - Base.noti_counter - 1;
                            if (j == -1) {
                                NotificationReadService(notification_list.get(0).notification_id);
                            } else {
                                //int i=notification_list.size()-Base.noti_counter;

                                for (int i = notification_list.size() - Base.noti_counter - 1; i < notification_list.size(); i++) {
                                    Log.e("in for loop", i + "");
                                    NotificationReadService(notification_list.get(i).notification_id);
                                }
                            }
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

        AppSingleton.getInstance(getActivity()).addToRequestQueue(request, tag);
    }

    private void NotificationReadService(String notification_id) {
        String tag = "notification";
        String url = AppConstants.url + "is_read_notification.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&message_id=" + notification_id;

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
                        //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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

        AppSingleton.getInstance(getActivity()).addToRequestQueue(request, tag);
    }
}
