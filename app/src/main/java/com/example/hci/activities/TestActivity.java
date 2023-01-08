package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.hci.R;

public class TestActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor sensor;
    private FrameLayout frameLayout;
    private ImageView ballImage;
    private SensorEventListener gyroListener;
    private ImageView boxImage;
    private int zeroX, zeroY;
    private int ballSize, boxSize;
    private int layoutWidth, layoutHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        frameLayout = findViewById(R.id.layout);
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutWidth = frameLayout.getWidth();
                layoutHeight = frameLayout.getHeight();

                ballSize = layoutWidth / 6;
                boxSize = ballSize * 2;
                zeroX = (int) (layoutWidth / 2.f);
                zeroY = (int) (layoutHeight / 2.f);

                ballImage = new ImageView(TestActivity.this);
                ballImage.setImageResource(R.drawable.ball);
                ballImage.setLayoutParams(new FrameLayout.LayoutParams(ballSize, ballSize));

                setBallPosition(0, 0);
                frameLayout.addView(ballImage);

                boxImage = new ImageView(TestActivity.this);
                boxImage.setImageResource(R.drawable.box);
                boxImage.setLayoutParams(new FrameLayout.LayoutParams(ballSize * 2, ballSize * 2));
                setBoxPosition(0, layoutHeight / 4);
                frameLayout.addView(boxImage);

                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        gyroListener = new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                transformGyroValues(event.values, layoutWidth / 2.f - ballSize / 2.f, layoutHeight / 2.f - ballSize / 2.f);
                setBallPosition((int) event.values[0], (int) event.values[1]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private void setBallPosition(int x, int y) {
        FrameLayout.LayoutParams ballLayoutParams = (FrameLayout.LayoutParams) ballImage.getLayoutParams();

        ballLayoutParams.leftMargin = zeroX - ballSize / 2 - x;
        ballLayoutParams.topMargin = zeroY - ballSize / 2 + y;

        ballImage.setLayoutParams(ballLayoutParams);
    }

    private void setBoxPosition(int x, int y) {
        FrameLayout.LayoutParams boxLayoutParams = new FrameLayout.LayoutParams(ballSize * 2, ballSize * 2);
        boxLayoutParams.leftMargin = zeroX - boxSize / 2 + x;
        boxLayoutParams.topMargin = zeroY - boxSize / 2 + y;
        boxImage.setLayoutParams(boxLayoutParams);
    }

    private void transformGyroValues(float[] measurements, float maxRangeX, float maxRangeY) {
        final float maxSensorRange = sensor.getMaximumRange() / 8f;

        float xRelative = Math.max(-maxSensorRange, Math.min(measurements[0], maxSensorRange)) / maxSensorRange;
        float yRelative = Math.max(-maxSensorRange, Math.min(measurements[1], maxSensorRange)) / maxSensorRange;

        measurements[0] = xRelative * maxRangeX;
        measurements[1] = yRelative * maxRangeY;
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }
}