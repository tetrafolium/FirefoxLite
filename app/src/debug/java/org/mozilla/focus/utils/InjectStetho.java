package org.mozilla.focus.utils;

import android.app.Application;
import android.os.AsyncTask;

import com.facebook.stetho.Stetho;

public class InjectStetho {

    public static void init(Application application) {
        new StethoInitTask().execute();
    }

    private static class StethoInitTask extends AsyncTask<Application, Void, Void> {

        @Override
        protected Void doInBackground(Application... applications) {
            Stetho.initializeWithDefaults(applications[0]);
            return null;
        }
    }

}
