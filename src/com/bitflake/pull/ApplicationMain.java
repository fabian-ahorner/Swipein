package com.bitflake.pull;

import java.util.HashMap;

import android.app.Application;

import com.bitflake.managers.FontManager;
import com.bitflake.managers.FontManager.Font;
import com.bitflake.managers.settings.SettingsObservable;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class ApplicationMain extends Application {
	/**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 * 
	 * A single tracker is usually enough for most purposes. In case you do need
	 * multiple trackers, storing them all in Application object helps ensure
	 * that they are created only once per application instance.
	 */
	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
							// company.
	}

	private static HashMap<TrackerName, Tracker> mTrackers;

	@Override
	public void onCreate() {
		super.onCreate();
		FontManager.getInstance().setDefaultFont(Font.STATION);
		SettingsObservable.init(this);
		mTrackers = new HashMap<>();
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		Tracker t = analytics.newTracker("UA-49999932-8");
		mTrackers.put(TrackerName.APP_TRACKER, t);
	}

	public static synchronized Tracker getTracker(TrackerName trackerId) {
		return mTrackers.get(trackerId);
	}

}
