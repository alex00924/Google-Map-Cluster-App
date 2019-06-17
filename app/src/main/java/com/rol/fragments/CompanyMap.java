package com.rol.fragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import com.rol.beans.CompanyListBean;
import com.rol.beans.LoginBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;
import com.rol.utils.TrackGPS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav on 2/7/18.
 */

public class CompanyMap extends Fragment implements OnMapReadyCallback {
    DBHelper db;
    LoginBean bean;
    GoogleMap map;
    List<CompanyListBean> company_list;
    LinearLayout info_window;
    ImageView company_img, fav_img;
    TextView company_name, company_des, location, website;

    LatLngBounds.Builder builder;
    CameraUpdate cu;
    List<Marker> markersList = new ArrayList<Marker>();
    private boolean is_load = false;
    private TrackGPS trackGPS;
    private double lat, lon;

    public CompanyMap() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.company_map, container, false);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

        info_window = view.findViewById(R.id.info_window_layout);
        company_img = view.findViewById(R.id.company_img);
        fav_img = view.findViewById(R.id.star);
        company_name = view.findViewById(R.id.company_name);
        company_des = view.findViewById(R.id.comapny_des);
        location = view.findViewById(R.id.location);
        website = view.findViewById(R.id.website);

        company_list = new ArrayList<>();

//        Company.add_btn.setVisibility(View.VISIBLE);

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

        if (!is_load) {
            SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (lat != 0.0 && lon != 0.0) {
            if (AppConstants.isNetworkAvailable(getActivity())) {
                for (int i = 0; i <= CompanyList.page_counter; i = i + 10) {
                    getCompanyListData(i);
                }
            }
        } else {
            Toast.makeText(getActivity(), "Location data not found.please turn on gps!", Toast.LENGTH_SHORT).show();
        }

        info_window.setVisibility(View.GONE);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                final CompanyListBean cBean = (CompanyListBean) marker.getTag();

                info_window.setVisibility(View.VISIBLE);

//                if (info_window.getVisibility() == View.VISIBLE) {
//                    Company.add_btn.setVisibility(View.GONE);
//                }
                if (cBean.company_name != null && cBean.company_name.length() > 0) {
                    company_name.setText(cBean.company_name);
                }

                if (cBean.description != null && cBean.description.length() > 0) {
                    company_des.setText(cBean.description);
                }

                if (cBean.company_locatin != null && cBean.company_locatin.length() > 0) {
                    location.setText(cBean.company_locatin);
                }

                if (cBean.website != null && cBean.website.length() > 0) {
                    website.setText(cBean.website);
                }

                if (cBean.is_favorite != null && cBean.is_favorite.length() > 0) {
                    if (cBean.is_favorite.equalsIgnoreCase("1")) {
                        fav_img.setImageResource(R.mipmap.star_1);
                    } else {
                        fav_img.setImageResource(R.mipmap.star_2);
                    }
                }

                if (cBean.company_image != null && cBean.company_image.length() > 0) {
                    Glide.with(getActivity())
                            .load(cBean.company_image)
                            .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                            .into(company_img);
                }

                info_window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

//                        Intent intent = new Intent(getActivity(), CompanyDetail.class);
//                        intent.putExtra("companyId", cBean.company_id);
//                        getActivity().startActivity(intent);
                    }
                });

                return true;
            }
        });
    }

    private void getCompanyListData(int counter) {

        String tag = "allCompany";
        String url = AppConstants.url + "companylist.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&organiser_id=" + "" + "&type=" + "all" + "&start=" + counter;

        url = url.replaceAll(" ", "%20");
        Log.e("map url", url);

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

                        if (!is_load) {
                            company_list.clear();
                        }

                        for (int i = 0; i < resArray.length(); i++) {
                            JSONObject resObject = resArray.getJSONObject(i);
                            CompanyListBean company_bean = new CompanyListBean();
                            company_bean.company_id = resObject.getString("comapany_id");
                            company_bean.company_name = resObject.getString("companyname");
                            company_bean.company_image = resObject.getString("companyimage");
                            company_bean.company_locatin = resObject.getString("location");
                            company_bean.website = resObject.getString("website");
                            company_bean.distance = resObject.getString("distance");
                            company_bean.description = resObject.getString("description");
                            company_bean.company_creator_id = resObject.getString("company_creator_id");
                            company_bean.is_favorite = resObject.getString("is_favorited");
                            company_bean.is_user_added = resObject.getString("is_myadded");
                            company_bean.latitude = Double.parseDouble(resObject.getString("latitude"));
                            company_bean.longitude = Double.parseDouble(resObject.getString("longitude"));

                            company_list.add(company_bean);

                            addMarker(company_bean);
                        }

                        is_load = true;

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

    private void addMarker(final CompanyListBean companyBean) {

        LatLng latLng = new LatLng(companyBean.latitude, companyBean.longitude);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_icon_sm)));
        marker.setTag(companyBean);

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

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(getActivity());
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }
}
