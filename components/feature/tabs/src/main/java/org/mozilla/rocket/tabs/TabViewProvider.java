/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.rocket.tabs;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebViewDatabase;

/**
 * A class to create TabView instance.
 */
public abstract class TabViewProvider {
  public abstract TabView create();

  /**
   * To clean up some persistent data which effect provided TabView, but not
   * directly inside the TabView. <p> For instance, a cookie effect a
   * @see{android.webkit.WebView} but it is stores in another place.
   */
  public void purify(final Context context) {}
}
