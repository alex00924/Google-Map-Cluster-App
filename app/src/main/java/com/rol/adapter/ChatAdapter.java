package com.rol.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rol.R;
import com.rol.beans.MessageBean;
import com.rol.utils.DBHelper;

import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class ChatAdapter extends BaseAdapter {
    static String user_email;
    Activity activity;
    List<MessageBean> list;
    DBHelper db;

    public ChatAdapter(Activity activity, List<MessageBean> list) {

        this.activity = activity;
        this.list = list;

        db = DBHelper.getInstance(activity);
        db.open();
        user_email = db.getRegisterData().email;
        db.close();
    }

    @Override
    public int getCount() {
        return list.size();
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
    public View getView(int i, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.chat_item, null);
        RelativeLayout other_user = view.findViewById(R.id.other_user);
        RelativeLayout me_user = view.findViewById(R.id.me_user);
        TextView other_user_msg = view.findViewById(R.id.other_user_msg);
        TextView me_user_msg = view.findViewById(R.id.me_user_msg);

        if (list.get(i).sender != null && list.get(i).sender.length() > 0) {
            if (list.get(i).sender.equalsIgnoreCase(user_email)) {
                other_user.setVisibility(View.GONE);
                me_user.setVisibility(View.VISIBLE);
            } else {
                other_user.setVisibility(View.VISIBLE);
                me_user.setVisibility(View.GONE);
            }
        }

        final MessageBean messageData = list.get(i);

        if (messageData != null) {
            if (messageData.message != null && messageData.message.length() > 0) {
                me_user_msg.setText(messageData.message);
                other_user_msg.setText(messageData.message);
            }
        }
        return view;
    }
}