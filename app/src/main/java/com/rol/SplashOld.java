package com.rol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.rol.utils.DBHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashOld extends AppCompatActivity {

    DBHelper db;

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

        proceedToTheNextActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.e("Grant_Results", grantResults.length + "=====");

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    db.open();

                    if (db.getRegisterData().user_id != null) {
                        startActivity(new Intent(SplashOld.this, Task.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashOld.this, Home.class));
                        finish();
                    }
                    db.close();
                }
            }, 3000);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void proceedToTheNextActivity() {

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SplashOld.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                    4);
        } else {
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

                            Intent passintent = new Intent(SplashOld.this, ResetPassword.class);
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

                            Intent passintent = new Intent(SplashOld.this, TaskDetail.class);
                            passintent.putExtra("task_id", final_id);
                            startActivity(passintent);
                            finish();
                        } else {
                            db.open();
                            if (db.getRegisterData().user_id != null) {
                                startActivity(new Intent(SplashOld.this, Task.class));
                                finish();
                            } else {
                                startActivity(new Intent(SplashOld.this, Home.class));
                                finish();
                            }
                            db.close();
                        }
                    } else {
                        db.open();
                        if (db.getRegisterData().user_id != null) {
                            startActivity(new Intent(SplashOld.this, Task.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashOld.this, Home.class));
                            finish();
                        }
                        db.close();
                    }
                }
            }, 3000);
        }
    }
}
