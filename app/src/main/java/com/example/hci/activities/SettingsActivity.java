package com.example.hci.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.hci.R;

public class SettingsActivity extends AppCompatActivity {
    Button back;
    EditText username;
    CheckBox training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //TODO: postavi vrijednosti na pocetku iz sharedPreferences
        username =findViewById(R.id.username);
        back = findViewById(R.id.back_button);
        training = findViewById(R.id.training);

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
                editor.apply();
                //Log.i("USERNAME: ", sharedPref.getString("username", ""));
                //Log.i("TRAINING: ", sharedPref.getString("training", ""));

                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}