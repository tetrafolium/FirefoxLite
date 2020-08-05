package org.mozilla.cachedrequestloader;

import androidx.annotation.MainThread;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

public class ResponseData extends MutableLiveData<Pair<Integer, String>> {

  public static final int SOURCE_NETWORK = 0;
  public static final int SOURCE_CACHE = 1;

  private boolean networkReturned;

  ResponseData() { networkReturned = false; }

  @Override
  @MainThread
  public void setValue(Pair<Integer, String> value) {
    setNetworkReturned(value);
    if (shouldIgnoreCache(value)) {
      return;
    }
    super.setValue(value);
  }

  @Override
  @MainThread
  public void postValue(Pair<Integer, String> value) {
    setNetworkReturned(value);
    if (shouldIgnoreCache(value)) {
      return;
    }
    super.postValue(value);
  }

  private void setNetworkReturned(Pair<Integer, String> value) {
    if (value != null && value.first != null && SOURCE_NETWORK == value.first) {
      networkReturned = true;
    }
  }

  private boolean shouldIgnoreCache(Pair<Integer, String> value) {
    return networkReturned && value != null && value.first != null &&
        SOURCE_CACHE == value.first;
  }
}