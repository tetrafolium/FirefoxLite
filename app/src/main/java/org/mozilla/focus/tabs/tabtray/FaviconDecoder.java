/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.tabs.tabtray;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import org.mozilla.focus.utils.DimenUtils;
import org.mozilla.icon.FavIconUtils;

public class FaviconDecoder implements ResourceDecoder<FaviconModel, Bitmap> {
  private Context context;
  private Glide glide;

  public FaviconDecoder(Context context, Glide glide) {
    this.context = context;
    this.glide = glide;
  }

  @Override
  public boolean handles(FaviconModel source, Options options) {
    return true;
  }

  @Nullable
  @Override
  public Resource<Bitmap> decode(FaviconModel source, int width, int height,
                                 Options options) {
    // Do not cache icons that are generated by Rocket
    if (source.type == DimenUtils.TYPE_GENERATED) {
      return null;
    }

    Bitmap refinedBitmap = DimenUtils.getRefinedBitmap(
        context.getResources(), source.originalIcon,
        FavIconUtils.getRepresentativeCharacter(source.url));
    if (refinedBitmap == source.originalIcon) {
      refinedBitmap = Bitmap.createBitmap(source.originalIcon);
    }
    return BitmapResource.obtain(refinedBitmap, glide.getBitmapPool());
  }
}
