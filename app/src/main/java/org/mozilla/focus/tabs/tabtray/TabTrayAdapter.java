/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.tabs.tabtray;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.mozilla.focus.R;
import org.mozilla.focus.utils.DimenUtils;
import org.mozilla.rocket.nightmode.themed.ThemedRecyclerView;
import org.mozilla.rocket.nightmode.themed.ThemedRelativeLayout;
import org.mozilla.rocket.nightmode.themed.ThemedTextView;
import org.mozilla.icon.FavIconUtils;
import org.mozilla.rocket.tabs.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TabTrayAdapter extends RecyclerView.Adapter<TabTrayAdapter.ViewHolder> {

private List<Session> tabs = new ArrayList<>();
private Session focusedTab;

private TabClickListener tabClickListener;

private RequestManager requestManager;

private HashMap<String, Drawable> localIconCache = new HashMap<>();

private boolean isNight;

TabTrayAdapter(RequestManager requestManager) {
	this.requestManager = requestManager;
}

@Override
public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	final ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
							 R.layout.item_tab_tray, parent, false));

	InternalTabClickListener listener = new InternalTabClickListener(holder, tabClickListener);

	holder.itemView.setOnClickListener(listener);
	holder.closeButton.setOnClickListener(listener);
	return holder;
}

@Override
public void onBindViewHolder(final ViewHolder holder, int position) {
	holder.itemView.setSelected(tabs.get(position) == focusedTab);

	Resources resources = holder.itemView.getResources();

	Session tab = tabs.get(position);

	String title = getTitle(tab, holder);
	holder.websiteTitle.setText(TextUtils.isEmpty(title) ?
	                            resources.getString(R.string.app_name) : title);

	String url = tab.getUrl();
	if (!TextUtils.isEmpty(url)) {
		holder.websiteSubtitle.setText(tab.getUrl());
	}

	setFavicon(tab, holder);
	holder.rootView.setNightMode(isNight);
	holder.websiteTitle.setNightMode(isNight);
	holder.websiteSubtitle.setNightMode(isNight);
}

@Override
public void onViewRecycled(ViewHolder holder) {
	holder.websiteTitle.setText("");
	holder.websiteSubtitle.setText("");
	updateFavicon(holder, null);
}

@Override
public int getItemCount() {
	return tabs.size();
}

@Override
public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
	super.onAttachedToRecyclerView(recyclerView);
	if (recyclerView instanceof ThemedRecyclerView) {
		ThemedRecyclerView themedRecyclerView = (ThemedRecyclerView) recyclerView;
		this.isNight = themedRecyclerView.isNightMode();
	}
}

void setTabClickListener(TabClickListener tabClickListener) {
	this.tabClickListener = tabClickListener;
}

void setData(List<Session> tabs) {
	this.tabs.clear();
	this.tabs.addAll(tabs);
}

List<Session> getData() {
	return this.tabs;
}

void setFocusedTab(Session tab) {
	focusedTab = tab;
}

Session getFocusedTab() {
	return focusedTab;
}

private String getTitle(Session tab, ViewHolder holder) {
	String newTitle = tab.getTitle();
	String currentTitle = String.valueOf(holder.websiteTitle.getText());

	if (TextUtils.isEmpty(newTitle)) {
		return TextUtils.isEmpty(currentTitle) ? "" : currentTitle;
	}

	return newTitle;
}

private void setFavicon(Session tab, final ViewHolder holder) {
	String uri = tab.getUrl();
	if (TextUtils.isEmpty(uri)) {
		return;
	}

	loadCachedFavicon(tab, holder);
}

private void loadCachedFavicon(final Session tab, final ViewHolder holder) {
	RequestOptions options = new RequestOptions()
	                         .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
	                         .dontAnimate();

	Bitmap favicon = tab.getFavicon();
	FaviconModel model = new FaviconModel(tab.getUrl(),
	                                      DimenUtils.getFavIconType(holder.itemView.getResources(), favicon),
	                                      favicon);

	requestManager
	.load(model)
	.apply(options)
	.listener(new RequestListener<Drawable>() {
			@Override
			public boolean onLoadFailed(@Nullable GlideException e, Object model,
			                            Target<Drawable> target,
			                            boolean isFirstResource) {
			        loadGeneratedFavicon(tab, holder);
			        return true;
			}

			@Override
			public boolean onResourceReady(Drawable resource, Object model,
			                               Target<Drawable> target,
			                               DataSource dataSource,
			                               boolean isFirstResource) {
			        return false;
			}
		})
	.into(new SimpleTarget<Drawable>() {
			@Override
			public void onResourceReady(Drawable resource,
			                            Transition<? super Drawable> transition) {
			        updateFavicon(holder, resource);
			}
		});
}

private void loadGeneratedFavicon(Session tab, final ViewHolder holder) {
	Character symbol = FavIconUtils.getRepresentativeCharacter(tab.getUrl());
	Bitmap favicon = tab.getFavicon();
	int backgroundColor = (favicon == null) ? Color.WHITE : FavIconUtils.getDominantColor(favicon);
	String key = symbol.toString() + "_" + Integer.toHexString(backgroundColor);

	if (localIconCache.containsKey(key)) {
		updateFavicon(holder, localIconCache.get(key));
	} else {
		BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(),
		                                             DimenUtils.getInitialBitmap(holder.itemView.getResources(), symbol, backgroundColor));
		localIconCache.put(key, drawable);
		updateFavicon(holder, drawable);
	}
}

private void updateFavicon(ViewHolder holder, @Nullable Drawable drawable) {
	if (drawable != null) {
		holder.websiteIcon.setImageDrawable(drawable);
		holder.websiteIcon.setBackgroundColor(Color.TRANSPARENT);
	} else {
		holder.websiteIcon.setImageResource(R.drawable.favicon_default);
		holder.websiteIcon.setBackgroundColor(ContextCompat.getColor(
							      holder.websiteIcon.getContext(),
							      R.color.tabTrayItemIconBackground));
	}
}

static class ViewHolder extends RecyclerView.ViewHolder {
ThemedRelativeLayout rootView;
ThemedTextView websiteTitle;
ThemedTextView websiteSubtitle;
View closeButton;
ImageView websiteIcon;

ViewHolder(View itemView) {
	super(itemView);
	rootView = itemView.findViewById(R.id.root_view);
	websiteTitle = itemView.findViewById(R.id.website_title);
	websiteSubtitle = itemView.findViewById(R.id.website_subtitle);
	closeButton = itemView.findViewById(R.id.close_button);
	websiteIcon = itemView.findViewById(R.id.website_icon);
}
}

static class InternalTabClickListener implements View.OnClickListener {
private ViewHolder holder;
private TabClickListener tabClickListener;

InternalTabClickListener(ViewHolder holder, TabClickListener tabClickListener) {
	this.holder = holder;
	this.tabClickListener = tabClickListener;
}

@Override
public void onClick(View v) {
	if (tabClickListener == null) {
		return;
	}

	int pos = holder.getAdapterPosition();
	if (pos != RecyclerView.NO_POSITION) {
		dispatchOnClick(v, pos);
	}
}

private void dispatchOnClick(View v, int position) {
	switch (v.getId()) {
	case R.id.root_view:
		tabClickListener.onTabClick(position);
		break;

	case R.id.close_button:
		tabClickListener.onTabCloseClick(position);
		break;

	default:
		break;
	}
}
}

public interface TabClickListener {
void onTabClick(int tabPosition);

void onTabCloseClick(int tabPosition);
}
}
