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
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rol.R;
import com.rol.TaskDetail;
import com.rol.beans.LoginBean;
import com.rol.beans.UpcomingTaskBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;
import com.rol.utils.TrackGPS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class CompletedTask extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ListView listView;
    DBHelper db;
    LoginBean bean;
    List<UpcomingTaskBean> completed_task_list;
    String id = "";
    boolean isFirstCall = true;
    View footer;
    CompletedTaskAdapter adapter;
    private boolean userScrolled = false;
    private int counter = 0;
    private boolean is_data = false;
    private SwipeRefreshLayout swipe_layout;
    private TrackGPS trackGPS;
    private double lat, lon;

    public CompletedTask() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view, container, false);

        listView = view.findViewById(R.id.data_list);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);
        listView.setFocusable(false);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);
        completed_task_list = new ArrayList<>();

        if (getArguments() != null) {
            id = getArguments().getString("id");
        }

        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.e("onscroll", "onScrollStateChanged");
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                    //   userScrolled = true;
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
                    Log.e("Position", firstVisibleItem + "-----");
                    if (firstVisibleItem == 0) {
                        swipe_layout.setEnabled(true);
                    } else {
                        swipe_layout.setEnabled(false);
                    }
                }

                if (userScrolled && totalrandom == totalItemCount) {
                    listView.addFooterView(footer);
                    userScrolled = false;
                    counter += 10;
                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        getCompletedTaskData();
                    }
                } else {
                    Log.e("userScrolled", "false=");
                }
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            counter = 0;
            isFirstCall = true;
            is_data = false;

            if (lat != 0.0 && lon != 0.0) {
                //completed_task_list.clear();
                getCompletedTaskData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("on resume", "method");

        if (!is_data) {
            isFirstCall = true;
            counter = 0;
            getCompletedTaskData();
        }
    }

    @Override
    public void onRefresh() {
        isFirstCall = true;
        is_data = false;
        counter = 0;
        getCompletedTaskData();
    }

    private void getCompletedTaskData() {
        String tag = "CompletedtaskList";
        String url;

        if (id != null && id.length() > 0) {
            url = AppConstants.url + "my_task.php?" + "user_id=" + bean.user_id + "&type=" + "completed" + "&auth_token=" + bean.auth_token + "&friend_id=" + id + "&start=" + counter + "&latitude=" + lat + "&longitude=" + lon;
        } else {
            url = AppConstants.url + "my_task.php?" + "user_id=" + bean.user_id + "&type=" + "completed" + "&auth_token=" + bean.auth_token + "&start=" + counter + "&latitude=" + lat + "&longitude=" + lon;
        }

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    listView.removeFooterView(footer);
                    swipe_layout.setRefreshing(false);

                    if (jsonObject.has("status")) {
                        String message = jsonObject.getString("message");
                        //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {

                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        if (!is_data) {
                            completed_task_list.clear();
                        }

                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            Log.e("second response", resObject + "");

                            UpcomingTaskBean task_bean = new UpcomingTaskBean();
                            task_bean.task_id = resObject.getString("task_id");
                            task_bean.organiser_id = resObject.getString("organiserid");
                            task_bean.organiser_name = resObject.getString("organisername");
                            task_bean.organiser_image = resObject.getString("organiserimage");
                            task_bean.task_name = resObject.getString("taskname");
                            task_bean.task_image = resObject.getString("image");
                            task_bean.location = resObject.getString("location");
                            task_bean.created_date = resObject.getString("taskcreateddate");
                            task_bean.start_date = resObject.getString("taskstartdate");
                            task_bean.no_of_helper = resObject.getString("noofhelper");
                            task_bean.distance = resObject.getString("distance");
                            task_bean.total_helper = resObject.getString("max_helper");
                            task_bean.task_status = resObject.getString("status");
                            task_bean.start_time = resObject.getString("taskstarttime");

                            completed_task_list.add(task_bean);

                            is_data = true;
                        }

                        if (isFirstCall) {
                            listView.addFooterView(footer);
                            adapter = new CompletedTaskAdapter(getActivity(), completed_task_list);
                            listView.setAdapter(adapter);
                            isFirstCall = false;
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                        userScrolled = true;
                        listView.removeFooterView(footer);
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

    //completed task adapter

    public class CompletedTaskAdapter extends BaseAdapter {

        Activity activity;
        List<UpcomingTaskBean> task_list;

        public CompletedTaskAdapter(Activity activity, List<UpcomingTaskBean> task_list) {
            this.activity = activity;
            this.task_list = task_list;
        }

        @Override
        public int getCount() {
            return task_list.size();
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
        public View getView(final int position, View view, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.completed_task_list_item, null);

            TextView task_name = view.findViewById(R.id.task_name);
            ImageView task_cover_img = view.findViewById(R.id.task_cover_img);
            TextView organiser_name = view.findViewById(R.id.organiser_name);
            TextView date_time = view.findViewById(R.id.date_time);
            TextView location = view.findViewById(R.id.location);
            TextView helper_count = view.findViewById(R.id.count);
            TextView completed = view.findViewById(R.id.completed_status);
            TextView cancel_status = view.findViewById(R.id.cancel_status);

            if (task_list.get(position).task_name != null && task_list.get(position).task_name.length() > 0) {
                task_name.setText(task_list.get(position).task_name);
            }
            if (task_list.get(position).organiser_name != null && task_list.get(position).organiser_name.length() > 0) {
                organiser_name.setText(task_list.get(position).organiser_name);
            }

            if (task_list.get(position).task_image != null && task_list.get(position).task_image.length() > 0) {
                Glide.with(activity)
                        .load(task_list.get(position).task_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(task_cover_img);
            }

            if (task_list.get(position).location != null && task_list.get(position).location.length() > 0) {
                location.setText(task_list.get(position).location);
            }

            if (task_list.get(position).no_of_helper != null && task_list.get(position).no_of_helper.length() > 0) {
                helper_count.setText(task_list.get(position).no_of_helper + "/" + task_list.get(position).total_helper);
            }

       /* if (task_list.get(position).created_date != null && task_list.get(position).created_date.length() > 0) {
            date_time.setText(AppConstants.convertTaskdate(task_list.get(position).created_date));
        }
*/
            if (task_list.get(position).start_date != null && task_list.get(position).start_date.length() > 0) {
                if (task_list.get(position).start_time != null && task_list.get(position).start_time.length() > 0) {

                    if (task_list.get(position).start_time.contains("00:00:00")) {
                        String time = task_list.get(position).start_date;
                        date_time.setText(AppConstants.convertOnlyTaskdate(time));
                    } else {
                        String time = task_list.get(position).start_date + " " + task_list.get(position).start_time;
                        date_time.setText(AppConstants.convertTaskdateTime(time));
                    }
                }
            }

            if (task_list.get(position).task_status != null && task_list.get(position).task_status.length() > 0) {

                if (task_list.get(position).task_status.equalsIgnoreCase("Cancel")) {
                    cancel_status.setVisibility(View.VISIBLE);
                } else if (task_list.get(position).task_status.equalsIgnoreCase("Complete")) {
                    completed.setVisibility(View.VISIBLE);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    is_data = false;
                    Intent intent = new Intent(activity, TaskDetail.class);
                    intent.putExtra("task_id", task_list.get(position).task_id);
                    activity.startActivity(intent);
                }
            });

            return view;
        }
    }
}
