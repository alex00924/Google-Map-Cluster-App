package com.rol.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rol.FriendProfile;
import com.rol.R;
import com.rol.beans.MyFriendBean;

import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class FriendsPhotoAdapter extends RecyclerView.Adapter<FriendsPhotoAdapter.ViewHolder> {

    Activity activity;
    List<MyFriendBean> friend_list;

    public FriendsPhotoAdapter(Activity activity, List<MyFriendBean> friend_list) {
        this.activity = activity;
        this.friend_list = friend_list;
    }

    @NonNull
    @Override
    public FriendsPhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_profile_item, null);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsPhotoAdapter.ViewHolder holder, final int position) {
        if (activity != null) {
            if (friend_list.get(position).friend_image != null && friend_list.get(position).friend_image.length() > 0) {

                Log.e("image in detail", friend_list.get(position).friend_image + "");
                Glide.with(activity)
                        .load(friend_list.get(position).friend_image)
                        .apply(new RequestOptions().error(R.mipmap.no_image)
                                .placeholder(R.mipmap.no_image))
                        .into(holder.photos);
            } else {
                Glide.with(activity)
                        .load(R.mipmap.no_image)
                        .into(holder.photos);
            }
        }

        holder.photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, FriendProfile.class);
                intent.putExtra("friend_id", friend_list.get(position).friend_id);
                // intent.putExtra("is_friend",bean.is_friend);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friend_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView photos;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            photos = (ImageView) itemLayoutView.findViewById(R.id.profile);
        }
    }
}
