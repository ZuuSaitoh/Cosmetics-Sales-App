package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import vn.vietmap.vietmapsdk.annotations.Icon;
import vn.vietmap.vietmapsdk.annotations.IconFactory;

public class IconUtils {

    /**
     * Converts any Drawable to an Icon for use as a marker icon.
     */
    public static Icon drawableToIcon(Context context, @DrawableRes int id, @ColorInt int colorRes) {
        try {
            // Lấy Drawable từ resource
            android.graphics.drawable.Drawable vectorDrawable =
                    ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());

            if (vectorDrawable == null) {
                return null;
            }

            // Tạo bitmap theo kích thước của drawable
            Bitmap bitmap = Bitmap.createBitmap(
                    vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

            // Vẽ drawable lên canvas bitmap
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            DrawableCompat.setTint(vectorDrawable, colorRes);
            vectorDrawable.draw(canvas);

            // Trả về Icon sử dụng VietMap IconFactory
            return IconFactory.getInstance(context).fromBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
