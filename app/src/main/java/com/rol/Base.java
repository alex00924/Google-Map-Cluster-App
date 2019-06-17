package com.rol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Base extends AppCompatActivity {

    public int          m_nBack = -1;

    public static String m_strApply = "";
    public static String m_strFavor = "";
    public static String m_strId = "";
    public static boolean m_bChanged = false;

    //////Friends
    public static int   m_nFriends = 0;     //1 : friend, 2 : No
    public static int   m_nBlockUser = 0;   //1 : block, 2 : No
    public static int   m_nAccept = 0;      //1 : Accept, 2 : Reject
    public static String    m_strFriendId = "";
    public static int   m_nRemoveFriend = 0;    //1 : remove

    public static int  m_nCurField = Global.TASK;

    public static TextView notification_count;
    public static int noti_counter;
    public String userId, authToken;
    TextView base_title;
    ImageView back_btn, share_btn, setting_btn, search_btn, logout_btn;
    FrameLayout wrapper;
    RelativeLayout bottom_layout, base_header, notification_layout;
    ImageView user_detail, task, company, friends, chat;
    SharedPreferences spf;
    String login_type;
    DBHelper db;
    Activity activity = this;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    public boolean bOperating = false;


    protected void goBack(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base);

        initMember();

        db = DBHelper.getInstance(this);
        db.open();
        userId = db.getRegisterData().user_id;
        authToken = db.getRegisterData().auth_token;
        db.close();

        if (AppConstants.isNetworkAvailable(Base.this)) {
            getNotificationCount();
            registerDevice();
        }

        spf = getSharedPreferences("my_pref", MODE_PRIVATE);
        login_type = spf.getString("login_status", "");

        ErrorReporter1 errReporter = new ErrorReporter1();
        errReporter.Init(Base.this);
        errReporter.CheckErrorAndSendMail(Base.this);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( m_nBack < 0 )
                    finish();
                else
                    goBack();

            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logoutDialog();
            }
        });

        user_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                user_detail.setImageResource(R.mipmap.user_blue);
//                task.setImageResource(R.mipmap.note);
//                company.setImageResource(R.mipmap.company);
//                friends.setImageResource(R.mipmap.friends);
//                chat.setImageResource(R.mipmap.chat);
//
//                Intent intent = new Intent(Base.this, UserProfile.class);
//                startActivity(intent);
            }
        });

        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( m_nCurField == Global.TASK )
                    return;

                m_nCurField = Global.TASK;
                user_detail.setImageResource(R.mipmap.user);
                task.setImageResource(R.mipmap.task_blue);
                company.setImageResource(R.mipmap.company);
                friends.setImageResource(R.mipmap.friends);
                chat.setImageResource(R.mipmap.chat);
                Intent intent = new Intent(Base.this, Task.class);
                startActivityForResult(intent, Global.FRIEND);
            }
        });
        company.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                user_detail.setImageResource(R.mipmap.user);
//                task.setImageResource(R.mipmap.note);
//                company.setImageResource(R.mipmap.com_blue);
//                friends.setImageResource(R.mipmap.friends);
//                chat.setImageResource(R.mipmap.chat);
//                Intent intent = new Intent(Base.this, Company.class);
//                startActivity(intent);
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( m_nCurField == Global.FRIEND )
                    return;
                m_nCurField = Global.FRIEND;
                user_detail.setImageResource(R.mipmap.user);
                task.setImageResource(R.mipmap.note);
                company.setImageResource(R.mipmap.company);
                friends.setImageResource(R.mipmap.fri_blue);
                chat.setImageResource(R.mipmap.chat);
                Intent intent = new Intent(Base.this, Friends.class);
                startActivityForResult(intent, Global.TASK);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                user_detail.setImageResource(R.mipmap.user);
//                task.setImageResource(R.mipmap.note);
//                company.setImageResource(R.mipmap.company);
//                friends.setImageResource(R.mipmap.friends);
//                chat.setImageResource(R.mipmap.message);
//                Intent intent = new Intent(Base.this, Message.class);
//                startActivity(intent);
            }
        });

        notification_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Base.this, Message.class);
//                intent.putExtra("page", "notification");
//                startActivity(intent);
            }
        });
    }

    private void initMember() {

        base_title = findViewById(R.id.base_title);
        back_btn = findViewById(R.id.back_btn);
        share_btn = findViewById(R.id.share_btn);
        setting_btn = findViewById(R.id.setting_btn);
        search_btn = findViewById(R.id.search_btn);
        logout_btn = findViewById(R.id.logout);
        wrapper = findViewById(R.id.wrapper);
        bottom_layout = findViewById(R.id.bottom_layout);
        base_header = findViewById(R.id.base_header);

        user_detail = findViewById(R.id.user_detail);
        task = findViewById(R.id.task);
        company = findViewById(R.id.company);
        friends = findViewById(R.id.friends);
        chat = findViewById(R.id.chat);
        notification_count = findViewById(R.id.notification_count);
        notification_layout = findViewById(R.id.notification_layout);
    }

    private void logoutDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_task_dialog);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button yes_btn = dialog.findViewById(R.id.yes_btn);
        Button no_btn = dialog.findViewById(R.id.no_btn);
        TextView title_text = dialog.findViewById(R.id.title_text);

        title_text.setText(activity.getString(R.string.logout_text));

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (login_type.equalsIgnoreCase("facebook_login")) {
                    LoginManager.getInstance().logOut();
                }

                db.open();
                db.deleteUser();
                db.close();

                dialog.dismiss();

              /*  Intent intent = new Intent(Base.this, Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                finishAffinity();
            }
        });
        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void registerDevice() {

        String REQUEST_TAG = "notification";
        FirebaseApp.initializeApp(this);

        String registerid = FirebaseInstanceId.getInstance().getToken();
        String deviceid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        String url = AppConstants.url + "Pushnotification_Android/register1.php" + "?" + "regId=" + registerid + "&user_id=" + userId + "&device_id=" + deviceid;

        url = url.replaceAll(" ", "%20");
        Log.e("register_device_url", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("notification_response", response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(stringRequest, REQUEST_TAG);
    }

    private void getNotificationCount() {
        String tag = "getNotificationCount";
        String url = AppConstants.url + "get_message_counter.php?" + "user_id=" + userId + "&auth_token=" + authToken;

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
                    } else {
                        JSONObject object = jsonObject.getJSONObject("responsedata");
                        String counter = object.getString("counter");
                        if (counter != null && counter.length() > 0 && !counter.equals("0")) {
                            Log.e("set counter", counter);
                            notification_count.setVisibility(View.VISIBLE);
                            notification_count.setText(counter);
                            noti_counter = Integer.parseInt(counter);
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

                        Log.e("reset pass", error.toString());
                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }

    static class ErrorReporter1 implements Thread.UncaughtExceptionHandler {

        private static String VersionName;
        private static String buildNumber;
        private static String PackageName;
        private static String FilePath;
        private static String PhoneModel;
        private static String AndroidVersion;
        private static String Board;
        private static String Brand;
        private static String Device;
        private static String Display;
        private static String FingerPrint;
        private static String Host;
        private static String ID;
        private static String Manufacturer;
        private static String Model;
        private static String Product;
        private static String Tags;
        private static String Type;
        private static String User;
        long Time;
        HashMap<String, String> CustomParameters = new HashMap<String, String>();
        private String[] _recipients = new String[]{"keshavandroiddev@gmail.com"};
        private String _subject = "Crash Report of Rol app Android";
        private Thread.UncaughtExceptionHandler PreviousHandler;
        private ErrorReporter1 S_mInstance;
        private Context CurContext;

        public void AddCustomData(String Key, String Value) {
            CustomParameters.put(Key, Value);
        }

        private String CreateCustomInfoString() {
            String CustomInfo = "";
            Iterator iterator = CustomParameters.keySet().iterator();
            while (iterator.hasNext()) {
                String CurrentKey = (String) iterator.next();
                String CurrentVal = CustomParameters.get(CurrentKey);
                CustomInfo += CurrentKey + " = " + CurrentVal + "\n";
            }
            return CustomInfo;
        }

        ErrorReporter1 getInstance() {
            if (S_mInstance == null)
                S_mInstance = new ErrorReporter1();
            return S_mInstance;
        }

        public void Init(Context context) {
            PreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
            CurContext = context;
        }

        public long getAvailableInternalMemorySize() {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        }

        public long getTotalInternalMemorySize() {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        }

        void RecoltInformations(Context context) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi;
                // Version
                pi = pm.getPackageInfo(context.getPackageName(), 0);
                VersionName = pi.versionName;
                buildNumber = currentVersionNumber(context);
                // Package name
                PackageName = pi.packageName;

                // Device model
                PhoneModel = android.os.Build.MODEL;
                // Android version
                AndroidVersion = android.os.Build.VERSION.RELEASE;

                Board = android.os.Build.BOARD;
                Brand = android.os.Build.BRAND;
                Device = android.os.Build.DEVICE;
                Display = android.os.Build.DISPLAY;
                FingerPrint = android.os.Build.FINGERPRINT;
                Host = android.os.Build.HOST;
                ID = android.os.Build.ID;
                Model = android.os.Build.MODEL;
                Product = android.os.Build.PRODUCT;
                Tags = android.os.Build.TAGS;
                Time = android.os.Build.TIME;
                Type = android.os.Build.TYPE;
                User = android.os.Build.USER;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String CreateInformationString() {
            RecoltInformations(CurContext);

            String ReturnVal = "";
            ReturnVal += "Version : " + VersionName;
            ReturnVal += "\n";
            ReturnVal += "Build Number : " + buildNumber;
            ReturnVal += "\n";
            ReturnVal += "Package : " + PackageName;
            ReturnVal += "\n";
            ReturnVal += "FilePath : " + FilePath;
            ReturnVal += "\n";
            ReturnVal += "Phone Model" + PhoneModel;
            ReturnVal += "\n";
            ReturnVal += "Android Version : " + AndroidVersion;
            ReturnVal += "\n";
            ReturnVal += "Board : " + Board;
            ReturnVal += "\n";
            ReturnVal += "Brand : " + Brand;
            ReturnVal += "\n";
            ReturnVal += "Device : " + Device;
            ReturnVal += "\n";
            ReturnVal += "Display : " + Display;
            ReturnVal += "\n";
            ReturnVal += "Finger Print : " + FingerPrint;
            ReturnVal += "\n";
            ReturnVal += "Host : " + Host;
            ReturnVal += "\n";
            ReturnVal += "ID : " + ID;
            ReturnVal += "\n";
            ReturnVal += "Model : " + Model;
            ReturnVal += "\n";
            ReturnVal += "Product : " + Product;
            ReturnVal += "\n";
            ReturnVal += "Tags : " + Tags;
            ReturnVal += "\n";
            ReturnVal += "Time : " + Time;
            ReturnVal += "\n";
            ReturnVal += "Type : " + Type;
            ReturnVal += "\n";
            ReturnVal += "User : " + User;
            ReturnVal += "\n";
            ReturnVal += "Total Internal memory : "
                    + getTotalInternalMemorySize();
            ReturnVal += "\n";
            ReturnVal += "Available Internal memory : "
                    + getAvailableInternalMemorySize();
            ReturnVal += "\n";

            return ReturnVal;
        }

        public void uncaughtException(Thread t, Throwable e) {
            String Report = "";
            Date CurDate = new Date(Time);
            Report += "Error Report collected on : " + CurDate.toString();
            Report += "\n";
            Report += "\n";
            Report += "Informations :";
            Report += "\n";
            Report += "==============";
            Report += "\n";
            Report += "\n";
            Report += CreateInformationString();

            Report += "Custom Informations :\n";
            Report += "=====================\n";
            Report += CreateCustomInfoString();

            Report += "\n\n";
            Report += "Stack : \n";
            Report += "======= \n";
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String stacktrace = result.toString();
            Report += stacktrace;

            Report += "\n";
            Report += "Cause : \n";
            Report += "======= \n";

            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                Report += result.toString();
                cause = cause.getCause();
            }
            printWriter.close();
            Report += "**** End of current Report ***";
            SaveAsFile(Report);
            // SendErrorMail( Report );
            PreviousHandler.uncaughtException(t, e);
        }

        private void SendErrorMail(Context _context, String ErrorContent) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            String subject = _subject;
            String body = "\n\n" + ErrorContent + "\n\n";
            sendIntent.putExtra(Intent.EXTRA_EMAIL, _recipients);
            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            sendIntent.setType("message/rfc822");
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(Intent.createChooser(sendIntent, "Title:"));
        }

        private void SaveAsFile(String ErrorContent) {
            try {
                Random generator = new Random();
                int random = generator.nextInt(99999);
                String FileName = "stack-" + random + ".doc";
                FileOutputStream trace = CurContext.openFileOutput(FileName,
                        Context.MODE_PRIVATE);
                trace.write(ErrorContent.getBytes());
                trace.close();
            } catch (Exception e) {
                // ...
            }
        }

        private String[] GetErrorFileList() {
            File dir = new File(FilePath + "/");
            // Try to create the files folder if it doesn't exist
            dir.mkdir();
            // Filter for ".stacktrace" files
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".doc");
                }
            };
            return dir.list(filter);
        }

        private boolean bIsThereAnyErrorFile() {
            return GetErrorFileList().length > 0;
        }

        public void CheckErrorAndSendMail(Context _context) {
            try {
                FilePath = _context.getFilesDir().getAbsolutePath();
                if (bIsThereAnyErrorFile()) {
                    String WholeErrorText = "";

                    String[] ErrorFileList = GetErrorFileList();
                    int curIndex = 0;
                    final int MaxSendMail = 5;
                    for (String curString : ErrorFileList) {
                        if (curIndex++ <= MaxSendMail) {
                            WholeErrorText += "New Trace collected :\n";
                            WholeErrorText += "=====================\n ";
                            String filePath = FilePath + "/" + curString;
                            Log.e("ERROR", filePath);
                            BufferedReader input = new BufferedReader(
                                    new FileReader(filePath));
                            String line;
                            while ((line = input.readLine()) != null) {
                                WholeErrorText += line + "\n";
                            }
                            input.close();
                        }

                        // DELETE FILES !!!!
                        File curFile = new File(FilePath + "/" + curString);
                        curFile.delete();
                    }
                    SendErrorMail(_context, WholeErrorText);
                }
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        public String currentVersionNumber(Context a) {
            PackageManager pm = a.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo("de.gamedisk.app",
                        PackageManager.GET_SIGNATURES);
                return pi.versionName
                        + (pi.versionCode > 0 ? " (" + pi.versionCode + ")"
                        : "");
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
    }

    @Override
    public void onBackPressed () {
        if( m_nBack < 0 )
            finish();
        else
            goBack();
    }
}
