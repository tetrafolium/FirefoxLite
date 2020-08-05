/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlag;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Keep;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.focus.R;
import org.mozilla.focus.helper.BeforeTestTask;
import org.mozilla.focus.helper.SessionLoadedIdlingResource;
import org.mozilla.focus.utils.AndroidTestUtils;
import org.mozilla.focus.utils.IntentUtils;

@Keep
@RunWith(AndroidJUnit4.class)
public class RateAppPromotionTest {

  private SessionLoadedIdlingResource sessionLoadedIdlingResource;

  @Rule
  public final IntentsTestRule<MainActivity> intentTestRule =
      new IntentsTestRule<>(MainActivity.class, true, false);

  /**
   * Precondition:
   * 1. Launch Rocket 6th times
   * 2. show [love firefox] dialog
   */

  @Before
  public void setUp() {

    new BeforeTestTask.Builder()
        .setRateAppPromotionEnabled(true)
        .setSkipFirstRun(true)
        .build()
        .execute();

    AndroidTestUtils.setRateAppPromotionIsReadyToShow();
    intentTestRule.launchActivity(new Intent());
  }

  @After
  public void tearDown() {
    // In case test fail will result in sessionLoadedIdlingResource not
    // unregistered
    if (sessionLoadedIdlingResource != null) {
      IdlingRegistry.getInstance().unregister(sessionLoadedIdlingResource);
    }
  }

  /**
   * Test case no: TC_0061
   * Test case name: In-app promotion - Press RATE 5 START to open Google play
   * Steps:
   * 1. tap close button
   * 2. Press feedback button
   * 3. check feedback page url matches expectation
   */

  @Test
  public void showRateAppPromotionAndClickRate_intendedToOpenGooglePlay() {
    // By default Espresso Intents does not stub any Intents. Stubbing needs to
    // be setup before every test run. In this case all external Intents will be
    // blocked.
    intending(not(isInternal()))
        .respondWith(
            new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

    // Check share app dialog is displayed and click the rate app button
    onView(withId(R.id.positive_button))
        .check(matches(isDisplayed()))
        .perform(click());

    // Check if the intent that open rocket in google play is fired
    final Uri uri = Uri.parse(IntentUtils.MARKET_INTENT_URI_PACKAGE_PREFIX +
                              InstrumentationRegistry.getInstrumentation()
                                  .getTargetContext()
                                  .getPackageName());
    intended(allOf(hasAction(equalTo(Intent.ACTION_VIEW)), hasData(uri),
                   hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK)));
  }

  /**
   * Test case no: TC_0062
   * Test case name: In-app promotion - Press FEEDBACK to Mozilla feedback site
   * Steps:
   * 1. tap close button
   * 2. Press feedback button
   * 3. check feedback page url matches expectation
   */

  @Test
  public void showRateAppPromotionAndClickFeedback_openFeedbackUrl() {
    sessionLoadedIdlingResource =
        new SessionLoadedIdlingResource(intentTestRule.getActivity());

    // Check share app dialog is displayed and click the feedback button
    onView(withId(R.id.negative_button))
        .check(matches(isDisplayed()))
        .perform(click());

    // Wait the feedback web page is loaded
    IdlingRegistry.getInstance().register(sessionLoadedIdlingResource);

    // Check if feedback url is matched
    final String feedbackUrl = InstrumentationRegistry.getInstrumentation()
                                   .getTargetContext()
                                   .getString(R.string.rate_app_feedback_url);
    onView(withId(R.id.display_url))
        .check(matches(isDisplayed()))
        .check(matches(withText(feedbackUrl)));
    IdlingRegistry.getInstance().unregister(sessionLoadedIdlingResource);
  }

  /**
   * Test case no: TC_0060
   * Test case name: in-app promotion - Press CLOSE button
   * Steps:
   * 1. tap close button
   * 2. Press back button
   * 3. back to home page
   */

  @Test
  public void showRateAppPromotionAndClickClose_backToHome() {
    // Check share app dialog is displayed and click the close button
    onView(withId(R.id.close_button))
        .check(matches(isDisplayed()))
        .perform(click());

    // Check if we are back to home
    onView(withId(R.id.home_fragment_fake_input)).check(matches(isDisplayed()));
  }
}