package com.rol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by keshav on 2/7/18.
 */

public class TaskListnew extends Fragment {

    ListView task_listView;
    DBHelper db;
    LoginBean bean;
    List<TaskListBean> task_list = new ArrayList<>();
    ;
    String filter_type;
    SharedPreferences spf;
    String task_name, location, category_value;
    int max_distance;
    boolean userScrolled = false;
    View footer;
    boolean isFirstCall = true;
    boolean is_service_call = true;
    TaskListAdapter adapter;
    private int counter = 0;
    private TrackGPS trackGPS;
    private double lat, lon;

    public TaskListnew() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view, container, false);

        task_listView = view.findViewById(R.id.data_list);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        SharedPreferences prefs = getActivity().getSharedPreferences("my_pref", MODE_PRIVATE);
        filter_type = prefs.getString("type", "distance");

        spf = getActivity().getSharedPreferences("search_pf", MODE_PRIVATE);
        task_name = spf.getString("task_name", "");
        location = spf.getString("location", "");
        category_value = spf.getString("category", "");
        max_distance = spf.getInt("max_distance", 0);

        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Task.add_btn.setVisibility(View.VISIBLE);

        Log.e("on create", "method");
        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);

        if (!statusOfGPS) {
            showSettingsAlert();
        } else {

            if (AppConstants.isNetworkAvailable(getActivity())) {
                if (lat != 0.0 && lon != 0.0) {
                    // if(is_service_call)
                    //  {
                    getTaskListData();

                    //  }
                }
            }
        }

        task_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (userScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {
                    task_listView.addFooterView(footer);
                    userScrolled = false;
                    counter += 10;
                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        getTaskListData();
                    }
                    // adapter.notifyDataSetChanged();

                }
            }
        });

        return view;
    }





/*
   @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
        {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();

        }
    }
*/



/*
   @Override
    public void onResume()
    {
        super.onResume();
        if(is_service_call)
        {
            Log.e("on resume","method");
            counter=0;
            task_list.clear();
           // task_list=new ArrayList<>();
            getTaskListData();

        }
    }
*/

    private void getTaskListData() {
        String tag = "taskList";
        String url;

        if (filter_type.equalsIgnoreCase("search")) {
            url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&searchbyname=" + task_name + "&category=" + category_value + "&max_distance=" + max_distance + "&currentlocation=" + location + "&start=" + counter;
        } else {

            //url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + 22.3039+ "&longitude=" +70.8022 + "&sort_by=" + filter_type;
            url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&sort_by=" + filter_type + "&start=" + counter;
        }
        //  is_service_call=false;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    task_listView.removeFooterView(footer);

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {

                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        // task_list.clear();

                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            TaskListBean task_bean = new TaskListBean();
                            task_bean.task_id = resObject.getString("id");
                            task_bean.organiser_id = resObject.getString("organiser_id");
                            task_bean.organiser_name = resObject.getString("organiser_name");
                            task_bean.organiser_image = resObject.getString("organiser_image");
                            task_bean.title = resObject.getString("title");
                            task_bean.task_image = resObject.getString("image");
                            task_bean.location = resObject.getString("location");
                            task_bean.created_date = resObject.getString("created_date");
                            task_bean.start_date = resObject.getString("start_date");
                            task_bean.start_time = resObject.getString("start_time");
                            task_bean.no_of_helper = resObject.getString("no_of_helper");
                            task_bean.distance = resObject.getString("distance");
                            task_bean.is_fav = resObject.getString("is_favourite");
                            task_bean.total_helper = resObject.getString("total_helper");
                            task_bean.is_applied = resObject.getString("is_applied");
                            task_bean.is_accepted = resObject.getString("is_accepted");

                            task_list.add(task_bean);
                        }

                        if (isFirstCall) {
                            task_listView.addFooterView(footer);
                            adapter = new TaskListAdapter(getActivity(), task_list, bean.user_id, bean.auth_token, task_listView);
                            task_listView.setAdapter(adapter);
                            isFirstCall = false;
                        } else {
                            adapter.notifyDataSetChanged();
                        }


                       /* adapter = new TaskListAdapter(getActivity(), task_list, bean.user_id, bean.auth_token, task_listView);
                        task_listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();*/
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

/*
        request.setRetryPolicy(new DefaultRetryPolicy(1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
*/

        AppSingleton.getInstance(getActivity()).addToRequestQueue(request, tag);
    }

    //task list adapter

    public void showSettingsAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("GPS Not Enabled");
        alertDialog.setMessage("Do you wants to turn On GPS");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                getActivity().finish();
            }
        });

        alertDialog.show();
    }

    public class TaskListAdapter extends BaseAdapter {

        Activity activity;
        String type;
        List<TaskListBean> task_list;
        String user_id, auth_token;
        LinearLayout linearLayout;
        ImageView fav_unfav_img;
        ListView task_listView;
        TextView task_status, applied_task_status;

        public TaskListAdapter(Activity activity, List<TaskListBean> task_list, String user_id, String auth_token, ListView task_listView) {

            this.activity = activity;
            this.task_list = task_list;
            this.user_id = user_id;
            this.auth_token = auth_token;
            this.task_listView = task_listView;
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

            ImageView task_cover_img = view.findViewById(R.id.task_cover_img);
            TextView task_name = view.findViewById(R.id.task_name);
            TextView organiser_name = view.findViewById(R.id.organiser_name);
            TextView date_time = view.findViewById(R.id.date_time);
            TextView location = view.findViewById(R.id.location);
            TextView helper_count = view.findViewById(R.id.count);
            TextView distance = view.findViewById(R.id.distance);
            fav_unfav_img = view.findViewById(R.id.star);
            linearLayout = view.findViewById(R.id.main_layout);
            task_status = view.findViewById(R.id.task_status);
            applied_task_status = view.findViewById(R.id.applied_task_status);

            if (task_list.get(position).organiser_name != null && task_list.get(position).organiser_name.length() > 0) {
                organiser_name.setText(task_list.get(position).organiser_name);
            }

            if (task_list.get(position).title != null && task_list.get(position).title.length() > 0) {
                task_name.setText(task_list.get(position).title);
            }

            if (task_list.get(position).is_applied != null && task_list.get(position).is_applied.length() > 0) {
                if (task_list.get(position).is_applied.equalsIgnoreCase("1")) {
                    task_status.setVisibility(View.GONE);
                    applied_task_status.setVisibility(View.VISIBLE);
                } else {
                    task_status.setText(activity.getString(R.string.apply));
                    task_status.setVisibility(View.VISIBLE);
                    applied_task_status.setVisibility(View.GONE);
                }
            }

            if (task_list.get(position).is_accepted != null && task_list.get(position).is_accepted.length() > 0) {
                if (task_list.get(position).is_accepted.equalsIgnoreCase("1")) {
                    applied_task_status.setText("Confirmed");
                }
            }

            if (task_list.get(position).task_image != null && task_list.get(position).task_image.length() > 0) {
                Glide.with(activity)
                        .load(task_list.get(position).task_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(task_cover_img);
            }
            if (task_list.get(position).distance != null && task_list.get(position).distance.length() > 0) {
                float result = Float.parseFloat(task_list.get(position).distance);
                distance.setText(result + " " + "Km");
            }
            if (task_list.get(position).location != null && task_list.get(position).location.length() > 0) {
                location.setText(task_list.get(position).location);
            }

            if (task_list.get(position).no_of_helper != null && task_list.get(position).no_of_helper.length() > 0) {
                helper_count.setText(task_list.get(position).no_of_helper + "/" + task_list.get(position).total_helper);
            }
          /*  if (task_list.get(position).created_date != null && task_list.get(position).created_date.length() > 0) {
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

            if (task_list.get(position).is_fav != null && task_list.get(position).is_fav.length() > 0) {
                if (task_list.get(position).is_fav.equalsIgnoreCase("1")) {
                    fav_unfav_img.setImageResource(R.mipmap.star_1);
                } else {
                    fav_unfav_img.setImageResource(R.mipmap.star_2);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(activity, TaskDetail.class);
                    intent.putExtra("task_id", task_list.get(position).task_id);
                    activity.startActivity(intent);
                    //  is_service_call=true;
                    Log.e("ID", "" + task_list.get(position).task_id);
                }
            });

            fav_unfav_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppConstants.isNetworkAvailable(activity)) {

                        if (task_list.get(position).is_fav.equalsIgnoreCase("1")) {
                            FavUnfavTask(task_list.get(position).task_id, "0", position);
                        } else {
                            FavUnfavTask(task_list.get(position).task_id, "1", position);
                        }
                    }
                }
            });

            task_status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppConstants.isNetworkAvailable(activity)) {
                        applyTask(task_list.get(position).task_id, position);
                    }
                }
            });

            return view;
        }

        private void FavUnfavTask(String task_id, final String type, final int pos) {
            String tag = "favUnfavTask";
            String url = AppConstants.url + "favouritetask.php?" + "user_id=" + user_id + "&auth_token=" + auth_token + "&task_id=" + task_id + "&type=" + type;

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

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(TaskListnew.this).attach(TaskListnew.this).commit();

                          /*  View rowView = task_listView.getChildAt(pos);
                            ImageView fav_img = rowView.findViewById(R.id.star);

                            if (type.equalsIgnoreCase("1")) {
                                fav_img.setImageResource(R.mipmap.star_1);
                            } else {
                                fav_img.setImageResource(R.mipmap.star_2);

                            }
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

        private void applyTask(String task_id, final int pos) {
            String tag = "applyTask";
            String url = AppConstants.url + "apply_task.php?" + "task_id=" + task_id + "&user_id=" + user_id + "&auth_token=" + auth_token;

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
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(TaskListnew.this).attach(TaskListnew.this).commit();
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
