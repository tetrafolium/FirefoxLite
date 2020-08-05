/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import org.mozilla.focus.home.HomeFragment;
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
import org.mozilla.strictmodeviolator.StrictModeViolation;

import javax.annotation.Nullable;

public class Inject {

private static boolean sIsNewCreated = true;

public static String getDefaultTopSites(Context context) {

	return PreferenceManager.getDefaultSharedPreferences(context)
	       .getString(HomeFragment.TOPSITES_PREF, null);

}

public static TabsDatabase getTabsDatabase(Context context) {
	return TabsDatabase.getInstance(context);
}


public static boolean isTelemetryEnabled(Context context) {
	// The first access to shared preferences will require a disk read.
	return StrictModeViolation.tempGrant(StrictMode.ThreadPolicy.Builder::permitDiskReads, ()->{
			final Resources resources = context.getResources();
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			final boolean isEnabledByDefault = AppConstants.isBuiltWithFirebase();
			// Telemetry is not enable by default in debug build. But the user / developer can choose to turn it on
			// in AndroidTest, this is enabled by default
			return preferences.getBoolean(resources.getString(R.string.pref_key_telemetry), isEnabledByDefault);
		});
}

public static void enableStrictMode() {
	if (AppConstants.isReleaseBuild()) {
		return;
	}

	final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll();
	final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll();

	threadPolicyBuilder.penaltyLog().penaltyDialog();
	// Previously we have penaltyDeath() for debug build, but in order to add crashlytics, we can't use it here.
	// ( crashlytics has untagged Network violation so it always crashes
	vmPolicyBuilder.penaltyLog();

	StrictMode.setThreadPolicy(threadPolicyBuilder.build());
	StrictMode.setVmPolicy(vmPolicyBuilder.build());
}

public static final boolean getActivityNewlyCreatedFlag() {
	return sIsNewCreated;
}

public static void setActivityNewlyCreatedFlag() {
	sIsNewCreated = false;
}

public static boolean isUnderEspressoTest() {
	return false;
}

public static RemoteConfigConstants.SURVEY getDefaultFeatureSurvey() {
	return RemoteConfigConstants.SURVEY.NONE;
}

public static DownloadInfoRepository provideDownloadInfoRepository() {
	//TODO inject data source, ex production DB or mock DB here
	return DownloadInfoRepository.getInstance();
}

public static DownloadIndicatorViewModel obtainDownloadIndicatorViewModel(FragmentActivity activity) {
	DownloadViewModelFactory factory = DownloadViewModelFactory.getInstance();
	return ViewModelProviders.of(activity, factory).get(DownloadIndicatorViewModel.class);
}

public static DownloadInfoViewModel obtainDownloadInfoViewModel(FragmentActivity activity) {
	DownloadViewModelFactory factory = DownloadViewModelFactory.getInstance();
	return ViewModelProviders.of(activity, factory).get(DownloadInfoViewModel.class);
}

private static QuickSearchRepository provideQuickSearchRepository(Application application) {
	return QuickSearchRepository.getInstance(GlobalDataSource.getInstance(application), LocaleDataSource.getInstance(application));
}

public static QuickSearchViewModel obtainQuickSearchViewModel(FragmentActivity activity) {
	QuickSearchViewModelFactory factory = new QuickSearchViewModelFactory(provideQuickSearchRepository(activity.getApplication()));
	return ViewModelProviders.of(activity, factory).get(QuickSearchViewModel.class);
}

public static BottomBarViewModel obtainBottomBarViewModel(FragmentActivity activity) {
	BottomBarViewModelFactory factory = BottomBarViewModelFactory.getInstance();
	return ViewModelProviders.of(activity, factory).get(BottomBarViewModel.class);
}

public static MenuViewModel obtainMenuViewModel(FragmentActivity activity) {
	MenuViewModelFactory factory = MenuViewModelFactory.getInstance();
	return ViewModelProviders.of(activity, factory).get(MenuViewModel.class);
}

public static PrivateBottomBarViewModel obtainPrivateBottomBarViewModel(FragmentActivity activity) {
	PrivateBottomBarViewModelFactory factory = PrivateBottomBarViewModelFactory.getInstance();
	return ViewModelProviders.of(activity, factory).get(PrivateBottomBarViewModel.class);
}

public static ChromeViewModel obtainChromeViewModel(FragmentActivity activity) {
	Settings settings = Settings.getInstance(activity);
	BookmarkRepository bookmarkRepo = BookmarkRepository.getInstance(BookmarksDatabase.getInstance(activity));
	PrivateMode privateMode = PrivateMode.getInstance(activity);
	Browsers Browsers = new Browsers(activity, "http://mozilla.org");
	StorageHelper storageHelper = new StorageHelper(activity);
	ChromeViewModelFactory factory = ChromeViewModelFactory.getInstance(settings, bookmarkRepo, privateMode, Browsers, storageHelper);
	return ViewModelProviders.of(activity, factory).get(ChromeViewModel.class);
}

public static void startAnimation(@Nullable View view, Animation animation) {
	if (view == null) {
		return;
	}
	view.startAnimation(animation);
}
}
