package com.rol.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.rol.R;

import java.util.Hashtable;

@SuppressLint("AppCompatCustomView")
public class CustomButton extends Button {

//    public CustomButton(Context context) {
//        super(context);
//    }
//
//    public CustomButton(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        setCustomFont(context, attrs);
//    }
//
//    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        setCustomFont(context, attrs);
//    }
//
//    private void setCustomFont(Context ctx, AttributeSet attrs) {
//        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomButton);
//        String customFont = a.getString(R.styleable.CustomButton_customFontButton);
//        setCustomFont(ctx, customFont);
//        a.recycle();
//    }
//
//    public boolean setCustomFont(Context ctx, String asset) {
//        Typeface tf = null;
//        try {
//            tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/" + asset);
//        } catch (Exception e) {
//            Log.e("CustomTextView", "Could not get typeface: " + e.getMessage());
//            return false;
//        }
//
//        setTypeface(tf);
//        return true;
//    }
//
//    @Override
//    public void setLongClickable(boolean longClickable) {
//        if (getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
//            super.setLongClickable(false);
//        } else {
//            super.setLongClickable(true);
//        }
//    }

    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(this, context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(this, context, attrs);
    }

    /**
     * Sets a font on a textview based on the custom com.my.package:font attribute
     * If the custom font attribute isn't found in the attributes nothing happens
     *
     * @param textview
     * @param context
     * @param attrs
     */
    public static void setCustomFont(TextView textview, Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomButton);
        String font = a.getString(R.styleable.CustomButton_customFontButton);
        setCustomFont(textview, font, context);
        a.recycle();
    }

    /**
     * Sets a font on a textview
     *
     * @param textview
     * @param font
     * @param context
     */
    public static void setCustomFont(TextView textview, String font, Context context) {
        if (font == null) {
            return;
        }
        Typeface tf = getFontCache(font, context);
        if (tf != null) {
            textview.setTypeface(tf);
        }
    }

    public static Typeface getFontCache(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}
