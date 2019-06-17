package com.rol.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rol.FriendProfile;
import com.rol.R;
import com.rol.TaskDetail;
import com.rol.beans.NotificationBean;

import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class NotificationAdapter extends BaseAdapter {

    Activity activity;
    List<NotificationBean> notification_list;
    String userId, authToken;

    public NotificationAdapter(Activity activity, List<NotificationBean> notification_list, String user_id, String auth_token) {

        this.activity = activity;
        this.notification_list = notification_list;
        this.userId = user_id;
        this.authToken = auth_token;
    }

    @Override
    public int getCount() {
        return notification_list.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.notification_item, null);

        ImageView user_profile = view.findViewById(R.id.user_profile);
        final ImageView notification_icon = view.findViewById(R.id.notification_icon);
        TextView notification_text = view.findViewById(R.id.notification_text);

        if (notification_list.get(position).message != null && notification_list.get(position).message.length() > 0) {
            if (notification_list.get(position).type.equalsIgnoreCase("Message")) {
                String name = notification_list.get(position).sender_name;
                notification_text.setText(name + " send message: " + notification_list.get(position).message);
            } else {
                notification_text.setText(notification_list.get(position).message);
            }
        }

        if (notification_list.get(position).user_image != null && notification_list.get(position).user_image.length() > 0) {
            Glide.with(activity)
                    .load(notification_list.get(position).user_image)
                    .apply(new RequestOptions().placeholder(R.mipmap.no_image).error(R.mipmap.no_image))
                    .into(user_profile);
        } else {
            Glide.with(activity)
                    .load(R.mipmap.profile)
                    .apply(new RequestOptions().placeholder(R.mipmap.profile).error(R.mipmap.profile))
                    .into(user_profile);
        }
        if (notification_list.get(position).type != null && notification_list.get(position).type.length() > 0) {
            if (notification_list.get(position).type.contains("task")) {
                // notification_icon.setImageResource(R.mipmap.thumb);
                notification_icon.setImageResource(R.mipmap.task_blue);
            } else if (notification_list.get(position).type.contains("friend_request")) {
                //notification_icon.setImageResource(R.mipmap.friend);
                notification_icon.setImageResource(R.mipmap.fri_blue);
            } else if (notification_list.get(position).type.equalsIgnoreCase("confirm_user")) {
                // notification_icon.setImageResource(R.mipmap.friend);
                notification_icon.setImageResource(R.mipmap.task_blue);
            } else if (notification_list.get(position).type.equalsIgnoreCase("Message")) {
                //notification_icon.setImageResource(R.mipmap.msg_blue);
                notification_icon.setImageResource(R.mipmap.message);
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (notification_list.get(position).task_id != null && notification_list.get(position).task_id.length() > 0) {
                    Intent intent = new Intent(activity, TaskDetail.class);
                    intent.putExtra("task_id", notification_list.get(position).task_id);
                    activity.startActivity(intent);
                } else if (notification_list.get(position).sender_id != null && notification_list.get(position).sender_id.length() > 0) {
                    if (notification_list.get(position).type.equalsIgnoreCase("Message")) {
//                        Intent intent = new Intent(activity, Chat.class);
//                        intent.putExtra("reciver_id", notification_list.get(position).sender_id);
//                        intent.putExtra("reciver_name", notification_list.get(position).sender_name);
//                        intent.putExtra("reciver_profile", notification_list.get(position).user_image);
//                        activity.startActivity(intent);
                    } else {
                        Intent resultIntent = new Intent(activity, FriendProfile.class);
                        resultIntent.putExtra("friend_id", notification_list.get(position).sender_id);
                        activity.startActivity(resultIntent);
                    }
                }
            }
        });
        return view;
    }
}
