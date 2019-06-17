package com.rol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.rol.utils.AppConstants;
import com.rol.utils.VolleyMultiPartRequest;
import com.rol.widgets.CustomButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private final int gallery = 1, camera = 2;
    String imagePath, IMAGE_DIRECTORY = "/Rol";
    Bitmap bitmap;
    CircleImageView user_profile;
    private ImageView back_btn;
    private CustomButton register_btn;
    private EditText first_name, last_name, email_address, password, cofirm_password;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        initialize();
    }

    private void initialize() {
        back_btn = findViewById(R.id.back_btn);
        register_btn = findViewById(R.id.register_btn);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email_address = findViewById(R.id.email_address);
        password = findViewById(R.id.password);
        cofirm_password = findViewById(R.id.confirm_pass);
        user_profile = findViewById(R.id.profile_picture);

        back_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        user_profile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == back_btn) {
            onBackPressed();
        } else if (view == register_btn) {
            if (validateData()) {
                if (AppConstants.isNetworkAvailable(activity)) {
                    RegisterUser();
                }
            }
        } else if (view == user_profile) {
            Log.e("click on", "user profile");
            showPictureDialog();
        }
    }

    private void RegisterUser() {
        String f_name = first_name.getText().toString();
        String l_name = last_name.getText().toString();
        String email = email_address.getText().toString();
        String password_text = password.getText().toString();

        String REQUEST_TAG = "register";
        String url = AppConstants.url + "register.php" + "?" + "first_name=" + f_name + "&last_name=" + l_name + "&email=" + email + "&password=" + password_text;

        Log.e("url", url);
        url = url.replaceAll(" ", "%20");

        // BitmapDrawable drawable = (BitmapDrawable) user_profile.getDrawable();
        // bitmap = drawable.getBitmap();

        VolleyMultiPartRequest request = new VolleyMultiPartRequest(VolleyMultiPartRequest.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    JSONObject main = new JSONObject(new String(response.data));
                    Log.e("response", main.toString());
                    if (main.getString("status").equalsIgnoreCase("1")) {
                        Toast.makeText(activity, main.getString("message"), Toast.LENGTH_SHORT).show();

                        openDialog();
                    } else {
                        Toast.makeText(activity, main.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {

                Map<String, DataPart> params = new HashMap<>();

                Log.e("image bitmap", "" + bitmap);

                if (bitmap != null) {
                    long imagename = System.currentTimeMillis();
                    params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                }
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                pickImage();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(galleryIntent, gallery);
    }

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, camera);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MainActivity.RESULT_CANCELED) {
            return;
        }

        if (requestCode == gallery) {
            if (data != null) {
                Uri selectedImage = data.getData();
                imagePath = getRealPathFromURI(selectedImage);

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                user_profile.setImageBitmap(bitmap);
            }
        } else if (requestCode == camera) {
            bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            File imageDirectory = new File(
                    Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

            if (!imageDirectory.exists()) {
                imageDirectory.mkdirs();
            }
            try {
                File f = new File(imageDirectory, Calendar.getInstance()
                        .getTimeInMillis() + ".jpg");

                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                imagePath = f.getAbsolutePath();
                Log.e("get file path", imagePath);

                user_profile.setImageBitmap(bitmap);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        Log.e("byte array of image", byteArrayOutputStream.toByteArray() + "");
        return byteArrayOutputStream.toByteArray();
    }

    private String getRealPathFromURI(Uri contentURI) {

        String thePath = "no-path-found";
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(contentURI, filePathColumn, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            thePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return thePath;
    }

    private boolean validateData() {
        if (AppConstants.nullCheck(activity, first_name.getText().toString(), activity.getString(R.string.enter_first_name))) {
            if (AppConstants.nullCheck(activity, last_name.getText().toString(), activity.getString(R.string.enter_last_name))) {
                if (AppConstants.nullCheck(activity, email_address.getText().toString(), activity.getString(R.string.enter_email))) {
                    if (AppConstants.isEmailValid(activity, email_address.getText().toString())) {
                        if (AppConstants.nullCheck(activity, password.getText().toString(), activity.getString(R.string.enter_password))) {
                            if (AppConstants.isPasswordValid(activity, password.getText().toString())) {
                                if (AppConstants.nullCheck(activity, cofirm_password.getText().toString(), activity.getString(R.string.enter_confirm_pass))) {
                                    if (password.getText().toString().equals(cofirm_password.getText().toString())) {
                                        return true;
                                    } else {
                                        Toast.makeText(activity, activity.getString(R.string.enter_pass_confirm_pass), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void openDialog() {
        final Dialog dialog = new Dialog(Register.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.varification_dialog);

        TextView ok_btn = dialog.findViewById(R.id.ok_btn);

        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }
}
