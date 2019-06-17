package com.rol.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.rol.Global;
import com.rol.R;
import com.rol.Task;
import com.rol.TaskDetail;
import com.rol.adapter.ListAdapter;
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

public class TaskList extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static int page_counter = 0;
    ListView task_listView;
    DBHelper db;
    LoginBean bean;
    String filter_type;
    SharedPreferences spf;
    String task_name, location, category_value;
    int max_distance;
    boolean isFirstCall = true;
    TaskListAdapter adapter;
    boolean statusOfGPS;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    TrackGPS trackGPS;
    private List<TaskListBean> task_list;
    private boolean userScrolled = false;
    private View footer;
    private boolean is_service_call = false;
    private SwipeRefreshLayout swipe_layout;
    private double lat, lon;

    public TaskList() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view, container, false);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        task_listView = view.findViewById(R.id.data_list);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);
        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        task_list = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences("my_pref", MODE_PRIVATE);
        filter_type = prefs.getString("type", "distance");

        spf = getActivity().getSharedPreferences("search_pf", MODE_PRIVATE);
        task_name = spf.getString("task_name", "");
        location = spf.getString("location", "");
        category_value = spf.getString("category", "");
        max_distance = spf.getInt("max_distance", 0);

        Task.add_btn.setVisibility(View.VISIBLE);

        Log.e("on create", "method");
        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);

/*

        if(AppConstants.isNetworkAvailable(getActivity()))
        {
            if(!is_service_call)
            {
                getTaskListData();

            }
        }
*/

        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);

        task_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                    if (firstVisibleItem == 0) {
                        swipe_layout.setEnabled(true);
                    } else {
                        swipe_layout.setEnabled(false);
                    }
                }

                if (userScrolled && totalrandom == totalItemCount) {
                    task_listView.addFooterView(footer);
                    userScrolled = false;
                    page_counter += 10;

                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        getTaskListData();
                    }
                } else {
                    Log.e("userScrolled", "false=");
                }
            }
        });

        if( isFirstCall ) {
            page_counter = 0;
            task_list.clear();
            getTaskListData();
        }
        else
        {
            adapter = new TaskListAdapter(getActivity(), task_list, bean.user_id, bean.auth_token, task_listView);
            task_listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && getActivity() != null) {
//            page_counter = 0;
//            isFirstCall = true;
//            if (lat != 0.0 && lon != 0.0) {
//                getTaskListData();
//            } else {
//                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//
//        Log.e("on resume in", "task method");
//
//        if (!is_service_call) {
//            isFirstCall = true;
//            page_counter = 0;
//            if (lat != 0.0 && lon != 0.0) {
//                getTaskListData();
//            } else {
//                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    public void onRefresh() {
        task_listView.removeFooterView(footer);
        swipe_layout.setRefreshing(false);
//
//        isFirstCall = true;
//        is_service_call = false;
//        page_counter = 0;
//        if (lat != 0.0 && lon != 0.0) {
//            getTaskListData();
//        } else {
//            Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
//        }
    }

    public void refreshData()
    {
        isFirstCall = true;
        is_service_call = false;
        page_counter = 0;
        if( task_list == null )
            task_list = new ArrayList<>();
        task_list.clear();
        getTaskListData();
    }


    private void getTaskListData() {

      /*  final ProgressDialog progDialog = ProgressDialog.show(getActivity(), null, null, false, true);
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progDialog.setContentView(R.layout.progress_dialog_layout);

*/
        String tag = "taskList";
        String url;

        if (filter_type.equalsIgnoreCase("search")) {
            if (max_distance == 0) {
                // url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + AppConstants.mLastLocation.getLatitude() + "&longitude=" + AppConstants.mLastLocation.getLongitude() + "&search_text=" + task_name + "&category=" + category_value + "&max_distance="+ "&currentlocation=" + location + "&start=" + page_counter;

                url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&search_text=" + task_name + "&category=" + category_value + "&max_distance=" + "&currentlocation=" + location + "&start=" + page_counter;
            } else {
                //  url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + AppConstants.mLastLocation.getLatitude() + "&longitude=" + AppConstants.mLastLocation.getLongitude() + "&search_text=" + task_name + "&category=" + category_value + "&max_distance=" + max_distance + "&currentlocation=" + location + "&start=" + page_counter;

                url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&search_text=" + task_name + "&category=" + category_value + "&max_distance=" + "&currentlocation=" + location + "&start=" + page_counter;
            }
        } else {

            // url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + AppConstants.mLastLocation.getLatitude() + "&longitude=" + AppConstants.mLastLocation.getLongitude() + "&sort_by=" + filter_type + "&start=" + page_counter;

            url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&sort_by=" + filter_type + "&start=" + page_counter;
        }

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //progDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    task_listView.removeFooterView(footer);
                    swipe_layout.setRefreshing(false);

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Context cont = getActivity();
                        if( cont != null )
                            Toast.makeText(cont, message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");

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

                            if (resObject.getString("latitude") != null && resObject.getString("latitude").length() > 0) {
                                task_bean.latitude = Double.parseDouble(resObject.getString("latitude"));
                            }
                            if (resObject.getString("longitude") != null && resObject.getString("longitude").length() > 0) {
                                task_bean.longitude = Double.parseDouble(resObject.getString("longitude"));
                            }

                            task_list.add(task_bean);
                        }

                        is_service_call = true;

                        if (isFirstCall) {
                            task_listView.addFooterView(footer);
                            adapter = new TaskListAdapter(getActivity(), task_list, bean.user_id, bean.auth_token, task_listView);
                            task_listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            isFirstCall = false;
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        userScrolled = true;
                        task_listView.removeFooterView(footer);
                        Log.e( "size : ", " --- " + task_list.size() );
                    }
                } catch (IndexOutOfBoundsException ex) {
                    Log.e("exception", ex.toString());
                    ex.printStackTrace();
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

    private class TaskListAdapter extends ListAdapter
    {

        public TaskListAdapter(Activity activity, List<TaskListBean> task_list, String user_id, String auth_token, ListView task_listView) {
            super(activity, task_list, user_id, auth_token, task_listView);
            m_nId = Global.TASK_LIST;
        }
        @Override
        public void refreshData(int nPos, int nKind, String strVal) {
            if( nKind == 1)
                task_list.get(nPos).is_applied = strVal;
            else
                task_list.get(nPos).is_fav = strVal;

            int start = task_listView.getFirstVisiblePosition();
            if( start > nPos )
                return;
            View view = task_listView.getChildAt(nPos-start);
            task_listView.getAdapter().getView(nPos, view, task_listView);

            if( nKind == 0 ) {
                if (strVal.equalsIgnoreCase("0"))
                    ((ImageView) view.findViewById(R.id.star)).setImageResource(R.mipmap.star_2);
                else
                    ((ImageView) view.findViewById(R.id.star)).setImageResource(R.mipmap.star_1);
            }
            else {
                if (strVal.equalsIgnoreCase("0")) {
                    ((TextView) view.findViewById(R.id.task_status)).setText(getString(R.string.apply));
                    view.findViewById(R.id.task_status).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.applied_task_status).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.task_status).setVisibility(View.GONE);
                    view.findViewById(R.id.applied_task_status).setVisibility(View.VISIBLE);
                    if (task_list.get(nPos).is_accepted.equalsIgnoreCase("1"))
                        ((TextView) view.findViewById(R.id.applied_task_status)).setText("Confirmed");
                    else
                        ((TextView) view.findViewById(R.id.applied_task_status)).setText("Applied");
                }
            }
        }
//
//            page_counter = 0;
//            isFirstCall = true;
//            is_service_call = false;
//            onResume();
//        }

    }

    public void updateView(String strApply, String strFavor, String strId)
    {
        int i = 0, nCnt = task_list.size(), nPos = -1;
        for( i = 0 ; i < nCnt ; i ++ )
        {
            if( !task_list.get(i).task_id.equals(strId))
                continue;
            nPos = i;
            break;
        }
        if( nPos < 0 )
            return;
        task_list.get(nPos).is_applied = strApply;
        task_list.get(nPos).is_fav = strFavor;

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

        if( strApply.equalsIgnoreCase("0"))
        {
            ( (TextView) view.findViewById(R.id.task_status) ).setText(getString(R.string.apply));
            view.findViewById(R.id.task_status).setVisibility(View.VISIBLE);
            view.findViewById(R.id.applied_task_status).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.task_status).setVisibility(View.GONE);
            view.findViewById(R.id.applied_task_status).setVisibility(View.VISIBLE);
            if (task_list.get(nPos).is_accepted.equalsIgnoreCase("1"))
                ( (TextView) view.findViewById(R.id.applied_task_status) ).setText("Confirmed");
            else
                ( (TextView) view.findViewById(R.id.applied_task_status) ).setText("Applied");
        }
    }
}
