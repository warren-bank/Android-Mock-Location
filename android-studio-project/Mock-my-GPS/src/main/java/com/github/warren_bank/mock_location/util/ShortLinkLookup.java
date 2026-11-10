package com.github.warren_bank.mock_location.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Link lookup for Google Maps short links, the app's only network use.
 *
 * Google Maps shares a place as a title plus a short link, never as coordinates. The short link's host answers
 * with one redirect whose "Location" holds a full Google Maps URL, which "GeoPointParserUtil" can read.
 *
 * Bounds (see README, "Privacy"):
 * - only "https://maps.app.goo.gl/<id>" and "https://goo.gl/maps/<id>" are ever contacted
 * - one HEAD request, with the query string and fragment of the shared link dropped
 * - redirects are not followed: the "Location" header is only parsed, never contacted
 * - "User-Agent: N/A", no cookies, no cache, nothing stored or logged
 * - 5 s connect timeout, 5 s read timeout
 */
public final class ShortLinkLookup {

    private static final Pattern shortLinkPattern = Pattern.compile(
        "https://(maps\\.app\\.goo\\.gl/|goo\\.gl/maps/)([A-Za-z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final int TIMEOUT_MS = 5000;

    /**
     * @return the first allowlisted short link in the text, normalized to "https://<host>/<id>", or null
     */
    public static String findShortLink(String text) {
        if (text == null) return null;

        Matcher matcher = shortLinkPattern.matcher(text);
        if (!matcher.find()) return null;

        return "https://" + matcher.group(1).toLowerCase(Locale.US) + matcher.group(2);
    }

    /**
     * Blocking: call from a background thread.
     *
     * @return the "Location" of the redirect, or null when there is none (ex: 404 for an unknown link)
     * @throws IOException when the host can't be reached (offline, timeout, TLS failure)
     */
    public static String lookUp(String shortLink) throws IOException {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(shortLink).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "N/A");

            int status = connection.getResponseCode();

            return ((status >= 300) && (status < 400))
                ? connection.getHeaderField("Location")
                : null;
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
    }
}
