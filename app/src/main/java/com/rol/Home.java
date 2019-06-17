package com.rol;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rol.beans.LoginBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Home extends AppCompatActivity {

    Button register_btn, login_btn;
    LinearLayout fblogin_btn;
    CallbackManager callbackManager;

    String fb_email = "";
    String fb_token, fb_id, last_name, first_name, picture, gender, birth_date;
    SharedPreferences spf;
    SharedPreferences.Editor editor;
    TextView terms_condition, privacy_policy;

    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        register_btn = findViewById(R.id.registration_btn);
        login_btn = findViewById(R.id.login_btn);
        fblogin_btn = findViewById(R.id.facebook_btn);
        terms_condition = findViewById(R.id.terms_condition);
        privacy_policy = findViewById(R.id.privacy_policy);

        spf = getSharedPreferences("my_pref", MODE_PRIVATE);
        editor = spf.edit();
        db = DBHelper.getInstance(this);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Register.class));
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Login.class));
                /*finish();*/

            }
        });

        fblogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.isNetworkAvailable(Home.this)) {
                    facebook_login();
                }
            }
        });

        terms_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, StaticPages.class);
                intent.putExtra("type", "terms");
                intent.putExtra("page", "home");
                startActivity(intent);
            }
        });

        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, StaticPages.class);
                intent.putExtra("type", "privacy");
                intent.putExtra("page", "home");
                startActivity(intent);
            }
        });
    }

    private void facebook_login() {
        FacebookSdk.sdkInitialize(Home.this);
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
                                        if (object.has("email")) {
                                            fb_email = object.getString("email");
                                            Log.e("login email", fb_email);
                                        }
                                        if (object.has("id")) {
                                            fb_id = object.getString("id");
                                        }

                                        if (object.has("first_name")) {
                                            first_name = object.getString("first_name");
                                        }
                                        if (object.has("last_name")) {
                                            last_name = object.getString("last_name");
                                        }
                                        if (object.has("picture")) {

                                            picture = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                            Log.e("LOG : picture", picture);
                                        }
                                        if (object.has("gender")) {
                                            gender = object.getString("gender");
                                        }
                                        if (object.has("birthday")) {
                                            birth_date = object.getString("birthday");
                                        }

                                        if (AppConstants.isNetworkAvailable(Home.this)) {
                                            facebook_login_servicecall();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,email,picture,last_name,gender,birthday");
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

        LoginManager.getInstance().logInWithReadPermissions(Home.this,
                Arrays.asList("public_profile", "email"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void facebook_login_servicecall() {
        String REQUEST_TAG = "facebook_login";
        String url = AppConstants.url + "login_with_facebook.php" + "?" + "email=" + fb_email + "&fb_token=" + fb_token + "&fb_id=" + fb_id + "&first_name=" + first_name + "&last_name=" + last_name + "&gender=" + gender + "&birth_date=" + birth_date + "&latitude=" + "" + "&longitude=" + "";

        Log.e("url", url);
        url = url.replaceAll(" ", "%20");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject main = new JSONObject(new String(response));
                    Log.e("response", main + "");

                    if (main.getString("status").equalsIgnoreCase("1")) {
                        editor.putString("login_status", "facebook_login");
                        editor.apply();
                        JSONObject resObject = main.getJSONObject("responseData");

                        LoginBean bean = new LoginBean();
                        bean.user_id = resObject.getString("user_id");
                        bean.first_name = resObject.getString("first_name");
                        bean.last_name = resObject.getString("last_name");
                        bean.email = resObject.getString("email");
                        bean.profile_picture = resObject.getString("profile_picture");
                        bean.auth_token = resObject.getString("auth_token");
                        bean.fb_token = resObject.getString("fb_id");
                        bean.fb_token = resObject.getString("fb_token");

                        db.open();
                        db.addRegisterData(bean);
                        db.close();
                        startActivity(new Intent(Home.this, Task.class));
                        finish();
                    } else {
                        Toast.makeText(Home.this, main.getString("message"), Toast.LENGTH_SHORT).show();
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

        AppSingleton.getInstance(this).addToRequestQueue(stringRequest, REQUEST_TAG);
    }
}
