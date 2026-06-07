package com.example.fitcoach.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.fitcoach.R;

public class BMIView extends View {
    private Drawable gaugeDrawable;
    private Drawable needleDrawable;
    private Paint textPaint;
    private float bmi = 0f;
    private float rotation = -65f;
    private String statusText = "";
    private int accentColor;

    public BMIView(Context context) {
        super(context);
        init();
    }

    public BMIView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BMIView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gaugeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.bmi_gauge_arc_bold);
        needleDrawable = ContextCompat.getDrawable(getContext(), R.drawable.bmi_needle_bold);
        accentColor = 0xFF2196F3;

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(accentColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(spToPx(14));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    private float spToPx(float sp) {
        return sp * getContext().getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Sets the BMI value and updates the gauge needle position and status text.
     * @param bmi The BMI value to display.
     */
    public void setBMI(float bmi) {
        this.bmi = bmi;
        updateRotation();
        updateStatusText();
        invalidate();
    }

    /**
     * Calculates BMI from weight and height, then updates the gauge.
     * @param weightKg Weight in kilograms.
     * @param heightCm Height in centimeters.
     */
    public void setBMI(float weightKg, float heightCm) {
        if (heightCm > 0) {
            float heightM = heightCm / 100f;
            float calculatedBmi = weightKg / (heightM * heightM);
            setBMI(calculatedBmi);
        }
    }

    /**
     * Returns the current BMI value displayed.
     * @return The BMI value.
     */
    public float getBMI() {
        return bmi;
    }

    private void updateRotation() {
        if (bmi <= 0) {
            rotation = -65f;
            return;
        }
        if (bmi < 15f) rotation = -65f;
        else if (bmi < 18.5f) rotation = map(bmi, 15f, 18.5f, -65f, -30f);
        else if (bmi < 25f) rotation = map(bmi, 18.5f, 25f, -30f, 30f);
        else if (bmi < 30f) rotation = map(bmi, 25f, 30f, 30f, 65f);
        else rotation = 65f;

        rotation = Math.max(-65f, Math.min(rotation, 65f));
    }

    private void updateStatusText() {
        if (bmi <= 0) {
            statusText = "";
            return;
        }
        if (bmi < 18.5) {
            statusText = "תת משקל";
        } else if (bmi < 25) {
            statusText = "משקל תקין";
        } else {
            statusText = "משקל עודף";
        }
    }

    private float map(float value, float fromLow, float fromHigh, float toLow, float toHigh) {
        return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        // Height = Gauge (width/2) + Margin (10dp) + TextHeight (~20dp)
        int gaugeHeight = width / 2;
        int extraHeight = (int) (spToPx(14) + spToPx(15)); // Text size + gap
        setMeasuredDimension(width, gaugeHeight + extraHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int gaugeHeight = width / 2;

        if (gaugeDrawable != null) {
            gaugeDrawable.setBounds(0, 0, width, gaugeHeight);
            gaugeDrawable.draw(canvas);
        }

        if (needleDrawable != null) {
            canvas.save();
            int needleSize = gaugeHeight; 
            int left = (width - needleSize) / 2;
            int top = 0;
            needleDrawable.setBounds(left, top, left + needleSize, top + needleSize);
            
            float pivotX = width / 2f;
            float pivotY = top + needleSize * 0.935f;
            
            canvas.rotate(rotation, pivotX, pivotY);
            needleDrawable.draw(canvas);
            canvas.restore();
        }

        if (statusText != null && !statusText.isEmpty()) {
            float textY = gaugeHeight + spToPx(20);
            canvas.drawText(statusText, width / 2f, textY, textPaint);
        }
    }
}
