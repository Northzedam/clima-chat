package com.climachat.app;

final class LocationData {
    final String name;
    final String region;
    final String country;
    final double latitude;
    final double longitude;

    LocationData(String name, String region, String country, double latitude, double longitude) {
        this.name = name;
        this.region = region == null ? "" : region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    String subtitle() {
        if (!region.isEmpty() && !region.equalsIgnoreCase(name)) {
            return region + ", " + country;
        }
        return country;
    }

    String resultLabel() {
        return name + "\n" + subtitle();
    }
}
