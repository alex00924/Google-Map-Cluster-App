package com.rol;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.LatLng;
import com.rol.beans.CommentBean;
import com.rol.beans.HelperBean;
import com.rol.beans.PhotosBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.VolleyMultiPartRequest;
import com.rol.widgets.CustomButton;
import com.rol.widgets.CustomEditText;
import com.rol.widgets.CustomTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaskDetail extends Base {
    RecyclerView applied_helper, confirm_helper;
    RecyclerView photos_list;
    ListView chat_listView;
    LinearLayout invite_friend, favourite, apply, delete, edit, addmore_btn, add_task_in_calendar,
            organizer_layout, facebook_share, company_layout, task_address, applied_helper_layout, task_cancel, last_one_layout;

    RelativeLayout last_two_layout;
    View bottom_view;
    int hgt;
    int wid;
    ImageView taskImage;
    CircleImageView organiserImage;
    CustomEditText write_message;
    CustomTextView orgName, msgCount, dateTime, location, description, helperCount, favText, applyText,
            task_date_time, task_end_date_time, task_created_date, company_name;

    String taskId, is_added, task_date, task_name, create_date, organiser_id, is_friend, task_cover_image, company_id, task_description;
    CustomButton send_btn;
    List<PhotosBean> taskImagesList;

    List<HelperBean> applied_helper_list;
    List<HelperBean> confirm_helper_list;
    List<CommentBean> commentsList;
    Bitmap bitmap, image_bitmap;
    List<Bitmap> add_image_list = new ArrayList<>();
    ProgressDialog progDialog;
    Uri dynamicLink;
    long start_time, end_time;
    LinearLayout task_photo_layout_new;
    ImageView task_photo_new;
    CustomTextView task_name_new, task_description_new, txt_category_name;
    private String isFavourite = "", isApplied = "0", task_status = "", task_location, isAccept = "";
    private String sharable_link = "", category_name = "";
    private ShareDialog shareDialog;
    private LoginManager loginManager;
    private CallbackManager callbackManager;
    private PhotosAdapter photosAdapter;

    private static void addTaskToCalendar(final Context ctx, final String title, long start_time, long end_time, final String taskLocation, String all_day) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start_time);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end_time);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, title + " " + "task start");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, taskLocation);
        intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);

        if (all_day.equalsIgnoreCase("allDay")) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        } else {
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
        }

        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.task_detail, wrapper);

        task.setImageResource(R.mipmap.task_blue);
        share_btn.setVisibility(View.VISIBLE);
        logout_btn.setVisibility(View.GONE);

        initViews();

        chat_listView.setFocusable(false);

        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();

        getData();

        if (AppConstants.isNetworkAvailable(activity)) {
            getTaskDetails();
        }

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareTaskDetail();
            }
        });

        facebook_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginManager = LoginManager.getInstance();
                /*faceBookShare();*/
                faceBookShare2();
                // shareFacebookLink();

            }
        });

        task_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LatLng latlon = getLocationFromAddress(TaskDetail.this, task_location);

                    String location_url = "geo:" + latlon.latitude + "," + latlon.longitude + "?q=+" + latlon.latitude + "," + latlon.longitude + (task_location);
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(location_url));
                    intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
                    startActivity(intent);
                } catch (ActivityNotFoundException ane) {
                    Toast.makeText(activity, "Please Install Google Maps ", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });

        invite_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TaskDetail.this, InviteFriend.class);
                intent.putExtra("page_name", "task_detail");
                intent.putExtra("task_id", taskId);
                startActivity(intent);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_bChanged = true;
                Log.e("fav flag", isFavourite);
                if (isFavourite != null && isFavourite.length() > 0 && isFavourite.equalsIgnoreCase("0")) {
                    setFavourite("1");
                    m_strFavor = "1";
                } else if (isFavourite != null && isFavourite.length() > 0 && isFavourite.equalsIgnoreCase("1")) {
                    setFavourite("0");
                    m_strFavor = "0";
                }
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_bChanged = true;
                if (isApplied != null && isApplied.length() > 0 && applyText.getText().toString().equalsIgnoreCase("Apply")) {
                    callForApply();
                    m_strApply = "1";
                }
                else
                    Toast.makeText(activity, "Already Applied for this Task", Toast.LENGTH_SHORT).show();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent intent = new Intent(activity, EditTask.class);
//                intent.putExtra("taskId", taskId);
//                intent.putExtra("taskName", task_name);
//                activity.startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (taskId != null && taskId.length() > 0) {
                    deleteTaskDialog(taskId);
                }
            }
        });

        task_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                m_bChanged = true;
                if (taskId != null && taskId.length() > 0) {
                    cancelTask(taskId);
                    m_strApply = "0";
                }
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppConstants.isNetworkAvailable(activity)) {
                    if (validateData()) {
                        writeCommentServiceCall();
                    }
                }
            }
        });
        addmore_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        organizer_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        orgName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity, FriendProfile.class);
                intent.putExtra("friend_id", organiser_id);
                //  intent.putExtra("is_friend", is_friend);
                activity.startActivity(intent);
            }
        });

        organiserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity, FriendProfile.class);
                intent.putExtra("friend_id", organiser_id);
                // intent.putExtra("is_friend", is_friend);
                activity.startActivity(intent);
            }
        });

        company_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent intent = new Intent(TaskDetail.this, CompanyDetail.class);
//                intent.putExtra("companyId", company_id);
//                startActivity(intent);
            }
        });

        taskImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DisplayMetrics metrics = new DisplayMetrics();
                TaskDetail.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                final int width = metrics.widthPixels;
                final int height = metrics.heightPixels;
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View v1 = inflater.inflate(R.layout.full_image_layout, null);

                final ImageView image = v1.findViewById(R.id.full_image);
                ImageView close_img = v1.findViewById(R.id.closeBtn);

//                Glide.with(activity)
//                        .load(task_cover_image)
//                        .apply(new RequestOptions().error(R.mipmap.no_image)
//                                .placeholder(R.mipmap.no_image))
//                        .into(image);
//                Picasso.with(activity).load(task_cover_image)
//                        .placeholder(R.mipmap.no_image)
//                        .error(R.mipmap.no_image)
//                        .resize(width/2,height/2)
//                        .into(image);
                Log.e("display Dimensions: ", width + "x" + height + "===");
                if (task_cover_image != null && task_cover_image.length() > 0) {
                    Log.e("task cover", "image in");
                    Picasso.with(activity).load(task_cover_image).placeholder(R.mipmap.no_image).error(R.mipmap.no_image).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            int wid = bitmap.getWidth();
                            int heg = bitmap.getHeight();
                            image.setImageBitmap(bitmap);

                            Log.e("Bitmap Dimensions 00: ", wid + "x" + heg + "===");
//
                            if (wid > heg) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height / 3);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                                image.setLayoutParams(layoutParams);
                                Log.e("Bitmap Dimensions: ", width + "x" + height + "===");
                            } else if (wid == heg) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                                image.setLayoutParams(layoutParams);
                                Log.e("Bitmap Dimensions: ", width + "x" + height + "===");
                            } else {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                                image.setLayoutParams(layoutParams);
                                Log.e("Bitmap Dimensions: ", width + "x" + height + "===");
                            }
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                } else {
                    Picasso.with(activity).load(R.mipmap.no_image).placeholder(R.mipmap.no_image).
                            error(R.mipmap.no_image).into(image);
                }

                final PopupWindow popupWindow = new PopupWindow(v1, (width), (height), true);
                popupWindow.setBackgroundDrawable(null);
                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(v1, Gravity.CENTER, 0, 0);

                close_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        popupWindow.dismiss();
                    }
                });
            }
        });

        add_task_in_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String task_time = task_date_time.getText().toString();
                String task_end_time = task_end_date_time.getText().toString();

                if (task_time.contains(",") && task_end_time.contains(",")) {
                    String[] current_date_time = task_time.split(",");
                    String[] final_date = current_date_time[0].split("/");
                    String[] final_time = current_date_time[1].split(":");

                    int day = Integer.parseInt(final_date[0]);
                    int month = Integer.parseInt(final_date[1]);
                    int year = Integer.parseInt(final_date[2]);
                    int hour = Integer.parseInt(final_time[0]);
                    int minute = Integer.parseInt(final_time[1]);
                    int second = Integer.parseInt(final_time[2]);

                    Calendar beginCal = Calendar.getInstance();
                    beginCal.set(year, month - 1, day, hour, minute, second);
                    start_time = beginCal.getTimeInMillis();

                    String[] current_end_date_time = task_end_time.split(",");
                    String[] final_end_date = current_end_date_time[0].split("/");
                    String[] final_end_time = current_end_date_time[1].split(":");

                    int end_day = Integer.parseInt(final_end_date[0]);
                    int end_month = Integer.parseInt(final_end_date[1]);
                    int end_year = Integer.parseInt(final_end_date[2]);
                    int end_hour = Integer.parseInt(final_end_time[0]);
                    int end_minute = Integer.parseInt(final_end_time[1]);
                    int end_second = Integer.parseInt(final_end_time[2]);

                    Calendar endCal = Calendar.getInstance();
                    endCal.set(end_year, end_month - 1, end_day, end_hour, end_minute, end_second);
                    end_time = endCal.getTimeInMillis();

                    addTaskToCalendar(TaskDetail.this, task_name, start_time, end_time, task_location, "");
                } else if (task_time.contains(",") && !task_end_time.contains(",")) {
                    String[] current_date_time = task_time.split(",");
                    String[] final_date = current_date_time[0].split("/");
                    String[] final_time = current_date_time[1].split(":");

                    int day = Integer.parseInt(final_date[0]);
                    int month = Integer.parseInt(final_date[1]);
                    int year = Integer.parseInt(final_date[2]);
                    int hour = Integer.parseInt(final_time[0]);
                    int minute = Integer.parseInt(final_time[1]);
                    int second = Integer.parseInt(final_time[2]);

                    Calendar beginCal = Calendar.getInstance();
                    beginCal.set(year, month - 1, day, hour, minute, second);
                    start_time = beginCal.getTimeInMillis();

                    String[] final_end_date = task_end_time.split("/");

                    int end_day = Integer.parseInt(final_end_date[0]);
                    int end_month = Integer.parseInt(final_end_date[1]);
                    int end_year = Integer.parseInt(final_end_date[2]);

                    Calendar endCal = Calendar.getInstance();
                    endCal.set(end_year, end_month - 1, end_day);
                    end_time = endCal.getTimeInMillis();

                    addTaskToCalendar(TaskDetail.this, task_name, start_time, end_time, task_location, "");
                } else if (!task_time.contains(",") && task_end_time.contains(",")) {
                    String[] final_date = task_time.split("/");

                    int day = Integer.parseInt(final_date[0]);
                    int month = Integer.parseInt(final_date[1]);
                    int year = Integer.parseInt(final_date[2]);

                    Calendar beginCal = Calendar.getInstance();
                    beginCal.set(year, month - 1, day);
                    start_time = beginCal.getTimeInMillis();

                    String[] current_end_date_time = task_end_time.split(",");
                    String[] final_end_date = current_end_date_time[0].split("/");
                    String[] final_end_time = current_end_date_time[1].split(":");

                    int end_day = Integer.parseInt(final_end_date[0]);
                    int end_month = Integer.parseInt(final_end_date[1]);
                    int end_year = Integer.parseInt(final_end_date[2]);
                    int end_hour = Integer.parseInt(final_end_time[0]);
                    int end_minute = Integer.parseInt(final_end_time[1]);
                    int end_second = Integer.parseInt(final_end_time[2]);

                    Calendar endCal = Calendar.getInstance();
                    endCal.set(end_year, end_month - 1, end_day, end_hour, end_minute, end_second);
                    end_time = endCal.getTimeInMillis();

                    addTaskToCalendar(TaskDetail.this, task_name, start_time, end_time, task_location, "");
                } else {
                    String[] final_date = task_time.split("/");

                    int day = Integer.parseInt(final_date[0]);
                    int month = Integer.parseInt(final_date[1]);
                    int year = Integer.parseInt(final_date[2]);

                    Calendar beginCal = Calendar.getInstance();
                    beginCal.set(year, month - 1, day);
                    start_time = beginCal.getTimeInMillis();

                    String[] final_end_date = task_end_time.split("/");

                    int end_day = Integer.parseInt(final_end_date[0]);
                    int end_month = Integer.parseInt(final_end_date[1]);
                    int end_year = Integer.parseInt(final_end_date[2]);

                    Calendar endCal = Calendar.getInstance();
                    endCal.set(end_year, end_month - 1, end_day);
                    end_time = endCal.getTimeInMillis();

                    addTaskToCalendar(TaskDetail.this, task_name, start_time, end_time, task_location, "allDay");
                }
            }
        });
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent.hasExtra("task_id")) {
            taskId = intent.getStringExtra("task_id");
        }
        m_strId = taskId;
    }

    private void getTaskDetails() {
        progDialog = ProgressDialog.show(TaskDetail.this, null, null, false, true);
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progDialog.setContentView(R.layout.progress_dialog_layout);

        String tag = "taskDetails";
        String url = AppConstants.url + "task_detail.php?" + "task_id=" + taskId + "&user_id=" + userId + "&auth_token=" + authToken;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject data = jsonObject.getJSONObject("responseData");

                        task_name = data.getString("task_name");
                        base_title.setText(task_name);
                        task_location = data.getString("location");
                        organiser_id = data.getString("organiser_id");
                        is_friend = data.getString("is_friend");
                        task_date = data.getString("task_start_datetime");
                        isFavourite = data.getString("is_favourite");
                        task_status = data.getString("status");
                        task_description = data.getString("description");
                        isAccept = data.getString("is_accepted");
                        create_date = data.getString("created_date");
                        category_name = data.getString("category_name");

                        m_strApply = isAccept;
                        m_strFavor = isFavourite;

                        if (task_name != null && task_name.length() > 0)
                            task_name_new.setText(task_name);
                        if (task_description != null && task_description.length() > 0)
                            task_description_new.setText(task_description);
                        if (category_name != null && category_name.length() > 0)
                            txt_category_name.setText(category_name);

                        if (task_status.equalsIgnoreCase("my")) {
                            //  favourite.setVisibility(View.GONE);
                            //  apply.setVisibility(View.GONE);
                            // delete.setVisibility(View.VISIBLE);
                            // edit.setVisibility(View.VISIBLE);
                            bottom_view.setVisibility(View.VISIBLE);
                            last_one_layout.setVisibility(View.VISIBLE);
                            addmore_btn.setVisibility(View.VISIBLE);
                        /*    if (organiser_id.equalsIgnoreCase( userId )) {
                                Log.e( "Matched", "organiser_id:" + organiser_id );
                                Log.e( "Matched", "userId:" + userId );
                                apply.setVisibility( View.GONE );
                            }*/
                        }

                        isApplied = data.getString("is_applied");
                        company_id = data.getString("company_id");

                        if (data.getString("company_name") != null && data.getString("company_name").length() > 0) {
                            company_name.setText(data.getString("company_name"));
                        }

                        if (create_date != null && create_date.length() > 0) {
                            task_created_date.setText(AppConstants.convertTaskdateTime(create_date));
                        }

                        if (data.getString("organiser_name") != null && data.getString("organiser_name").length() > 0) {
                            orgName.setText(data.getString("organiser_name"));
                        }

                        if (data.getString("total_comments") != null && data.getString("total_comments").length() > 0)
                            msgCount.setText(data.getString("total_comments"));

                        if (task_location != null && task_location.length() > 0)
                            location.setText(data.getString("location"));

                        if (task_description != null && task_description.length() > 0)
                            description.setText(data.getString("description"));

                        if (isFavourite != null && isFavourite.length() > 0) {
                            if (isFavourite.equalsIgnoreCase("0")) {
                                favText.setText(getString(R.string.favorite));
                            } else {
                                favText.setText(getString(R.string.remove_favorite));
                            }
                        }

                        if (isApplied != null && isApplied.length() > 0) {
                            if (isApplied.equalsIgnoreCase("1")) {
                                apply.setVisibility(View.GONE);
                                task_cancel.setVisibility(View.VISIBLE);
                            }
                        }

                        if (task_date != null && task_date.length() > 0) {
                            String[] time4 = task_date.split(" ");
                            if (time4[1].contains("00:00:00")) {
                                dateTime.setText(AppConstants.convertOnlyTaskdate(time4[0]));
                                task_date_time.setText(AppConstants.convertOnlyTaskdate(time4[0]));
                            } else {
                                dateTime.setText(AppConstants.convertTaskdateTime(task_date));
                                task_date_time.setText(AppConstants.convertTaskdateTime(task_date));
                            }
                        }

                        if (data.getString("total_confirmed_helper") != null && data.getString("total_confirmed_helper").length() > 0
                                && data.getString("total_helper") != null && data.getString("total_helper").length() > 0)
                            helperCount.setText(data.getString("total_confirmed_helper") + "/" + data.getString("total_helper"));

                        String time1 = data.getString("task_end_datetime");
                        String[] time2 = time1.split(" ");
                        if (time2[1].contains("00:00:00")) {
                            task_end_date_time.setText(AppConstants.convertOnlyTaskdate(time2[0]));
                        } else {
                            task_end_date_time.setText(AppConstants.convertTaskdateTime(data.getString("task_end_datetime")));
                        }

                        task_cover_image = data.getString("task_image");
                        String organiser_image = data.getString("organiser_image");

                        Glide.with(activity)
                                .load(task_cover_image)
                                .apply(new RequestOptions().error(R.mipmap.no_image)
                                        .placeholder(R.mipmap.no_image))
                                .into(taskImage);

                        Glide.with(activity)
                                .load(task_cover_image)
                                .apply(new RequestOptions().error(R.mipmap.no_image)
                                        .placeholder(R.mipmap.no_image))
                                .into(task_photo_new);

                        Glide.with(activity)
                                .load(organiser_image)
                                .apply(new RequestOptions().error(R.mipmap.no_image)
                                        .placeholder(R.mipmap.no_image))
                                .into(organiserImage);

                        taskImagesList = new ArrayList<>();

                        JSONArray task_images = data.getJSONArray("task_images");

                        for (int i = 0; i < task_images.length(); i++) {
                            if (task_images.getJSONObject(i).has("status")) {
                                if (task_images.getJSONObject(i).getString("status").equalsIgnoreCase("0")) {
                                    //Toast.makeText(activity, task_images.getJSONObject(i).getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                PhotosBean pBean = new PhotosBean();
                                pBean.image = task_images.getJSONObject(i).getString("image");
                                pBean.image_id = task_images.getJSONObject(i).getString("image_id");

                                taskImagesList.add(pBean);
                            }
                        }

                        if (taskImagesList.size() > 0 && taskImagesList != null) {
                            photos_list.setVisibility(View.VISIBLE);
                            photosAdapter = new PhotosAdapter(activity, taskImagesList, task_status, userId, authToken);
                            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                            photos_list.setLayoutManager(linearLayoutManager1);
                            photos_list.setAdapter(photosAdapter);
                        }

                        JSONObject helper_img_list = data.getJSONObject("helper_images");

                        if (helper_img_list.has("status")) {
                        } else {
                            if (task_status.equalsIgnoreCase("my")) {
                                applied_helper_layout.setVisibility(View.VISIBLE);
                                applied_helper.setVisibility(View.VISIBLE);

                                JSONArray applied_array = helper_img_list.getJSONArray("applied_user");

                                applied_helper_list = new ArrayList<>();

                                for (int j = 0; j < applied_array.length(); j++) {
                                    if (applied_array.getJSONObject(j).has("status")) {
                                        if (applied_array.getJSONObject(j).getString("status").equalsIgnoreCase("0")) {
                                            //Toast.makeText(activity, applied_array.getJSONObject(j).getString("message"), Toast.LENGTH_SHORT).show();

                                        }
                                    } else {
                                        HelperBean helperBean = new HelperBean();
                                        helperBean.helper_id = applied_array.getJSONObject(j).getString("helper_id");
                                        helperBean.helper_image = applied_array.getJSONObject(j).getString("image");
                                        helperBean.is_friend = applied_array.getJSONObject(j).getString("is_helper_friend");
                                        helperBean.is_apply = applied_array.getJSONObject(j).getString("is_applied");
                                        helperBean.is_confirm = applied_array.getJSONObject(j).getString("is_confirmed");

                                        applied_helper_list.add(helperBean);
                                    }
                                }

                                if (applied_helper_list.size() > 0 && applied_helper_list != null) {
                                    applied_helper.setVisibility(View.VISIBLE);
                                    HelperListAdapter adapter = new HelperListAdapter(TaskDetail.this, applied_helper_list, task_status, userId, authToken, taskId, "applied");
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                                    applied_helper.setLayoutManager(linearLayoutManager);
                                    applied_helper.setAdapter(adapter);
                                } else {
                                    applied_helper.setVisibility(View.GONE);
                                }
                            }

                            JSONArray confirm_array = helper_img_list.getJSONArray("confirmed_user");

                            confirm_helper_list = new ArrayList<>();

                            for (int j = 0; j < confirm_array.length(); j++) {
                                if (confirm_array.getJSONObject(j).has("status")) {
                                    if (confirm_array.getJSONObject(j).getString("status").equalsIgnoreCase("0")) {
                                        confirm_helper_list.clear();
                                    }
                                } else {
                                    HelperBean helperBean = new HelperBean();
                                    helperBean.helper_id = confirm_array.getJSONObject(j).getString("helper_id");
                                    helperBean.helper_image = confirm_array.getJSONObject(j).getString("image");
                                    helperBean.is_friend = confirm_array.getJSONObject(j).getString("is_helper_friend");
                                    helperBean.is_apply = confirm_array.getJSONObject(j).getString("is_applied");
                                    helperBean.is_confirm = confirm_array.getJSONObject(j).getString("is_confirmed");
                                    helperBean.task_hours = confirm_array.getJSONObject(j).getString("task_hours");
                                    helperBean.hours_confirmed = confirm_array.getJSONObject(j).getString("hours_confirmed");

                                    confirm_helper_list.add(helperBean);
                                }
                            }
                            if (confirm_helper_list.size() > 0 && confirm_helper_list != null) {
                                confirm_helper.setVisibility(View.VISIBLE);
                                HelperListAdapter adapter1 = new HelperListAdapter(TaskDetail.this, confirm_helper_list, task_status, userId, authToken, taskId, "confirm");
                                LinearLayoutManager linear_manager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                                confirm_helper.setLayoutManager(linear_manager);
                                confirm_helper.setAdapter(adapter1);
                            }
                        }
                        JSONArray comments = data.getJSONArray("comments");
                        commentsList = new ArrayList<>();
                        for (int i = 0; i < comments.length(); i++) {
                            if (comments.getJSONObject(i).has("status")) {
                                if (comments.getJSONObject(i).getString("status").equalsIgnoreCase("0")) {
                                }
                            } else {
                                CommentBean commentsBean = new CommentBean();
                                commentsBean.comment_id = comments.getJSONObject(i).getString("id");
                                commentsBean.user_id = comments.getJSONObject(i).getString("user_id");
                                commentsBean.user_name = comments.getJSONObject(i).getString("user_name");
                                commentsBean.user_image = comments.getJSONObject(i).getString("user_image");
                                commentsBean.comment = comments.getJSONObject(i).getString("comment");
                                commentsBean.is_friend = comments.getJSONObject(i).getString("is_friend");
                                commentsBean.comment_date = comments.getJSONObject(i).getString("add_date");
                                Log.e("user_name :: ", commentsBean.user_name + "");

                                commentsList.add(commentsBean);
                            }
                        }

//                        TaskDetailChatAdapter chatAdapter = new TaskDetailChatAdapter(activity, commentsList);
//                        chat_listView.setAdapter(chatAdapter);
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

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    private void writeCommentServiceCall() {

        String comment = write_message.getText().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Log.e("Task_Detail:", "DATE:" + date);
        String tag = "add_comment";
        String url = AppConstants.url + "add_comment.php?" + "user_id=" + userId + "&task_id=" + taskId + "&comment=" + comment + "&auth_token=" + authToken + "&add_date=" + date;
        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        // finish();
                        // startActivity(getIntent());
                        write_message.setText("");
                        getTaskDetails();
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

    private void callForApply() {
        String tag = "setFavourite";
        String url = AppConstants.url + "apply_task.php?" + "user_id=" + userId + "&auth_token=" + authToken
                + "&task_id=" + taskId;

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
                        String message = jsonObject.getString("message");
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        //applyText.setText(getString(R.string.applied));
                        apply.setVisibility(View.GONE);
                        task_cancel.setVisibility(View.VISIBLE);
//                        getTaskDetails();
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

    private void setFavourite(final String status) {

        String tag = "setFavourite";
        String url = AppConstants.url + "favouritetask.php?" + "user_id=" + userId + "&auth_token=" + authToken
                + "&task_id=" + taskId + "&type=" + status;

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
                        String message = jsonObject.getString("message");
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                        if (status != null && status.equalsIgnoreCase("0")) {
                            favText.setText(getString(R.string.favorite));
                            isFavourite = "0";
                        } else {
                            favText.setText(getString(R.string.remove_favorite));
                            isFavourite = "1";
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

    private void deleteTask(String task_id) {
        String tag = "deleteTask";
        String url = AppConstants.url + "delete_task.php?" + "user_id=" + userId + "&auth_token=" + authToken
                + "&task_id=" + task_id;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");
                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(TaskDetail.this, Task.class);
                        intent.putExtra("pageName", "mytask");
                        finish();
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

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    private void addPhotos() {
        int total_img = add_image_list.size();

        String tag = "addPhotos";
        String url = AppConstants.url + "add_photo_task.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&task_id=" + taskId + "&count=" + total_img;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        VolleyMultiPartRequest vollyMultipartRequest = new VolleyMultiPartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    JSONObject main = new JSONObject(new String(response.data));
                    Log.e("response", main.toString());

                    if (main.getString("status").equalsIgnoreCase("1")) {

                        Toast.makeText(activity, main.getString("message"), Toast.LENGTH_SHORT).show();
                        //  finish();
                        // startActivity(getIntent());

                        getTaskDetails();
                    } else {
                        Toast.makeText(activity, main.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                final Map<String, DataPart> params = new HashMap<>();

                for (int j = 0; j < add_image_list.size(); j++) {
                    long imagename = System.currentTimeMillis();
                    params.put("image_" + (j + 1), new DataPart(imagename + ".png", getFileDataFromDrawable(add_image_list.get(j))));
                }
                return params;
            }
        };

        Volley.newRequestQueue(this).add(vollyMultipartRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            add_image_list.clear();
            if (data.getClipData() != null) {

                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    onSelectFromGalleryResult(imageUri);
                }

                addPhotos();
            } else if (data.getData() != null) {
                Uri imgUri = data.getData();
                onSelectFromGalleryResult(imgUri);

                addPhotos();
            }
        } else {
            finish();
        }
    }

    //for choose image from gallery
    private void onSelectFromGalleryResult(Uri image_uri) {

        if (image_uri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), image_uri);
                Log.e("image bitmap", bitmap + "");
                add_image_list.add(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String thePath = "no-path-found";
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(image_uri, filePathColumn, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            thePath = cursor.getString(columnIndex);
        }
        cursor.close();
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        Log.e("byte array of image", byteArrayOutputStream.toByteArray() + "");
        return byteArrayOutputStream.toByteArray();
    }

    private void initViews() {
        //Listing Lables
        applied_helper = findViewById(R.id.applied_helper_list);
        confirm_helper = findViewById(R.id.confirm_helper_list);
        photos_list = findViewById(R.id.photos_list);
        chat_listView = findViewById(R.id.chat_list);
        invite_friend = findViewById(R.id.invite);

        // Normal Components
        taskImage = findViewById(R.id.task_photo);
        organiserImage = findViewById(R.id.user_profile);

        orgName = findViewById(R.id.creator_name);
        msgCount = findViewById(R.id.message_count);
        dateTime = findViewById(R.id.date_time);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        helperCount = findViewById(R.id.count);
        favourite = findViewById(R.id.favorite);
        favText = findViewById(R.id.favText);
        apply = findViewById(R.id.apply);
        applyText = findViewById(R.id.apply_text);
        write_message = findViewById(R.id.write_message);
        send_btn = findViewById(R.id.send_btn);
        delete = findViewById(R.id.delete);
        edit = findViewById(R.id.edit);
        addmore_btn = findViewById(R.id.addmore_btn_layout);
        task_date_time = findViewById(R.id.selected_date_time);
        add_task_in_calendar = findViewById(R.id.task_calender);
        task_end_date_time = findViewById(R.id.end_date_time);
        organizer_layout = findViewById(R.id.organizer_layout);
        facebook_share = findViewById(R.id.facebook_share);
        task_created_date = findViewById(R.id.created_date_time);
        company_layout = findViewById(R.id.company_layout);
        company_name = findViewById(R.id.company_name);
        task_address = findViewById(R.id.task_address);
        applied_helper_layout = findViewById(R.id.applied_halper_layout);
        task_cancel = findViewById(R.id.cancel);
        last_one_layout = findViewById(R.id.last_one_layout);
        last_two_layout = findViewById(R.id.last_two_layout);
        bottom_view = findViewById(R.id.bottom_view);
        task_photo_layout_new = findViewById(R.id.task_photo_layout_new);
        task_photo_new = findViewById(R.id.task_photo_new);
        task_name_new = findViewById(R.id.task_name_new);
        task_description_new = findViewById(R.id.task_description_new);
        txt_category_name = findViewById(R.id.category_name);
    }

    private boolean validateData() {
        if (AppConstants.nullCheck(activity, write_message.getText().toString(), activity.getString(R.string.enter_comment))) {
            return true;
        }
        return false;
    }

    private void shareTaskDetail() {
        dynamicLink = generateDynamicLink(taskId);

        try {
            URL url = new URL(URLDecoder.decode(dynamicLink.toString(), "UTF-8"));
            sharable_link = url.toString();
        } catch (Exception e) {

        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "app name");
        i.putExtra(Intent.EXTRA_TEXT, sharable_link);
        startActivity(Intent.createChooser(i, "Share Task"));
    }

    public Uri generateDynamicLink(String taskId) {
        Uri dynamicLink;

        final Uri deepLink = Uri.parse("https://rolapplication.page?");

        String packageName = TaskDetail.this.getPackageName();
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(AppConstants.Deeplinking)
                .path("/")
                .appendQueryParameter("redirect", deepLink.toString() + "ref=" + taskId)
                .appendQueryParameter("apn", packageName);
        dynamicLink = builder.build();

        Log.e("DynamicLink", "dynamicLink: " + dynamicLink.toString());

        return dynamicLink;
    }

    private void faceBookShare() {
        Log.e("in", "facebook share");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            URL url = new URL(task_cover_image);
            image_bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            System.out.println(e);
        }

        if (isFbinstalled()) {
            loginManager.getInstance().logOut();

            ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                    .putString("og:type", "fitness.course")
                    .putString("og:title", task_name)
                    .putString("og:description", task_description)
                    .build();

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(image_bitmap)
                    .build();

            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType("fitness.run")
                    .putObject("fitness:course", object)
                    .putPhoto("image", photo)
                    .build();

            ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                    .setPreviewPropertyName("fitness:course")
                    .setAction(action)
                    .build();



       /*     ShareOpenGraphObject object = new  ShareOpenGraphObject.Builder()
                    .putString("og:type", "task.share")
                    .putString("og:title", task_name)
                    .putString("og:description", task_description)
                    .build();



            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(image_bitmap)
                    .build();

            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType("task.detail")
                    .putObject("task:share", object)
                    .putPhoto("image", photo)
                    .build();

            ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                    .setPreviewPropertyName("task:share")
                    .setAction(action)
                    .build();
*/

            ShareDialog shareDialog = new ShareDialog(TaskDetail.this);

            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Toast.makeText(TaskDetail.this, "Share Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    //Toast.makeText(SharePhoto.this, "Share Cancelled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(TaskDetail.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    // Log.e("CUR", "Share: " + exception.getMessage());
                    exception.printStackTrace();
                }
            });

            /*if (ShareDialog.canShow(SharePhotoContent.class))
            {
                shareDialog.show(content);
            }*/
            shareDialog.show(content);
        } else {
            Toast.makeText(TaskDetail.this, "Facebook app is not installed in this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void faceBookShare2() {
        Log.e("in", "facebook share");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        task_photo_layout_new.setDrawingCacheEnabled(true);

        task_photo_layout_new.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(task_photo_layout_new.getDrawingCache());
        task_photo_layout_new.setDrawingCacheEnabled(false);

        /*if(task_name!=null && task_name.length()>0)
            task_name_new.setText(task_name);
        if(task_description!=null && task_description.length()>0)
            task_description_new.setText(task_description);*/
        if (isFbinstalled()) {
            loginManager.getInstance().logOut();

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/saved_images");
            myDir.mkdirs();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            String fname = "Image-" + n + ".jpg";
            File file = new File(myDir, fname);
            if (file.exists()) file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                b.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(b)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            ShareDialog shareDialog = new ShareDialog(TaskDetail.this);
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Toast.makeText(TaskDetail.this, getResources().getString(R.string.sharesucces), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    //Toast.makeText(SharePhoto.this, "Share Cancelled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(TaskDetail.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    // Log.e("CUR", "Share: " + exception.getMessage());
                    exception.printStackTrace();
                }
            });

            if (ShareDialog.canShow(SharePhotoContent.class)) {
                shareDialog.show(content);
            }
        } else {
            Toast.makeText(TaskDetail.this, "Facebook app is not installed in this device.", Toast.LENGTH_SHORT).show();
        }



       /*     ShareOpenGraphObject object = new  ShareOpenGraphObject.Builder()
                    .putString("og:type", "task.share")
                    .putString("og:title", task_name)
                    .putString("og:description", task_description)
                    .build();



            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(image_bitmap)
                    .build();

            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType("task.detail")
                    .putObject("task:share", object)
                    .putPhoto("image", photo)
                    .build();

            ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                    .setPreviewPropertyName("task:share")
                    .setAction(action)
                    .build();
*/


            /*ShareDialog shareDialog = new ShareDialog(TaskDetail.this);

            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Toast.makeText(TaskDetail.this, "Share Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel()
                {
                    //Toast.makeText(SharePhoto.this, "Share Cancelled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(TaskDetail.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    // Log.e("CUR", "Share: " + exception.getMessage());
                    exception.printStackTrace();
                }
            });

            *//*if (ShareDialog.canShow(SharePhotoContent.class))
            {
                shareDialog.show(content);
            }*//*
            shareDialog.show(content);

        } else {
            Toast.makeText(TaskDetail.this, "Facebook app is not installed in this device.", Toast.LENGTH_SHORT).show();
        }*/

    }


/*
    private void shareFacebookLink() {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle(task_name)
                .setImageUrl(Uri.parse("https://scontent-sin6-1.xx.fbcdn.net/t31.0-8/13403381_247495578953089_8113745370016563192_o.png"))
                .setContentDescription(task_description)
                .setContentUrl(Uri.parse(sharable_link))
                .setQuote("Learn and share your knowledge")
                .build();

        ShareButton shareButton = (ShareButton) findViewById(R.id.fb_share_button);
        shareButton.setShareContent(content);

    }
*/

    public boolean isFbinstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //delete task dialog

    private void deleteTaskDialog(final String task_id) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_task_dialog);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button yes_btn = dialog.findViewById(R.id.yes_btn);
        Button no_btn = dialog.findViewById(R.id.no_btn);

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                if (AppConstants.isNetworkAvailable(activity)) {
                    deleteTask(task_id);
                }
            }
        });
        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    //cancel task

    private void cancelTask(String task_id) {
        String tag = "cancelTask";
        String url = AppConstants.url + "cancel_task.php?" + "user_id=" + userId + "&auth_token=" + authToken
                + "&task_id=" + task_id;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");
                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        task_cancel.setVisibility(View.GONE);
                        apply.setVisibility(View.VISIBLE);
                        //getTaskDetails();

//                        finish();
//                          startActivity(getIntent());
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

        AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
    }

    private void showEditTimeDialog(final String helper_id) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.edit_time_dialog);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button confirm_btn = dialog.findViewById(R.id.confirm_btn);
        Button cancel_btn = dialog.findViewById(R.id.cancel_btn);
        final EditText hour_time = dialog.findViewById(R.id.task_hour);
        final EditText minute_time = dialog.findViewById(R.id.task_minute);

        minute_time.setFilters(new InputFilter[]{new InputFilterMinMax(0, 59)});

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String total_time = hour_time.getText().toString() + ":" + minute_time.getText().toString();
                Log.e("total time", total_time);
                dialog.dismiss();

                editTaskTimeService(helper_id, total_time);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    //helper list adapter

    public void editTaskTimeService(String helper_id, String time) {
        String tag = "editHours";
        String url = AppConstants.url + "add_task_hours.php?" + "&user_id=" + userId + "&auth_token=" + authToken + "&add_user_id=" + helper_id + "&task_id=" + taskId + "&hours=" + time;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(TaskDetail.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TaskDetail.this, message, Toast.LENGTH_SHORT).show();
                        getTaskDetails();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e("reset pass", error.toString());
                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }

    public void confirmTaskTime(String helper_id) {
        String tag = "confirmHours";
        String url = AppConstants.url + "confirm_hours.php?" + "&user_id=" + userId + "&auth_token=" + authToken + "&task_id=" + taskId + "&helper_id=" + helper_id;

        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(TaskDetail.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TaskDetail.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e("reset pass", error.toString());
                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<android.location.Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            android.location.Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    //photos adapter
    public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {
        Activity activity;
        List<PhotosBean> list;
        String task_status, userId, authToken;

        public PhotosAdapter(Activity activity, List<PhotosBean> list, String taskStatus, String userId, String authToken) {

            this.activity = activity;
            this.list = list;
            this.task_status = taskStatus;
            this.userId = userId;
            this.authToken = authToken;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photos_list_item, null);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

            if (list.get(position).image != null && list.get(position).image.length() > 0) {
                Glide.with(activity)
                        .load(list.get(position).image)
                        .apply(new RequestOptions().error(R.mipmap.no_image)
                                .placeholder(R.mipmap.no_image))
                        .into(holder.photos);
            }

            if (task_status.equalsIgnoreCase("my")) {
                holder.delete_img.setVisibility(View.VISIBLE);
            } else {
                holder.delete_img.setVisibility(View.GONE);
            }

            holder.delete_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deletePhoto(list.get(position).image_id, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private void deletePhoto(String image_id, final int pos) {
            String tag = "deletePhoto";

            String url = AppConstants.url + "delete_photo_task.php?" + "user_id=" + userId + "&auth_token=" + authToken
                    + "&photo_id=" + image_id;

            url = url.replaceAll(" ", "%20");
            Log.e("url", url);

            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.e("response", jsonObject + "");

                        String message = jsonObject.getString("message");

                        if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            // finish();
                            //startActivity(getIntent());

                            list.remove(pos);
                            photosAdapter.notifyDataSetChanged();
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

            AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView photos, delete_img;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                photos = (ImageView) itemLayoutView.findViewById(R.id.photo);
                delete_img = (ImageView) itemLayoutView.findViewById(R.id.delete);
            }
        }
    }

    public class HelperListAdapter extends RecyclerView.Adapter<HelperListAdapter.ViewHolder> {

        Activity activity;
        List<HelperBean> helper_image_list = new ArrayList<>();
        String task_status, userId, authToken, task_id, helper_type;

        public HelperListAdapter(Activity activity, List<HelperBean> helper_image, String task_status, String userId, String authToken, String task_id, String helper_type) {

            this.activity = activity;
            this.helper_image_list = helper_image;
            this.task_status = task_status;
            this.userId = userId;
            this.authToken = authToken;
            this.task_id = task_id;
            this.helper_type = helper_type;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_item_layout, null);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

            Glide.with(activity)
                    .load(helper_image_list.get(position).helper_image)
                    .apply(new RequestOptions().error(R.mipmap.no_image)
                            .placeholder(R.mipmap.no_image))
                    .into(holder.helper_image);

            if (helper_image_list.get(position).task_hours != null && helper_image_list.get(position).task_hours.length() > 0) {
                holder.task_taken_hours.setText(helper_image_list.get(position).task_hours);
            }

            if (helper_image_list.get(position).hours_confirmed != null && helper_image_list.get(position).hours_confirmed.length() > 0) {
                if (helper_image_list.get(position).hours_confirmed.equalsIgnoreCase("1")) {
                    holder.confirm_time.setText(activity.getString(R.string.confirmed));
                } else {
                    holder.confirm_time.setText(activity.getString(R.string.confirm_time));
                }
            }

            holder.helper_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (helper_image_list.get(position).is_confirm.equalsIgnoreCase("1")) {
                        Intent intent = new Intent(activity, FriendProfile.class);
                        intent.putExtra("friend_id", helper_image_list.get(position).helper_id);
                        activity.startActivity(intent);
                    } else {
                        showHelperDialog(helper_image_list.get(position).helper_id);
                    }
                }
            });

            if (helper_image_list.get(position).helper_id.equalsIgnoreCase(userId)) {
                holder.edit_time.setVisibility(View.VISIBLE);
                // holder.confirm_time.setVisibility(View.VISIBLE);
                // holder.task_taken_hours.setVisibility(View.VISIBLE);

            } else {
                holder.edit_time.setVisibility(View.GONE);
                holder.confirm_time.setVisibility(View.GONE);
                holder.task_taken_hours.setVisibility(View.GONE);
            }

            if (task_status.equalsIgnoreCase("my")) {
                holder.edit_time.setVisibility(View.VISIBLE);
                holder.confirm_time.setVisibility(View.VISIBLE);
                holder.task_taken_hours.setVisibility(View.VISIBLE);
            }



          /*  if (helper_type.equalsIgnoreCase("confirm")) {
                holder.edit_time.setVisibility(View.VISIBLE);
            }
            if (task_status.equalsIgnoreCase("my"))
            {
                holder.confirm_time.setVisibility(View.VISIBLE);
                holder.task_taken_hours.setVisibility(View.VISIBLE);
            } else {
                if (helper_image_list.get(position).helper_id.equalsIgnoreCase(userId)) {
                    holder.edit_time.setVisibility(View.VISIBLE);
                }
            }
*/
            holder.edit_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditTimeDialog(helper_image_list.get(position).helper_id);
                }
            });

            holder.confirm_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (helper_image_list.get(position).hours_confirmed.equalsIgnoreCase("0")) {
                        confirmTaskTime(helper_image_list.get(position).helper_id);
                    } else {
                        Toast.makeText(activity, "You already confirm this time!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return helper_image_list.size();
        }

        private void showHelperDialog(final String id) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_helper);
            dialog.show();
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            Button confirm_btn = dialog.findViewById(R.id.confirm_btn);
            Button cancel_btn = dialog.findViewById(R.id.cancel_btn);

            confirm_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmHelper(id);
                    dialog.dismiss();
                }
            });
            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }

        private void confirmHelper(String helper_id) {
            String tag = "HelperConfirm";

            String url = AppConstants.url + "confirm_helper.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&task_id=" + task_id + "&friend_id=" + helper_id;
            url = url.replaceAll(" ", "%20");
            Log.e("url", url);

            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.e("response", jsonObject + "");
                        String message = jsonObject.getString("message");

                        if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            //  finish();
                            // startActivity(getIntent());
                            getTaskDetails();
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

            AppSingleton.getInstance(activity).addToRequestQueue(request, tag);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView helper_image;
            public LinearLayout hours_layout;
            public TextView edit_time;
            public TextView confirm_time;
            public TextView task_taken_hours;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                helper_image = (ImageView) itemLayoutView.findViewById(R.id.profile);
                hours_layout = (LinearLayout) itemLayoutView.findViewById(R.id.hours_layout);
                edit_time = (TextView) itemLayoutView.findViewById(R.id.edit_time);
                confirm_time = (TextView) itemLayoutView.findViewById(R.id.confirm_time);
                task_taken_hours = (TextView) itemLayoutView.findViewById(R.id.task_taken_hour);
            }
        }
    }

    public class InputFilterMinMax implements InputFilter {
        private int min;
        private int max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            //noinspection EmptyCatchBlock
            try {
                int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length()));
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}
