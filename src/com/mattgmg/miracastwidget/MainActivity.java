package com.mattgmg.miracastwidget;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String ACTION_WIFI_DISPLAY_SETTINGS = "android.settings.WIFI_DISPLAY_SETTINGS";
    public static final String ACTION_CAST_SETTINGS = "android.settings.CAST_SETTINGS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Intent wifiActionIntent = new Intent(ACTION_WIFI_DISPLAY_SETTINGS);
        Intent castActionIntent = new Intent(ACTION_CAST_SETTINGS);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the
        // action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}

}
