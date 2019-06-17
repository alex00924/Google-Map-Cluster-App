package com.rol.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppConstants {

    public static final String TAG_APP = "abe";

    public static final String url = "http://keshavinfotechdemo.com/KESHAV/KG2/ROL/webservices/";
    //public static final String url = "http://192.168.43.88:8080/ROL/webservices/";
    //public static final String url = "http://lendyourhand.org/ROL/webservices/";
    public static final String Deeplinking = "rolapplication.page.link";
    public static Bitmap thumbnail;
    public static String name = "name";
    public static DecimalFormat decimalFormat;

    //public static final String Deeplinking="rolapplication.page.link";

    public static boolean isEmailValid(Activity activity, String email) {
        String regExpn = "^[\\w\\.'-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            return true;
        else
            Toast.makeText(activity, "Please enter valid email address", Toast.LENGTH_SHORT).show();
        return false;
    }

    public static boolean isPasswordValid(Activity activity, String password) {

        if (password != null && password.length() > 5)
            return true;
        else
            Toast.makeText(activity, "Please enter at least 6 characters in password", Toast.LENGTH_SHORT).show();
        return false;
    }

    public static boolean nullCheck(Activity activity, String string, String message) {
        if (string != null && string.length() > 0) {
            return true;
        } else {
            if (message.length() > 0)
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {

        boolean isMobile = false, isWifi = false;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] infoAvailableNetworks = cm.getAllNetworkInfo();

        if (infoAvailableNetworks != null) {
            for (NetworkInfo network : infoAvailableNetworks) {

                if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (network.isConnected() && network.isAvailable())
                        isWifi = true;
                }
                if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (network.isConnected() && network.isAvailable())
                        isMobile = true;
                }
            }
        }

        if (isMobile || isWifi) {

        } else {
            Toast.makeText(context, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        return isMobile || isWifi;
    }

    public static String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public static Date getFormattedCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = c.getTime();
        df.format(date);
        return date;
    }

    public static String convertOnlyTaskdate(String dt) {
        String convertedDate = "";
        SimpleDateFormat targetFormater = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date mdate = formatter.parse(dt);
            convertedDate = targetFormater.format(mdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*Log.e("returned_date",convertedDate);*/
        return convertedDate;
    }

    public static String convertTaskdateTime(String dt) {
        String convertedDate = "";
        // SimpleDateFormat targetFormater = new SimpleDateFormat("dd/MM/yyyy,hh:mm:aa");
        // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        SimpleDateFormat targetFormater = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date mdate = formatter.parse(dt);
            convertedDate = targetFormater.format(mdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*Log.e("returned_date",convertedDate);*/
        return convertedDate;
    }

    public static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        Log.e("converted value", twoDForm.format(d));
        return Double.valueOf(twoDForm.format(d));
    }

    public enum PAGINATION {
        First_Load, Previous, Next
    }

    public enum IMAGES {
        ProductImages1, eventImages1, category
    }
}
