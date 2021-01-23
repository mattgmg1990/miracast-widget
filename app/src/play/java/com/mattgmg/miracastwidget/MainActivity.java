package com.mattgmg.miracastwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;

public class MainActivity extends Activity {
	public static final String ACTION_WIFI_DISPLAY_SETTINGS = "android.settings.WIFI_DISPLAY_SETTINGS";
    public static final String ACTION_CAST_SETTINGS = "android.settings.CAST_SETTINGS";
    public static final String EXTRA_WIDGET_LAUNCH = "widget_launch";

    private static final String SCREEN_NAME = "MainActivity";
    private static final int LAUNCH_SOURCE_DIMEN_IDX = 1;
    private static final String LAUNCH_SOURCE_DIMENSION_WIDGET = "widget";
    private static final String LAUNCH_SOURCE_DIMENSION_LAUNCHER = "launcher";

    private static final String CATEGORY_CAST_LAUNCH = "launch_cast";
    private static final String ACTION_ERROR = "error";
    private static final String ACTION_SUCCESS = "success";

    private Tracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        MiracastApplication application = (MiracastApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(SCREEN_NAME);
        recordScreenView(getIntent().hasExtra(EXTRA_WIDGET_LAUNCH));
        updateWidget();

        Intent wifiActionIntent = new Intent(ACTION_WIFI_DISPLAY_SETTINGS);
        Intent castActionIntent = new Intent(ACTION_CAST_SETTINGS);

        ResolveInfo systemResolveInfo = getSystemResolveInfo(wifiActionIntent);
        if(systemResolveInfo != null){
            try {
                Intent systemWifiIntent = new Intent();
                systemWifiIntent.setClassName(systemResolveInfo.activityInfo.applicationInfo.packageName,
                                    systemResolveInfo.activityInfo.name);
                startSettingsActivity(systemWifiIntent);
                sendEvent(CATEGORY_CAST_LAUNCH, ACTION_SUCCESS, "wifi_action");
                finish();
                return;
            } catch (ActivityNotFoundException exception) {
                // We'll show an error below if the next Intent can't be launched
                mTracker.send(new HitBuilders.ExceptionBuilder()
                                .setDescription("Launching systemResolveInfo for wifi action")
                                .setFatal(false)
                                .build());
                sendEvent(CATEGORY_CAST_LAUNCH, ACTION_ERROR, "wifi_action_ANF_exception");
            }
        }

        systemResolveInfo = getSystemResolveInfo(castActionIntent);
        if(systemResolveInfo != null) {
            try {
                Intent systemCastIntent = new Intent();
                systemCastIntent.setClassName(systemResolveInfo.activityInfo.applicationInfo.packageName,
                                    systemResolveInfo.activityInfo.name);
                startSettingsActivity(systemCastIntent);
                sendEvent(CATEGORY_CAST_LAUNCH, ACTION_SUCCESS, "cast_action");
                finish();
                return;
            } catch (ActivityNotFoundException exception) {
                // Show an error in the block below.
                mTracker.send(new HitBuilders.ExceptionBuilder()
                                .setDescription("Launching systemResolveInfo for cast action")
                                .setFatal(false)
                                .build());
                sendEvent(CATEGORY_CAST_LAUNCH, ACTION_ERROR, "cast_action_ANF_exception");
            }
        }

        sendEvent(CATEGORY_CAST_LAUNCH, ACTION_ERROR, "launch_failure");
        // Show an error and fail
        showErrorToast();
        finish();
	}

    private void updateWidget() {
        Intent intent = new Intent(this, MiracastWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids
                = AppWidgetManager.getInstance(getApplication())
                        .getAppWidgetIds(new ComponentName(getApplication(), MiracastWidgetProvider.class));
        if (ids != null) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    }

    /**
     * Records that this activity was viewed and reports to GA.
     * @param widgetLaunch Was this activity launched from the widget?
     */
    private void recordScreenView(boolean widgetLaunch) {
        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder();
        String dimensionValue = widgetLaunch ? LAUNCH_SOURCE_DIMENSION_WIDGET
                                             : LAUNCH_SOURCE_DIMENSION_LAUNCHER;
        builder.setCustomDimension(LAUNCH_SOURCE_DIMEN_IDX, dimensionValue);
        mTracker.send(builder.build());
    }

    private void sendEvent(String category, String action, String label) {
        mTracker.send(new HitBuilders.EventBuilder()
                       .setCategory(category)
                       .setAction(action)
                       .setLabel(label)
                       .build());
    }

    private void showErrorToast() {
        String errorMessage = getResources().getString(R.string.error_toast);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
	
    private ResolveInfo getSystemResolveInfo(Intent intent) {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : list) {
            try {
                ApplicationInfo activityInfo = pm.getApplicationInfo(info.activityInfo.packageName,
                                                                   0);
                if ((activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    return info;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Continue to next ResolveInfo
            }
        }
        return null;
    }
	
    private void startSettingsActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            startActivity(intent);
        } catch (SecurityException e) {
            // We don't have permission to launch this activity, alert the user and return.
            showErrorToast();

            mTracker.send(new HitBuilders.ExceptionBuilder()
                                .setDescription("SecurityException launching intent: "
                                        + intent.getAction()
                                        + ", "
                                        + intent.getComponent())
                                .setFatal(false)
                                .build());
            sendEvent(CATEGORY_CAST_LAUNCH, ACTION_ERROR, "security_exception");
            return;
        }
    } 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the
        // action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}

}
