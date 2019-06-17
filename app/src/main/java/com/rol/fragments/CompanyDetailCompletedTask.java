package com.rol.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.rol.R;
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

public class CompanyDetailCompletedTask extends Fragment {
    ListView task_listView;
    String company_id;
    DBHelper db;
    LoginBean bean;
    List<TaskListBean> task_list;
    private TrackGPS trackGPS;
    private double lat, lon;

    public CompanyDetailCompletedTask() {

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


       /* if (AppConstants.isNetworkAvailable(getActivity()))
        {
            if(AppConstants.mLastLocation !=null)
            {
                getCompanyCompletedTask();

            }
            else
            {
                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
            }
        }*/
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (AppConstants.isNetworkAvailable(getActivity())) {
                if (lat != 0.0 && lon != 0.0) {
                    getCompanyCompletedTask();
                } else {
                    Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
                }
            }
            // getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    private void getCompanyCompletedTask() {
        final ProgressDialog progDialog = ProgressDialog.show(getActivity(), null, null, false, true);
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progDialog.setContentView(R.layout.progress_dialog_layout);

        String tag = "completedTask";
        String url = AppConstants.url + "company_task_list.php?" + "company_id=" + company_id + "&latitude=" + lat
                + "&longitude=" + lon + "&user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&type=" + "completed";

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
//                        Toast.makeText(getActivity(), "Task not found - 1", Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");
                        task_list = new ArrayList<>();
                        task_list.clear();

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
                            task_bean.task_status = resObject.getString("task_status");

                            task_list.add(task_bean);
                        }

//                        CompanyDetailCompletedAdapter adapter = new CompanyDetailCompletedAdapter(getActivity(), task_list);
//                        task_listView.setAdapter(adapter);
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
}
