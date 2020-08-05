package org.mozilla.lite.newspoint;

import android.content.Context;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.lite.partner.Repository;

public class RepositoryNewsPoint extends Repository<NewsPointItem> {
  static final String SUBSCRIPTION_KEY_NAME = "newspoint";

  static final int FIRST_PAGE = 1;

  static Parser<NewsPointItem> PARSER = source -> {
    List<NewsPointItem> ret = new ArrayList<>();
    // TODO: 11/2/18 It takes 0.1s - 0.2s to create JsonObject, do we want to
    // improve this?
    JSONObject root = new JSONObject(source);
    JSONArray items = root.getJSONArray("items");
    for (int i = 0; i < items.length(); i++) {
      JSONObject row = items.getJSONObject(i);
      String id = safeGetString(row, "id");
      String hl = safeGetString(row, "hl");
      String imageid = safeGetString(row, "imageid");
      JSONArray array = safeGetArray(row, "images");
      String imageUrl = array == null ? null : array.getString(0);
      String pn = safeGetString(row, "pn");
      String dl = safeGetString(row, "dl");
      String dm = safeGetString(row, "dm");
      long pid = safeGetLong(row, "pid");
      long lid = safeGetLong(row, "lid");
      String lang = safeGetString(row, "lang");
      String tn = safeGetString(row, "tn");
      String wu = safeGetString(row, "wu");
      String pnu = safeGetString(row, "pnu");
      String fu = safeGetString(row, "fu");
      String sec = safeGetString(row, "sec");
      String mwu = safeGetString(row, "mwu");
      String m = safeGetString(row, "m");
      String separator = "" + '\0';
      List<String> tags = Arrays.asList(
          row.getJSONArray("tags").join(separator).split(separator));
      if (id == null || hl == null || mwu == null || dl == null) {
        continue;
      }
      long timestamp = 0;
      try {
        timestamp =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss 'IST' yyyy", Locale.US)
                .parse(dl)
                .getTime();
      } catch (ParseException e) {
        e.printStackTrace();
        // skip this item
        continue;
      }
      NewsPointItem newspointeItem =
          new NewsPointItem(id, imageUrl, hl, mwu, timestamp, imageid, pn, dm,
                            pid, lid, lang, tn, wu, pnu, fu, sec, m, tags);
      ret.add(newspointeItem);
    }
    return ret;
  };

  private static JSONArray safeGetArray(JSONObject object, String key) {
    try {
      return object.getJSONArray(key);
    } catch (JSONException ex) {
      return null;
    }
  }

  private static String safeGetString(JSONObject object, String key) {
    try {
      return object.getString(key);
    } catch (JSONException ex) {
      return null;
    }
  }

  private static long safeGetLong(JSONObject object, String key) {
    try {
      return object.getLong(key);
    } catch (JSONException ex) {
      return -1L;
    }
  }

  public RepositoryNewsPoint(Context context, String subscriptionUrl) {
    super(context, null, 3, null, null, SUBSCRIPTION_KEY_NAME, subscriptionUrl,
          FIRST_PAGE, PARSER, true);
  }

  @Override
  protected String getSubscriptionUrl(int pageNumber) {
    return String.format(Locale.US, subscriptionUrl, pageNumber,
                         DEFAULT_PAGE_SIZE);
  }
}
