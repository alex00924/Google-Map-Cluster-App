package com.rol;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;
import com.rol.widgets.CustomButton;
import com.rol.widgets.CustomTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Settings extends Base {

    String user_id = "", auth_token = "", email_login = "";
    ToggleButton appConfirm, newFriendReq, friendReqConfirm, newMessage,
            newRecommTask, taskCancel, newTaskFav;

    RadioButton picMe, picFrnds, picEveryone,
            taskMe, taskFrnds, taskEveryone,
            profileInfoMe, profileInfoFrnds, profileInfoEveryone,
            myFrndListMe, myFrndListFrnds, myFrndListEveryone;

    RadioGroup groupPic, groupTask, groupProfileInfo, groupFrndList;

    String myPicture, myTask, profileInfo, myFrndList;

    DBHelper db;

    String appconfirmed = "appconfirmed", newfriend = "newfriend", friendreqconfirm = "friendreqconfirm",
            newmsg = "newmsg", newrecommendedtask = "newrecommendedtask", taskcancel = "taskcancel", newtaskoffav = "newtaskoffav";

    CustomTextView help, termsConditions, privacyPolicy, rateReviewApp, feedback, imprint, changePassword;
    CustomButton deleteProfile;

    //Delete Dialog Components
    CustomTextView title;
    CustomButton yes, no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.settings, wrapper);
        base_title.setText(R.string.general_settings);
        user_detail.setImageResource(R.mipmap.user_blue);

        db = DBHelper.getInstance(activity);
        db.open();
        user_id = db.getRegisterData().user_id;
        email_login = db.getRegisterData().email;
        auth_token = db.getRegisterData().auth_token;
        db.close();

        initViews();

        if (AppConstants.isNetworkAvailable(activity))
            getSettings();

        //Notification Settings
        appConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, appconfirmed, "1");
                else
                    setNotification(user_id, appconfirmed, "0");
            }
        });

        newFriendReq.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, newfriend, "1");
                else
                    setNotification(user_id, newfriend, "0");
            }
        });

        friendReqConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, friendreqconfirm, "1");
                else
                    setNotification(user_id, friendreqconfirm, "0");
            }
        });

        newMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, newmsg, "1");
                else
                    setNotification(user_id, newmsg, "0");
            }
        });

        newRecommTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, newrecommendedtask, "1");
                else
                    setNotification(user_id, newrecommendedtask, "0");
            }
        });

        taskCancel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, taskcancel, "1");
                else
                    setNotification(user_id, taskcancel, "0");
            }
        });

        newTaskFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    setNotification(user_id, newtaskoffav, "1");
                else
                    setNotification(user_id, newtaskoffav, "0");
            }
        });

        //Privacy Settings
        groupPic.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (radioGroup.getCheckedRadioButtonId() == R.id.me_btn)
                    myPicture = "me";
                else if (radioGroup.getCheckedRadioButtonId() == R.id.frds_btn)
                    myPicture = "friend";
                else
                    myPicture = "everyone";

                setPrivacySettings(myPicture, myTask, profileInfo, myFrndList);
            }
        });

        groupTask.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (radioGroup.getCheckedRadioButtonId() == R.id.task_me_btn)
                    myTask = "me";
                else if (radioGroup.getCheckedRadioButtonId() == R.id.task_frds_btn)
                    myTask = "friend";
                else
                    myTask = "everyone";

                setPrivacySettings(myPicture, myTask, profileInfo, myFrndList);
            }
        });

        groupProfileInfo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (radioGroup.getCheckedRadioButtonId() == R.id.my_profile_info_btn)
                    profileInfo = "me";
                else if (radioGroup.getCheckedRadioButtonId() == R.id.profile_info_frds_btn)
                    profileInfo = "friend";
                else
                    profileInfo = "everyone";

                setPrivacySettings(myPicture, myTask, profileInfo, myFrndList);
            }
        });

        groupFrndList.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (radioGroup.getCheckedRadioButtonId() == R.id.my_frd_list_btn)
                    myFrndList = "me";
                else if (radioGroup.getCheckedRadioButtonId() == R.id.my_frdlist_frds_btn)
                    myFrndList = "friend";
                else
                    myFrndList = "everyone";

                setPrivacySettings(myPicture, myTask, profileInfo, myFrndList);
            }
        });

        final Intent intent = new Intent(this, StaticPages.class);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent.putExtra("type", "help");
                startActivity(intent);
            }
        });

        termsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent.putExtra("type", "terms");
                startActivity(intent);
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent.putExtra("type", "privacy");
                startActivity(intent);
            }
        });
        imprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent.putExtra("type", "imprint");
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(Settings.this, ChangePassword.class));
            }
        });

        deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });

        rateReviewApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToGMail("this is admin email address)");
            }
        });
    }

    public void shareToGMail(String email) {
        String[] emails = {email};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails);
        emailIntent.setType("text/plain");
        final PackageManager pm = activity.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        activity.startActivity(emailIntent);
    }

    private void setPrivacySettings(String myPicture, String myTask, String profileInfo, String myFrndList) {

        String tag = "setPrivacyPolicy";
        String url = AppConstants.url + "set_privacy.php?" + "user_id=" + user_id + "&auth_token=" + auth_token
                + "&mypicture=" + myPicture + "&mytask=" + myTask + "&profileinformation=" + profileInfo
                + "&myfriendlist=" + myFrndList;
        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                   /* String message = jsonObject.getString("message");
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();*/

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

    private void setNotification(String user_id, String notiType, String notiStatus) {

        String tag = "setNotification";
        String url = AppConstants.url + "set_notification.php?" + "user_id=" + user_id + "&auth_token=" + auth_token
                + "&type=" + notiType + "&status=" + notiStatus;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                   /* String message = jsonObject.getString("message");
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();*/

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

    private void showDeleteDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_delete);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        title = dialog.findViewById(R.id.title);
        yes = dialog.findViewById(R.id.confirm_btn);
        no = dialog.findViewById(R.id.cancel_btn);

        title.setText("Are you sure that you want to delete your profile?");

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
                dialog.dismiss();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void deleteUser() {

        String tag = "deleteUser";

        String url = AppConstants.url + "delete_user.php?" + "user_id=" + user_id + "&auth_token=" + auth_token;
        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    db.open();
                    db.deleteUser();
                    db.close();

                    startActivity(new Intent(activity, Login.class));
                    finishAffinity();
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

    private void initViews() {

        appConfirm = findViewById(R.id.application_confirm_switch);
        newFriendReq = findViewById(R.id.new_frd_request_switch);
        friendReqConfirm = findViewById(R.id.frd_request_confirm_switch);
        newMessage = findViewById(R.id.new_msg_switch);
        newRecommTask = findViewById(R.id.new_recomm_task_switch);
        taskCancel = findViewById(R.id.task_cancelled_switch);
        newTaskFav = findViewById(R.id.new_task_favorite_switch);

        picMe = findViewById(R.id.me_btn);
        picFrnds = findViewById(R.id.frds_btn);
        picEveryone = findViewById(R.id.everyone_btn);
        taskMe = findViewById(R.id.task_me_btn);
        taskFrnds = findViewById(R.id.task_frds_btn);
        taskEveryone = findViewById(R.id.task_everyone_btn);
        profileInfoMe = findViewById(R.id.my_profile_info_btn);
        profileInfoFrnds = findViewById(R.id.profile_info_frds_btn);
        profileInfoEveryone = findViewById(R.id.my_profile_info_everyone_btn);
        myFrndListMe = findViewById(R.id.my_frd_list_btn);
        myFrndListFrnds = findViewById(R.id.my_frdlist_frds_btn);
        myFrndListEveryone = findViewById(R.id.my_frdlist_everyone_btn);

        //radioGroups
        groupPic = findViewById(R.id.my_picture_status);
        groupTask = findViewById(R.id.my_task_status);
        groupProfileInfo = findViewById(R.id.my_profile_info);
        groupFrndList = findViewById(R.id.my_frd_list);

        //Static Page Labels
        help = findViewById(R.id.help);
        termsConditions = findViewById(R.id.terms_condition);
        privacyPolicy = findViewById(R.id.privacy_policy);
        imprint = findViewById(R.id.imprint);
        rateReviewApp = findViewById(R.id.rate_review_app);
        feedback = findViewById(R.id.feedback);

        //deleteProfile
        deleteProfile = findViewById(R.id.delete_profile);

        changePassword = findViewById(R.id.change_password);
    }

    private void getSettings() {

        String tag = "getSettings";
        String url = AppConstants.url + "getsettings.php?" + "user_id=" + user_id + "&auth_token=" + auth_token;

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
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {

                        JSONObject resObject = jsonObject.getJSONObject("responsedata");
                        Log.e("second response", resObject + "");

                        if (resObject.has("notificationstatus")) {

                            //Notification Switches / Toggle Buttons
                            JSONObject notiObject = resObject.getJSONObject("notificationstatus");
                            if (notiObject.getString("applicationconfirm") != null && notiObject.getString("applicationconfirm").equalsIgnoreCase("on"))
                                appConfirm.setChecked(true);
                            else
                                appConfirm.setChecked(false);

                            if (notiObject.getString("newfriendrequest") != null && notiObject.getString("newfriendrequest").equalsIgnoreCase("on"))
                                newFriendReq.setChecked(true);
                            else
                                newFriendReq.setChecked(false);

                            if (notiObject.getString("friendrequestconfirmed") != null && notiObject.getString("friendrequestconfirmed").equalsIgnoreCase("on"))
                                friendReqConfirm.setChecked(true);
                            else
                                friendReqConfirm.setChecked(false);

                            if (notiObject.getString("newmessage") != null && notiObject.getString("newmessage").equalsIgnoreCase("on"))
                                newMessage.setChecked(true);
                            else
                                newMessage.setChecked(false);

                            if (notiObject.getString("newrecommended") != null && notiObject.getString("newrecommended").equalsIgnoreCase("on"))
                                newRecommTask.setChecked(true);
                            else
                                newRecommTask.setChecked(false);

                            if (notiObject.getString("taskcancelled") != null && notiObject.getString("taskcancelled").equalsIgnoreCase("on"))
                                taskCancel.setChecked(true);
                            else
                                taskCancel.setChecked(false);

                            if (notiObject.getString("newtaskoffavouritedprofile") != null && notiObject.getString("newtaskoffavouritedprofile").equalsIgnoreCase("on"))
                                newTaskFav.setChecked(true);
                            else
                                newTaskFav.setChecked(false);

                            if (resObject.has("privacysettings")) {
                                myPicture = resObject.getJSONObject("privacysettings").getString("mypicture");
                                myTask = resObject.getJSONObject("privacysettings").getString("mytask");
                                profileInfo = resObject.getJSONObject("privacysettings").getString("profileinformation");
                                myFrndList = resObject.getJSONObject("privacysettings").getString("myfriendlist");

                                if (myPicture != null && myPicture.length() > 0) {
                                    if (myPicture.equalsIgnoreCase("me"))
                                        picMe.setChecked(true);
                                    else if (myPicture.equalsIgnoreCase("friend"))
                                        picFrnds.setChecked(true);
                                    else
                                        picEveryone.setChecked(true);
                                }

                                if (myTask != null && myTask.length() > 0) {
                                    if (myTask.equalsIgnoreCase("me"))
                                        taskMe.setChecked(true);
                                    else if (myTask.equalsIgnoreCase("friend"))
                                        taskFrnds.setChecked(true);
                                    else
                                        taskEveryone.setChecked(true);
                                }

                                if (profileInfo != null && profileInfo.length() > 0) {
                                    if (profileInfo.equalsIgnoreCase("me"))
                                        profileInfoMe.setChecked(true);
                                    else if (profileInfo.equalsIgnoreCase("friend"))
                                        profileInfoFrnds.setChecked(true);
                                    else
                                        profileInfoEveryone.setChecked(true);
                                }

                                if (myFrndList != null && myFrndList.length() > 0) {
                                    if (myFrndList.equalsIgnoreCase("me"))
                                        myFrndListMe.setChecked(true);
                                    else if (myFrndList.equalsIgnoreCase("friend"))
                                        myFrndListFrnds.setChecked(true);
                                    else
                                        myFrndListEveryone.setChecked(true);
                                }
                            }
                        }
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
}
