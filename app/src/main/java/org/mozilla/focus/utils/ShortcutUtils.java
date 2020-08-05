/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import android.text.TextUtils;

import org.mozilla.icon.FavIconUtils;

import java.util.ArrayList;
import java.util.List;

public class ShortcutUtils {

    private static final String TAG = "ShortcutUtils";


    // Request pinned shortcut for both API level
    public static void requestPinShortcut(@NonNull final Context context, @NonNull final Intent shortcutIntent,
                                          @NonNull final String title, @NonNull final String urlAsShortcutId, final Bitmap bitmap) {

        final Bitmap icon;
        final Resources resources = context.getResources();
        final char representativeCharacter = FavIconUtils.getRepresentativeCharacter(urlAsShortcutId);

        if (bitmap == null) {
            // if favicon is not ready, we use the default initial icon with white color
            icon = DimenUtils.getInitialBitmap(resources, null, representativeCharacter);
        } else {
            // if favicon is ready, resize it using size that fits shortcut better
            icon = DimenUtils.getRefinedShortcutIcon(resources, bitmap, representativeCharacter);
        }
        // label must not be empty
        String label = title;
        if (TextUtils.isEmpty(title)) {
            label = urlAsShortcutId;
        }

        final ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, urlAsShortcutId)
        .setShortLabel(label)
        .setIcon(IconCompat.createWithBitmap(icon))
        .setIntent(shortcutIntent)
        .build();

        // Display home screen after add to home screen
        final IntentSender intentSender = IntentUtils.getLauncherHomePendingIntent(context)
                                          .getIntentSender();

        // Update the shortcut icon on launcher since previous one may not ready. API 26+ only.
        // TODO: find a way to update the shortcut icon for API 25 and below. Currently the only way is remove old shortcut and add again.
        updateShortcut26(context, shortcut);

        // If the launcher or system didn't support shortcut, we don't bother to call it.
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, intentSender);
        }

    }

    @TargetApi(26) // Add this to make lint happy
    private static void updateShortcut26(@NonNull Context context, ShortcutInfoCompat shortcut) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager != null) {
                final List<ShortcutInfo> list = new ArrayList<>();
                list.add(shortcut.toShortcutInfo());
                shortcutManager.updateShortcuts(list);
            }
        }
    }

}