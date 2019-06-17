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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rol.Global;
import com.rol.R;
import com.rol.Task;
import com.rol.TaskDetail;
import com.rol.beans.LoginBean;
import com.rol.beans.TaskListBean;
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
 * Created by keshav on 2/7/18.
 */

public class MyTask extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    ListView task_listView;
    DBHelper db;
    LoginBean bean;
    View footer;
    boolean isFirstCall = true;
    MyTaskAdapter adapter;
    boolean refreshing = true, isFirstRefresh = true;
    private List<TaskListBean> my_task_list;
    private boolean userScrolled = false;
    private int counter = 0;
    private boolean is_getData = false;
    private SwipeRefreshLayout swipe_layout;
    private List<TaskListBean> new_data;
    private TrackGPS trackGPS;
    private double lat, lon;

    public MyTask() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view, container, false);

        task_listView = view.findViewById(R.id.data_list);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        Task.add_btn.setVisibility(View.VISIBLE);

        my_task_list = new ArrayList<>();

        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);

        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);

        task_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                    task_listView.addFooterView(footer);
                    userScrolled = false;
                    counter += 10;
                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        if (lat != 0.0 && lon != 0.0) {
                            getMytaskData();
                        }
                    }
                }
            }
        });

        if( isFirstCall ) {
            isFirstCall = true;
            counter = 0;
            my_task_list.clear();
            if (lat != 0.0 && lon != 0.0) {
                getMytaskData();
            }
        }
        else
        {
            adapter = new MyTaskAdapter(getActivity(), my_task_list);
            task_listView.setAdapter(adapter);
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
//            if (lat != 0.0 && lon != 0.0) {
//                getMytaskData();
//            }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//
//        if (!is_getData) {
//            isFirstCall = true;
//            counter = 0;
//            if (lat != 0.0 && lon != 0.0) {
//                getMytaskData();
//            }
//        }

    }

    @Override
    public void onRefresh() {
        task_listView.removeFooterView(footer);
        swipe_layout.setRefreshing(false);


//        isFirstCall = true;
//        is_getData = false;
//        counter = 0;
//
//        if (lat != 0.0 && lon != 0.0) {
//            getMytaskData();
//        }
    }

    private void getMytaskData() {
        String tag = "myTaskList";
        String url = AppConstants.url + "task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&start=" + counter;//+ "&latitude=" + lat + "&longitude="+lon;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    task_listView.removeFooterView(footer);
                    swipe_layout.setRefreshing(false);

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        new_data = new ArrayList<>();
                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);

                            TaskListBean task_bean = new TaskListBean();

                            task_bean.task_id = resObject.getString("task_id");
                            task_bean.task_image = resObject.getString("image");
                            task_bean.title = resObject.getString("taskname");
                            task_bean.organiser_id = resObject.getString("organiserid");
                            task_bean.organiser_name = resObject.getString("organisername");
                            task_bean.organiser_image = resObject.getString("organiserimage");
                            task_bean.created_date = resObject.getString("taskcreateddate");
                            task_bean.start_date = resObject.getString("taskstartdate");
                            task_bean.start_time = resObject.getString("taskstarttime");
                            task_bean.location = resObject.getString("location");
                            task_bean.no_of_helper = resObject.getString("noofhelper");
                            task_bean.total_helper = resObject.getString("max_helper");
                            task_bean.is_added = resObject.getString("is_added");
                            task_bean.distance = resObject.getString("distance");
                            task_bean.is_applied = resObject.getString("is_applied");
                            task_bean.is_fav = resObject.getString("is_favourited");
                            task_bean.is_recommanded = resObject.getString("is_recommended");
                            task_bean.task_status = resObject.getString("task_status");
                            task_bean.is_accepted = resObject.getString("is_confirmed");

                            // my_task_list.add(task_bean);
                            new_data.add(task_bean);
                        }
                        is_getData = true;

                        if (isFirstCall) {
                            my_task_list.clear();
                            for (int i = 0; i < new_data.size(); i++) {
                                my_task_list.add(new_data.get(i));
                            }
                            task_listView.addFooterView(footer);
                            adapter = new MyTaskAdapter(getActivity(), my_task_list);
                            task_listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            isFirstCall = false;
                        } else {
                            for (int i = 0; i < new_data.size(); i++) {
                                my_task_list.add(new_data.get(i));
                            }
                            adapter.notifyDataSetChanged();
                        }

                        Log.e( "my_size : ", " --- " + my_task_list.size() );

                        userScrolled = true;
                        task_listView.removeFooterView(footer);
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

    //my task adapter
    public class MyTaskAdapter extends BaseAdapter {

        Activity activity;
        private List<TaskListBean> task_list;

        public MyTaskAdapter(Activity activity, List<TaskListBean> task_list) {

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(R.layout.task_list_item, null);
            TextView task_date = view.findViewById(R.id.task_date);
            TextView task_name = view.findViewById(R.id.task_name);
            TextView organiser_name = view.findViewById(R.id.organiser_name);
            TextView task_create_date = view.findViewById(R.id.date_time);
            TextView location = view.findViewById(R.id.location);
            TextView helper_count = view.findViewById(R.id.count);
            TextView distance = view.findViewById(R.id.distance);
            ImageView task_cover_img = view.findViewById(R.id.task_cover_img);
            ImageView fav_unfav_img = view.findViewById(R.id.star);
            TextView task_status = view.findViewById(R.id.task_status);

            task_status.setVisibility(View.VISIBLE);

            if (task_list.get(position).title != null && task_list.get(position).title.length() > 0) {
                task_name.setText(task_list.get(position).title);
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
            if (task_list.get(position).distance != null && task_list.get(position).distance.length() > 0) {
                /*double result =AppConstants.roundTwoDecimals(Double.parseDouble(task_list.get(position).distance));
                distance.setText(result+ " " + "Km");*/

                double result = Double.parseDouble(task_list.get(position).distance);
                int i = (int) Math.round(result);
                distance.setText(i + " " + "Km");
            }
            if (task_list.get(position).location != null && task_list.get(position).location.length() > 0) {
                location.setText(task_list.get(position).location);
            }

            if (task_list.get(position).no_of_helper != null && task_list.get(position).no_of_helper.length() > 0) {
                helper_count.setText(task_list.get(position).no_of_helper + "/" + task_list.get(position).total_helper);
            }

            if (task_list.get(position).start_date != null && task_list.get(position).start_date.length() > 0) {
                if (task_list.get(position).start_time != null && task_list.get(position).start_time.length() > 0) {

                    if (task_list.get(position).start_time.contains("00:00:00")) {
                        String time = task_list.get(position).start_date;
                        task_create_date.setText(AppConstants.convertOnlyTaskdate(time));
                    } else {
                        String time = task_list.get(position).start_date + " " + task_list.get(position).start_time;
                        task_create_date.setText(AppConstants.convertTaskdateTime(time));
                    }
                }
            }

            if (task_list.get(position).is_fav != null && task_list.get(position).is_fav.length() > 0) {
                if (task_list.get(position).is_fav.equalsIgnoreCase("1")) {
                    fav_unfav_img.setImageResource(R.mipmap.star_1);
                } else {
                    fav_unfav_img.setImageResource(R.mipmap.star_2);
                }
            }

            if (task_list.get(position).is_applied.length() > 0 && task_list.get(position).is_fav.length() > 0 && task_list.get(position).is_recommanded.length() > 0 && task_list.get(position).is_accepted.length() > 0) {
                if (task_list.get(position).is_accepted.equalsIgnoreCase("1")) {
                    task_status.setText(activity.getString(R.string.upcoming));
                    task_status.setTextColor(activity.getResources().getColor(R.color.upcoming_text_color));
                } else if (task_list.get(position).is_applied.equals("1")) {
                    task_status.setText(activity.getString(R.string.applied));
                    task_status.setTextColor(activity.getResources().getColor(R.color.applied_text_color));
                } else if (task_list.get(position).is_recommanded.equals("1")) {
                    task_status.setText(activity.getString(R.string.recommended));
                    task_status.setTextColor(activity.getResources().getColor(R.color.recommend_text_color));
                } else if (task_list.get(position).is_fav.equals("1")) {
                    task_status.setText(activity.getString(R.string.favourited));
                    task_status.setTextColor(activity.getResources().getColor(R.color.fav_text_color));
                }
                else
                {
                    task_status.setText("Apply");
                    task_status.setTextColor(getResources().getColor(R.color.base_header_color));
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    is_getData = false;
//                    Global.m_arrState.add(0);
                    Intent intent = new Intent(activity, TaskDetail.class);
                    intent.putExtra("task_id", task_list.get(position).task_id);
                    activity.startActivityForResult(intent, Global.TASK_MY);
                }
            });

            return view;
        }
    }

    public void refreshData()
    {
        isFirstCall = true;
        counter = 0;
        my_task_list.clear();
        if (lat != 0.0 && lon != 0.0) {
            getMytaskData();
        }
    }

    public void updateView(String strApply, String strFavor, String strId)
    {
        int i = 0, nCnt = my_task_list.size(), nPos = -1;
        for( i = 0 ; i < nCnt ; i ++ )
        {
            if( !my_task_list.get(i).task_id.equals(strId))
                continue;
            nPos = i;
            break;
        }
        if( nPos < 0 )
            return;
        my_task_list.get(nPos).is_applied = strApply;
        my_task_list.get(nPos).is_fav = strFavor;

        int start = task_listView.getFirstVisiblePosition();
        if( start > nPos )
            return;
        View view = task_listView.getChildAt(nPos-start);
        task_listView.getAdapter().getView(nPos, view, task_listView);

        if( view == null )
            return;

        if( strFavor.equalsIgnoreCase("0"))
            ((ImageView)view.findViewById(R.id.star)).setImageResource(R.mipmap.star_2);
        else
            ((ImageView)view.findViewById(R.id.star)).setImageResource(R.mipmap.star_1);

        TextView txtStatus = view.findViewById(R.id.task_status);
        if (my_task_list.get(nPos).is_accepted.equalsIgnoreCase("1")) {
            txtStatus.setText(getString(R.string.upcoming));
            txtStatus.setTextColor(getResources().getColor(R.color.upcoming_text_color));
        } else if (my_task_list.get(nPos).is_applied.equals("1")) {
            txtStatus.setText(getString(R.string.applied));
            txtStatus.setTextColor(getResources().getColor(R.color.applied_text_color));
        } else if (my_task_list.get(nPos).is_recommanded.equals("1")) {
            txtStatus.setText(getString(R.string.recommended));
            txtStatus.setTextColor(getResources().getColor(R.color.recommend_text_color));
        } else if (my_task_list.get(nPos).is_fav.equals("1")) {
            txtStatus.setText(getString(R.string.favourited));
            txtStatus.setTextColor(getResources().getColor(R.color.fav_text_color));
        }
        else
        {
            txtStatus.setText("Apply");
            txtStatus.setTextColor(getResources().getColor(R.color.base_header_color));
        }
    }
}
