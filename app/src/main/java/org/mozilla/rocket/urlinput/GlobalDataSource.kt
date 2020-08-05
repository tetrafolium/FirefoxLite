/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.rocket.urlinput

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GlobalDataSource : QuickSearchDataSource {

    private lateinit var context: Context

    override fun fetchEngines(): LiveData<List<QuickSearch>> {
        val liveData = MutableLiveData<List<QuickSearch>>()
        QuickSearchUtils.loadDefaultEngines(context, liveData)
        return liveData
    }

    companion object {

        // It's an Application Context so it should be safe
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: GlobalDataSource? = null

        @JvmStatic
        fun getInstance(context: Context): GlobalDataSource? =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: GlobalDataSource().also {
                    INSTANCE = it
                    it.context = context.applicationContext
                }
            }
    }
}
