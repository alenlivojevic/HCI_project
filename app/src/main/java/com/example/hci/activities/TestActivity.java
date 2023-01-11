package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hci.R;
import com.example.hci.utils.LogEntry;

import java.util.Random;

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
    private final float requiredTimeInBox = 0.2f;
    private float lastTime = 0.f;
    private float[][] lastMeasurements;
    private static final int NR_OF_MEASUREMENTS = 5;
    private int measurementCount = 0;

    private Random randomNumberGenerator = new Random();

    private static final int BOXES_PER_MODE = 9;
    private TextView boxCounterLabel, modeLabel, difficultyLabel;
    private int boxCounter = 0;
    private LogEntry.ContextMode contextMode;
    private LogEntry.DifficultyMode difficultyMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        boxCounterLabel = findViewById(R.id.box_counter);
        modeLabel = findViewById(R.id.mode);
        difficultyLabel = findViewById(R.id.difficulty);

        lastTime = System.nanoTime();

        lastMeasurements = new float[NR_OF_MEASUREMENTS][2];

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        frameLayout = findViewById(R.id.frame_layout);

        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutWidth = frameLayout.getWidth();
                layoutHeight = frameLayout.getHeight();

                ballSize = layoutWidth / 6;
                zeroX = (int) (layoutWidth / 2.f);
                zeroY = (int) (layoutHeight / 2.f);

                ballImage = new ImageView(TestActivity.this);
                ballImage.setImageResource(R.drawable.ball);
                ballImage.setLayoutParams(new FrameLayout.LayoutParams(ballSize, ballSize));

                setBallPosition(ballPositionX, ballPositionY);
                frameLayout.addView(ballImage);

                boxImage = new ImageView(TestActivity.this);
                boxImage.setImageResource(R.drawable.box);
                frameLayout.addView(boxImage);

                startNewTestingCycle();

                ballImage.bringToFront();

                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        gyroListener = new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
               onNewMeasurement(event.values[0], event.values[1]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private void onNewMeasurement(float x, float y) {
        float currentTime = System.nanoTime();
        lastMeasurements[measurementCount][0] = x;
        lastMeasurements[measurementCount][1] = y;
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
                ++boxCounter;
                boxImage.setBackgroundColor(Color.TRANSPARENT);
                spawnRandomBox();
                boxCounterLabel.setText(boxCounter + "/" + BOXES_PER_MODE);
                if (boxCounter == BOXES_PER_MODE) {
                    finish();
                }
            }
        } else if (timeInBox > 0.f) {
            timeInBox = 0.f;
            boxImage.setBackgroundColor(Color.TRANSPARENT);
        }

        lastTime = currentTime;
    }

    private void startNewTestingCycle() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("user_data", 0);
        String contextModeString = sharedPreferences.getString("modality", "IN_HANDS");
        String difficultyModeString = sharedPreferences.getString("difficulty", "EASY");

        switch (contextModeString) {
            case "IN_HANDS":
                contextMode = LogEntry.ContextMode.IN_HANDS;
                break;
            case "WALKING":
                contextMode = LogEntry.ContextMode.WALKING;
                break;
            case "ON_SURFACE":
                contextMode = LogEntry.ContextMode.ON_SURFACE;
                break;
        }

        switch (difficultyModeString) {
            case "EASY":
                boxSize = (int) (ballSize * 2.5f);
                difficultyMode = LogEntry.DifficultyMode.EASY;
                break;
            case "MEDIUM":
                boxSize = (int) (ballSize * 2.f);
                difficultyMode = LogEntry.DifficultyMode.MEDIUM;
                break;
            case "HARD":
                boxSize = (int) (ballSize * 1.5f);
                difficultyMode = LogEntry.DifficultyMode.HARD;
                break;
        }

        FrameLayout.LayoutParams boxLayoutParams = (FrameLayout.LayoutParams) boxImage.getLayoutParams();
        boxLayoutParams.width = boxSize;
        boxLayoutParams.height = boxSize;
        boxImage.setLayoutParams(boxLayoutParams);

        boxCounter = 0;
        modeLabel.setText(contextMode.toString());
        difficultyLabel.setText(difficultyMode.toString());
        boxCounterLabel.setText(boxCounter + "/" + BOXES_PER_MODE);

        spawnRandomBox();
    }

    private void spawnRandomBox() {
        boxPositionX = randomNumberGenerator.nextInt(layoutWidth - boxSize) - layoutWidth / 2 + boxSize / 2;
        boxPositionY = randomNumberGenerator.nextInt(layoutHeight - boxSize) - layoutHeight / 2 + boxSize / 2;
        setBoxPosition(boxPositionX, boxPositionY);
    }

    private int calculateColorIntensity() {
        return (int)((timeInBox / requiredTimeInBox) * 255);
    }

    private void setBallPosition(int x, int y) {
        FrameLayout.LayoutParams ballLayoutParams = (FrameLayout.LayoutParams) ballImage.getLayoutParams();
        ballLayoutParams.leftMargin = zeroX - ballSize / 2 + x;
        ballLayoutParams.topMargin = zeroY - ballSize / 2 - y;
        ballImage.setLayoutParams(ballLayoutParams);
    }

    private void setBoxPosition(int x, int y) {
        FrameLayout.LayoutParams boxLayoutParams = (FrameLayout.LayoutParams) boxImage.getLayoutParams();
        boxLayoutParams.leftMargin = zeroX - boxSize / 2 + x;
        boxLayoutParams.topMargin = zeroY - boxSize / 2 - y;
        boxImage.setLayoutParams(boxLayoutParams);
    }

    private void transformGyroValues(float[] measurements, float maxRangeX, float maxRangeY) {
        final float maxSensorRange = sensor.getMaximumRange() / 8f;

        float xRelative = Math.max(-maxSensorRange, Math.min(measurements[0], maxSensorRange)) / maxSensorRange;
        float yRelative = Math.max(-maxSensorRange, Math.min(measurements[1], maxSensorRange)) / maxSensorRange;

        measurements[0] = -xRelative * maxRangeX; // - is necessary because of inverted x axis on gyroscope
        measurements[1] = -yRelative * maxRangeY;
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