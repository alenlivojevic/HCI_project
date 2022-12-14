package com.example.hci;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView textX, textY, textZ, poruka, poruka2;
    SensorManager sensorManager;
    Sensor sensor;
    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        textX = findViewById(R.id.textx);
        textY = findViewById(R.id.texty);
        textZ = findViewById(R.id.textz);
        poruka = findViewById(R.id.poruka);
        poruka2 = findViewById(R.id.poruka2);
        start = findViewById(R.id.buttonStart);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }

    public SensorEventListener gyroListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            textX.setText("X : " + (int) x + " m/s");
            textY.setText("Y : " + (int) y + " m/s");
            textZ.setText("Z : " + (int) z + " m/s");

            if(x < -3){
                poruka.setText("Uredaj je nagnut u desnu stranu");
            } else if (x > 3){
                poruka.setText("Uredaj je nagnut u lijevu stranu");
            } else{
                poruka.setText("Uredaj je centriran");
            }

            if(y < -3){
                poruka2.setText("Uredaj je nagnut prema naprijed");

            } else if (y > 3){
                poruka2.setText("Uredaj je nagnut prema nazad");

            } else{
                poruka2.setText("Uredaj je centriran");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
}