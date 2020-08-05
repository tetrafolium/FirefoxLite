/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.focus.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.mozilla.focus.BuildConfig;
import org.mozilla.focus.R;
import org.mozilla.focus.activity.MainActivity;
import org.mozilla.focus.notification.NotificationActionBroadcastReceiver;
import org.mozilla.rocket.tabs.TabView;

public class IntentUtils {

  @VisibleForTesting
  public static final String MARKET_INTENT_URI_PACKAGE_PREFIX =
      "market://details?id=";
  
  public static final String EXTRA_IS_INTERNAL_REQUEST = "is_internal_request";
  public static final String EXTRA_OPEN_NEW_TAB = "open_new_tab";
  public static final String EXTRA_SHOW_RATE_DIALOG = "show_rate_dialog";

  public static final String EXTRA_NOTIFICATION_ACTION_RATE_STAR =
      "ex_no_action_rate_star";
  public static final String EXTRA_NOTIFICATION_ACTION_FEEDBACK =
      "ex_no_action_feedback";
  public static final String EXTRA_NOTIFICATION_CLICK_DEFAULT_BROWSER =
      "ex_no_click_default_browser";
  public static final String EXTRA_NOTIFICATION_CLICK_LOVE_FIREFOX =
      "ex_no_click_love_firefox";
  public static final String EXTRA_NOTIFICATION_CLICK_PRIVACY_POLICY_UPDATE =
      "ex_no_click_privacy_policy_update";

  public static final String ACTION_NOTIFICATION = "action_notification";

  /**
   * Find and open the appropriate app for a given Uri. If appropriate, let the
   * user select between multiple supported apps. Returns a boolean indicating
   * whether the URL was handled. A fallback URL will be opened in the supplied
   * WebView if appropriate (in which case the URL was handled, and true will
   * also be returned). If not handled, we should  fall back to webviews error
   * handling (which ends up calling our error handling code as needed). <p>
   * Note: this method "leaks" the target Uri to Android before asking the user
   * whether they want to use an external app to open the uri. Ultimately the OS
   * can spy on anything we're doing in the app, so this isn't an actual "bug".
   */
  public static boolean handleExternalUri(final Context context,
                                          final String uri) {
    // This code is largely based on Fennec's
    // ExternalIntentDuringPrivateBrowsingPromptFragment.java
    final Intent intent;
    try {
      intent = Intent.parseUri(uri, 0);
    } catch (URISyntaxException e) {
      // Let the browser handle the url (i.e. let the browser show it's
      // unsupported protocol / invalid URL page).
      return false;
    }

    // Since we're a browser:
    intent.addCategory(Intent.CATEGORY_BROWSABLE);

    final PackageManager packageManager = context.getPackageManager();

    // This is where we "leak" the uri to the OS. If we're using the system
    // webview, then the OS already knows that we're opening this uri. Even if
    // we're using GeckoView, the OS can still see what domains we're visiting,
    // so there's no loss of privacy here:
    final List<ResolveInfo> matchingActivities =
        packageManager.queryIntentActivities(intent, 0);

    if (matchingActivities.size() > 0) {
      context.startActivity(intent);
    }
    return true;
  }

  

  // We only need one param for both scenarios, hence we use just one "param"
  // argument. If we ever end up needing more or a variable number we can change
  // this, but java varargs are a bit messy so let's try to avoid that seeing as
  // it's not needed right now.
  

  public static void intentOpenFile(Context context, String fileUriStr,
                                    String mimeType) {
    if (fileUriStr != null) {
      String authorities =
          BuildConfig.APPLICATION_ID + ".provider.fileprovider";
      Uri fileUri = FileProvider.getUriForFile(
          context, authorities, new File(URI.create(fileUriStr).getPath()));

      Intent launchIntent = new Intent(Intent.ACTION_VIEW);
      launchIntent.setDataAndType(fileUri, mimeType);
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

      try {
        context.startActivity(launchIntent);
      } catch (Exception e) {
        openDownloadPage(context);
      }
    } else {
      openDownloadPage(context);
    }
  }

  private static void openDownloadPage(Context context) {
    Intent pageView = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
    pageView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(pageView);
  }

  @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
  public static void openUrl(Context context, String url,
                             boolean openInNewTab) {
    context.startActivity(
        createInternalOpenUrlIntent(context, url, openInNewTab));
  }

  public static Intent createInternalOpenUrlIntent(Context context, String url,
                                                   boolean openInNewTab) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url), context,
                               MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(IntentUtils.EXTRA_IS_INTERNAL_REQUEST, true);
    intent.putExtra(IntentUtils.EXTRA_OPEN_NEW_TAB, openInNewTab);
    return intent;
  }

  // FLAG_ACTIVITY_NEW_TASK is needed if the context is not an activity
  public static void goToPlayStore(Context context) {
    goToPlayStore(context, context.getPackageName());
  }

  public static void goToPlayStore(Context context, String appPackageName) {
    try {
      final Intent intent = new Intent(
          Intent.ACTION_VIEW,
          Uri.parse(MARKET_INTENT_URI_PACKAGE_PREFIX + appPackageName));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    } catch (android.content.ActivityNotFoundException ex) {
      // No google play install
      final Intent intent = new Intent(
          Intent.ACTION_VIEW,
          Uri.parse("https://play.google.com/store/apps/details?id=" +
                    appPackageName));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }
  }

  public static Intent
  genDefaultBrowserSettingIntentForBroadcastReceiver(Context context) {

    final Intent setAsDefault =
        new Intent(context, NotificationActionBroadcastReceiver.class);
    setAsDefault.setAction(IntentUtils.ACTION_NOTIFICATION);
    setAsDefault.putExtra(IntentUtils.EXTRA_NOTIFICATION_CLICK_DEFAULT_BROWSER,
                          true);
    return setAsDefault;
  }

  static Intent
  genRateStarNotificationActionForBroadcastReceiver(Context context) {
    final Intent rateStar =
        new Intent(context, NotificationActionBroadcastReceiver.class);
    rateStar.setAction(IntentUtils.ACTION_NOTIFICATION);
    rateStar.putExtra(IntentUtils.EXTRA_NOTIFICATION_ACTION_RATE_STAR, true);
    return rateStar;
  }

  static Intent
  genFeedbackNotificationActionForBroadcastReceiver(Context context) {
    final Intent feedback =
        new Intent(context, NotificationActionBroadcastReceiver.class);
    feedback.setAction(IntentUtils.ACTION_NOTIFICATION);
    feedback.putExtra(IntentUtils.EXTRA_NOTIFICATION_ACTION_FEEDBACK, true);
    return feedback;
  }

  static Intent
  genFeedbackNotificationClickForBroadcastReceiver(Context context) {
    final Intent openRocket =
        new Intent(context, NotificationActionBroadcastReceiver.class);
    openRocket.setAction(IntentUtils.ACTION_NOTIFICATION);
    openRocket.putExtra(IntentUtils.EXTRA_NOTIFICATION_CLICK_LOVE_FIREFOX,
                        true);
    return openRocket;
  }

  static Intent genPrivacyPolicyUpdateNotificationActionForBroadcastReceiver(
      Context context) {
    final Intent privacyPolicyUpdate =
        new Intent(context, NotificationActionBroadcastReceiver.class);
    privacyPolicyUpdate.setAction(IntentUtils.ACTION_NOTIFICATION);
    privacyPolicyUpdate.putExtra(
        IntentUtils.EXTRA_NOTIFICATION_CLICK_PRIVACY_POLICY_UPDATE, true);
    return privacyPolicyUpdate;
  }

  @CheckResult
  public static boolean openDefaultAppsSettings(Context context) {
    try {
      Intent intent = new Intent(
          android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
      context.startActivity(intent);
      return true;
    } catch (ActivityNotFoundException e) {
      // In some cases, a matching Activity may not exist (according to the
      // Android docs).
      return false;
    }
  }

  public static Intent getLauncherHomeIntent() {
    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
    homeIntent.addCategory(Intent.CATEGORY_HOME);
    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return homeIntent;
  }

  public static PendingIntent getLauncherHomePendingIntent(Context context) {
    return PendingIntent.getActivity(context, 0, getLauncherHomeIntent(),
                                     PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
