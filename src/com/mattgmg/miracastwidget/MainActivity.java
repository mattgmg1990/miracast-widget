package com.mattgmg.miracastwidget;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String ACTION_WIFI_DISPLAY_SETTINGS = "android.settings.WIFI_DISPLAY_SETTINGS";
    public static final String ACTION_CAST_SETTINGS = "android.settings.CAST_SETTINGS";
    public static final String SETTINGS_APP_PACKAGE_NAME = "com.android.settings";
    public static final String SAMSUNG_ALL_CAST_PERMISSION
            = "com.android.setting.permission.ALLSHARE_CAST_SERVICE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // Check for Samsung permission. If it is defined, but not granted (as it is on older
        // devices), then we can't launch the proper Settings Activity.
        if (isPermissionDefined(SAMSUNG_ALL_CAST_PERMISSION)
                && !isPermissionGranted(SAMSUNG_ALL_CAST_PERMISSION)) {
            // Show different message or tutorial if Samsung?
            showErrorToast();
            finish();
            return;
        }

        Intent wifiActionIntent = new Intent(ACTION_WIFI_DISPLAY_SETTINGS);
        wifiActionIntent.setPackage(SETTINGS_APP_PACKAGE_NAME);
        Intent castActionIntent = new Intent(ACTION_CAST_SETTINGS);
        castActionIntent.setPackage(SETTINGS_APP_PACKAGE_NAME);
        if(isCallable(wifiActionIntent)){
            try {
                startSettingsActivity(wifiActionIntent);
            } catch (ActivityNotFoundException exception) {
                showErrorToast();
            }
        } else if(isCallable(castActionIntent)) {
            try {
                startSettingsActivity(castActionIntent);
            } catch (ActivityNotFoundException exception) {
                showErrorToast();
            }
        } else {
            showErrorToast();
        }
		finish();
	}

    private void showErrorToast() {
        String errorMessage = getResources().getString(R.string.error_toast);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
	
	private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,
                                                                           PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }	
	
    private void startSettingsActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private boolean isPermissionGranted(String permission) {
        PackageManager pm = getPackageManager();
        int hasPerm = pm.checkPermission(permission, getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPermissionDefined(String permission) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPermissionInfo(permission, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // Exception thrown, permission not defined.
            return false;
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
