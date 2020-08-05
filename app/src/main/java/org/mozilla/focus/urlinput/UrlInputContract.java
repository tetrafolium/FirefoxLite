/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.urlinput;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class UrlInputContract {

  interface View {
    /**
     * To set Url bar content directly
     *
     * @param text
     */
    void setUrlText(@Nullable CharSequence text);

    /**
     * Use these suggestions, or replace existing ones, if any.
     *
     * @param texts
     */
    void setSuggestions(@Nullable List<CharSequence> texts);

    /**
     * Set quick search view's visibility
     *
     * @param visible
     */
    void setQuickSearchVisible(boolean visible);
  }

  interface Presenter {
    /**
     * Connect this Presenter to a View
     *
     * @param view to be connected
     */
    void setView(View view);

    /**
     * Called when user input any text in Url bar
     *
     * @param input
     * @param isThrottled
     */
    void onInput(@NonNull CharSequence input, boolean isThrottled);
  }
}
