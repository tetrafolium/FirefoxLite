/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.history.model;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import org.mozilla.focus.utils.AppConstants;

@Entity(tableName = "browsing_history", indices = { @Index("view_count") })
public class Site {

  public Site(long id, String title, @NonNull String url, long viewCount,
              long lastViewTimestamp, String favIconUri) {
    this.id = id;
    this.title = title;
    this.url = url;
    this.viewCount = viewCount;
    this.lastViewTimestamp = lastViewTimestamp;
    this.favIconUri = favIconUri;
  }

  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") private long id;

  private String title;

  @NonNull private String url;

  @Ignore @NonNull private boolean isDefault = false;

  @ColumnInfo(name = "view_count") private long viewCount;

  @ColumnInfo(name = "last_view_timestamp") private long lastViewTimestamp;

  @ColumnInfo(name = "fav_icon_uri") private String favIconUri;

  public long getId() { return this.id; }

  public void setId(long id) { this.id = id; }

  public String getTitle() { return this.title; }

  public void setTitle(String title) { this.title = title; }

  @NonNull
  public String getUrl() {
    return this.url;
  }

  public void setUrl(@NonNull String url) { this.url = url; }

  public long getViewCount() { return this.viewCount; }

  public void setViewCount(long count) { this.viewCount = count; }

  public long getLastViewTimestamp() { return this.lastViewTimestamp; }

  public void setLastViewTimestamp(long timestamp) {
    this.lastViewTimestamp = timestamp;
  }

  public String getFavIconUri() { return favIconUri; }

  public void setFavIconUri(String favIconUri) { this.favIconUri = favIconUri; }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Site && ((Site)obj).getId() == this.getId();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return AppConstants.isReleaseBuild() ? toStringRelease() : toStringNormal();
  }

  private String toStringRelease() {
    return "Site{"
        + "id='" + id + '\'' + ", viewCount='" + viewCount + '\'' +
        ", lastViewTimestamp='" + lastViewTimestamp + '\'' + '}';
  }

  private String toStringNormal() {
    return "Site{"
        + "id='" + id + '\'' + ", title='" + title + '\'' + ", url='" + url +
        '\'' + ", viewCount='" + viewCount + '\'' + ", lastViewTimestamp='" +
        lastViewTimestamp + '\'' + ", favIconUri='" + favIconUri + '\'' + '}';
  }

  public boolean isDefault() { return isDefault; }

  public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
