package com.rol.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

public class CompanyList extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static int page_counter = 0;
    DBHelper db;
    LoginBean bean;
    CompanyListAdapter adapter;
    private ListView company_listView;
    private List<CompanyListBean> company_list;
    private boolean userScrolled = false;
    private View footer;
    private boolean isFirstCall = true;
    private boolean load_company_data = false;
    private SwipeRefreshLayout swipe_layout;
    private TrackGPS trackGPS;
    private double lat, lon;

    public CompanyList() {

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

                    // userScrolled = true;
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
                    Log.e("Position", firstVisibleItem + "-----");
                    if (firstVisibleItem == 0) {
                        swipe_layout.setEnabled(true);
                    } else {
                        swipe_layout.setEnabled(false);
                    }
                }

                if (userScrolled && totalrandom == totalItemCount) {
                    company_listView.addFooterView(footer);
                    userScrolled = false;
                    page_counter += 10;

                    if (AppConstants.isNetworkAvailable(getActivity())) {
                        if (lat != 0.0 && lon != 0.0) {
                            getCompanyListData();
                        } else {
                            Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
                        }
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
            page_counter = 0;
            isFirstCall = true;
            if (lat != 0.0 && lon != 0.0) {

                getCompanyListData();
            } else {
                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("on resume", "method");

        if (!load_company_data) {
            isFirstCall = true;
            page_counter = 0;
            if (lat != 0.0 && lon != 0.0) {

                getCompanyListData();
            } else {
                Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRefresh() {
        isFirstCall = true;
        load_company_data = false;
        page_counter = 0;
        if (lat != 0.0 && lon != 0.0) {
            getCompanyListData();
        } else {
            Toast.makeText(getActivity(), "Location data not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCompanyListData() {

        String tag = "allCompany";
        String url = AppConstants.url + "companylist.php?" + "user_id=" + bean.user_id + "&auth_token=" + bean.auth_token + "&latitude=" + lat + "&longitude=" + lon + "&organiser_id=" + "" + "&type=" + "all" + "&start=" + page_counter;

        url = url.replaceAll(" ", "%20");
        Log.e("list url", url);

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
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray resArray = jsonObject.getJSONArray("responseData");

                        if (!load_company_data) {
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

                            company_list.add(company_bean);
                        }

                        load_company_data = true;

                        if (isFirstCall) {
                            company_listView.addFooterView(footer);
                            adapter = new CompanyListAdapter(getActivity(), company_list, bean.user_id, bean.auth_token, company_listView);
                            company_listView.setAdapter(adapter);
                            isFirstCall = false;
                        } else {
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

    //company list adapter

    public class CompanyListAdapter extends BaseAdapter {

        Activity activity;
        List<CompanyListBean> company_list;
        String user_id, auth_token;
        ListView company_listView;

        public CompanyListAdapter(Activity activity, List<CompanyListBean> company_list, String user_id, String auth_token, ListView company_listView) {
            this.activity = activity;
            this.company_list = company_list;
            this.user_id = user_id;
            this.auth_token = auth_token;
            this.company_listView = company_listView;
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

            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.company_list_item, null);

            TextView company_name = view.findViewById(R.id.company_name);
            TextView company_des = view.findViewById(R.id.comapny_des);
            TextView location = view.findViewById(R.id.location);
            TextView website = view.findViewById(R.id.website);
            TextView distance = view.findViewById(R.id.distance);
            ImageView company_image = view.findViewById(R.id.company_img);
            ImageView fav_unfav_img = view.findViewById(R.id.star);

            if (company_list.get(position).company_name != null && company_list.get(position).company_name.length() > 0) {
                company_name.setText(company_list.get(position).company_name);
            }
            if (company_list.get(position).description != null && company_list.get(position).description.length() > 0) {
                company_des.setText(Html.fromHtml(company_list.get(position).description));
            }
            if (company_list.get(position).company_locatin != null && company_list.get(position).company_locatin.length() > 0) {
                location.setText(company_list.get(position).company_locatin);
            }
            if (company_list.get(position).website != null && company_list.get(position).website.length() > 0) {
                website.setText(company_list.get(position).website);
            }
            if (company_list.get(position).distance != null && company_list.get(position).distance.length() > 0) {
                distance.setText(company_list.get(position).distance + " " + "Km");
            }

            if (company_list.get(position).is_favorite != null && company_list.get(position).is_favorite.length() > 0) {
                if (company_list.get(position).is_favorite.equalsIgnoreCase("1")) {
                    fav_unfav_img.setImageResource(R.mipmap.star_1);
                } else {
                    fav_unfav_img.setImageResource(R.mipmap.star_2);
                }
            }

            if (company_list.get(position).company_image != null && company_list.get(position).company_image.length() > 0) {
                Glide.with(activity)
                        .load(company_list.get(position).company_image)
                        .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                        .into(company_image);
            }

            fav_unfav_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (AppConstants.isNetworkAvailable(activity)) {
                        if (company_list.get(position).is_favorite.equalsIgnoreCase("1")) {
                            addFavoriteCompany(company_list.get(position).company_id, "0", position);
                        } else {
                            addFavoriteCompany(company_list.get(position).company_id, "1", position);
                        }
                    }
                }
            });

           /* website.setMovementMethod(LinkMovementMethod.getInstance());
            website.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse("https://"+company_list.get(position).website));
                    startActivity(browserIntent);
                }
            });*/
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    load_company_data = false;
//                    Intent intent = new Intent(activity, CompanyDetail.class);
//                    intent.putExtra("companyId", company_list.get(position).company_id);
//                    activity.startActivity(intent);
                }
            });

            return view;
        }

        private void addFavoriteCompany(String company_id, final String type, final int pos) {

            String tag = "fav_Company";
            String url = AppConstants.url + "favourite_company.php?" + "user_id=" + user_id + "&auth_token=" + auth_token + "&company_id=" + company_id + "&type=" + type;

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

                          /*  FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(CompanyList.this).attach(CompanyList.this).commit();
*/

                            page_counter = 0;
                            isFirstCall = true;
                            load_company_data = false;
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
