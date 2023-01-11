package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.hci.R;

public class SettingsActivity extends AppCompatActivity {
    Button back;
    EditText username;
    CheckBox training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
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
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}