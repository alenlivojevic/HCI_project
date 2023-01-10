package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
    private Vibrator vibrator;
    private int boxPositionX = 0, boxPositionY = 0;
    private int ballPositionX = 0, ballPositionY = 0;
    private float boxGreenColor = 0;
    private float timeInBox = 0.f;
    private final float requiredTimeInBox = 3.f;
    private float lastTime = 0.f;
    private float[][] lastMeasurements;
    private static final int NR_OF_MEASUREMENTS = 5;
    private int measurementCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        lastTime = System.nanoTime();

        lastMeasurements = new float[NR_OF_MEASUREMENTS][2];

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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

                setBallPosition(ballPositionX, ballPositionY);
                frameLayout.addView(ballImage);

                boxImage = new ImageView(TestActivity.this);
                boxImage.setImageResource(R.drawable.box);
                boxImage.setLayoutParams(new FrameLayout.LayoutParams(ballSize * 2, ballSize * 2));
                boxPositionY = layoutHeight / 4;
                setBoxPosition(boxPositionX, boxPositionY);
                frameLayout.addView(boxImage);

                ballImage.bringToFront();

                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        gyroListener = new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                float currentTime = System.nanoTime();
                lastMeasurements[measurementCount][0] = event.values[0];
                lastMeasurements[measurementCount][1] = event.values[1];
                ++measurementCount;

                if (measurementCount == NR_OF_MEASUREMENTS) {
                    measurementCount = 0;
                }
                float[] averageMeasurements = new float[2];
                averageMeasurements[0] = 0.f;
                averageMeasurements[1] = 0.f;
                for (int i = 0; i < NR_OF_MEASUREMENTS; ++i) {
                    averageMeasurements[0] += lastMeasurements[i][0];
                    averageMeasurements[1] += lastMeasurements[i][1];
                }

                averageMeasurements[0] /= NR_OF_MEASUREMENTS;
                averageMeasurements[1] /= NR_OF_MEASUREMENTS;

                transformGyroValues(averageMeasurements, layoutWidth / 2.f - ballSize / 2.f, layoutHeight / 2.f - ballSize / 2.f);
                ballPositionX = (int) averageMeasurements[0];
                ballPositionY = (int) averageMeasurements[1];

                setBallPosition(ballPositionX, ballPositionY);
                if (ballPositionX - ballSize / 2 > boxPositionX - boxSize / 2 &&
                        ballPositionX + ballSize / 2 < boxPositionX + boxSize / 2 &&
                        ballPositionY + ballSize / 2 < boxPositionY + boxSize / 2 &&
                        ballPositionY - ballSize / 2 > boxPositionY - boxSize / 2) {
                    // Change box color
                    float deltaTime = currentTime - lastTime;
                    timeInBox += deltaTime / 1e9;
                    int colorIntensity = calculateColorIntensity();
                    boxImage.setBackgroundColor(Color.rgb(255 - colorIntensity, 255, 255 - colorIntensity));

                    if (timeInBox >= requiredTimeInBox) {
                        vibrator.vibrate(100);
                        timeInBox = 0.f;
                    }
                } else if (timeInBox > 0.f) {
                    timeInBox = 0.f;
                    boxImage.setBackgroundColor(Color.TRANSPARENT);
                }

                lastTime = currentTime;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private int calculateColorIntensity() {
        return (int)((timeInBox / requiredTimeInBox) * 255);
    }

    private void setBallPosition(int x, int y) {
        FrameLayout.LayoutParams ballLayoutParams = (FrameLayout.LayoutParams) ballImage.getLayoutParams();

        ballLayoutParams.leftMargin = zeroX - ballSize / 2 + x;
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

        measurements[0] = -xRelative * maxRangeX; // - is necessary because of inverted x axis on gyroscope
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