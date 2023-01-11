package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.hci.R;

public class SettingsActivity extends AppCompatActivity {
    private Button back;
    private EditText username;
    private CheckBox training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("user_data", 0);

        username = findViewById(R.id.username);
        username.setText(sharedPref.getString("username", ""));
        back = findViewById(R.id.back_button);

        training = findViewById(R.id.training);
        if(sharedPref.getString("training", "").equals("true")) training.setChecked(true);
        else training.setChecked(false);

        Spinner modality = findViewById(R.id.modality);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.modality, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        modality.setAdapter(adapter);

        Spinner difficulty = findViewById(R.id.difficulty);
        ArrayAdapter<CharSequence> adapter2=ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_item);
        difficulty.setAdapter(adapter2);

        difficulty.setSelection(adapter2.getPosition(sharedPref.getString("difficulty", "")));
        modality.setSelection(adapter.getPosition(sharedPref.getString("modality", "")));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username.getText().toString());
                if(training.isChecked()){
                    editor.putString("training", "true");
                } else{
                    editor.putString("training", "false");
                }
                editor.putString("difficulty", difficulty.getSelectedItem().toString());
                editor.putString("modality", modality.getSelectedItem().toString());
                editor.apply();
                /*
                Log.i("USERNAME: ", sharedPref.getString("username", ""));
                Log.i("TRAINING: ", sharedPref.getString("training", ""));
                Log.i("DIFFICULTY: ", sharedPref.getString("difficulty", ""));
                Log.i("MODALITY: ", sharedPref.getString("modality", ""));
                */

                finish();
            }
        });

    }
}