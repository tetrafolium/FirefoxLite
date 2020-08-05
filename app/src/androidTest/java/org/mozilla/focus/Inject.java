/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import org.mozilla.focus.persistence.BookmarksDatabase;
import org.mozilla.focus.persistence.TabsDatabase;
import org.mozilla.focus.repository.BookmarkRepository;
import org.mozilla.focus.utils.AppConstants;
import org.mozilla.focus.utils.Browsers;
import org.mozilla.focus.utils.RemoteConfigConstants;
import org.mozilla.focus.utils.Settings;
import org.mozilla.rocket.chrome.BottomBarViewModel;
import org.mozilla.rocket.chrome.BottomBarViewModelFactory;
import org.mozilla.rocket.chrome.ChromeViewModel;
import org.mozilla.rocket.chrome.ChromeViewModelFactory;
import org.mozilla.rocket.chrome.MenuViewModel;
import org.mozilla.rocket.chrome.MenuViewModelFactory;
import org.mozilla.rocket.chrome.PrivateBottomBarViewModel;
import org.mozilla.rocket.chrome.PrivateBottomBarViewModelFactory;
import org.mozilla.rocket.content.news.NewsViewModel;
import org.mozilla.rocket.content.news.NewsViewModelFactory;
import org.mozilla.rocket.download.DownloadIndicatorViewModel;
import org.mozilla.rocket.download.DownloadInfoRepository;
import org.mozilla.rocket.download.DownloadInfoViewModel;
import org.mozilla.rocket.download.DownloadViewModelFactory;
import org.mozilla.rocket.helper.StorageHelper;
import org.mozilla.rocket.privately.PrivateMode;
import org.mozilla.rocket.urlinput.GlobalDataSource;
import org.mozilla.rocket.urlinput.LocaleDataSource;
import org.mozilla.rocket.urlinput.QuickSearchRepository;
import org.mozilla.rocket.urlinput.QuickSearchViewModel;
import org.mozilla.rocket.urlinput.QuickSearchViewModelFactory;

public class Inject {

  private static boolean sIsNewCreated = true;

  private static final String TOP_SITES =
      "[{\"id\":-1,\"url\":\"file:\\/\\/\\/android_asset\\/gpl.html\",\"title\":\"Sample Top Site\",\"favicon\":\"ic_youtube.png\",\"viewCount\":20,\"lastViewTimestamp\":1517196818119},{\"id\":-3,\"url\":\"http:\\/\\/m.tribunnews.com\\/\",\"title\":\"Tribunnews\",\"favicon\":\"ic_tribunnews.png\",\"viewCount\":18,\"lastViewTimestamp\":1517196818119},{\"id\":-5,\"url\":\"https:\\/\\/m.tokopedia.com\\/\",\"title\":\"Tokopedia\",\"favicon\":\"ic_tokopedia.png\",\"viewCount\":16,\"lastViewTimestamp\":1517196818119},{\"id\":-4,\"url\":\"https:\\/\\/m.facebook.com\\/\",\"title\":\"Facebook\",\"favicon\":\"ic_facebook.png\",\"viewCount\":14,\"lastViewTimestamp\":1517196818119},{\"id\":-8,\"url\":\"https:\\/\\/m.bukalapak.com\\/\",\"title\":\"Bukalapak\",\"favicon\":\"ic_bukalapak.png\",\"viewCount\":12,\"lastViewTimestamp\":1517196818119},{\"id\":-6,\"url\":\"http:\\/\\/m.liputan6.com\\/\",\"title\":\"Liputan6\",\"favicon\":\"ic_liputan6.png\",\"viewCount\":10,\"lastViewTimestamp\":1517196818119},{\"id\":-7,\"url\":\"http:\\/\\/www.kompas.com\\/\",\"title\":\"Kompas\",\"favicon\":\"ic_kompas.png\",\"viewCount\":8,\"lastViewTimestamp\":1517196818119},{\"id\":-9,\"url\":\"https:\\/\\/m.kapanlagi.com\\/\",\"title\":\"Kapanlagi\",\"favicon\":\"ic_kapanlagi.png\",\"viewCount\":6,\"lastViewTimestamp\":1517196818119}]";

  private static volatile TabsDatabase tabsDatabase;

  public static String getDefaultTopSites(Context context) { return TOP_SITES; }

  public static TabsDatabase getTabsDatabase(Context context) {
    if (tabsDatabase == null || !tabsDatabase.isOpen()) {
      synchronized (Inject.class) {
        if (tabsDatabase == null || !tabsDatabase.isOpen()) {
          // using an in-memory database because the information stored here
          // disappears when the process is killed
          tabsDatabase =
              Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                                           TabsDatabase.class)
                  .build();
        }
      }
    }
    return tabsDatabase;
  }

  // The pref is not persist so we need to inject the condition instead of
  // override defaultSharedPreference
  public static boolean isTelemetryEnabled(Context context) {
    // The first access to shared preferences will require a disk read.
    final StrictMode.ThreadPolicy threadPolicy =
        StrictMode.allowThreadDiskReads();
    try {
      final Resources resources = context.getResources();
      final SharedPreferences preferences =
          PreferenceManager.getDefaultSharedPreferences(context);
      return preferences.getBoolean(
          resources.getString(R.string.pref_key_telemetry), true);
    } finally {
      StrictMode.setThreadPolicy(threadPolicy);
    }
  }

  public static void enableStrictMode() {
    if (AppConstants.isReleaseBuild()) {
      return;
    }

    final StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
        new StrictMode.ThreadPolicy.Builder().detectAll();
    final StrictMode.VmPolicy.Builder vmPolicyBuilder =
        new StrictMode.VmPolicy.Builder().detectAll();

    // In AndroidTest we are super kind :)
    threadPolicyBuilder.penaltyLog();
    // Previously we have penaltyDeath() for debug build, but in order to add
    // crashlytics, we can't use it here. ( crashlytics has untagged Network
    // violation so it always crashes
    vmPolicyBuilder.penaltyLog();

    StrictMode.setThreadPolicy(threadPolicyBuilder.build());
    StrictMode.setVmPolicy(vmPolicyBuilder.build());
  }

  public static final boolean getActivityNewlyCreatedFlag() {
    return sIsNewCreated;
  }

  public static void setActivityNewlyCreatedFlag() {
    // Do nothing in testing
  }

  public static boolean isUnderEspressoTest() { return true; }

  public static RemoteConfigConstants.SURVEY getDefaultFeatureSurvey() {
    return RemoteConfigConstants.SURVEY.VPN_RECOMMENDER;
  }

  public static DownloadInfoRepository provideDownloadInfoRepository() {
    // TODO inject data source, ex production DB or mock DB here
    return DownloadInfoRepository.getInstance();
  }

  public static DownloadIndicatorViewModel
  obtainDownloadIndicatorViewModel(FragmentActivity activity) {
    DownloadViewModelFactory factory = DownloadViewModelFactory.getInstance();
    return ViewModelProviders.of(activity, factory)
        .get(DownloadIndicatorViewModel.class);
  }

  public static DownloadInfoViewModel
  obtainDownloadInfoViewModel(FragmentActivity activity) {
    DownloadViewModelFactory factory = DownloadViewModelFactory.getInstance();
    return ViewModelProviders.of(activity, factory)
        .get(DownloadInfoViewModel.class);
  }

  private static QuickSearchRepository
  provideQuickSearchRepository(Context context) {
    // TODO add mock data source
    return QuickSearchRepository.getInstance(
        GlobalDataSource.getInstance(context),
        LocaleDataSource.getInstance(context));
  }

  public static QuickSearchViewModel
  obtainQuickSearchViewModel(FragmentActivity activity) {
    QuickSearchViewModelFactory factory = new QuickSearchViewModelFactory(
        provideQuickSearchRepository(activity.getApplicationContext()));
    return ViewModelProviders.of(activity, factory)
        .get(QuickSearchViewModel.class);
  }

  public static BottomBarViewModel
  obtainBottomBarViewModel(FragmentActivity activity) {
    BottomBarViewModelFactory factory = BottomBarViewModelFactory.getInstance();
    return ViewModelProviders.of(activity, factory)
        .get(BottomBarViewModel.class);
  }

  public static MenuViewModel obtainMenuViewModel(FragmentActivity activity) {
    MenuViewModelFactory factory = MenuViewModelFactory.getInstance();
    return ViewModelProviders.of(activity, factory).get(MenuViewModel.class);
  }

  public static PrivateBottomBarViewModel
  obtainPrivateBottomBarViewModel(FragmentActivity activity) {
    PrivateBottomBarViewModelFactory factory =
        PrivateBottomBarViewModelFactory.getInstance();
    return ViewModelProviders.of(activity, factory)
        .get(PrivateBottomBarViewModel.class);
  }

  public static ChromeViewModel
  obtainChromeViewModel(FragmentActivity activity) {
    Settings settings = Settings.getInstance(activity);
    BookmarkRepository bookmarkRepo =
        BookmarkRepository.getInstance(BookmarksDatabase.getInstance(activity));
    PrivateMode privateMode = PrivateMode.getInstance(activity);
    Browsers Browsers = new Browsers(activity, "http://mozilla.org");
    StorageHelper storageHelper = new StorageHelper(activity);
    ChromeViewModelFactory factory = ChromeViewModelFactory.getInstance(
        settings, bookmarkRepo, privateMode, Browsers, storageHelper);
    return ViewModelProviders.of(activity, factory).get(ChromeViewModel.class);
  }

  public static void startAnimation(View view, Animation animation) {}
}
