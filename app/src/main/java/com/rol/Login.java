package com.rol;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.beans.LoginBean;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.utils.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    DBHelper db;
    SharedPreferences spf;
    SharedPreferences.Editor editor;
    private Button login_btn;
    private EditText email_address, password;
    private Activity activity = this;
    private TextView forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Base.m_nCurField = Global.TASK;

        login_btn = findViewById(R.id.login_btn);
        email_address = findViewById(R.id.email_address);
        password = findViewById(R.id.password);
        forgot_password = findViewById(R.id.forgot_password);

        db = DBHelper.getInstance(this);

        spf = getSharedPreferences("my_pref", MODE_PRIVATE);
        editor = spf.edit();

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateData()) {
                    if (AppConstants.isNetworkAvailable(activity)) {
                        LoginUser();
                    }
                }
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(Login.this, ForgotPassword.class));
            }
        });
    }

    private void LoginUser() {
        String email = email_address.getText().toString();
        String pwd = password.getText().toString();

        String tag = "login";
        String url = AppConstants.url + "login.php?" + "email=" + email + "&password=" + pwd;

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

                        editor.putString("login_status", "login");
                        editor.apply();

                        JSONObject resObject = jsonObject.getJSONObject("responseData");
                        Log.e("second response", resObject + "");

                        LoginBean bean = new LoginBean();
                        bean.user_id = resObject.getString("user_id");
                        bean.first_name = resObject.getString("first_name");
                        bean.last_name = resObject.getString("last_name");
                        bean.email = resObject.getString("email");
                        bean.profile_picture = resObject.getString("profile_picture");
                        bean.auth_token = resObject.getString("auth_token");

                        db.open();
                        db.addRegisterData(bean);
                        db.close();
                        Intent intent = new Intent(Login.this, Task.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                            finishAffinity();
                        } else {

                            finish();
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

    private boolean validateData() {
        if (AppConstants.nullCheck(activity, email_address.getText().toString(), activity.getString(R.string.enter_email))) {
            if (AppConstants.isEmailValid(activity, email_address.getText().toString())) {
                if (AppConstants.nullCheck(activity, password.getText().toString(), activity.getString(R.string.enter_password))) {
                    if (AppConstants.isPasswordValid(activity, password.getText().toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
