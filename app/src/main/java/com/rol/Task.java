package com.rol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.beans.SelectCategoryBean;
import com.rol.fragments.MyTask;
import com.rol.fragments.TaskList;
import com.rol.fragments.TaskMap;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.TrackGPS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.rol.Global.TASK_LIST;
import static com.rol.Global.TASK_MAP;
import static com.rol.Global.TASK_MY;

public class Task extends Base {

    private TaskMap     m_frgMap;
    private TaskList    m_frgList;
    private MyTask      m_frgMy;

    public static FloatingActionButton add_btn;
    TabLayout tabs;
    ViewPager viewPager;
    TextView one, two, three;
    Activity activity = this;
    ViewPagerAdapter adapter;
    SharedPreferences type_pref, search_pref;
    SharedPreferences.Editor editor, search_editor;
    Calendar cal;
    Spinner category_spinner = null;
    List<SelectCategoryBean> category_list;
    ImageView map_img;
    TextView current_location, selected_cat;
    SeekBar distance_seekbar;
    TrackGPS trackGPS;
    private int mYear, mMonth, mDay;
    private String date_time;
    private String category_value, all_category_name, page_name = "";
    private double lat, lon;
    public  List<Integer> m_arrState = new ArrayList<>();

    @Override
    protected void goBack()
    {
        int nSize = m_arrState.size();
        if( nSize < 2 )     //if end, finish
        {
            m_arrState.clear();
            finish();
            return;
        }

        int nState = m_arrState.get(nSize - 2);   //Prev state
        if( nState == 10)
        {
            viewPager.setCurrentItem(0);
            setting_btn.setVisibility(View.VISIBLE);
            search_btn.setVisibility(View.VISIBLE);
            logout_btn.setVisibility(View.VISIBLE);
            one.setTextColor(getResources().getColor(R.color.base_header_color));
            two.setTextColor(getResources().getColor(R.color.gray));
            three.setTextColor(getResources().getColor(R.color.gray));
        } else if (nState == 12) {
            viewPager.setCurrentItem(2);
            setting_btn.setVisibility(View.GONE);
            logout_btn.setVisibility(View.VISIBLE);
            search_btn.setVisibility(View.GONE);
            three.setTextColor(getResources().getColor(R.color.base_header_color));
            two.setTextColor(getResources().getColor(R.color.gray));
            one.setTextColor(getResources().getColor(R.color.gray));
        }
        else {
            viewPager.setCurrentItem(1);
            setting_btn.setVisibility(View.GONE);
            logout_btn.setVisibility(View.VISIBLE);
            search_btn.setVisibility(View.GONE);
            two.setTextColor(getResources().getColor(R.color.base_header_color));
            one.setTextColor(getResources().getColor(R.color.gray));
            three.setTextColor(getResources().getColor(R.color.gray));
            m_frgMap.m_listWind.setVisibility(View.GONE);
            m_frgMap.info_window.setVisibility(View.GONE);
            if (nState != 11) {
                add_btn.setVisibility(View.GONE);
                m_frgMap.goState();
            }
            else
                add_btn.setVisibility(View.VISIBLE);
        }

        m_arrState.remove(nSize - 1);
        m_arrState.remove(nSize - 1);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_nBack = 1;
        m_arrState.add(10);              //Set Task List at first

        getLayoutInflater().inflate(R.layout.company, wrapper);
        base_title.setText(R.string.task);
        task.setImageResource(R.mipmap.task_blue);

        search_btn.setVisibility(View.VISIBLE);
        setting_btn.setVisibility(View.VISIBLE);

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);

        add_btn = findViewById(R.id.add_btn);

        type_pref = getSharedPreferences("my_pref", MODE_PRIVATE);
        search_pref = getSharedPreferences("search_pf", MODE_PRIVATE);

        cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!statusOfGPS) {
            showSettingsAlert();
        } else {
            //buildGoogleApiClient();

            trackGPS = new TrackGPS(Task.this);
            lat = trackGPS.getLatitude();
            lon = trackGPS.getLongitude();

            Intent intent = getIntent();
            if (intent.hasExtra("pageName")) {
                page_name = intent.getStringExtra("pageName");
            }

            setUpViewPager(viewPager);
            tabs.setupWithViewPager(viewPager);

            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
            one = view.findViewById(R.id.tab_title);
            one.setText(R.string.list);
            one.setTextColor(getResources().getColor(R.color.base_header_color));
            tabs.getTabAt(0).setCustomView(view);

            View view1 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
            two = view1.findViewById(R.id.tab_title);
            two.setText(R.string.map);
            tabs.getTabAt(1).setCustomView(view1);

            View view2 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
            three = view2.findViewById(R.id.tab_title);
            three.setText(R.string.my);
            tabs.getTabAt(2).setCustomView(view2);

            if (page_name.equalsIgnoreCase("mytask")) {
                viewPager.setCurrentItem(2, true);
                setting_btn.setVisibility(View.GONE);
                logout_btn.setVisibility(View.VISIBLE);
                search_btn.setVisibility(View.GONE);
                three.setTextColor(getResources().getColor(R.color.base_header_color));
                two.setTextColor(getResources().getColor(R.color.gray));
                one.setTextColor(getResources().getColor(R.color.gray));
            } else {
                viewPager.setCurrentItem(0, true);
            }
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if( bOperating )
                    return;

                viewPager.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() == 0) {
                    m_arrState.add(10);
                    setting_btn.setVisibility(View.VISIBLE);
                    search_btn.setVisibility(View.VISIBLE);
                    logout_btn.setVisibility(View.VISIBLE);
                    one.setTextColor(getResources().getColor(R.color.base_header_color));
                    two.setTextColor(getResources().getColor(R.color.gray));
                    three.setTextColor(getResources().getColor(R.color.gray));
                } else if (tab.getPosition() == 1) {
                    if(m_arrState.size() < 1 )
                        m_arrState.add(10);
                    m_arrState.add(11);
                    setting_btn.setVisibility(View.GONE);
                    logout_btn.setVisibility(View.VISIBLE);
                    search_btn.setVisibility(View.GONE);
                    two.setTextColor(getResources().getColor(R.color.base_header_color));
                    one.setTextColor(getResources().getColor(R.color.gray));
                    three.setTextColor(getResources().getColor(R.color.gray));

                } else if (tab.getPosition() == 2) {
                    if(m_arrState.size() < 1 )
                        m_arrState.add(10);
                    m_arrState.add(12);
                    setting_btn.setVisibility(View.GONE);
                    logout_btn.setVisibility(View.VISIBLE);
                    search_btn.setVisibility(View.GONE);
                    three.setTextColor(getResources().getColor(R.color.base_header_color));
                    two.setTextColor(getResources().getColor(R.color.gray));
                    one.setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(Task.this, AddTask.class);
//                startActivity(intent);
            }
        });

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupWindow popupWindow = new PopupWindow(activity);

                // inflate your layout or dynamically add view
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View v1 = inflater.inflate(R.layout.filter_task_menu, null);
                popupWindow.setBackgroundDrawable(null);
                popupWindow.setFocusable(true);
                popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(v1);
                popupWindow.showAsDropDown(base_header);

                LinearLayout newest = v1.findViewById(R.id.newest);
                LinearLayout date_filter = v1.findViewById(R.id.date);
                LinearLayout distance = v1.findViewById(R.id.distance);

                newest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();

                        Intent intent1 = new Intent(Task.this, Task.class);
                        editor = type_pref.edit();
                        editor.putString("type", "newest");
                        editor.apply();
                        startActivity(intent1);
                    }
                });

                date_filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();

                        Intent intent1 = new Intent(Task.this, Task.class);
                        editor = type_pref.edit();
                        editor.putString("type", "date");
                        editor.apply();
                        startActivity(intent1);
                    }
                });

                distance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();

                        Intent intent1 = new Intent(Task.this, Task.class);
                        editor = type_pref.edit();
                        editor.putString("type", "distance");
                        editor.apply();
                        startActivity(intent1);
                    }
                });
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupWindow popupWindow = new PopupWindow(Task.this);

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View v1 = inflater.inflate(R.layout.search_task, null);

                Button reset_btn = v1.findViewById(R.id.reset_btn);
                Button apply_btn = v1.findViewById(R.id.apply_btn);
                final TextView distance_progress = v1.findViewById(R.id.distance_progress);
                final EditText task_name = v1.findViewById(R.id.task_name);
                // category_spinner = v1.findViewById(R.id.category_spinner);
                RelativeLayout select_cat = v1.findViewById(R.id.select_cat);
                distance_seekbar = v1.findViewById(R.id.distance_seekbar);
                current_location = v1.findViewById(R.id.location);
                selected_cat = v1.findViewById(R.id.selected_cat);
                map_img = v1.findViewById(R.id.map_icon);

                popupWindow.setBackgroundDrawable(null);
                popupWindow.setFocusable(true);
                popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(v1);
                popupWindow.showAsDropDown(base_header);

                if (AppConstants.isNetworkAvailable(Task.this)) {
                    getCategoryData();
                }

                map_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        setCurrentlocation();
                    }
                });

                select_cat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openCategoryDialog();
                    }
                });

                distance_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        distance_progress.setText(progress + " " + "Km");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

/*
                category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(category_list.get(i).category_name.equals("Select Category"))
                        {

                        }
                        else
                        {
                            category_value=category_list.get(i).category_name;

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
*/

                apply_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String task_name_text = task_name.getText().toString();
                        String current_loc = current_location.getText().toString();
                        int distance = distance_seekbar.getProgress();

                        Intent intent1 = new Intent(Task.this, Task.class);
                        editor = type_pref.edit();
                        editor.putString("type", "search");
                        editor.apply();
                        search_editor = search_pref.edit();
                        search_editor.putString("task_name", task_name_text);
                        search_editor.putString("location", current_loc);
                        search_editor.putString("category", category_value);
                        search_editor.putInt("max_distance", distance);
                        search_editor.apply();
                        startActivity(intent1);
                    }
                });

                reset_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        task_name.setText("");
                        current_location.setText("");
                        // category_spinner.setSelection(0);
                        selected_cat.setText("Select Category");
                        distance_seekbar.setProgress(0);
                        editor = type_pref.edit();
                        editor.putString("type", "distance");
                        editor.apply();
                    }
                });
            }
        });
    }

    private void setUpViewPager(ViewPager viewpager) {

        m_frgMap = new TaskMap();
        m_frgList = new TaskList();
        m_frgMy = new MyTask();
        m_frgMap.setActivity(this);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(m_frgList);
        adapter.addFragment(m_frgMap);
        adapter.addFragment(m_frgMy);

        viewpager.setAdapter(adapter);
        viewpager.setOffscreenPageLimit(3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        user_detail.setImageResource(R.mipmap.user);
        task.setImageResource(R.mipmap.task_blue);
        company.setImageResource(R.mipmap.company);
        friends.setImageResource(R.mipmap.friends);
        chat.setImageResource(R.mipmap.chat);
    }

    private void setCurrentlocation() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {

            addresses = geocoder.getFromLocation(lat, lon, 1);

            //addresses = geocoder.getFromLocation(22.3039,70.8022, 1);

            if (addresses != null) {
                String address = addresses.get(0).getAddressLine(0);
                current_location.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //get category list
    private void getCategoryData() {
        String tag = "getCategory";
        String url = AppConstants.url + "categorylist.php?" + "user_id=" + userId + "&auth_token=" + authToken;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(Task.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        category_list = new ArrayList<>();
                        category_list.clear();
                        JSONArray resArray = jsonObject.getJSONArray("responsedata");
                        for (int i = 0; i <= resArray.length(); i++) {
                            SelectCategoryBean categoryBean = new SelectCategoryBean();

                            if (i == 0) {
                                categoryBean.category_id = "";
                                categoryBean.category_name = "Select Category";
                                category_list.add(categoryBean);
                            } else {
                                JSONObject resObject = resArray.getJSONObject(i - 1);
                                categoryBean.category_id = resObject.getString("id");
                                categoryBean.category_name = resObject.getString("categoryname");

                                category_list.add(categoryBean);
                            }
                        }

                       /* categorySpinnerAdapter adapter = new categorySpinnerAdapter(getApplication(), category_list);
                         category_spinner.setAdapter(adapter);
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

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }

    private void openCategoryDialog() {
        category_value = "";
        all_category_name = "";
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.select_category_dialog);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView confirm_btn = dialog.findViewById(R.id.ok_btn);
        TextView cancel_btn = dialog.findViewById(R.id.cancel_btn);
        final ListView cat_list = dialog.findViewById(R.id.category_list);

        categorySpinnerAdapter adapter = new categorySpinnerAdapter(getApplication(), category_list);
        // category_spinner.setAdapter(adapter);
        cat_list.setAdapter(adapter);

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                for (int i = 1; i < cat_list.getChildCount(); i++) {
                    RelativeLayout itemLayout = (RelativeLayout) cat_list.getChildAt(i);
                    CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.choose_category);
                    if (cb.isChecked()) {
                        all_category_name = all_category_name + "," + category_list.get(i).category_name;
                        category_value = category_value + "," + category_list.get(i).category_id;
                    }
                }
                all_category_name = all_category_name.substring(1, all_category_name.length());
                category_value = category_value.substring(1, category_value.length());
                selected_cat.setText(all_category_name);

                Log.e("selected category", category_value);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void showSettingsAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("GPS Not Enabled");
        alertDialog.setMessage("Do you wants to turn On GPS");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
                dialog.cancel();
                // activity.finish();
                finishAffinity();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // activity.finish();
                finishAffinity();
            }
        });

        alertDialog.show();
    }

    //category spinner

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }

        public void addFragment(Fragment fragment, int position) {
            fragmentList.add(position, fragment);
        }

        public void removeFragment(Fragment fragment, int position) {
            fragmentList.remove(position);
        }
    }

    public class categorySpinnerAdapter extends BaseAdapter {
        Context activity;
        List<SelectCategoryBean> category_list;

        public categorySpinnerAdapter(Context activity, List<SelectCategoryBean> category_list) {
            this.activity = activity;
            this.category_list = category_list;
        }

        @Override
        public int getCount() {
            return category_list.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            View v = inflater.inflate(R.layout.spinner_item, null);

            TextView cat_name = v.findViewById(R.id.category);
            CheckBox choose_cat = v.findViewById(R.id.choose_category);

            if (i == 0) {
                choose_cat.setVisibility(View.GONE);
            }

            if (category_list.get(i).category_name != null && category_list.get(i).category_name.length() > 0) {
                cat_name.setText(category_list.get(i).category_name);
            }

            return v;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == Global.TASK )
        {
            m_arrState.clear();
            viewPager.setCurrentItem(0);
            setting_btn.setVisibility(View.VISIBLE);
            search_btn.setVisibility(View.VISIBLE);
            logout_btn.setVisibility(View.VISIBLE);
            one.setTextColor(getResources().getColor(R.color.base_header_color));
            two.setTextColor(getResources().getColor(R.color.gray));
            three.setTextColor(getResources().getColor(R.color.gray));

            m_frgList.refreshData();
            m_frgMy.refreshData();
            m_frgMap.refreshData();

            m_nCurField = Global.TASK;
            return;
        }
        updateAllViews();
    }

    public void updateAllViews()
    {
        if( !m_bChanged )
            return;

        if( m_frgList != null )
            m_frgList.updateView(m_strApply, m_strFavor, m_strId);
        if( m_frgMap != null )
            m_frgMap.updateView(m_strApply, m_strFavor, m_strId);
        if( m_frgMy != null )
            m_frgMy.updateView(m_strApply, m_strFavor, m_strId);

        m_bChanged = false;
    }
}

