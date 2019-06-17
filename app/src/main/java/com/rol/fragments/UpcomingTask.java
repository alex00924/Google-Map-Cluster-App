package com.rol.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

public class UpcomingTask extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ListView listView;
    DBHelper db;
    LoginBean bean;
    String id = "";
    View footer;
    boolean isFirstCall = true;
    UpcomingTaskAdapter adapter;
    DisplayMetrics metrics = new DisplayMetrics();
    int width;
    int height;
    private List<UpcomingTaskBean> upcoming_task_list;
    private boolean userScrolled = false;
    private int counter = 0;
    private boolean is_call = false;
    private SwipeRefreshLayout swipe_layout;
    private TrackGPS trackGPS;
    private double lat, lon;

    public UpcomingTask() {
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
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        if (getArguments() != null) {
            id = getArguments().getString("id");
        }

        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);
        upcoming_task_list = new ArrayList<>();

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

                if (userScrolled && totalrandom == totalItemCount) {
                    listView.addFooterView(footer);
                    userScrolled = false;
                    counter += 10;
                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        getUpcomingTaskData();
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
            is_call = false;

            if (lat != 0.0 && lon != 0.0) {
                getUpcomingTaskData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("on resume", "method");
        if (!is_call) {
            isFirstCall = true;
            counter = 0;
            getUpcomingTaskData();
        }
    }

    @Override
    public void onRefresh() {
        isFirstCall = true;
        is_call = false;
        counter = 0;
        getUpcomingTaskData();
    }

    private void getUpcomingTaskData() {
        String tag = "UpcomingtaskList";
        String url;

        if (id != null && id.length() > 0) {
            url = AppConstants.url + "my_task.php?" + "user_id=" + bean.user_id + "&type=" + "upcoming" + "&auth_token=" + bean.auth_token + "&friend_id=" + id + "&start=" + counter + "&latitude=" + lat + "&longitude=" + lon;
        } else {
            url = AppConstants.url + "my_task.php?" + "user_id=" + bean.user_id + "&type=" + "upcoming" + "&auth_token=" + bean.auth_token + "&start=" + counter + "&latitude=" + lat + "&longitude=" + lon;
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
                        if (jsonObject.get("status").equals("Task not found")) {
                            Log.e("in if", "condition");
                            String message = jsonObject.getString("message");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        if (!is_call) {
                            upcoming_task_list.clear();
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
                            task_bean.is_my_added = resObject.getString("is_added");
                            task_bean.start_time = resObject.getString("taskstarttime");

                            upcoming_task_list.add(task_bean);

                            is_call = true;
                        }

                        if (isFirstCall) {
                            listView.addFooterView(footer);
                            adapter = new UpcomingTaskAdapter(getActivity(), upcoming_task_list);
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

    //upcoming task adapter
    public class UpcomingTaskAdapter extends BaseAdapter {

        Activity activity;
        private List<UpcomingTaskBean> task_list;

        public UpcomingTaskAdapter(Activity activity, List<UpcomingTaskBean> task_list) {

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
            view = inflater.inflate(R.layout.upcoming_task_list_item, null);

            TextView task_name = view.findViewById(R.id.task_name);
            final ImageView task_cover_img = view.findViewById(R.id.task_cover_img);
            TextView organiser_name = view.findViewById(R.id.organiser_name);
            TextView date_time = view.findViewById(R.id.date_time);
            TextView location = view.findViewById(R.id.location);
            TextView helper_count = view.findViewById(R.id.count);
            TextView distance = view.findViewById(R.id.distance);
            LinearLayout edit_task = view.findViewById(R.id.edit_layout);
            final LinearLayout delete_task = view.findViewById(R.id.delete_layout);
            final TextView cancel_task = view.findViewById(R.id.task_status);

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

//                Picasso.with(activity).load(task_list.get(position).task_image).placeholder(R.mipmap.no_image).error(R.mipmap.no_image).into(new com.squareup.picasso.Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                        int wid = bitmap.getWidth();
//                        int heg = bitmap.getHeight();
//                        task_cover_img.setImageBitmap(bitmap);
//
//                        Log.e("Bitmap Dimensions 00: ", wid + "x" + heg+"===");
////
//                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(wid,wid);
//                        task_cover_img.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        task_cover_img.setLayoutParams(layoutParams);
//                        Log.e("Bitmap Dimensions: ", width + "x" + height+"===");
//                    }
//
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//                    }
//
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//                    }
//                });
            }
            if (task_list.get(position).distance != null && task_list.get(position).distance.length() > 0) {
              /*  double result=AppConstants.roundTwoDecimals(Double.parseDouble(task_list.get(position).distance));
                distance.setText(result+ " " + "Km");
               */
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

            /*if (task_list.get(position).created_date != null && task_list.get(position).created_date.length() > 0)
            {
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

            if (task_list.get(position).is_my_added != null && task_list.get(position).is_my_added.length() > 0) {

                if (task_list.get(position).is_my_added.equalsIgnoreCase("1")) {
                    cancel_task.setVisibility(View.GONE);
                    edit_task.setVisibility(View.VISIBLE);
                    delete_task.setVisibility(View.VISIBLE);
                } else {

                    cancel_task.setVisibility(View.VISIBLE);
                    edit_task.setVisibility(View.GONE);
                    delete_task.setVisibility(View.GONE);
                }
            }

            if (id != null && id.length() > 0) {
                cancel_task.setVisibility(View.GONE);
            }

            cancel_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (AppConstants.isNetworkAvailable(activity)) {
                        cancelTask(task_list.get(position).task_id, position);
                    }
                }
            });

            edit_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                    Intent intent = new Intent(activity, EditTask.class);
//                    intent.putExtra("taskId", task_list.get(position).task_id);
//                    intent.putExtra("taskName", task_list.get(position).task_name);
//                    activity.startActivity(intent);
                }
            });

            delete_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteTaskDialog(task_list.get(position).task_id, position);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    is_call = false;
                    Intent intent = new Intent(activity, TaskDetail.class);
                    intent.putExtra("task_id", task_list.get(position).task_id);
                    activity.startActivity(intent);
                }
            });

            return view;
        }

//delete task dialog

        private void deleteTaskDialog(final String task_id, final int pos) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.delete_task_dialog);
            dialog.show();
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            Button yes_btn = dialog.findViewById(R.id.yes_btn);
            Button no_btn = dialog.findViewById(R.id.no_btn);

            yes_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    if (AppConstants.isNetworkAvailable(activity)) {
                        deleteTask(task_id, pos);
                    }
                }
            });
            no_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }

        //delete task

        private void deleteTask(String task_id, final int pos) {
            String tag = "deleteTask";
            String url = AppConstants.url + "delete_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token
                    + "&task_id=" + task_id;

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

                            task_list.remove(pos);
                            adapter.notifyDataSetChanged();
                            counter = 0;
                            isFirstCall = true;
                            upcoming_task_list.clear();
                            is_call = false;
                            onResume();
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

        //cancel task
        private void cancelTask(String task_id, final int pos) {
            String tag = "cancelTask";
            String url = AppConstants.url + "cancel_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token
                    + "&task_id=" + task_id;

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

                            task_list.remove(pos);
                            adapter.notifyDataSetChanged();
                            counter = 0;
                            isFirstCall = true;
                            upcoming_task_list.clear();
                            is_call = false;
                            onResume();
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
}