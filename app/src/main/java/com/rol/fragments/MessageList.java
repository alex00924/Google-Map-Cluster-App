package com.rol.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rol.R;
import com.rol.beans.LoginBean;
import com.rol.beans.MessageBean;
import com.rol.utils.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class MessageList extends Fragment {

    ListView msg_listview;

    String user_id;
    List<MessageBean> list = new ArrayList<>();

//    MessageAdapter adapter;
    private boolean is_load = false;

    public MessageList() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_list, container, false);

        msg_listview = view.findViewById(R.id.message_list);

        DBHelper db = DBHelper.getInstance(getActivity());
        db.open();
        LoginBean loginBean = db.getRegisterData();
        user_id = loginBean.user_id;
        Log.e("user id", user_id);
        db.close();

        setDatainList();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();

            /*if(AppConstants.isNetworkAvailable(getActivity()))
            {
                setDatainList();
            }
*/

        }
    }

    private void setDatainList() {
        final ProgressDialog progDialog = ProgressDialog.show(getActivity(), null, null, false, true);
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progDialog.setContentView(R.layout.progress_dialog_layout);

        list.clear();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference friendIdRef = rootRef.child("chat_rol");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String key = ds.getKey();
                    if (key.contains(user_id)) {
                        DatabaseReference usersRef = friendIdRef.child(key);
                        ValueEventListener eventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot dSnapshot : dataSnapshot.getChildren()) {
                                    Log.e("list size in loop", "" + list.size());

                                    MessageBean all_data = dSnapshot.getValue(MessageBean.class);
                                    Log.e("data", all_data.message);
                                    Log.e("data", all_data.senderUserId);
                                    Log.e("data", all_data.receiverUserId);

                                    MessageBean paymentData = new MessageBean(all_data);
                                    // Log.e("payment data",paymentData.message);
                                    list.add(paymentData);

                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        };

                        is_load = true;
                        usersRef.addListenerForSingleValueEvent(eventListener);
                    }
                }

                Log.e("message list", "" + list.size());

//                adapter.notifyDataSetChanged();
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (list.size() > 0 && list != null) {
//                            adapter.notifyDataSetChanged();
//                            progDialog.dismiss();
//                        } else {
//                            progDialog.dismiss();
//                        }
//                    }
//                }, 1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        friendIdRef.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
       /* if(!is_load)
        {
            if(AppConstants.isNetworkAvailable(getActivity()))
            {
                setDatainList();
            }
        }
        else
        {
            adapter = new MessageAdapter(getActivity(), list, user_id);
            msg_listview.setAdapter(adapter);

        }*/

//        adapter = new MessageAdapter(getActivity(), list, user_id);
//        msg_listview.setAdapter(adapter);
    }
}
