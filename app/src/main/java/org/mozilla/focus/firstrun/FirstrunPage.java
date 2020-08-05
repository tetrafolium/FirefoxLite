package org.mozilla.focus.firstrun;

import androidx.annotation.DrawableRes;

class FirstrunPage {
  public final String title;
  public final CharSequence text;
  public final String lottieString;
  public final @DrawableRes int imageResource;

  FirstrunPage(String title, CharSequence text, String lottieString) {
    this.title = title;
    this.text = text;
    this.lottieString = lottieString;
    this.imageResource = -1;
  }

  FirstrunPage(String title, CharSequence text, int imageResource) {
    this.title = title;
    this.text = text;
    this.lottieString = null;
    this.imageResource = imageResource;
  }
}
