package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    final int REQUEST_CODE = 123;
    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "e72____PLEASE_REPLACE_ME_____13";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityControler.class);
                startActivity(myIntent);
            }
        });
    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if (city != null) {
            Log.d("Clima", "Getting weather for " + city);
            getWeatherForNewCity(city);
        } else {
            Log.d("Clima", "Getting weather for current location");
            getWeatherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }


    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        // Grab the location manager from the system
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a new listener to attempt to get the location
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback recived");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("Clima", "Current long is " + longitude);
                Log.d("Clima", "Current lat is " + latitude);

                // Create a new Param holder for our request containing the gathered lat/lon
                RequestParams params = new RequestParams();

                // insert the lat lon and app id into the new object
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);

                // call the method which will send the request with the new params
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("Clima", "onStatusChanged() callback recived");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("Clima", "onProviderEnabled() callback recived");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Clima", "onProviderDisabled() callback recived");
            }
        };

        // Check to see if we have permission to access location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If we don't have permission then request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE
            );
            // ONCE REQUESTED AND USER RESPONDS ON RESUME WILL GET CALLED AGAIN
            // THIS MEANS WE CAN RETURN AND THIS METHOD WILL GET CALLED AGAIN THROUGH
            // ON RESUME
            return;
        }

        // If we reach here then we have permision so we can request the location
        // from the manager.
        mLocationManager.requestLocationUpdates(
                LOCATION_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                mLocationListener
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the request belongs to our request code
        if (requestCode == REQUEST_CODE) {
            // If the length of the array is greater than one and if the first result is "PERMISSION_GRANTED"
            // then we are ok to use the service otherwise we are not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "onRequestPermissionsResult(): Permision Granted");
                getWeatherForCurrentLocation();
            } else {
                Log.d("Clima", "onRequestPermissionsResult(): Permision Denyed");
            }
        }
    }
// TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params) {
        // Create a new http client to send our request
        AsyncHttpClient client = new AsyncHttpClient();

        // Attempt to get the data by sending a GET request to which we expect a
        // JSON response
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Clima", "Success! JSON: " + response.toString());
                // Create a DataModel based on the json data
                updateUI(WeatherDataModel.frmJson(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.e("Clima", "Failure! " + e.toString());
                Log.e("Clima", "Status code " + statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weatherDataModel) {
        // Set the text of the temp and city with the data from the model
        mTemperatureLabel.setText(weatherDataModel.getTemperature());
        mCityLabel.setText(weatherDataModel.getCity());

        // Since we only have the name of the image we want we have to look up the id by the name
        // this is done by passing the name and folder to the getIdentifier method along with
        // the current package.
        mWeatherImage.setImageResource(
            getResources().getIdentifier(
                weatherDataModel.getIconName(), "drawable", getPackageName()
            )
        );
    }



    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        // Free up resources when we pause as we no longer need them.
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
