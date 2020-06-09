package org.mozilla.focus.utils;
/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

public final class DebugUtils {

    private static final String UNKNOWN_WEBVIEW_VERSION = "";

    private DebugUtils() {

    }

    static String loadWebViewVersion(Context context) {
        String webViewVersion;
        try {
            webViewVersion = loadWebViewVersion(new WebView(context));
        } catch (Exception exception) {
            webViewVersion = UNKNOWN_WEBVIEW_VERSION;
        }
        return webViewVersion;
    }

    private static String loadWebViewVersion(WebView webView) {
        String webViewVersion;
        try {
            final String userAgent = webView.getSettings().getUserAgentString();
            webViewVersion = parseWebViewVersion(userAgent);
        } catch (Throwable error) {
            webViewVersion = UNKNOWN_WEBVIEW_VERSION;
        }
        return webViewVersion;
    }

    public static String parseWebViewVersion(String userAgent) {
        if (TextUtils.isEmpty(userAgent)) {
            return UNKNOWN_WEBVIEW_VERSION;
        }
        final String separator = "Chrome/";
        final int from = userAgent.lastIndexOf(separator) + separator.length();
        return userAgent.substring(from, userAgent.indexOf(' ', from));
    }

}
