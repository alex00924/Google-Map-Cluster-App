package com.rol;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rol.utils.DBHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Splash extends AppCompatActivity {
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    DBHelper db;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private boolean isPermission_check = true;

    public static void hashkey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.rol",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("SIGNACTIVITY", "KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        db = DBHelper.getInstance(this);
        hashkey(this);

        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isPermission_check) {
            checkPermission();
        }
    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Dexter.withActivity(Splash.this)
                    .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {

                                proceedFurther();
                            } else {
                                Toast.makeText(Splash.this, "Please grant all required permissions to use the ROL Application.", Toast.LENGTH_SHORT).show();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()) {
                                showSettingsDialog();
                                // finish();
                                Toast.makeText(Splash.this, "Please grant all required permissions to use the ROL Application.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            Toast.makeText(Splash.this, "Please grant all required permissions to use the ROL Application.", Toast.LENGTH_SHORT).show();
                            token.continuePermissionRequest();
                        }
                    })
                    .onSameThread()
                    .check();
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                isPermission_check = false;
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void proceedFurther() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = getIntent();
                if (i.getData() != null) {
                    String url = i.getData().toString();
                    Log.e("SPLASH", "" + url);
                    if (url.contains("_")) {
                        String[] parts = url.split("_"); // escape .
                        String part1 = parts[0];
                        String part2 = parts[1];

                        Log.e("SPLASH", "part1:=" + part1);
                        Log.e("SPLASH", "part2:=" + part2);

                        Intent passintent = new Intent(Splash.this, ResetPassword.class);
                        passintent.putExtra("user_id", part2);
                        startActivity(passintent);
                        finish();
                    } else if (url.contains("ref")) {

                        String[] parts = url.split("="); // escape .
                        String part1 = parts[0];
                        String part2 = parts[1];
                        String part3 = parts[2];

                        Log.e("SPLASH link1", "part1:=" + part1);
                        Log.e("SPLASH link2", "part3:=" + part3);

                        String[] second_parts = part3.split("&"); // escape .
                        String part4 = second_parts[0];
                        String part5 = second_parts[1];

                        String final_id = part4.replaceFirst("=", "");
                        Log.e("SPLASH link3", "part4:" + final_id);

                        Intent passintent = new Intent(Splash.this, TaskDetail.class);
                        passintent.putExtra("task_id", final_id);
                        startActivity(passintent);
                        finish();
                    } else {
                        db.open();
                        if (db.getRegisterData().user_id != null) {
                            startActivity(new Intent(Splash.this, Task.class));
                            finish();
                        } else {
                            startActivity(new Intent(Splash.this, Home.class));
                            finish();
                        }
                        db.close();
                    }
                } else {
                    db.open();
                    if (db.getRegisterData().user_id != null) {
                        startActivity(new Intent(Splash.this, Task.class));
                        finish();
                    } else {
                        startActivity(new Intent(Splash.this, Home.class));
                        finish();
                    }
                    db.close();
                }



                /*db.open();

                if (db.getRegisterData().user_id != null)
                {
                    startActivity(new Intent(Splash.this, Task.class));
                    finish();
                }
                else
                {
                    startActivity(new Intent(Splash.this, Home.class));
                    finish();

                }
                db.close();*/

            }
        }, 3000);
    }
}
