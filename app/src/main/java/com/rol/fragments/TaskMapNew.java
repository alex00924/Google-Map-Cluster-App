package com.rol.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class TaskMapNew extends Fragment implements OnMapReadyCallback {
    DBHelper db;
    LoginBean bean;
    String filter_type = "distance";
    GoogleMap map;
    LinearLayout info_window;
    ImageView task_img, fav_img;
    TextView task_name, organiser_name, date_time, location, distance, helper_count, task_status, applied_task_status;
    List<TaskListBean> list;
    LatLngBounds.Builder builder;
    CameraUpdate cu;
    List<Marker> markersList = new ArrayList<Marker>();
    private boolean is_DataLoad = false;
    private TrackGPS trackGPS;
    private double lat, lon;

    public TaskMapNew() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_map, container, false);

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

        list = new ArrayList<>();

        info_window = view.findViewById(R.id.info_window_layout);
        task_img = view.findViewById(R.id.task_cover_img);
        fav_img = view.findViewById(R.id.star);
        task_name = view.findViewById(R.id.task_name);
        organiser_name = view.findViewById(R.id.organization_name);
        location = view.findViewById(R.id.location);
        date_time = view.findViewById(R.id.date_time);
        distance = view.findViewById(R.id.distance);
        helper_count = view.findViewById(R.id.count);
        task_status = view.findViewById(R.id.task_status);
        applied_task_status = view.findViewById(R.id.applied_task_status);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && getActivity() != null) {
            Log.e("on resume active", "method");

            SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("on resume map", "method");

        if (!is_DataLoad) {
            SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        map = googleMap;

        for (int i = 0; i <= TaskList.page_counter; i = i + 10) {
            if (AppConstants.isNetworkAvailable(getActivity())) {
                getTaskListData(i);
            }
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (info_window.getVisibility() == View.VISIBLE) {
                    info_window.setVisibility(View.GONE);
                    Task.add_btn.setVisibility(View.VISIBLE);
                } else {
                    final TaskListBean bean = (TaskListBean) marker.getTag();

                    Task.add_btn.setVisibility(View.GONE);

                    info_window.setVisibility(View.VISIBLE);

                    if (bean.title != null && bean.title.length() > 0) {
                        task_name.setText(bean.title);
                    }
                    if (bean.organiser_name != null && bean.organiser_name.length() > 0) {
                        organiser_name.setText(bean.organiser_name);
                    }

                    if (bean.is_applied != null && bean.is_applied.length() > 0) {
                        if (bean.is_applied.equalsIgnoreCase("1")) {
                            task_status.setVisibility(View.GONE);
                            applied_task_status.setVisibility(View.VISIBLE);
                        } else {
                            task_status.setVisibility(View.VISIBLE);
                            applied_task_status.setVisibility(View.GONE);
                        }
                    }

                    if (bean.is_accepted != null && bean.is_accepted.length() > 0) {
                        if (bean.is_accepted.equalsIgnoreCase("1")) {
                            applied_task_status.setText("Confirmed");
                        }
                    }

                    if (bean.task_image != null && bean.task_image.length() > 0) {
                        Glide.with(getActivity())
                                .load(bean.task_image)
                                .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                                .into(task_img);
                    }
                    if (bean.distance != null && bean.distance.length() > 0) {
                        double result = AppConstants.roundTwoDecimals(Double.parseDouble(bean.distance));
                        distance.setText(result + " " + "Km");
                    }
                    if (bean.location != null && bean.location.length() > 0) {
                        location.setText(bean.location);
                    }

                    if (bean.no_of_helper != null && bean.no_of_helper.length() > 0) {
                        helper_count.setText(bean.no_of_helper + "/" + bean.total_helper);
                    }
                  /*  if (bean.created_date != null && bean.created_date.length() > 0)
                    {
                        date_time.setText(AppConstants.convertTaskdate(bean.created_date));
                    }
                    */

                    if (bean.start_date != null && bean.start_date.length() > 0) {
                        if (bean.start_time != null && bean.start_time.length() > 0) {

                            if (bean.start_time.contains("00:00:00")) {
                                String time = bean.start_date;
                                date_time.setText(AppConstants.convertOnlyTaskdate(time));
                            } else {
                                String time = bean.start_date + " " + bean.start_time;
                                date_time.setText(AppConstants.convertTaskdateTime(time));
                            }
                        }
                    }

                    if (bean.is_fav != null && bean.is_fav.length() > 0) {
                        if (bean.is_fav.equalsIgnoreCase("1")) {
                            fav_img.setImageResource(R.mipmap.star_1);
                        } else {
                            fav_img.setImageResource(R.mipmap.star_2);
                        }
                    }

                    task_status.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            applyTask(bean.task_id);
                        }
                    });

                    info_window.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), TaskDetail.class);
                            intent.putExtra("task_id", bean.task_id);
                            getActivity().startActivity(intent);
                        }
                    });

                    fav_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (bean.is_fav.equalsIgnoreCase("1")) {
                                if (AppConstants.isNetworkAvailable(getActivity())) {
                                    FavUnfavTask(bean.task_id, "0");
                                    bean.is_fav = "0";
                                }
                            } else {
                                if (AppConstants.isNetworkAvailable(getActivity())) {
                                    FavUnfavTask(bean.task_id, "1");
                                    bean.is_fav = "1";
                                }
                            }
                        }
                    });
                }
                return true;
            }
        });
    }

    private void getTaskListData(int counter) {
        String tag = "taskList";
        String url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&sort_by=" + filter_type + "&start=" + counter;

        url = url.replaceAll(" ", "%20");
        Log.e("map task url", url);

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

                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        list = new ArrayList<>();

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
                            task_bean.latitude = Double.parseDouble(resObject.getString("latitude"));
                            task_bean.longitude = Double.parseDouble(resObject.getString("longitude"));
                            task_bean.is_applied = resObject.getString("is_applied");
                            task_bean.is_accepted = resObject.getString("is_accepted");

                            list.add(task_bean);

                            addMarker(task_bean);
                        }

                        is_DataLoad = true;

                        Log.e("map list size", "=====>" + list.size());
                        setAllMarker();
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

    private void addMarker(TaskListBean tBean) {

        LatLng latLng = new LatLng(tBean.latitude, tBean.longitude);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_icon_sm)));
        marker.setTag(tBean);

        markersList.add(marker);
    }

    private void setAllMarker() {
        builder = new LatLngBounds.Builder();
        for (Marker m : markersList) {
            builder.include(m.getPosition());
        }
        int padding = 50;
        LatLngBounds bounds = builder.build();
        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                map.animateCamera(cu);
            }
        });
    }

    private void FavUnfavTask(String task_id, final String type) {
        String tag = "favUnfavTask";
        String url = AppConstants.url + "favouritetask.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&task_id=" + task_id + "&type=" + type;

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
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                        if (type.equalsIgnoreCase("1")) {
                            fav_img.setImageResource(R.mipmap.star_1);
                        } else {
                            fav_img.setImageResource(R.mipmap.star_2);
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

    private void applyTask(String task_id) {
        String tag = "applyTask";
        String url = AppConstants.url + "apply_task.php?" + "task_id=" + task_id + "&user_id=" + bean.user_id + "&auth_token=" + bean.auth_token;

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
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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