package com.rol.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.rol.beans.MyTaskBean;
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

public class CompanyDetailUpcomingTask extends Fragment {

    ListView task_listView;
    String company_id;
    DBHelper db;
    LoginBean bean;
    List<MyTaskBean> task_list;
    private TrackGPS trackGPS;
    private double lat, lon;

    public CompanyDetailUpcomingTask() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.company_detail_task_list, container, false);

        task_listView = view.findViewById(R.id.list);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        Bundle bundle = getArguments();
        if (bundle != null) {
            company_id = bundle.getString("company_id");
        }

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        if (AppConstants.isNetworkAvailable(getActivity())) {
            if (lat != 0.0 && lon != 0.0) {
                getCompanyUpcomingTask();
            } else {
                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    //adapter for task list

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    private void getCompanyUpcomingTask() {
        final ProgressDialog progDialog = ProgressDialog.show(getActivity(), null, null, false, true);
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progDialog.setContentView(R.layout.progress_dialog_layout);

        String tag = "upcomingTask";
        String url = AppConstants.url + "company_task_list.php?" + "company_id=" + company_id + "&latitude=" + lat
                + "&longitude=" + lon + "&user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&type=" + "upcoming";

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    progDialog.dismiss();

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");
                        task_list = new ArrayList<>();

                        task_list.clear();

                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);

                            MyTaskBean task_bean = new MyTaskBean();

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
                            task_bean.is_user_added = resObject.getString("is_myadded");
                            task_bean.is_applied = resObject.getString("is_applied");

                            task_list.add(task_bean);
                        }

                        CompanyDetailUpcomingAdapter adapter = new CompanyDetailUpcomingAdapter(getActivity(), task_list, bean.user_id, bean.auth_token);
                        task_listView.setAdapter(adapter);
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

//detail task adapter

    public class CompanyDetailUpcomingAdapter extends BaseAdapter {

        Activity activity;
        List<MyTaskBean> task_list;
        String user_id, auth_token;

        public CompanyDetailUpcomingAdapter(Activity activity, List<MyTaskBean> task_list, String user_id, String auth_token) {

            this.activity = activity;
            this.task_list = task_list;
            this.user_id = user_id;
            this.auth_token = auth_token;
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
            View view = inflater.inflate(R.layout.company_detail_item, null);

            LinearLayout edit_layout = view.findViewById(R.id.edit_layout);
            LinearLayout delete_layout = view.findViewById(R.id.delete_layout);
            TextView task_status = view.findViewById(R.id.task_status);

            TextView task_name = view.findViewById(R.id.task_name);
            TextView organiser_name = view.findViewById(R.id.organiser_name);
            TextView date_time = view.findViewById(R.id.date_time);
            TextView location = view.findViewById(R.id.location);
            TextView helper_count = view.findViewById(R.id.count);
            TextView distance = view.findViewById(R.id.distance);
            ImageView task_image = view.findViewById(R.id.task_cover_img);
            ImageView delete_button = view.findViewById(R.id.delete_button);

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
                        .into(task_image);
            }
            if (task_list.get(position).distance != null && task_list.get(position).distance.length() > 0) {
               /* double result=AppConstants.roundTwoDecimals(Double.parseDouble(task_list.get(position).distance));
                distance.setText(result+" "+"Km");
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
          /*  if(task_list.get(position).created_date !=null && task_list.get(position).created_date.length()>0)
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

            if (task_list.get(position).is_user_added != null && task_list.get(position).is_user_added.length() > 0) {
                if (task_list.get(position).is_user_added.equalsIgnoreCase("1")) {
                    task_status.setVisibility(View.GONE);
                    edit_layout.setVisibility(View.VISIBLE);
                    delete_layout.setVisibility(View.VISIBLE);
                } else {
                    if (task_list.get(position).is_applied.equalsIgnoreCase("1")) {
                        task_status.setVisibility(View.VISIBLE);
                        edit_layout.setVisibility(View.GONE);
                        delete_layout.setVisibility(View.GONE);
                        delete_button.setVisibility(View.GONE);
                    } else {
                        task_status.setVisibility(View.GONE);
                        edit_layout.setVisibility(View.GONE);
                        delete_layout.setVisibility(View.GONE);
                        delete_button.setVisibility(View.GONE);
                    }
                }
            }

            edit_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(activity, EditTask.class);
//                    intent.putExtra("taskId", task_list.get(position).task_id);
//                    intent.putExtra("taskName", task_list.get(position).title);
//                    intent.putExtra("page_name", "CompantDetailTask");
//                    activity.startActivity(intent);
                }
            });

            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteTaskDialog(task_list.get(position).task_id);
                }
            });

            delete_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteTaskDialog(task_list.get(position).task_id);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, TaskDetail.class);
                    intent.putExtra("task_id", task_list.get(position).task_id);
                    activity.startActivity(intent);
                }
            });

            return view;
        }

        private void deleteTaskDialog(final String task_id) {
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

                        deleteTask(task_id);
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

        private void deleteTask(String task_id) {
            String tag = "deleteTask";
            String url = AppConstants.url + "delete_task.php?" + "user_id=" + user_id + "&auth_token=" + auth_token
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
//                            Toast.makeText(activity, "Task not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(CompanyDetailUpcomingTask.this).attach(CompanyDetailUpcomingTask.this).commit();
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

