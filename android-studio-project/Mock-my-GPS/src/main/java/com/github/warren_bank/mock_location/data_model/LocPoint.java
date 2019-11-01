package com.github.warren_bank.mock_location.data_model;

// copied from:
//   https://github.com/xiangtailiang/FakeGPS/blob/V1.1/app/src/main/java/com/github/fakegps/model/LocPoint.java

public class LocPoint {
    private double mLatitude;
    private double mLongitude;

    public LocPoint(LocPoint locPoint) {
        mLatitude = locPoint.getLatitude();
        mLongitude = locPoint.getLongitude();
    }

    public LocPoint(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public LocPoint(String text) throws NumberFormatException {
        String[] parts = text.split(",");
        if (parts.length == 2) {
            mLatitude  = Double.parseDouble(parts[0].trim());
            mLongitude = Double.parseDouble(parts[1].trim());
        }
        else {
            throw new NumberFormatException("expected: latitude,longitude");
        }
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Override
    public String toString() {
        return String.format(
            "%1$s, %2$s",
            mLatitude,
            mLongitude
        );
    }

    public boolean equals(LocPoint locPoint) {
        double threshold = 1e-4;
        return equals(locPoint, threshold);
    }

    public boolean equals(LocPoint locPoint, double threshold) {
        double that_lat = locPoint.getLatitude();
        double that_lon = locPoint.getLongitude();

        return (Math.abs(that_lat - mLatitude) < threshold) && (Math.abs(that_lon - mLongitude) < threshold); // todo: fix that longitude difference doesn't work if straddling opposite sides of the 180th meridian (ex: 179.99999 and -179.99999)
    }

}
