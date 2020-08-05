/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.tabs.tabtray;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class TabTray {
@Nullable
public static TabTrayFragment show(FragmentManager manager) {
	if (!manager.isStateSaved()) {
		TabTrayFragment tabTray = TabTrayFragment.newInstance();
		tabTray.show(manager, TabTrayFragment.FRAGMENT_TAG);
		return tabTray;
	}
	return null;
}

public static void dismiss(FragmentManager manager) {
	Fragment tabTray = manager.findFragmentByTag(TabTrayFragment.FRAGMENT_TAG);
	if (tabTray != null) {
		((DialogFragment) tabTray).dismissAllowingStateLoss();
	}
}

public static boolean isShowing(@Nullable FragmentManager manager) {
	return manager != null && (manager.findFragmentByTag(TabTrayFragment.FRAGMENT_TAG) != null);
}
}
