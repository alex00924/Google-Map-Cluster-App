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
import com.rol.beans.CompanyListBean;
import com.rol.beans.LoginBean;
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

public class MyCompany extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    DBHelper db;
    LoginBean bean;
    private ListView company_listView;
    private List<CompanyListBean> company_list;
    private boolean userScrolled = false;
    private View footer;
    private int page_counter = 0;
    private boolean isFirstCall = true;
    private boolean load_data = false;
    private MyCompanyAdapter adapter;
    private SwipeRefreshLayout swipe_layout;
    private List<CompanyListBean> new_data;
    private TrackGPS trackGPS;
    private double lat, lon;

    public MyCompany() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view, container, false);

        company_listView = view.findViewById(R.id.data_list);
        swipe_layout = view.findViewById(R.id.swipe_refresh_layout);

        trackGPS = new TrackGPS(getActivity());
        lat = trackGPS.getLatitude();
        lon = trackGPS.getLongitude();

        db = DBHelper.getInstance(getActivity());
        bean = new LoginBean();
        db.open();
        bean = db.getRegisterData();
        db.close();

//        Company.add_btn.setVisibility(View.VISIBLE);

        company_list = new ArrayList<>();

        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_dialog_layout, null, false);

        swipe_layout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipe_layout.setOnRefreshListener(this);

        company_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.e("onscroll", "onScrollStateChanged");
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                    //userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int totalrandom = firstVisibleItem + visibleItemCount;

                if (firstVisibleItem == 0) {
                    swipe_layout.setEnabled(true);
                }

                if (userScrolled && totalrandom == totalItemCount) {
                    company_listView.addFooterView(footer);
                    userScrolled = false;
                    page_counter += 10;

                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        if (lat != 0.0 && lon != 0.0) {
                            getMyCompanyList();
                        } else {
                            Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Location data not found.please turn on gps!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            page_counter = 0;
            isFirstCall = true;
            Log.e("on visible", "method");

            if (lat != 0.0 && lon != 0.0) {
                getMyCompanyList();
            } else {
                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("resume", "method");
        if (!load_data) {
            isFirstCall = true;
            page_counter = 0;

            if (lat != 0.0 && lon != 0.0) {
                getMyCompanyList();
            }
        }
    }

    @Override
    public void onRefresh() {
        isFirstCall = true;
        load_data = false;
        page_counter = 0;

        if (lat != 0.0 && lon != 0.0) {
            getMyCompanyList();
        }
    }

    private void getMyCompanyList() {
        String tag = "myCompany";
        String url = AppConstants.url + "companylist.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&organiser_id=" + bean.user_id + "&type=" + "my" + "&start=" + page_counter;

        url = url.replaceAll(" ", "%20");
        Log.e("my company url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    company_listView.removeFooterView(footer);
                    swipe_layout.setRefreshing(false);

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");
                        if (!load_data) {
                            company_list.clear();
                        }

                        new_data = new ArrayList<>();
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

                            // company_list.add(company_bean);

                            new_data.add(company_bean);
                        }
                        load_data = true;

                        if (isFirstCall) {
                            company_list.clear();
                            for (int i = 0; i < new_data.size(); i++) {
                                company_list.add(new_data.get(i));
                            }
                            company_listView.addFooterView(footer);
                            adapter = new MyCompanyAdapter(getActivity(), company_list, bean.user_id, bean.auth_token);
                            company_listView.setAdapter(adapter);
                            isFirstCall = false;
                        } else {
                            for (int i = 0; i < new_data.size(); i++) {
                                company_list.add(new_data.get(i));
                            }
                            adapter.notifyDataSetChanged();
                        }

                        userScrolled = true;
                        company_listView.removeFooterView(footer);
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

    //my company list adapter

    public class MyCompanyAdapter extends BaseAdapter {
        Activity activity;
        String user_id, auth_token;
        private List<CompanyListBean> company_list;

        public MyCompanyAdapter(Activity activity, List<CompanyListBean> company_list, String user_id, String auth_token) {
            this.activity = activity;
            this.company_list = company_list;
            this.user_id = user_id;
            this.auth_token = auth_token;
        }

        @Override
        public int getCount() {
            return company_list.size();
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

            final LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.my_company_list_item, null);

            TextView company_name = view.findViewById(R.id.company_name);
            TextView company_des = view.findViewById(R.id.comapny_des);
            TextView location = view.findViewById(R.id.location);
            TextView website = view.findViewById(R.id.website);
            ImageView company_image = view.findViewById(R.id.company_img);
            LinearLayout delete_company = view.findViewById(R.id.delete_layout);
            LinearLayout edit_company = view.findViewById(R.id.edit_layout);
            ImageView fav_img = view.findViewById(R.id.star);
            TextView favorite_text = view.findViewById(R.id.favourite);

            if (company_list.get(position).company_name != null && company_list.get(position).company_name.length() > 0) {
                company_name.setText(company_list.get(position).company_name);
            }
            if (company_list.get(position).description != null && company_list.get(position).description.length() > 0) {
                company_des.setText(company_list.get(position).description);
            }
            if (company_list.get(position).company_locatin != null && company_list.get(position).company_locatin.length() > 0) {
                location.setText(company_list.get(position).company_locatin);
            }
            if (company_list.get(position).website != null && company_list.get(position).website.length() > 0) {
                website.setText(company_list.get(position).website);
            }

            if (company_list.get(position).is_favorite != null && company_list.get(position).is_favorite.length() > 0) {
                if (company_list.get(position).is_favorite.equalsIgnoreCase("1")) {
                    fav_img.setVisibility(View.VISIBLE);
                }
            }

            if (company_list.get(position).company_image != null && company_list.get(position).company_image.length() > 0) {
                Glide.with(activity)
                        .load(company_list.get(position).company_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(company_image);
            }

            if (company_list.get(position).is_user_added != null && company_list.get(position).is_user_added.length() > 0) {
                if (company_list.get(position).is_user_added.equalsIgnoreCase("1")) {
                    edit_company.setVisibility(View.VISIBLE);
                    delete_company.setVisibility(View.VISIBLE);
                    favorite_text.setVisibility(View.GONE);
                } else if (company_list.get(position).is_user_added.equalsIgnoreCase("0") && company_list.get(position).is_favorite.equalsIgnoreCase("1")) {
                    edit_company.setVisibility(View.GONE);
                    delete_company.setVisibility(View.GONE);
                    favorite_text.setVisibility(View.VISIBLE);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    load_data = false;
//                    Intent intent = new Intent(activity, CompanyDetail.class);
//                    intent.putExtra("companyId", company_list.get(position).company_id);
//                    activity.startActivity(intent);
                }
            });

            delete_company.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCompanyDialog(company_list.get(position).company_id, position);
                }
            });

            edit_company.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                    Intent intent = new Intent(activity, EditCompany.class);
//                    intent.putExtra("companyId", company_list.get(position).company_id);
//                    activity.startActivity(intent);
                }
            });

            return view;
        }

        //delete company dialog

        private void deleteCompanyDialog(final String company_id, final int pos) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.delete_task_dialog);
            dialog.show();
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            Button yes_btn = dialog.findViewById(R.id.yes_btn);
            Button no_btn = dialog.findViewById(R.id.no_btn);
            TextView title = dialog.findViewById(R.id.title_text);

            title.setText(activity.getString(R.string.delete_organisation_text));

            yes_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    if (AppConstants.isNetworkAvailable(activity)) {
                        deleteCompany(company_id, pos);
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

        private void deleteCompany(String company_id, final int pos) {
            String tag = "deleteCompany";

            String url = AppConstants.url + "deletecompany.php?" + "user_id=" + user_id + "&auth_token=" + auth_token + "&company_id=" + company_id;

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

                           /* FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(MyCompany.this).attach(MyCompany.this).commit();*/
                            Log.e("data position:", pos + "");
                            company_list.remove(pos);
                            adapter.notifyDataSetChanged();
                            page_counter = 0;
                            isFirstCall = true;
                            load_data = false;
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
