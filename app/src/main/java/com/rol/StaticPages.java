package com.rol;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rol.utils.AppConstants;
import com.rol.utils.AppSingleton;
import com.rol.widgets.CustomTextView;

import org.json.JSONException;
import org.json.JSONObject;

public class StaticPages extends Base {

    String user_id, auth_token, type = "", page_type = "";
    CustomTextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.static_page, wrapper);

        if (getIntent().hasExtra("page")) {
            page_type = getIntent().getStringExtra("page");
        }

        if (page_type.equalsIgnoreCase("home")) {
            bottom_layout.setVisibility(View.GONE);
            notification_layout.setVisibility(View.GONE);
            logout_btn.setVisibility(View.GONE);
        }

        if (getIntent().hasExtra("type"))
            type = getIntent().getStringExtra("type");

        if (type.equalsIgnoreCase("terms")) {
            base_title.setText("Terms & condition");
        } else if (type.equalsIgnoreCase("privacy")) {
            base_title.setText("Privacy policy");
        } else if (type.equalsIgnoreCase("help")) {
            base_title.setText("Help");
        } else if (type.equalsIgnoreCase("imprint")) {
            base_title.setText("imprint");
        }/* else if(type.equalsIgnoreCase("Feedback"))
        {
            base_title.setText("feedback");

        }*/

        content = findViewById(R.id.content);

        db.open();
        user_id = db.getRegisterData().user_id;
        auth_token = db.getRegisterData().auth_token;
        db.close();

        if (AppConstants.isNetworkAvailable(activity))
            showStaticContent();
    }

    private void showStaticContent() {

        String tag = "staticContent";
        String url = AppConstants.url + "static_pages.php?" + "user_id=" + user_id + "&auth_token=" + auth_token + "&type=" + type;
        url = url.replaceAll(" ", "%20");
        Log.e("url", url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject + "");

                    JSONObject data = jsonObject.getJSONObject("responseData");
                    content.setText(Html.fromHtml(data.getString("content")));
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


