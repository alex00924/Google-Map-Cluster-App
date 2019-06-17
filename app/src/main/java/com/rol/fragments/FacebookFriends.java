package com.rol.fragments;

import android.app.Activity;
import android.content.Intent;
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
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rol.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by keshav on 3/7/18.
 */

public class FacebookFriends extends Fragment {

    ListView frd_listView;
    SharedPreferences facebook;

    CallbackManager callbackManager;
    String fb_email = "";
    String fb_token, fb_id;
    SharedPreferences shpf;
    String login_type, fb_login_status;

    SharedPreferences fb_spf;
    SharedPreferences.Editor fb_editor;

    public FacebookFriends() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.facebook_friends, container, false);
        frd_listView = view.findViewById(R.id.facebook_friend_list);

        shpf = getActivity().getSharedPreferences("my_pref", MODE_PRIVATE);
        login_type = shpf.getString("login_status", "");

        fb_spf = getActivity().getSharedPreferences("fb_pref", MODE_PRIVATE);
        fb_login_status = fb_spf.getString("fb_login", "");
        fb_editor = fb_spf.edit();

        if (login_type.equalsIgnoreCase("facebook_login") || fb_login_status.equalsIgnoreCase("1")) {
            Log.e("user already", "login in fb");
            //getFBfriendList();
        } else {
            Log.e("user not", "login in fb");

            //facebook_login();
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    private void facebook_login() {
        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("in success", "on success");

                fb_token = AccessToken.getCurrentAccessToken().getToken();
                Log.e("fb_token==", "" + fb_token);

                GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.e("response", "" + response);

                                    if (object != null && object.length() > 0) {
                                        Log.e("object", object + "");

                                        if (object.has("id")) {
                                            fb_id = object.getString("id");
                                        }

                                        if (object.has("email")) {
                                            fb_email = object.getString("email");
                                            Log.e("login email", fb_email);
                                            fb_editor.putString("fb_login", "1");
                                            fb_editor.apply();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,friendlist,members");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void getFBfriendList() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        Log.e("fb token", token.getToken());
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try {
                    JSONArray jsonArrayFriends = jsonObject.getJSONObject("friendlist").getJSONArray("data");
                    JSONObject friendlistObject = jsonArrayFriends.getJSONObject(0);
                    String friendListID = friendlistObject.getString("id");
                    myNewGraphReq(friendListID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "friendlist,members");
        graphRequest.setParameters(param);
        graphRequest.executeAsync();
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
                    /* Do something with the user list */
                    /* ex: get first user in list, "name" */
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
