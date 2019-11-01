package com.github.warren_bank.mock_location.data_model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

public final class BookmarkItem {
    public String title;
    public double lat;
    public double lon;

    public BookmarkItem(String title, double lat, double lon) {
        this.title = title;
        this.lat   = lat;
        this.lon   = lon;
    }

    @Override
    public String toString() {
        return title;
    }

    public LocPoint toPoint() {
        return new LocPoint(lat, lon);
    }

    // =======
    // helpers
    // =======

    public static ArrayList<BookmarkItem> fromJson(String json) {
        ArrayList<BookmarkItem> arrayList;
        Gson gson = new Gson();
        arrayList = gson.fromJson(json, new TypeToken<ArrayList<BookmarkItem>>(){}.getType());
        return arrayList;
    }

    public static String toJson(ArrayList<BookmarkItem> arrayList) {
        String json = new Gson().toJson(arrayList);
        return json;
    }

    public static boolean contains(ArrayList<BookmarkItem> arrayList, double lat, double lon) {
        int index = indexOf(arrayList, lat, lon);
        return (index >= 0);
    }

    public static boolean contains(ArrayList<BookmarkItem> arrayList, double lat, double lon, double threshold) {
        int index = indexOf(arrayList, lat, lon, threshold);
        return (index >= 0);
    }

    public static int indexOf(ArrayList<BookmarkItem> arrayList, double lat, double lon) {
        double threshold = 1e-4;

        return indexOf(arrayList, lat, lon, threshold);
    }

    public static int indexOf(ArrayList<BookmarkItem> arrayList, double lat, double lon, double threshold) {
        BookmarkItem item;
        boolean is_match;

        for (int i=0; i < arrayList.size(); i++) {
            try {
                item     = arrayList.get(i);
                is_match = (Math.abs(item.lat - lat) < threshold) && (Math.abs(item.lon - lon) < threshold); // todo: fix that longitude difference doesn't work if straddling opposite sides of the 180th meridian (ex: 179.99999 and -179.99999)

                if (is_match)
                    return i;
            }
            catch(Exception e) { continue; }
        }
        return -1;
    }
}
