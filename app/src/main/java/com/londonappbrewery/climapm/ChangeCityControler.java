package com.londonappbrewery.climapm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityControler extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tells the activity which layout to use
        setContentView(R.layout.change_city_layout);
        final EditText editTextField = (EditText) findViewById(R.id.queryET);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);

        // Tell the activity to finish once the back button is pressed
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Method gets called once the user is presses enter on they keyboard
        editTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // Get the value of the text box
                String newCity = editTextField.getText().toString();

                // Create a new intent pointing towards the weather controller class
                Intent newCityIntent = new Intent(ChangeCityControler.this, WeatherController.class);

                // Add the city and start the activity
                newCityIntent.putExtra("City", newCity);
                startActivity(newCityIntent);
                return false;
            }
        });
    }
}
