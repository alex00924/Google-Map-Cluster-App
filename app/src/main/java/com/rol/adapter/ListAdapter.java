package com.rol.adapter;


//task list adapter

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.rol.AsyncImageLoad.ImageLoader;
import com.rol.Global;
import com.rol.R;
import com.rol.Task;
import com.rol.TaskDetail;
import com.rol.beans.TaskListBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class ListAdapter extends BaseAdapter implements RefreshDataCallback {

    public int  m_nId = 0;
    Activity activity;
    String type;
    List<TaskListBean> task_data_list;
    String user_id, auth_token;
    LinearLayout linearLayout;
    ImageView fav_unfav_img;
    ListView task_listView;
    TextView task_status, applied_task_status;

    public ListAdapter(Activity activity, List<TaskListBean> task_list, String user_id, String auth_token, ListView task_listView) {

        this.activity = activity;
        this.task_data_list = task_list;
        this.user_id = user_id;
        this.auth_token = auth_token;
        this.task_listView = task_listView;
    }

    @Override
    public int getCount() {
        return task_data_list.size();
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

        if (task_data_list.get(position).organiser_name != null && task_data_list.get(position).organiser_name.length() > 0) {
            organiser_name.setText(task_data_list.get(position).organiser_name);
        }

        if (task_data_list.get(position).title != null && task_data_list.get(position).title.length() > 0) {
            task_name.setText(task_data_list.get(position).title);
        }

        if (task_data_list.get(position).is_applied != null && task_data_list.get(position).is_applied.length() > 0) {
            if (task_data_list.get(position).is_applied.equalsIgnoreCase("1")) {
                task_status.setVisibility(View.GONE);
                applied_task_status.setVisibility(View.VISIBLE);
            } else {
                task_status.setText(activity.getString(R.string.apply));
                task_status.setVisibility(View.VISIBLE);
                applied_task_status.setVisibility(View.GONE);
            }
        }

        if (task_data_list.get(position).is_accepted != null && task_data_list.get(position).is_accepted.length() > 0) {
            if (task_data_list.get(position).is_accepted.equalsIgnoreCase("1")) {
                applied_task_status.setText("Confirmed");
            }
        }

        if (task_data_list.get(position).task_image != null && task_data_list.get(position).task_image.length() > 0) {
            Glide.with(activity)
                    .load(task_data_list.get(position).task_image)
                    .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                    .into(task_cover_img);
        }

//        ImageLoader imageLoader=new ImageLoader(activity);
//        imageLoader.DisplayImage(task_data_list.get(position).task_image, task_cover_img);

/*
            if (task_data_list.get(position).distance != null && task_data_list.get(position).distance.length() > 0) {
                // float result = Float.parseFloat(task_data_list.get(position).distance);
                double result = AppConstants.roundTwoDecimals(Double.parseDouble(task_data_list.get(position).distance));
                distance.setText(result + " " + "Km");
            }
*/

        if (task_data_list.get(position).distance != null && task_data_list.get(position).distance.length() > 0) {
            double result = Double.parseDouble(task_data_list.get(position).distance);
            String strDist = String.format("%.2f", result);
            distance.setText(strDist + "Km");
        }

        if (task_data_list.get(position).location != null && task_data_list.get(position).location.length() > 0) {
            location.setText(task_data_list.get(position).location);
        }

        if (task_data_list.get(position).no_of_helper != null && task_data_list.get(position).no_of_helper.length() > 0) {
            helper_count.setText(task_data_list.get(position).no_of_helper + "/" + task_data_list.get(position).total_helper);
        }
          /*  if (task_list.get(position).created_date != null && task_list.get(position).created_date.length() > 0) {
                date_time.setText(AppConstants.convertTaskdate(task_list.get(position).created_date));
            }

*/
        if (task_data_list.get(position).start_date != null && task_data_list.get(position).start_date.length() > 0) {
            if (task_data_list.get(position).start_time != null && task_data_list.get(position).start_time.length() > 0) {

                if (task_data_list.get(position).start_time.contains("00:00:00")) {
                    String time = task_data_list.get(position).start_date;
                    date_time.setText(AppConstants.convertOnlyTaskdate(time));
                } else {
                    String time = task_data_list.get(position).start_date + " " + task_data_list.get(position).start_time;
                    date_time.setText(AppConstants.convertTaskdateTime(time));
                }
            }
        }

        if (task_data_list.get(position).is_fav != null && task_data_list.get(position).is_fav.length() > 0) {
            if (task_data_list.get(position).is_fav.equalsIgnoreCase("1")) {
                fav_unfav_img.setImageResource(R.mipmap.star_1);
            } else {
                fav_unfav_img.setImageResource(R.mipmap.star_2);
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                is_service_call = false;
//                Global.m_arrState.add(0);
                Intent intent = new Intent(activity, TaskDetail.class);
                intent.putExtra("task_id", task_data_list.get(position).task_id);
                activity.startActivityForResult(intent, m_nId);
            }
        });

        fav_unfav_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.isNetworkAvailable(activity)) {

                    if (task_data_list.get(position).is_fav.equalsIgnoreCase("1")) {
                        FavUnfavTask(task_data_list.get(position).task_id, "0", position);
                    } else {
                        FavUnfavTask(task_data_list.get(position).task_id, "1", position);
                    }
                }
            }
        });

        task_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.isNetworkAvailable(activity)) {
                    applyTask(task_data_list.get(position).task_id, position, v);
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
//                        int start = task_listView.getFirstVisiblePosition();
//                        if( start > pos )
//                            return;
//                        View view = task_listView.getChildAt(pos-start);
//                        task_listView.getAdapter().getView(pos, view, task_listView);
//
//                        if( message.contains("Un") )
//                        {
//                            task_data_list.get(pos).is_fav = "0";
//                            ((ImageView)view.findViewById(R.id.star)).setImageResource(R.mipmap.star_2);
//                            refreshData(pos, 0, "0");
//                        }
//                        else
//                        {
//                            task_data_list.get(pos).is_fav = "1";
//                            ((ImageView)view.findViewById(R.id.star)).setImageResource(R.mipmap.star_1);
//                            refreshData(pos, 0, "1");
//                        }

                        Task curTast = (Task)activity;
                        curTast.m_bChanged = true;
                        if( message.contains("Un") )
                            curTast.m_strFavor = "0";
                        else
                            curTast.m_strFavor = "1";
                        curTast.m_strApply = task_data_list.get(pos).is_applied;
                        curTast.m_strId = task_data_list.get(pos).task_id;
                        curTast.updateAllViews();



//                        refreshData(pos, 0);
//                        page_counter = 0;
//                        isFirstCall = true;
//                        is_service_call = false;
//                        onResume();

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

    private void applyTask(String task_id, final int pos, final View v) {
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

                        Task curTast = (Task)activity;
                        curTast.m_bChanged = true;
                        curTast.m_strApply = "1";
                        curTast.m_strFavor =task_data_list.get(pos).is_fav;
                        curTast.m_strId = task_data_list.get(pos).task_id;
                        curTast.updateAllViews();
//
//                        task_data_list.get(pos).task_status = "1";
//                        task_data_list.get(pos).is_applied = "1";
//
//                        int start = task_listView.getFirstVisiblePosition();
//                        if( start > pos )
//                            return;
//                        View view = task_listView.getChildAt(pos-start);
//                        task_listView.getAdapter().getView(pos, view, task_listView);
//                        view.findViewById(R.id.task_status).setVisibility(View.GONE);
//                        view.findViewById(R.id.applied_task_status).setVisibility(View.VISIBLE);
//                        refreshData(pos, 1, "1");


//                        refreshData(pos, 1);
//                        page_counter = 0;
//                        isFirstCall = true;
//                        is_service_call = false;
//                        onResume();
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

    @Override
    public void refreshData(int nPos, int nKind, String strVal) {
   }

}


