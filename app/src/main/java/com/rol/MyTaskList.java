package com.rol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rol.fragments.CompletedTask;
import com.rol.fragments.UpcomingTask;

import java.util.ArrayList;
import java.util.List;

public class MyTaskList extends Base {

    TabLayout tabs;
    ViewPager viewPager;
    TextView one, two;
    Activity activity = this;
    ImageView add_btn;
    String friend_id, pageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.company, wrapper);
        base_title.setText(R.string.my_task);
        task.setImageResource(R.mipmap.task_blue);

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        add_btn = findViewById(R.id.add_btn);

        Intent i = getIntent();

        if (i.hasExtra("friend_id")) {
            friend_id = i.getStringExtra("friend_id");
            pageName = i.getStringExtra("page_name");
        }

        setUpViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        one = view.findViewById(R.id.tab_title);
        one.setText(R.string.upcoming_task);
        one.setTextColor(getResources().getColor(R.color.base_header_color));
        tabs.getTabAt(0).setCustomView(view);

        View view1 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        two = view1.findViewById(R.id.tab_title);
        two.setText(R.string.completed_task);
        tabs.getTabAt(1).setCustomView(view1);

        if (pageName.equalsIgnoreCase("completed")) {
            viewPager.setCurrentItem(1, true);
        } else {
            viewPager.setCurrentItem(0, true);
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    two.setTextColor(getResources().getColor(R.color.base_header_color));
                    one.setTextColor(getResources().getColor(R.color.gray));
                } else {
                    one.setTextColor(getResources().getColor(R.color.base_header_color));
                    two.setTextColor(getResources().getColor(R.color.gray));
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
/*
                Intent intent = new Intent(MyTaskList.this, AddTask.class);
                startActivity(intent);
*/
            }
        });
    }

    private void setUpViewPager(ViewPager viewpager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putString("id", friend_id);
        UpcomingTask fragobj = new UpcomingTask();
        fragobj.setArguments(bundle);

        adapter.addFragment(fragobj);

        CompletedTask object = new CompletedTask();
        object.setArguments(bundle);

        adapter.addFragment(object);

        viewpager.setAdapter(adapter);
    }

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
    }
}
