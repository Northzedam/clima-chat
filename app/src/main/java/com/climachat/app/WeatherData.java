package com.climachat.app;

import java.util.List;

final class WeatherData {
    final double temperature;
    final double apparentTemperature;
    final int humidity;
    final double windSpeed;
    final int weatherCode;
    final List<DailyForecast> daily;

    WeatherData(
            double temperature,
            double apparentTemperature,
            int humidity,
            double windSpeed,
            int weatherCode,
            List<DailyForecast> daily
    ) {
        this.temperature = temperature;
        this.apparentTemperature = apparentTemperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
        this.daily = daily;
    }

    static final class DailyForecast {
        final String date;
        final int weatherCode;
        final double maximum;
        final double minimum;

        DailyForecast(String date, int weatherCode, double maximum, double minimum) {
            this.date = date;
            this.weatherCode = weatherCode;
            this.maximum = maximum;
            this.minimum = minimum;
        }
    }
}
