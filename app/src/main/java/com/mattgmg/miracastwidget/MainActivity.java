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

import java.util.List;

public class MainActivity extends Activity {

	public static final String ACTION_WIFI_DISPLAY_SETTINGS = "android.settings.WIFI_DISPLAY_SETTINGS";
    public static final String ACTION_CAST_SETTINGS = "android.settings.CAST_SETTINGS";
    public static final String EXTRA_WIDGET_LAUNCH = "widget_launch";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
                finish();
                return;
            } catch (ActivityNotFoundException exception) {
                // We'll show an error below if the next Intent can't be launched
                exception.printStackTrace();
            }
        }

        systemResolveInfo = getSystemResolveInfo(castActionIntent);
        if(systemResolveInfo != null) {
            try {
                Intent systemCastIntent = new Intent();
                systemCastIntent.setClassName(systemResolveInfo.activityInfo.applicationInfo.packageName,
                                    systemResolveInfo.activityInfo.name);
                startSettingsActivity(systemCastIntent);
                finish();
                return;
            } catch (ActivityNotFoundException exception) {
                // Show an error in the block below.
                exception.printStackTrace();
            }
        }

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
                e.printStackTrace();
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
            e.printStackTrace();
            showErrorToast();
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
