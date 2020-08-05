package org.mozilla.focus.persistence;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class BookmarkModel {  

  @PrimaryKey @NonNull private String id;  

  private String title;  

  private String url;

  public BookmarkModel(String id, String title, String url) {
    this.id = id;
    this.title = title;
    this.url = url;
  }

  @NonNull
  public String getId() {
    return id;
  }

  public void setId(@NonNull String id) { this.id = id; }

  public String getTitle() { return title; }

  public void setTitle(String title) { this.title = title; }

  public String getUrl() { return url; }

  public void setUrl(String url) { this.url = url; }

  @Override
  public String toString() {
    return "BookmarkModel{"
        + "id='" + id + '\'' + ", title='" + title + '\'' + ", url='" + url +
        '\'' + '}';
  }
}
