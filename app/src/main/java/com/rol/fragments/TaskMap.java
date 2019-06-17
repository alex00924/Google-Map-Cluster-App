package com.rol.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

import net.sharewire.googlemapsclustering.Cluster;
import net.sharewire.googlemapsclustering.ClusterItem;
import net.sharewire.googlemapsclustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Math.abs;

/**
 * Created by keshav on 2/7/18.
 */

public class TaskMap extends Fragment implements OnMapReadyCallback,  SwipeRefreshLayout.OnRefreshListener {

    private final float BOUN_LAT = 0.05f;
    private final float BOUN_LON = 0.2f;

    DBHelper db;
    LoginBean bean;
    String filter_type = "distance";
    GoogleMap map = null;
    public LinearLayout info_window;
    ImageView task_img, fav_img;
    TextView task_name, organiser_name, date_time, location, distance, helper_count, task_status, applied_task_status;
    LatLngBounds.Builder builder;
    CameraUpdate cu;
    List<Marker> markersList = new ArrayList<Marker>();
    SharedPreferences spf;
    String task_name_text, location_text, category_value;
    int max_distance;
    private boolean is_DataLoad = false;
    private TrackGPS trackGPS;
    private double lat, lon;


    private ClusterManager<MyClusterItem>       m_clusterManager;
    private List<MyClusterItem>                 m_listClusterItems;

    List<MyClusterItem>                         m_curCluster;
    private int                                 m_ncurCluster = 0;
    private int                                 m_nStart = 0;

    private boolean                             m_bRun = false;
    private Button                              m_btnPrev;
    private Button                              m_btnNext;
    private Button                              m_btnAll;
    private ListView                            m_listView;
    public View                                 m_listWind;
    private TaskListAdapter                     m_listAdapter;
    private List<TaskListBean>                  m_listData;
//    private View                                m_btnBack;
    private View                                m_btnLay;
    private SwipeRefreshLayout                  swipe_layout;
    public  Task                                m_pParent;
    private List<LatLngBounds >                 m_listVisitArea;
    private LatLng                              m_curLatLon;

    private Task                                m_parentActivity;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable()
    {

        @Override
        public void run() {
            getTaskListData(m_nStart);
        }
    };

    public void setActivity(Task task)
    {
        m_parentActivity = task;
    }

    public void goState()
    {
        int nState = m_parentActivity.m_arrState.get(m_parentActivity.m_arrState.size() - 2);
        if( nState == 13 ) {
            updateInfoView();
        }
        else
        {
            m_listWind.setVisibility(View.VISIBLE);
            info_window.setVisibility(View.GONE);
        }
    }

    public TaskMap() {

    }

    public void startRead()
    {
//        if( map != null )
//            map.clear();
//
//        m_bRun = true;
//        Toast.makeText(getActivity(), "Please wait while read data", Toast.LENGTH_SHORT).show();
//        m_listClusterItems.clear();
//        m_ncurCluster = 0;
//        m_curCluster.clear();
//        m_nStart = 0;
//        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void stopRead()
    {
//        m_bRun = false;
//        m_nStart = 0;
//        timerHandler.removeCallbacks(timerRunnable);
//        m_listClusterItems.clear();
//        m_ncurCluster = 0;
//        m_curCluster.clear();
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

        m_listClusterItems = new ArrayList<>();
        m_listVisitArea = new ArrayList<>();

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

        Task.add_btn.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getActivity().getSharedPreferences("my_pref", MODE_PRIVATE);
        filter_type = prefs.getString("type", "distance");

        spf = getActivity().getSharedPreferences("search_pf", MODE_PRIVATE);
        task_name_text = spf.getString("task_name", "");
        location_text = spf.getString("location", "");
        category_value = spf.getString("category", "");
        max_distance = spf.getInt("max_distance", 0);

        m_btnPrev = view.findViewById(R.id.btn_Prev);
        m_btnNext = view.findViewById(R.id.btn_Next);
        m_btnAll = view.findViewById(R.id.btn_All);
//        m_btnBack = view.findViewById(R.id.btn_back);
        m_btnLay = view.findViewById(R.id.btn_lay);
        m_listView = view.findViewById(R.id.data_list);
        m_listWind = view.findViewById(R.id.wnd_show);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);
        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);
        swipe_layout.setEnabled(false);
        m_listData = new ArrayList<>();
        m_listAdapter = new TaskListAdapter(getActivity(), m_listData, bean.user_id, bean.auth_token, m_listView);
        m_listView.setAdapter(m_listAdapter);

        m_btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( m_ncurCluster < 1 )
                    return;
                m_ncurCluster--;
                updateInfoView();
            }
        });


        m_btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( m_ncurCluster > m_curCluster.size() - 1 )
                    return;
                m_ncurCluster++;
                updateInfoView();
            }
        });

//        m_btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                m_listWind.setVisibility(View.GONE);
//            }
//        });

        m_btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_parentActivity.m_arrState.add(14);
                showListView();
            }
        });

        setupMapFragment();
        m_nStart = 0;

        info_window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_parentActivity.m_arrState.add(0);
                Intent intent = new Intent(getActivity(), TaskDetail.class);
                intent.putExtra("task_id", m_curCluster.get(m_ncurCluster).task_id);
                getActivity().startActivityForResult(intent, Global.TASK_MAP);
            }
        });

        m_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
//                if( i == 0 )
//                    swipe_layout.setEnabled(true);
//                else
//                    swipe_layout.setEnabled(false);
            }
        });

        m_curCluster = new ArrayList<>();

        return view;
    }

    private void showListView()
    {
        //Set data to list view.
        int i = 0, nCnt = m_curCluster.size();
        m_listData.clear();
        for( i = 0 ; i < nCnt ; i ++ )
        {
            TaskListBean newData = new TaskListBean();
            newData.task_id = m_curCluster.get(i).task_id;
            newData.organiser_id = m_curCluster.get(i).organiser_id;
            newData.organiser_name = m_curCluster.get(i).organiser_name;
            newData.organiser_image = m_curCluster.get(i).organiser_image;
            newData.task_image = m_curCluster.get(i).task_image;
            newData.location = m_curCluster.get(i).str_location;
            newData.created_date = m_curCluster.get(i).created_date;
            newData.start_date = m_curCluster.get(i).start_date;
            newData.start_time = m_curCluster.get(i).start_time;
            newData.no_of_helper = m_curCluster.get(i).no_of_helper;
            newData.distance = m_curCluster.get(i).distance;
            newData.is_fav = m_curCluster.get(i).is_fav;
            newData.description = m_curCluster.get(i).description;
            newData.total_helper = m_curCluster.get(i).total_helper;
            newData.latitude = m_curCluster.get(i).latitude;
            newData.longitude = m_curCluster.get(i).longitude;
            newData.is_added = m_curCluster.get(i).is_added;
            newData.is_applied = m_curCluster.get(i).is_applied;
            newData.is_recommanded = m_curCluster.get(i).is_recommanded;
            newData.task_status = m_curCluster.get(i).task_status;
            newData.is_accepted = m_curCluster.get(i).is_accepted;
            newData.total_comments = m_curCluster.get(i).total_comments;
            newData.total_confirmed_helper = m_curCluster.get(i).total_confirmed_helper;
            newData.is_accepted = m_curCluster.get(i).is_accepted;
            newData.is_friend = m_curCluster.get(i).is_friend;
            newData.title = m_curCluster.get(i).title;
            m_listData.add(newData);
        }
        m_listAdapter.notifyDataSetChanged();
        info_window.setVisibility(View.GONE);
        m_listWind.setVisibility(View.VISIBLE);
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && getActivity() != null) {
            setupMapFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!is_DataLoad) {
            setupMapFragment();
        }
    }

    private void updateInfoView()
    {
        Task.add_btn.setVisibility(View.GONE);

        info_window.setVisibility(View.VISIBLE);
        if( m_ncurCluster > m_curCluster.size() - 1 && m_curCluster.size() != 1) {
            m_ncurCluster = m_curCluster.size() - 1;
            return;
        }
        if( m_ncurCluster < 0 )
        {
            m_ncurCluster = 0;
            return;
        }
        final MyClusterItem item = m_curCluster.get(m_ncurCluster);

        if (m_curCluster.get(m_ncurCluster).organiser_name != null && item.organiser_name.length() > 0) {
            organiser_name.setText(item.organiser_name);
        }

        if (item.title != null && item.title.length() > 0) {
            task_name.setText(item.title);
        }

        if (item.is_applied != null && item.is_applied.length() > 0) {
            if (item.is_applied.equalsIgnoreCase("1")) {
                task_status.setVisibility(View.GONE);
                applied_task_status.setVisibility(View.VISIBLE);
            } else {
                task_status.setText(getActivity().getString(R.string.apply));
                task_status.setVisibility(View.VISIBLE);
                applied_task_status.setVisibility(View.GONE);
            }
        }

        if (item.is_accepted != null && item.is_accepted.length() > 0) {
            if (item.is_accepted.equalsIgnoreCase("1")) {
                applied_task_status.setText("Confirmed");
            }
        }

        if (item.task_image != null && item.task_image.length() > 0) {
            Glide.with(getActivity())
                    .load(item.task_image)
                    .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                    .into(task_img);
        }

        if (item.distance != null && item.distance.length() > 0) {

            double result = Double.parseDouble(item.distance);
            distance.setText(result + "Km");
        }

        if (item.location != null && item.str_location.length() > 0) {
            location.setText(item.str_location);
        }

        if (item.no_of_helper != null && item.no_of_helper.length() > 0) {
            helper_count.setText(item.no_of_helper + "/" + item.total_helper);
        }

        if (item.start_date != null && item.start_date.length() > 0) {
            if (item.start_time != null && item.start_time.length() > 0) {

                if (item.start_time.contains("00:00:00")) {
                    String time = item.start_date;
                    date_time.setText(AppConstants.convertOnlyTaskdate(time));
                } else {
                    String time = item.start_date + " " + item.start_time;
                    date_time.setText(AppConstants.convertTaskdateTime(time));
                }
            }
        }

        if (item.is_fav != null && item.is_fav.length() > 0) {
            if (item.is_fav.equalsIgnoreCase("1")) {
                fav_img.setImageResource(R.mipmap.star_1);
            } else {
                fav_img.setImageResource(R.mipmap.star_2);
            }
        }

        fav_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.isNetworkAvailable(getActivity())) {

                    if (item.is_fav.equalsIgnoreCase("1")) {
                        FavUnfavTask(item.task_id, "0");
                    } else {
                        FavUnfavTask(item.task_id, "1");
                    }
                }
            }
        });

        task_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.isNetworkAvailable(getActivity())) {
                    applyTask(item.task_id);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if( map != null )
            return;
        map = googleMap;
        m_clusterManager = new ClusterManager<>(getContext(), map);
        final LatLngBounds  bounds = new LatLngBounds(
                new LatLng(lat-BOUN_LAT, lon-BOUN_LON), new LatLng(lat+BOUN_LAT, lon+BOUN_LON));
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
        m_listVisitArea.add(bounds);

//        m_listClusterItems.clear();
        m_clusterManager.setCallbacks(new ClusterManager.Callbacks<MyClusterItem>() {
            @Override
            public boolean onClusterClick(@NonNull Cluster<MyClusterItem> cluster) {
                if( m_parentActivity.m_arrState.size() > 0 )
                {
                    while (m_parentActivity.m_arrState.get(m_parentActivity.m_arrState.size() - 1) > 12) {
                        m_parentActivity.m_arrState.remove(m_parentActivity.m_arrState.size() - 1);
                        if( m_parentActivity.m_arrState.size() <= 1 )
                            break;
                    }
                }
                m_parentActivity.m_arrState.add(13);

//                calculateDist();

                m_ncurCluster = 0;
                m_curCluster.clear();
                List<MyClusterItem> items= cluster.getItems();
                int i = 0, nCnt = items.size();
                for( i = 0 ; i < nCnt ; i ++ ) {
                    m_curCluster.add(items.get(i));
                }
            //sgs
                m_btnAll.setVisibility(View.VISIBLE);
                m_btnLay.setVisibility(View.VISIBLE);
                m_listWind.setVisibility(View.GONE);
                updateInfoView();
                return true;
            }

            @Override
            public boolean onClusterItemClick(@NonNull MyClusterItem clusterItem) {
                if( m_parentActivity.m_arrState.size() > 0 ) {
                    if (m_parentActivity.m_arrState.get(m_parentActivity.m_arrState.size() - 1) > 12)
                        m_parentActivity.m_arrState.add(13);
                    else
                        m_parentActivity.m_arrState.set(m_parentActivity.m_arrState.size() - 1, 13);
                }
//                calculateDist();
                m_ncurCluster = 0;
                m_curCluster.clear();
                m_btnAll.setVisibility(View.GONE);
                m_btnLay.setVisibility(View.GONE);
                m_listWind.setVisibility(View.GONE);
                m_curCluster.add(clusterItem);
                updateInfoView();
                return true;
            }
        });
//        googleMap.setOnCameraIdleListener(m_clusterManager);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                info_window.setVisibility(View.GONE);
                m_listWind.setVisibility(View.GONE);
                Task.add_btn.setVisibility(View.VISIBLE);
            }
        });

        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                info_window.setVisibility(View.GONE);
                m_listWind.setVisibility(View.GONE);
                Task.add_btn.setVisibility(View.VISIBLE);
            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                m_curLatLon = map.getCameraPosition().target;
                //clustering current markers.
                m_clusterManager.setItems(m_listClusterItems);
                m_clusterManager.cluster();
                m_nStart = 0;
                LatLngBounds curBounds = map.getProjection().getVisibleRegion().latLngBounds;
                int i = 0;
                for( i = 0 ; i < m_listVisitArea.size(); i ++ )
                {
                    if( m_listVisitArea.get(i).contains(curBounds.northeast) && m_listVisitArea.get(i).contains(curBounds.southwest))
                        return;
                }
                m_listVisitArea.add(curBounds);
                timerHandler.postDelayed(timerRunnable, 0);

//                LatLngBounds curBounds = map.getProjection().getVisibleRegion().latLngBounds;
//                LatLng curCentter =  curBounds.getCenter();


//                getTaskListData(0);
            }
        });
       info_window.setVisibility(View.GONE);
    }

    private static final Random RANDOM = new Random();

    @NonNull
    static LatLng generate(@NonNull LatLngBounds bounds) {
        double minLatitude = bounds.southwest.latitude;
        double maxLatitude = bounds.northeast.latitude;
        double minLongitude = bounds.southwest.longitude;
        double maxLongitude = bounds.northeast.longitude;
        return new LatLng(
                minLatitude + (maxLatitude - minLatitude) * RANDOM.nextDouble(),
                minLongitude + (maxLongitude - minLongitude) * RANDOM.nextDouble());
    }

    private boolean checkDoubleData(String strId)
    {
        int  i = 0;
        for( i = 0 ; i < m_listClusterItems.size() ; i ++ )
        {
            if( m_listClusterItems.get(i).task_id.equals(strId) )
                return true;
        }
        return false;
    }

    private void getTaskListData(int counter) {
//        if( !m_bRun )
//            return;

        LatLngBounds curBounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng curCentter =  curBounds.getCenter();
        double maxDist = ( curBounds.northeast.latitude - curBounds.southwest.latitude ) * 111;

        String tag = "taskMap";


        String url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + curCentter.latitude + "&longitude=" + curCentter.longitude + "&sort_by=distance" + filter_type + "&start=" + counter + "&max_distance="+maxDist;
//        if (filter_type.equalsIgnoreCase("search")) {
//            if (max_distance == 0) {
//                url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&search_text=" + task_name_text + "&category=" + category_value + "&max_distance=" + "&currentlocation=" + location_text + "&start=" + counter;
//            } else {
//                url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&search_text=" + task_name_text + "&category=" + category_value + "&max_distance=" + max_distance + "&currentlocation=" + location_text + "&start=" + counter;
//            }
//        } else {
//
//            url = AppConstants.url + "all_task.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&sort_by=" + filter_type + "&start=" + counter;
//        }

        url = url.replaceAll(" ", "%20");

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                if( !m_bRun )
//                    return;
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        setAllMarker();
                        timerHandler.removeCallbacks(timerRunnable);
                        return;
                    }
                    else {

                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        boolean bLatLon = false;

                        int k = 0;
                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            if(checkDoubleData(resObject.getString("id")))
                                continue;
                            MyClusterItem item = new MyClusterItem(new LatLng(0, 0));
                            item .task_id = resObject.getString("id");
                            item .organiser_id = resObject.getString("organiser_id");
                            item .organiser_name = resObject.getString("organiser_name");
                            item .organiser_image = resObject.getString("organiser_image");
                            item .title = resObject.getString("title");
                            item .task_image = resObject.getString("image");
                            item .str_location= resObject.getString("location");
                            item .created_date = resObject.getString("created_date");
                            item .start_date = resObject.getString("start_date");
                            item .start_time = resObject.getString("start_time");
                            item .no_of_helper = resObject.getString("no_of_helper");
                            item .distance = resObject.getString("distance");
                            item .is_fav = resObject.getString("is_favourite");
                            item .total_helper = resObject.getString("total_helper");
                            item .is_applied = resObject.getString("is_applied");
                            item .is_accepted = resObject.getString("is_accepted");

                            bLatLon = true;
                            try
                            {
                                if (resObject.getString("longitude") != null ) {
                                    if (resObject.getString("longitude").length() > 0) {
                                        Double dLat = Double.parseDouble(resObject.getString("longitude"));
                                        String strLon = String.format("%.4f", dLat);
                                        item.longitude = Double.valueOf(strLon);
                                    }
                                }
                                else
                                    bLatLon = false;

                                if ( resObject.getString("latitude") != null ) {
                                    if (resObject.getString("latitude").length() > 0) {
                                        Double dLat = Double.parseDouble(resObject.getString("latitude"));
                                        String strLat = String.format("%.3f", dLat);
                                        item.latitude = Double.valueOf(strLat);
                                    } else
                                        bLatLon = false;
                                }
                            }
                            catch (NumberFormatException e)
                            {
                                Log.e("Lat/lon error", e.toString());
                                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }

                            if( bLatLon )
                            {
                                item.setLatLong();
                                item.distance = getDist(item.latitude, item.longitude);
                                item.nIndx = m_listClusterItems.size();
                                m_listClusterItems.add(item );
                            }

//                            addMarker(task_bean);
                        }
                        m_nStart += 10;
                        setAllMarker();
                        timerHandler.postDelayed(timerRunnable, 0);
                        is_DataLoad = true;
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
//        if( !m_bRun )
//            return;
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

        if( m_listClusterItems.size() < 1 )
            return;
        m_clusterManager.setItems(m_listClusterItems);
//        m_clusterManager.cluster();
//        Toast.makeText(getActivity(), "tot:" + m_listClusterItems.size(), Toast.LENGTH_LONG).show();
//        final LatLngBounds  bounds = new LatLngBounds(
//                new LatLng(lat-BOUN_LAT, lon-BOUN_LON), new LatLng(lat+BOUN_LAT, lon+BOUN_LON));
//        builder = new LatLngBounds.Builder();
//
//        for (MyClusterItem  m : m_listClusterItems) {
//            builder.include(m.location);
//        }
//        int padding = 50;
//        LatLngBounds bounds = builder.build();
//        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                //map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
//            }
//        });
        return;
/*
        if( m_listClusterItems.size() < 1 )
            return;
        builder = new LatLngBounds.Builder();

        for (MyClusterItem  m : m_listClusterItems) {
            builder.include(m.location);
        }
        int padding = 50;
        LatLngBounds bounds = builder.build();
        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
//                map.animateCamera(cu);
            }
        });

        */
    }

    private void FavUnfavTask(String task_id, final String type) {
        String tag = "favUnfavTask";
        String url = AppConstants.url + "favouritetask.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&task_id=" + task_id + "&type=" + type;

        url = url.replaceAll(" ", "%20");

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String message = jsonObject.getString("message");
                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                        Task curTast = (Task)getActivity();
                        if( message.contains("Un") )
                            curTast.m_strFavor = "0";
                        else
                            curTast.m_strFavor = "1";

                        curTast.m_bChanged = true;
                        curTast.m_strApply = m_listClusterItems.get(m_curCluster.get(m_ncurCluster).nIndx).is_applied;
                        curTast.m_strId = m_listClusterItems.get(m_curCluster.get(m_ncurCluster).nIndx).task_id;
                        curTast.updateAllViews();

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

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String message = jsonObject.getString("message");
                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//                        task_status.setVisibility(View.GONE);
//                        applied_task_status.setVisibility(View.VISIBLE);
//                        m_curCluster.get(m_ncurCluster).task_status = "1";
//                        m_listClusterItems.get(m_curCluster.get(m_ncurCluster).nIndx).task_status = "1";
                        Task curTast = (Task)getActivity();
                        curTast.m_bChanged = true;
                        curTast.m_strApply = "1";
                        curTast.m_strFavor =m_listClusterItems.get(m_curCluster.get(m_ncurCluster).nIndx).is_fav;
                        curTast.m_strId = m_listClusterItems.get(m_curCluster.get(m_ncurCluster).nIndx).task_id;
                        curTast.updateAllViews();

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

    @Override
    public void onRefresh() {
    }

    private class TaskListAdapter extends ListAdapter
    {

        public TaskListAdapter(Activity activity, List<TaskListBean> task_list, String user_id, String auth_token, ListView task_listView) {
            super(activity, task_list, user_id, auth_token, task_listView);
            m_nId = Global.TASK_MAP;
        }
        @Override
        public void refreshData(int nPos, int nKind, String strVal) {
            if( nKind == 0 )
            {
                m_listClusterItems.get(m_curCluster.get(nPos).nIndx).is_fav = strVal;
            }
            else
            {
                m_listClusterItems.get(m_curCluster.get(nPos).nIndx).is_applied = strVal;
            }
        }

    }

    private void calculateDist()
    {
        int i , nCnt;
        if( m_curLatLon == null )
            return;

        nCnt = m_listClusterItems.size();
        for( i = 0 ; i < nCnt ; i ++ )
        {
            double dist = getDistanceFromLatLonInKm(m_curLatLon.latitude, m_curLatLon.longitude, m_listClusterItems.get(i).latitude, m_listClusterItems.get(i).longitude);
            m_listClusterItems.get(i).distance = String.format("%.2f", dist);
        }
    }

    private String getDist(double dLat, double dLon)
    {
        double gpsLat = trackGPS.getLatitude();
        double gpsLon = trackGPS.getLongitude();
        double dist = getDistanceFromLatLonInKm(gpsLat, gpsLon, dLat,dLon);
        return String.format("%.2f", dist);
    }

    //Lat to Km
    private double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }


    public void refreshData()
    {
        m_nStart = 0;
        m_listData.clear();
        m_listVisitArea.clear();
        m_listClusterItems.clear();
        getTaskListData(m_nStart);
        info_window.setVisibility(View.GONE);
        m_listWind.setVisibility(View.GONE);
    }

    public void updateView(String strApply, String strFavor, String strId)
    {
        int i = 0;
        if( m_curCluster != null ) {
            for (i = 0; i < m_curCluster.size(); i++) {
                if ( m_curCluster.get(i).task_id.equals(strId) )
                {
                    m_curCluster.get(i).is_fav = strFavor;
                    m_curCluster.get(i).is_applied = strApply;
                }
            }
            if( info_window.getVisibility() == View.VISIBLE )
                updateInfoView();
            else if( m_listWind.getVisibility() == View.VISIBLE )
                showListView();
        }

        if( m_listClusterItems == null )
            return;

        for( i = 0; i < m_listClusterItems.size() ; i ++  )
        {
            if ( m_listClusterItems.get(i).task_id.equals(strId) )
            {
                m_listClusterItems.get(i).is_fav = strFavor;
                m_listClusterItems.get(i).is_applied = strApply;
            }
        }

    }
}