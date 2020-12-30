package com.mattgmg.miracastwidget;

import androidx.multidex.MultiDexApplication;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * An application subclass to contain shared objects. Used right now for Google Analytics.
 */
public class MiracastApplication extends MultiDexApplication {
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link MultiDexApplication}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
