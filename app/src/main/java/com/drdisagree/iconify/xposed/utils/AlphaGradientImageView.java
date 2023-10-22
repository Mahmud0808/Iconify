package com.drdisagree.iconify.xposed.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class AlphaGradientImageView extends AppCompatImageView {

    private Paint paint;
    private boolean alphaGradient = false;

    public AlphaGradientImageView(Context context) {
        super(context);
        init();
    }

    public AlphaGradientImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlphaGradientImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
    }

    public void setAlphaGradient(boolean alphaGradient) {
        this.alphaGradient = alphaGradient;
        postInvalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable && alphaGradient) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                float scaleWidth = getWidth() / (float) bitmap.getWidth();
                float scaleHeight = getHeight() / (float) bitmap.getHeight();

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);

                BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                bitmapShader.setLocalMatrix(matrix);

                int viewHeight = getHeight();
                int gradientHeight = viewHeight / 2;
                int[] colors = {0xFFFFFFFF, 0x00FFFFFF};
                float[] positions = {0, gradientHeight / (float) viewHeight};
                Shader gradientShader = new LinearGradient(0, viewHeight - gradientHeight, 0, viewHeight, colors, positions, Shader.TileMode.CLAMP);

                Shader shader = new ComposeShader(bitmapShader, gradientShader, PorterDuff.Mode.DST_IN);

                paint.setShader(shader);
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                return;
            }
        }

        super.onDraw(canvas);
    }
}
