package org.mozilla.banner;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BannerConfigViewModel extends ViewModel {

private MutableLiveData<String[]> homeBannerConfig;
private MutableLiveData<String[]> couponBannerConfig;

public MutableLiveData<String[]> getHomeConfig() {
	if (homeBannerConfig == null) {
		homeBannerConfig = new MutableLiveData<>();
	}
	return homeBannerConfig;
}

public MutableLiveData<String[]> getCouponConfig() {
	if (couponBannerConfig == null) {
		couponBannerConfig = new MutableLiveData<>();
	}
	return couponBannerConfig;
}

}
