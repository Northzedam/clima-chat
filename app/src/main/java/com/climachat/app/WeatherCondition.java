package com.climachat.app;

final class WeatherCondition {
    final String label;
    final String icon;

    private WeatherCondition(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    static WeatherCondition fromCode(int code) {
        if (code == 0) return new WeatherCondition("Despejado", "☀");
        if (code == 1) return new WeatherCondition("Mayormente despejado", "🌤");
        if (code == 2) return new WeatherCondition("Parcialmente nublado", "⛅");
        if (code == 3) return new WeatherCondition("Nublado", "☁");
        if (contains(code, 45, 48)) return new WeatherCondition("Niebla", "≋");
        if (contains(code, 51, 53, 55, 56, 57)) return new WeatherCondition("Llovizna", "🌦");
        if (contains(code, 61, 63, 65, 66, 67, 80, 81, 82)) return new WeatherCondition("Lluvia", "🌧");
        if (contains(code, 71, 73, 75, 77, 85, 86)) return new WeatherCondition("Nieve", "❄");
        if (contains(code, 95, 96, 99)) return new WeatherCondition("Tormenta", "⛈");
        return new WeatherCondition("Condición variable", "☁");
    }

    private static boolean contains(int value, int... values) {
        for (int candidate : values) {
            if (value == candidate) return true;
        }
        return false;
    }
}
