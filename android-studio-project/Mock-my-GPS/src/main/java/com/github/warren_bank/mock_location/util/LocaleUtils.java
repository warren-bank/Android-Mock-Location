package com.github.warren_bank.mock_location.util;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;

import android.annotation.TargetApi;
import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Locale;

/*
 * App language chosen by the user, instead of always following the system language.
 *
 * Android 13+ : the system's per-app language (LocaleManager) is the source of truth,
 *               so the in-app picker and "Settings > Apps > Mock my GPS > Language" always agree.
 * Android <13 : the choice is stored in SharedPrefs and applied by hand to the Resources
 *               of every Activity and Service, by calling apply() in onCreate().
 *
 * A language tag of "" means: follow the system language.
 */
public final class LocaleUtils {
    private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

    // --------------------------------------------------------------------------------------------- supported languages

    // language tags listed in res/xml/locales_config.xml, in file order
    public static ArrayList<String> getSupportedLanguageTags(Context context) {
        ArrayList<String> tags = new ArrayList<String>();
        XmlResourceParser parser = null;

        try {
            parser = context.getResources().getXml(R.xml.locales_config);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if ((eventType == XmlPullParser.START_TAG) && "locale".equals(parser.getName())) {
                    String tag = parser.getAttributeValue(ANDROID_NAMESPACE, "name");

                    if (!TextUtils.isEmpty(tag))
                        tags.add(tag);
                }
                eventType = parser.next();
            }
        }
        catch(Exception e) {}
        finally {
            if (parser != null)
                parser.close();
        }

        return tags;
    }

    // name of the language in that language, e.g. "Deutsch", "Português (Brasil)"
    public static String getDisplayName(String tag) {
        Locale locale = toLocale(tag);
        String name   = locale.getDisplayName(locale);

        if (TextUtils.isEmpty(name))
            return tag;

        return name.substring(0, 1).toUpperCase(locale) + name.substring(1);
    }

    // Locale.forLanguageTag() needs API 21; the tags in locales_config.xml are only "ll" or "ll-RR"
    public static Locale toLocale(String tag) {
        String[] parts = tag.split("-");

        return (parts.length >= 2)
            ? new Locale(parts[0], parts[1])
            : new Locale(parts[0]);
    }

    // --------------------------------------------------------------------------------------------- selected language

    public static String getSelectedLanguageTag(Context context) {
        if (Build.VERSION.SDK_INT >= 33)
            return getApplicationLanguageTag(context);

        return SharedPrefs.getLanguageTag(context);
    }

    public static void setSelectedLanguageTag(Context context, String tag) {
        if (tag == null)
            tag = "";

        if (Build.VERSION.SDK_INT >= 33)
            setApplicationLanguageTag(context, tag);
        else
            SharedPrefs.putLanguageTag(context, tag);
    }

    // --------------------------------------------------------------------------------------------- apply

    // call in onCreate() of every Activity and Service, before any string is read
    public static void apply(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            migrateToLocaleManager(context);
            return;
        }

        String tag    = SharedPrefs.getLanguageTag(context);
        Locale locale = TextUtils.isEmpty(tag) ? getSystemLocale() : toLocale(tag);

        Resources resources = context.getResources();
        updateResources(resources, locale);

        Context appContext = context.getApplicationContext();
        if ((appContext != null) && (appContext.getResources() != resources))
            updateResources(appContext.getResources(), locale);
    }

    @SuppressWarnings("deprecation")
    private static Locale getSystemLocale() {
        return Resources.getSystem().getConfiguration().locale;
    }

    @SuppressWarnings("deprecation")
    private static void updateResources(Resources resources, Locale locale) {
        Configuration config = new Configuration(resources.getConfiguration());

        if (locale.equals(config.locale))
            return;

        if (Build.VERSION.SDK_INT >= 17)
            config.setLocale(locale);
        else
            config.locale = locale;

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // --------------------------------------------------------------------------------------------- Android 13+

    @TargetApi(33)
    private static String getApplicationLanguageTag(Context context) {
        LocaleList locales = context.getSystemService(LocaleManager.class).getApplicationLocales();

        return locales.isEmpty() ? "" : locales.get(0).toLanguageTag();
    }

    @TargetApi(33)
    private static void setApplicationLanguageTag(Context context, String tag) {
        LocaleList locales = TextUtils.isEmpty(tag)
            ? LocaleList.getEmptyLocaleList()
            : LocaleList.forLanguageTags(tag);

        context.getSystemService(LocaleManager.class).setApplicationLocales(locales);
    }

    // a language chosen on Android <13 is carried over once, after the device is upgraded to 13+
    @TargetApi(33)
    private static void migrateToLocaleManager(Context context) {
        String tag = SharedPrefs.getLanguageTag(context);

        if (TextUtils.isEmpty(tag))
            return;

        if (TextUtils.isEmpty(getApplicationLanguageTag(context)))
            setApplicationLanguageTag(context, tag);

        SharedPrefs.putLanguageTag(context, "");
    }
}
