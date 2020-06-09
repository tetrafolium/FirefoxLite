/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.rocket.content.news.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.mozilla.focus.utils.AppConfigWrapper;
import org.mozilla.focus.utils.Settings;
import org.mozilla.threadutils.ThreadUtils;


public final class NewsSourceManager {
    public static final String TAG = "NewsSource";

    // "DainikBhaskar.com" doesn't provide their feed anymore.
    private final static String NEWS_DB = "DainikBhaskar.com";
    private final static String NEWS_NP = "Newspoint";

    public final static String PREF_INT_NEWS_PRIORITY = "pref_int_news_priority";

    private static NewsSourceManager instance = new NewsSourceManager();

    private String newsSource = null;

    private String newsSourceUrl = "";

    public static NewsSourceManager getInstance() {
        return instance;
    }

    private NewsSourceManager() {
    }

    public void init(Context context) {

        ThreadUtils.postToBackgroundThread(() -> {
            final Settings settings = Settings.getInstance(context);
            final String source = settings.getNewsSource();
            // "DainikBhaskar.com" doesn't provide their feed anymore. Switch to "Newspoint" by default
            if (TextUtils.isEmpty(source) || NEWS_DB.equals(source)) {
                newsSource = NEWS_NP;
                Log.d(NewsSourceManager.TAG, "NewsSourceManager sets default:" + newsSource);

                settings.setNewsSource(newsSource);
                settings.setPriority(PREF_INT_NEWS_PRIORITY, Settings.PRIORITY_SYSTEM);
            } else {
                newsSource = settings.getNewsSource();
                Log.d(NewsSourceManager.TAG, "NewsSourceManager already set:" + newsSource);
            }
            // previously we only set the source url after remote config is fetched. But when there's no internet connection,
            // the remote config's callback will never be called, thus newsSourceUrl will always be null.
            // We try to set the url here if previously we already got the value from remote config.
            final String url = AppConfigWrapper.getNewsProviderUrl(settings.getNewsSource());
            if (!TextUtils.isEmpty(url)) {
                this.newsSourceUrl = url;
            }
        });
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public String getNewsSourceUrl() {
        return newsSourceUrl;
    }

    public void setNewsSourceUrl(String newsSourceUrl) {
        this.newsSourceUrl = newsSourceUrl;
    }
}
