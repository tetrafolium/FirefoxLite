/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.firstrun;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.OnCompositionLoadedListener;

import org.mozilla.focus.R;

import java.util.ArrayList;

public class FirstrunPagerAdapter extends PagerAdapter {

protected ArrayList<FirstrunPage> pages = new ArrayList<>();
protected Context context;
private View.OnClickListener listener;

public FirstrunPagerAdapter(Context context, View.OnClickListener listener) {
	this.context = context;
	this.listener = listener;
}

protected View getView(int position, ViewPager pager) {
	final View view = LayoutInflater.from(context).inflate(
		R.layout.firstrun_page, pager, false);

	final FirstrunPage page = pages.get(position);

	final TextView titleView = (TextView) view.findViewById(R.id.title);
	titleView.setText(page.title);

	final TextView textView = (TextView) view.findViewById(R.id.text);
	textView.setText(page.text);
	textView.setMovementMethod(LinkMovementMethod.getInstance());

	final ImageView imageView = (ImageView) view.findViewById(R.id.image);
	if (page.lottieString != null) {
		final LottieDrawable drawable = new LottieDrawable();
		LottieComposition.Factory.fromAssetFileName(context,
		                                            page.lottieString,
		                                            new OnCompositionLoadedListener() {
				@Override
				public void onCompositionLoaded(@Nullable LottieComposition composition) {
				        drawable.setComposition(composition);
				}
			});
		imageView.setImageDrawable(drawable);
	} else {
		imageView.setImageResource(page.imageResource);
	}

	final Button buttonView = (Button) view.findViewById(R.id.button);
	buttonView.setOnClickListener(listener);
	if (position == pages.size() - 1) {
		buttonView.setText(R.string.firstrun_close_button);
		buttonView.setId(R.id.finish);
	} else {
		buttonView.setText(R.string.firstrun_next_button);
		buttonView.setId(R.id.next);
	}


	return view;
}

@Override
public boolean isViewFromObject(View view, Object object) {
	return view == object;
}

@Override
public int getCount() {
	return pages.size();
}

@Override
public Object instantiateItem(ViewGroup container, int position) {
	ViewPager pager = (ViewPager) container;
	View view = getView(position, pager);

	pager.addView(view);

	return view;
}

@Override
public void destroyItem(ViewGroup container, int position, Object view) {
	container.removeView((View) view);
}

}
