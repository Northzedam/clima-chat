package com.climachat.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final LocationData MENDOZA = new LocationData(
            "Mendoza", "Mendoza", "Argentina", -32.8895, -68.8458
    );

    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private LocationData currentLocation = MENDOZA;
    private boolean hasWeather = false;

    private View loadingPanel;
    private LinearLayout weatherContent;
    private LinearLayout forecastContainer;
    private TextView errorBanner;
    private TextView locationName;
    private TextView locationSubtitle;
    private TextView weatherIcon;
    private TextView currentTemperature;
    private TextView weatherCondition;
    private TextView highLow;
    private TextView humidityValue;
    private TextView apparentValue;
    private TextView windValue;
    private EditText searchInput;
    private Button searchButton;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        configureActions();
        loadWeather(currentLocation);
    }

    private void bindViews() {
        loadingPanel = findViewById(R.id.loadingPanel);
        weatherContent = findViewById(R.id.weatherContent);
        forecastContainer = findViewById(R.id.forecastContainer);
        errorBanner = findViewById(R.id.errorBanner);
        locationName = findViewById(R.id.locationName);
        locationSubtitle = findViewById(R.id.locationSubtitle);
        weatherIcon = findViewById(R.id.weatherIcon);
        currentTemperature = findViewById(R.id.currentTemperature);
        weatherCondition = findViewById(R.id.weatherCondition);
        highLow = findViewById(R.id.highLow);
        humidityValue = findViewById(R.id.humidityValue);
        apparentValue = findViewById(R.id.apparentValue);
        windValue = findViewById(R.id.windValue);
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        refreshButton = findViewById(R.id.refreshButton);
    }

    private void configureActions() {
        refreshButton.setOnClickListener(view -> loadWeather(currentLocation));
        searchButton.setOnClickListener(view -> search());
        searchInput.setSingleLine(true);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            }
            return false;
        });
    }

    private void loadWeather(LocationData location) {
        setWeatherLoading(true);
        hideError();
        networkExecutor.execute(() -> {
            try {
                WeatherData weather = WeatherApi.fetchWeather(location);
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    currentLocation = location;
                    renderWeather(location, weather);
                    hasWeather = true;
                    setWeatherLoading(false);
                });
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    setWeatherLoading(false);
                    showError("No pudimos conectar con el servicio del clima. Revisá tu conexión e intentá nuevamente.");
                });
            }
        });
    }

    private void search() {
        String query = searchInput.getText().toString().trim();
        if (query.length() < 2) {
            searchInput.setError("Escribí al menos dos caracteres.");
            return;
        }

        hideKeyboard();
        searchButton.setEnabled(false);
        searchButton.setText("Buscando…");
        hideError();

        networkExecutor.execute(() -> {
            try {
                List<LocationData> locations = WeatherApi.searchCities(query);
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    finishSearch();
                    if (locations.isEmpty()) {
                        showError("No encontramos ciudades con ese nombre.");
                    } else {
                        showLocationPicker(locations);
                    }
                });
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    finishSearch();
                    showError("No se pudo realizar la búsqueda. Intentá nuevamente.");
                });
            }
        });
    }

    private void finishSearch() {
        searchButton.setEnabled(true);
        searchButton.setText(R.string.search);
    }

    private void showLocationPicker(List<LocationData> locations) {
        List<String> labels = new ArrayList<>();
        for (LocationData location : locations) labels.add(location.resultLabel());
        new AlertDialog.Builder(this)
                .setTitle("Elegí una ciudad")
                .setItems(labels.toArray(new String[0]), (dialog, index) -> {
                    searchInput.setText("");
                    loadWeather(locations.get(index));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void renderWeather(LocationData location, WeatherData weather) {
        WeatherCondition condition = WeatherCondition.fromCode(weather.weatherCode);
        WeatherData.DailyForecast today = weather.daily.get(0);
        locationName.setText(location.name);
        locationSubtitle.setText(location.subtitle());
        weatherIcon.setText(condition.icon);
        currentTemperature.setText(String.format(Locale.getDefault(), "%d°", Math.round(weather.temperature)));
        weatherCondition.setText(condition.label);
        highLow.setText(String.format(
                Locale.getDefault(),
                "Máx. %d° · Mín. %d°",
                Math.round(today.maximum),
                Math.round(today.minimum)
        ));
        humidityValue.setText(String.format(Locale.getDefault(), "%d%%", weather.humidity));
        apparentValue.setText(String.format(Locale.getDefault(), "%d°", Math.round(weather.apparentTemperature)));
        windValue.setText(String.format(Locale.getDefault(), "%d km/h", Math.round(weather.windSpeed)));
        renderForecast(weather.daily);
        weatherContent.setVisibility(View.VISIBLE);
    }

    private void renderForecast(List<WeatherData.DailyForecast> forecast) {
        forecastContainer.removeAllViews();
        for (int index = 0; index < forecast.size(); index++) {
            WeatherData.DailyForecast day = forecast.get(index);
            WeatherCondition condition = WeatherCondition.fromCode(day.weatherCode);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(13), dp(7), dp(13), dp(7));
            row.setMinimumHeight(dp(68));

            LinearLayout labels = new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(labels, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            TextView dayName = textView(formatDay(day.date, index), 14, Color.WHITE, Typeface.BOLD);
            labels.addView(dayName);
            TextView description = textView(condition.label, 11, getColor(R.color.muted), Typeface.NORMAL);
            description.setSingleLine(true);
            labels.addView(description);

            TextView icon = textView(condition.icon, 29, getColor(R.color.sun), Typeface.NORMAL);
            icon.setGravity(Gravity.CENTER);
            row.addView(icon, new LinearLayout.LayoutParams(dp(58), LinearLayout.LayoutParams.MATCH_PARENT));

            TextView temperatures = textView(
                    String.format(Locale.getDefault(), "%d°   %d°", Math.round(day.maximum), Math.round(day.minimum)),
                    14,
                    Color.WHITE,
                    Typeface.BOLD
            );
            temperatures.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            row.addView(temperatures, new LinearLayout.LayoutParams(dp(88), LinearLayout.LayoutParams.MATCH_PARENT));

            forecastContainer.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(68)
            ));

            if (index < forecast.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(getColor(R.color.line));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );
                dividerParams.setMargins(dp(13), 0, dp(13), 0);
                forecastContainer.addView(divider, dividerParams);
            }
        }
    }

    private TextView textView(String text, int sizeSp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans", style));
        return view;
    }

    private String formatDay(String value, int index) {
        if (index == 0) return "Hoy";
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = parser.parse(value);
            SimpleDateFormat formatter = new SimpleDateFormat("EEE", new Locale("es", "AR"));
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formatted = formatter.format(date).replace(".", "");
            return formatted.substring(0, 1).toUpperCase(new Locale("es", "AR")) + formatted.substring(1);
        } catch (Exception ignored) {
            return value;
        }
    }

    private void setWeatherLoading(boolean loading) {
        refreshButton.setEnabled(!loading);
        loadingPanel.setVisibility(loading && !hasWeather ? View.VISIBLE : View.GONE);
        if (hasWeather) weatherContent.setAlpha(loading ? 0.55f : 1f);
    }

    private void showError(String message) {
        errorBanner.setText(message);
        errorBanner.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void hideError() {
        errorBanner.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focused = getCurrentFocus();
        if (manager != null && focused != null) manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        searchInput.clearFocus();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        networkExecutor.shutdownNow();
        super.onDestroy();
    }
}
