package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText cityEditText;
    private Button getForecastButton;
    private TextView currentTempTextView, maxTempTextView, minTempTextView, humidityTextView, descriptionTextView;
    private ImageView weatherIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        cityEditText = findViewById(R.id.cityNameEditText);
        getForecastButton = findViewById(R.id.getForecastButton);
        currentTempTextView = findViewById(R.id.currentTempTextView);
        maxTempTextView = findViewById(R.id.maxTempTextView);
        minTempTextView = findViewById(R.id.minTempTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        weatherIconImageView = findViewById(R.id.weatherIconImageView);

        // Set up the button click listener
        getForecastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEditText.getText().toString().trim();
                if (!city.isEmpty()) {
                    getWeatherData(city);
                }
            }
        });
    }

    private void getWeatherData(final String city) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Log the city to verify it is being captured correctly
                    Log.d("WeatherApp", "City entered: " + city);

                    // Your API key and URL (corrected base URL)
                    String apiKey = "14bee426eaedb538bc6559326e38b512"; // Replace with your actual API key
                    String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + apiKey;



                    // Create the URL object and open the connection
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    // Check if the response code is 200 (OK)
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e("WeatherApp", "Failed to get data. Response Code: " + responseCode);
                        return;
                    }

                    // Read the response from the API
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();

                    // Log the API response
                    String response = stringBuilder.toString();
                    Log.d("WeatherApp", "Response: " + response);

                    // Parse the JSON response
                    JSONObject jsonObject = new JSONObject(response);

                    // Check if the 'main' object exists in the response
                    if (!jsonObject.has("main")) {
                        Log.e("WeatherApp", "No 'main' object in the response.");
                        return;
                    }

                    JSONObject main = jsonObject.getJSONObject("main");
                    JSONArray weatherArray = jsonObject.getJSONArray("weather");

                    // Check if weatherArray is empty
                    if (weatherArray.length() == 0) {
                        Log.e("WeatherApp", "No weather data in the response.");
                        return;
                    }

                    JSONObject weatherObject = weatherArray.getJSONObject(0);

                    final String description = weatherObject.getString("description");
                    final String icon = weatherObject.getString("icon");
                    final double temp = main.getDouble("temp");
                    final double tempMax = main.getDouble("temp_max");
                    final double tempMin = main.getDouble("temp_min");
                    final int humidity = main.getInt("humidity");

                    // Update the UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentTempTextView.setText("The current temperature is " + temp + "°C");
                            maxTempTextView.setText("The max temperature is " + tempMax + "°C");
                            minTempTextView.setText("The min temperature is " + tempMin + "°C");
                            humidityTextView.setText("The humidity is " + humidity + "%");
                            descriptionTextView.setText(description);

                            // Load the weather icon
                            String iconUrl = "http://openweathermap.org/img/wn/" + icon + "@2x.png";
                            Picasso.get().load(iconUrl).into(weatherIconImageView);
                        }
                    });

                } catch (Exception e) {
                    Log.e("WeatherApp", "Error fetching weather data", e); // Log any errors
                }
            }
        }).start();
    }
}