package com.rol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePassword extends Base {

    TextView old_password, new_password, confirm_password;
    Button submit_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.change_password, wrapper);

        base_title.setText(R.string.change_password);
        bottom_layout.setVisibility(View.GONE);
        logout_btn.setVisibility(View.GONE);

        old_password = findViewById(R.id.edt_old_pasword);
        new_password = findViewById(R.id.edt_new_pasword);
        confirm_password = findViewById(R.id.edt_confirm_pasword);
        submit_btn = findViewById(R.id.submit_btn);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateData()) {
                    if (AppConstants.isNetworkAvailable(ChangePassword.this)) {
                        changepassword();
                    }
                }
            }
        });
    }

    private boolean validateData() {
        if (AppConstants.nullCheck(activity, old_password.getText().toString(), activity.getString(R.string.enter_old_password))) {
            if (AppConstants.isPasswordValid(activity, old_password.getText().toString())) {
                if (AppConstants.nullCheck(activity, new_password.getText().toString(), activity.getString(R.string.enter_new_password))) {
                    if (AppConstants.isPasswordValid(activity, new_password.getText().toString())) {
                        if (AppConstants.nullCheck(activity, confirm_password.getText().toString(), activity.getString(R.string.enter_confirm_pass))) {
                            if (new_password.getText().toString().equals(confirm_password.getText().toString())) {
                                return true;
                            } else {
                                Toast.makeText(activity, activity.getString(R.string.enter_pass_confirm_pass), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void changepassword() {
        String newPassword = new_password.getText().toString();
        String oldPassword = old_password.getText().toString();

        String tag = "changepassword";
        String url = AppConstants.url + "change_pass.php?" + "user_id=" + userId + "&auth_token=" + authToken + "&new_pass=" + newPassword + "&old_pass=" + oldPassword;

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
                        Toast.makeText(ChangePassword.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChangePassword.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ChangePassword.this, Login.class));
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

                        Log.e("reset pass", error.toString());
                    }
                });

        AppSingleton.getInstance(this).addToRequestQueue(request, tag);
    }
}
