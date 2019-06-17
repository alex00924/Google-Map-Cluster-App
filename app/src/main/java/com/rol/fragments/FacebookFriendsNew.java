package com.rol.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rol.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by keshav on 3/7/18.
 */

public class FacebookFriendsNew extends Fragment {

    ListView frd_listView;
    SharedPreferences facebook;

    CallbackManager callbackManager;

    public FacebookFriendsNew() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.facebook_friends, container, false);
        frd_listView = view.findViewById(R.id.facebook_friend_list);
        return view;
    }

    private void getfbfriendlist() {
        List<String> PERMISSIONS = Arrays.asList("public_profile", "user_friends");
        LoginManager.getInstance().logInWithReadPermissions(this, PERMISSIONS);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("Accesstoken", "'" + AccessToken.getCurrentAccessToken());
                GraphRequestAsyncTask request = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/{user-id}/friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.e("FB", "!" + response.getJSONArray());
                                Log.e("FB", "!" + response.getJSONObject());
                                Log.e("FB", "!" + response.getRawResponse());
                            }
                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void facebook_login() {
        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("in success", "on success");

                AccessToken token = AccessToken.getCurrentAccessToken();
                GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        try {
                            Log.e("Accesstoken", "'" + AccessToken.getCurrentAccessToken());
                            JSONArray jsonArrayFriends = jsonObject.getJSONObject("friendlist").getJSONArray("data");
                            JSONObject friendlistObject = jsonArrayFriends.getJSONObject(0);
                            String friendListID = friendlistObject.getString("id");
                            Log.e("FRIENDSS", "" + friendListID);
                            Log.e("FRIENDSS", "" + jsonArrayFriends.toString());
                            myNewGraphReq(friendListID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle param = new Bundle();
                param.putString("fields", "friendlist");
                graphRequest.setParameters(param);
                graphRequest.executeAsync();
//                FacebookFriendAdapter adapter = new FacebookFriendAdapter(getActivity());
//                frd_listView.setAdapter(adapter);
            }

            @Override
            public void onCancel() {
                Log.e("in cancle", "on Logout");
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                Log.e("in error", error.toString());
                LoginManager.getInstance().logOut();
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(getActivity(),
                Arrays.asList("public_profile", "email"));
    }

    private void myNewGraphReq(String friendlistId) {
        final String graphPath = "/" + friendlistId + "/members/";
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = new GraphRequest(token, graphPath, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                JSONObject object = graphResponse.getJSONObject();
                try {
                    JSONArray arrayOfUsersInFriendList = object.getJSONArray("data");
                    JSONObject user = arrayOfUsersInFriendList.getJSONObject(0);
                    String usersName = user.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "name");
        request.setParameters(param);
        request.executeAsync();
    }
}
