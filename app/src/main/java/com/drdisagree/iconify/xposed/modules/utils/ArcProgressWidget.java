package com.drdisagree.iconify.xposed.modules.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class ArcProgressWidget {

    public static Bitmap generateBitmap(Context context, int percentage, String textInside, int textInsideSizePx, @Nullable String textBottom, int textBottomSizePx) {
        return generateBitmap(context, percentage, textInside, textInsideSizePx, null, 28, textBottom, textBottomSizePx);
    }

    public static Bitmap generateBitmap(Context context, int percentage, String textInside, int textInsideSizePx, @Nullable Drawable iconDrawable, int iconSizePx) {
        return generateBitmap(context, percentage, textInside, textInsideSizePx, iconDrawable, iconSizePx, "Usage", 28);
    }

    public static Bitmap generateBitmap(Context context, int percentage, String textInside, int textInsideSizePx, @Nullable Drawable iconDrawable, int iconSizePx, @Nullable String textBottom, int textBottomSizePx) {
        int width = 400;
        int height = 400;
        int stroke = 40;
        int padding = 5;
        int minAngle = 135;
        int maxAngle = 275;

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(ViewHelper.dp2px(context, textInsideSizePx));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        final RectF arc = new RectF();
        arc.set(((float) stroke / 2) + padding, ((float) stroke / 2) + padding, width - padding - ((float) stroke / 2), height - padding - ((float) stroke / 2));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        paint.setColor(Color.argb(75, 255, 255, 255));
        canvas.drawArc(arc, minAngle, maxAngle, false, paint);

        paint.setColor(Color.WHITE);
        canvas.drawArc(arc, minAngle, ((float) maxAngle / 100) * percentage, false, paint);

        mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(textInside, (float) bitmap.getWidth() / 2, (bitmap.getHeight() - mTextPaint.ascent() * 0.7f) / 2, mTextPaint);

        if (iconDrawable != null) {
            int size = ViewHelper.dp2px(context, iconSizePx);
            int left = (bitmap.getWidth() - size) / 2;
            int top = bitmap.getHeight() - (int) (size / 1.5) - (stroke + padding);
            int right = left + size;
            int bottom = top + size;

            iconDrawable.setBounds(left, top, right, bottom);
            iconDrawable.setColorFilter(new BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN));
            iconDrawable.draw(canvas);
        } else if (textBottom != null) {
            mTextPaint.setTextSize(ViewHelper.dp2px(context, textBottomSizePx));
            canvas.drawText(textBottom, (float) bitmap.getWidth() / 2, bitmap.getHeight() - (stroke + padding), mTextPaint);
        }

        return bitmap;
    }
}
