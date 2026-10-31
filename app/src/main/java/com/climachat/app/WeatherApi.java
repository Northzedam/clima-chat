package com.climachat.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WeatherApi {
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private WeatherApi() {}

    static WeatherData fetchWeather(LocationData location) throws Exception {
        String endpoint = String.format(
                Locale.US,
                "%s?latitude=%.6f&longitude=%.6f&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=5",
                FORECAST_URL,
                location.latitude,
                location.longitude
        );

        JSONObject root = requestJson(endpoint);
        JSONObject current = root.getJSONObject("current");
        JSONObject dailyJson = root.getJSONObject("daily");
        JSONArray dates = dailyJson.getJSONArray("time");
        JSONArray codes = dailyJson.getJSONArray("weather_code");
        JSONArray maximums = dailyJson.getJSONArray("temperature_2m_max");
        JSONArray minimums = dailyJson.getJSONArray("temperature_2m_min");

        List<WeatherData.DailyForecast> daily = new ArrayList<>();
        int count = Math.min(5, dates.length());
        for (int index = 0; index < count; index++) {
            daily.add(new WeatherData.DailyForecast(
                    dates.getString(index),
                    codes.getInt(index),
                    maximums.getDouble(index),
                    minimums.getDouble(index)
            ));
        }

        if (daily.isEmpty()) throw new IOException("El servicio no devolvió un pronóstico diario.");

        return new WeatherData(
                current.getDouble("temperature_2m"),
                current.getDouble("apparent_temperature"),
                current.getInt("relative_humidity_2m"),
                current.getDouble("wind_speed_10m"),
                current.getInt("weather_code"),
                daily
        );
    }

    static List<LocationData> searchCities(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String endpoint = GEOCODING_URL + "?name=" + encoded + "&count=8&language=es&format=json";
        JSONObject root = requestJson(endpoint);
        JSONArray results = root.optJSONArray("results");
        List<LocationData> locations = new ArrayList<>();
        if (results == null) return locations;

        for (int index = 0; index < results.length(); index++) {
            JSONObject item = results.getJSONObject(index);
            locations.add(new LocationData(
                    item.getString("name"),
                    item.optString("admin1", ""),
                    item.optString("country", ""),
                    item.getDouble("latitude"),
                    item.getDouble("longitude")
            ));
        }
        return locations;
    }

    private static JSONObject requestJson(String endpoint) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "ClimaChat-Android/1.0");

        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readFully(stream);
            if (status < 200 || status >= 300) {
                throw new IOException("Open-Meteo respondió con el estado " + status + ".");
            }
            return new JSONObject(body);
        } finally {
            connection.disconnect();
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) content.append(line);
        }
        return content.toString();
    }
}
