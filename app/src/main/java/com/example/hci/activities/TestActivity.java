package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hci.R;
import com.example.hci.utils.Database;
import com.example.hci.utils.LogEntry;

public class TestActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor sensor;
    private FrameLayout frameLayout;
    private ImageView ballImage;
    private SensorEventListener gyroListener;
    private ImageView boxImage;
    private int ballZeroX, ballZeroY;
    private int ballSize, boxSize;
    private int betweenBoxesSpaceX, betweenBoxesSpaceY;
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

    private TextView boxCounterLabel, modeLabel, difficultyLabel, cycleLabel;
    private static final int BOXES_PER_CYCLE = 15, NR_OF_CYCLES = 3;
    private Button startNewCycleButton;
    private int boxCounter = 0, cycleCounter = 0;
    private LogEntry.ContextMode contextMode;
    private LogEntry.DifficultyMode difficultyMode;

    private int[] cycleSequence;
    private int currentCycleIndex;

    private int[][] cycleBoxSequences;
    private float[] cycleTimes;

    private int boxIndexX = 0, boxIndexY = 0;

    private float taskTime = 0.f;

    boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        boxCounterLabel = findViewById(R.id.box_counter);
        modeLabel = findViewById(R.id.mode);
        difficultyLabel = findViewById(R.id.difficulty);
        cycleLabel = findViewById(R.id.cycle);
        startNewCycleButton = findViewById(R.id.startNewCycle);

        cycleBoxSequences = new int[][] {
            {8, 1, 3, 8, 6, 2, 7, 0, 7, 2, 0, 7, 6, 7, 1},
            {2, 4, 7, 6, 3, 6, 5, 8, 0, 2, 1, 8, 1, 8, 2},
            {0, 8, 2, 8, 3, 2, 3, 8, 0, 5, 6, 7, 3, 1, 6}
        };

        cycleTimes = new float[3];

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
                ballZeroX = (int) (layoutWidth / 2.f);
                ballZeroY = (int) (layoutHeight / 2.f);

                ballImage = new ImageView(TestActivity.this);
                ballImage.setImageResource(R.drawable.ball);
                ballImage.setLayoutParams(new FrameLayout.LayoutParams(ballSize, ballSize));

                setBallPosition(ballPositionX, ballPositionY);
                frameLayout.addView(ballImage);

                boxImage = new ImageView(TestActivity.this);
                boxImage.setImageResource(R.drawable.box);
                frameLayout.addView(boxImage);

                ballImage.bringToFront();

                SharedPreferences sharedPreferences = getApplication().getSharedPreferences("user_data", 0);
                String contextModeString = sharedPreferences.getString("modality", "SITTING");
                String difficultyModeString = sharedPreferences.getString("difficulty", "EASY");

                switch (contextModeString) {
                    case "WALKING":
                        contextMode = LogEntry.ContextMode.WALKING;
                        cycleSequence = new int[] {1, 2, 0};
                        break;
                    case "SITTING":
                        contextMode = LogEntry.ContextMode.SITTING;
                        cycleSequence = new int[] {2, 0, 1};
                        break;
                }

                switch (difficultyModeString) {
                    case "EASY":
                        boxSize = (int) (ballSize * 2.f);
                        difficultyMode = LogEntry.DifficultyMode.EASY;
                        break;
                    case "HARD":
                        boxSize = (int) (ballSize * 1.5f);
                        difficultyMode = LogEntry.DifficultyMode.HARD;
                        break;
                }

                betweenBoxesSpaceX = (layoutWidth - 3 * boxSize) / 4;
                betweenBoxesSpaceY = (layoutHeight - 3 * boxSize) / 4;

                FrameLayout.LayoutParams boxLayoutParams = (FrameLayout.LayoutParams) boxImage.getLayoutParams();
                boxLayoutParams.width = boxSize;
                boxLayoutParams.height = boxSize;
                boxImage.setLayoutParams(boxLayoutParams);


                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                frameLayout.setVisibility(View.GONE);
            }
        });

        startNewCycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewCycleButton.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_GAME);
                startNewTestingCycle();
            }
        });

        gyroListener = new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                if(ready)
                    onNewMeasurement(event.values[0], event.values[1]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private void onNewMeasurement(float x, float y) {
        float currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1e9f;
        taskTime += deltaTime;
        lastTime = currentTime;

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

        // Transforming ball coordinate system to global (box) coordinate system
        ballPositionX += layoutWidth / 2 - ballSize / 2;
        ballPositionY += layoutHeight / 2 - ballSize / 2;
        if (ballPositionX > boxPositionX &&
                ballPositionX + ballSize < boxPositionX + boxSize &&
                ballPositionY > boxPositionY &&
                ballPositionY + ballSize < boxPositionY + boxSize) {
            // Change box color
            timeInBox += deltaTime;
            int colorIntensity = calculateColorIntensity();
            boxImage.setBackgroundColor(Color.rgb(255 - colorIntensity, 255, 255 - colorIntensity));

            if (timeInBox >= requiredTimeInBox) {
                vibrator.vibrate(100);
                timeInBox = 0.f;
                ++boxCounter;
                boxImage.setBackgroundColor(Color.TRANSPARENT);
                boxCounterLabel.setText(boxCounter + "/" +  BOXES_PER_CYCLE);
                if (boxCounter == BOXES_PER_CYCLE) {
                    cycleTimes[cycleCounter] = taskTime;
                    ++cycleCounter;
                    frameLayout.setVisibility(View.GONE);
                    startNewCycleButton.setVisibility(View.VISIBLE);
                    ready = false;
                    if (cycleCounter == NR_OF_CYCLES) {
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("user_data", 0);
                        if(sharedPref.getString("training", "").equals("false")) logResults();
                        finish();
                    }
                } else {
                    spawnNextBox();
                }
            }
        } else if (timeInBox > 0.f) {
            timeInBox = 0.f;
            boxImage.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void logResults() {
        Database database = Database.getInstance(this);
        SharedPreferences sp = getApplicationContext().getSharedPreferences("user_data", 0);
        String username = sp.getString("username", "");
        taskTime = (cycleTimes[0] + cycleTimes[1] + cycleTimes[2]) / 3f;
        LogEntry logEntry = new LogEntry(username, contextMode, difficultyMode, taskTime);
        database.writeNewLog(logEntry);
    }

    private void startNewTestingCycle() {
        boxCounter = 0;
        modeLabel.setText(contextMode.toString());
        difficultyLabel.setText(difficultyMode.toString());
        boxCounterLabel.setText(boxCounter + "/" + BOXES_PER_CYCLE);
        cycleLabel.setText((cycleCounter + 1) + "/" + NR_OF_CYCLES);
        currentCycleIndex = cycleSequence[cycleCounter];

        taskTime = 0.f;
        lastTime = System.nanoTime();
        ready = true;
        for (int i = 0; i < lastMeasurements.length; ++i) {
            lastMeasurements[i][0] = 0.f;
            lastMeasurements[i][1] = 0.f;
        }
        measurementCount = 0;
        setBallPosition(0, 0);
        spawnNextBox();
    }

    private void spawnNextBox() {
        int newBoxIndex = cycleBoxSequences[currentCycleIndex][boxCounter];
        boxPositionX = ((newBoxIndex % 3) * boxSize) + (((newBoxIndex % 3) + 1) * betweenBoxesSpaceX);
        boxPositionY = ((newBoxIndex / 3) * boxSize) + (((newBoxIndex / 3) + 1) * betweenBoxesSpaceY);
        setBoxPosition(boxPositionX, boxPositionY);
    }

    private int calculateColorIntensity() {
        return (int)((timeInBox / requiredTimeInBox) * 255);
    }

    private void setBallPosition(int x, int y) {
        // Ball coordinate system with center in the middle of the screen
        FrameLayout.LayoutParams ballLayoutParams = (FrameLayout.LayoutParams) ballImage.getLayoutParams();
        ballLayoutParams.leftMargin = ballZeroX - ballSize / 2 + x;
        ballLayoutParams.topMargin = ballZeroY - ballSize / 2 + y;
        ballImage.setLayoutParams(ballLayoutParams);
    }

    private void setBoxPosition(int x, int y) {
        // Box coordinate system with 0, 0 in top left corner
        FrameLayout.LayoutParams boxLayoutParams = (FrameLayout.LayoutParams) boxImage.getLayoutParams();
        boxLayoutParams.leftMargin = x;
        boxLayoutParams.topMargin = y;
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
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }
}