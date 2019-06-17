package com.rol;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rol.fragments.DiscoverFriends;
import com.rol.fragments.MyFriends;
import com.rol.fragments.RequestFriends;

import java.util.ArrayList;
import java.util.List;

public class Friends extends Base {

    TabLayout tabs;
    ViewPager viewPager;
    TextView one, two, three, four;


    private DiscoverFriends m_frgDiscover;
    private RequestFriends  m_frgRequest;
    private MyFriends       m_frgMy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.friends, wrapper);
        base_title.setText(R.string.friends);
        friends.setImageResource(R.mipmap.fri_blue);

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);

        setUpViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        one = view.findViewById(R.id.tab_title);
        one.setText(R.string.discover);
        one.setTextColor(getResources().getColor(R.color.base_header_color));
        tabs.getTabAt(0).setCustomView(view);

        View view1 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        two = view1.findViewById(R.id.tab_title);
        two.setText(R.string.request);
        tabs.getTabAt(1).setCustomView(view1);

      /*  View view2 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        three = view2.findViewById(R.id.tab_title);
        three.setText(R.string.facebook);
        tabs.getTabAt(2).setCustomView(view2);
*/

        View view3 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tabs, null);
        four = view3.findViewById(R.id.tab_title);
        four.setText(R.string.my_friends);
        tabs.getTabAt(2).setCustomView(view3);

        viewPager.setCurrentItem(0, true);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    two.setTextColor(getResources().getColor(R.color.base_header_color));
                    one.setTextColor(getResources().getColor(R.color.gray));
//                    three.setTextColor(getResources().getColor(R.color.gray));
                    four.setTextColor(getResources().getColor(R.color.gray));
                } else if (tab.getPosition() == 2) {
                    // three.setTextColor(getResources().getColor(R.color.base_header_color));
                    one.setTextColor(getResources().getColor(R.color.gray));
                    two.setTextColor(getResources().getColor(R.color.gray));
                    four.setTextColor(getResources().getColor(R.color.base_header_color));
                }
                /*
                else if (tab.getPosition() == 3) {
                    four.setTextColor(getResources().getColor(R.color.base_header_color));
                    three.setTextColor(getResources().getColor(R.color.gray));
                    one.setTextColor(getResources().getColor(R.color.gray));
                    two.setTextColor(getResources().getColor(R.color.gray));

                } */
                else {
                    one.setTextColor(getResources().getColor(R.color.base_header_color));
                    two.setTextColor(getResources().getColor(R.color.gray));
                    //  three.setTextColor(getResources().getColor(R.color.gray));
                    four.setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setUpViewPager(ViewPager viewpager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        m_frgDiscover = new DiscoverFriends();
        m_frgRequest = new RequestFriends();
        m_frgMy = new MyFriends();

        m_frgDiscover.setParent(this);
        m_frgRequest.setParent(this);
        m_frgMy.setParent(this);


        adapter.addFragment(m_frgDiscover);
        adapter.addFragment(m_frgRequest);
        //   adapter.addFragment(new FacebookFriends());
        adapter.addFragment(m_frgMy);

        viewpager.setAdapter(adapter);
        viewpager.setOffscreenPageLimit(3);
    }

    @Override
    protected void onResume() {
        super.onResume();

        user_detail.setImageResource(R.mipmap.user);
        task.setImageResource(R.mipmap.note);
        company.setImageResource(R.mipmap.company);
        friends.setImageResource(R.mipmap.fri_blue);
        chat.setImageResource(R.mipmap.chat);
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == Global.FRIEND )
        {
            m_frgDiscover.refreshData();
            m_frgRequest.refreshData();
            m_frgMy.refreshData();

            m_nCurField = Global.FRIEND;
            viewPager.setCurrentItem(0);
            one.setTextColor(getResources().getColor(R.color.base_header_color));
            two.setTextColor(getResources().getColor(R.color.gray));
            four.setTextColor(getResources().getColor(R.color.gray));

            return;
        }
        if (!m_bChanged)
            return;

        updateViews();

/*        switch (requestCode) {
            case Global.FRIEND_DISCOVER:
                m_frgDiscover.updateView(m_strFriendId,  m_nFriends, m_nBlockUser, m_nAccept, m_nRemoveFriend);
                break;
            case Global.FRIEND_REQUEST:
                m_frgRequest.updateView(m_strFriendId, m_nAccept);
                break;
            case Global.FRIEND_MY:
                m_frgMy.updateView(m_strFriendId, m_nRemoveFriend);
                break;
        }
*/
    }

    public void updateViews()
    {
        m_frgDiscover.updateView(m_strFriendId,  m_nFriends, m_nBlockUser, m_nAccept, m_nRemoveFriend);
        m_frgRequest.updateView(m_strFriendId, m_nAccept);
        m_frgMy.updateView(m_strFriendId, m_nRemoveFriend, m_nAccept);

        m_strFriendId = "";
        m_nFriends = 0;
        m_nBlockUser = 0;
        m_nAccept = 0;
        m_nRemoveFriend = 0;

    }
}