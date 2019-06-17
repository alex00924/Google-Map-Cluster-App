package com.rol;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ForgotPassword extends Base {

    EditText email_address;
    Button submit_btn;
    String msg = null;
    DBHelper db;
    LoginBean bean;
    String TAG = "FORGOTPASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.forgot_password, wrapper);

        base_title.setText(R.string.forgot_password_text);

        bottom_layout.setVisibility(View.GONE);
        logout_btn.setVisibility(View.GONE);
        notification_layout.setVisibility(View.GONE);

        email_address = findViewById(R.id.email_address);

        db = DBHelper.getInstance(ForgotPassword.this);

        submit_btn = findViewById(R.id.submit_btn);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppConstants.isNetworkAvailable(ForgotPassword.this)) {
                    forgotPasswordServiceCall();
                }
            }
        });
    }

    private void forgotPasswordServiceCall() {
        String email = email_address.getText().toString();
        String tag = "forgotPassword";
        String url = AppConstants.url + "forgot_password.php?" + "email=" + email;
        url = url.replaceAll(" ", "%20");
        Log.e(TAG, url);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e(TAG, "response" + jsonObject + "");

                    String message = jsonObject.getString("message");

                    if (jsonObject.getString("status").equalsIgnoreCase("0")) {
                        Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
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

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }
}
