/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import org.mozilla.focus.Inject;
import org.mozilla.focus.R;

import java.util.ArrayList;
import java.util.List;

public class ScreenCaptureDialogFragment extends DialogFragment {

private LottieAnimationView mLottieAnimationView;

private static final String ANIMATE_DONE = "screenshots-done.json";

private List<DialogInterface.OnDismissListener> mOnDismissListeners = new ArrayList<>();

public static ScreenCaptureDialogFragment newInstance() {
	ScreenCaptureDialogFragment screenCaptureDialogFragment = new ScreenCaptureDialogFragment();
	screenCaptureDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ScreenCaptureDialog);
	return screenCaptureDialogFragment;
}

@NonNull
@Override
public Dialog onCreateDialog(Bundle savedInstanceState) {
	Dialog dialog = super.onCreateDialog(savedInstanceState);
	dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			        if (keyCode == KeyEvent.KEYCODE_BACK) {
			                return true;
				}
			        return false;
			}
		});
	return dialog;
}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.dialog_capture_screen, container, false);
	mLottieAnimationView = (LottieAnimationView) view.findViewById(R.id.animation_view);
	if (!Inject.isUnderEspressoTest()) {
		// Since Lottie 2.5.0, infinite animation may affect ScreenshotCaptureTask to be finished on bitrise.
		// We don't play animation during testing
		mLottieAnimationView.playAnimation();
	}
	return view;
}

@Override
public void onDismiss(DialogInterface dialog) {
	super.onDismiss(dialog);
	for (DialogInterface.OnDismissListener listener : mOnDismissListeners) {
		listener.onDismiss(dialog);
	}
}

public void dismiss(boolean waitAnimation) {
	if (waitAnimation) {
		LottieComposition.Factory.fromAssetFileName(getContext(), ANIMATE_DONE, new OnCompositionLoadedListener() {
				@Override
				public void onCompositionLoaded(@Nullable LottieComposition composition) {
				        mLottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationCancel(Animator animation) {
						        dismissAllowingStateLoss();
						}

						@Override
						public void onAnimationEnd(Animator animation) {
						        dismissAllowingStateLoss();
						}
					});
				        mLottieAnimationView.loop(false);
				        mLottieAnimationView.setComposition(composition);
				        mLottieAnimationView.playAnimation();
				}
			});
	} else {
		dismissAllowingStateLoss();
	}
}

public void addOnDismissListener(DialogInterface.OnDismissListener listener) {
	mOnDismissListeners.add(listener);
}
}
