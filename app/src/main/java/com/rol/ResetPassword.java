package com.rol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.widgets.CustomButton;
import com.rol.widgets.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPassword extends Base implements View.OnClickListener {
    private CustomEditText edtNewPasword, edtConfirmPasword;
    private CustomButton submitBtn;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.reset_password);

        getLayoutInflater().inflate(R.layout.reset_password, wrapper);

        base_title.setText(R.string.reset_password);
        bottom_layout.setVisibility(View.GONE);
        logout_btn.setVisibility(View.GONE);
        notification_layout.setVisibility(View.GONE);

        findViews();

        Intent intent = getIntent();
        if (intent.hasExtra("user_id")) {
            userId = intent.getStringExtra("user_id");
        }
    }

    private void findViews() {
        edtNewPasword = findViewById(R.id.edt_new_pasword);
        edtConfirmPasword = findViewById(R.id.edt_confirm_pasword);
        submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == submitBtn) {
            validation();
        }
    }

    private void validation() {
        if (edtNewPasword.getText().toString().length() > 0 && !edtNewPasword.getText().toString().equalsIgnoreCase("")) {
            if (AppConstants.isPasswordValid(activity, edtNewPasword.getText().toString())) {
                if (edtConfirmPasword.getText().toString().length() > 0 && !edtConfirmPasword.getText().toString().equalsIgnoreCase("")) {
                    if (AppConstants.isPasswordValid(activity, edtConfirmPasword.getText().toString())) {
                        if (edtConfirmPasword.getText().toString().equals(edtNewPasword.getText().toString())) {
                            resetService();
                        } else {
                            Toast.makeText(ResetPassword.this, getString(R.string.confirm_password), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ResetPassword.this, getString(R.string.enter_toast_confirm_password), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(ResetPassword.this, getString(R.string.enter_toast_new_password), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetService() {
        String newPassword = edtNewPasword.getText().toString();

        String tag = "resetpassword";
        String url = AppConstants.url + "resetpassword.php?" + "new_pass=" + newPassword + "&user_id=" + userId;

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
                        Toast.makeText(ResetPassword.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPassword.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPassword.this, Login.class));
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
