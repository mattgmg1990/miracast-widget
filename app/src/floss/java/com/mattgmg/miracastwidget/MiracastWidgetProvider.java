package com.mattgmg.miracastwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.view.Display;
import android.widget.RemoteViews;

public class MiracastWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int length = appWidgetIds.length;

        for (int i = 0; i < length; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_WIDGET_LAUNCH, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.miracast_widget);
            views.setOnClickPendingIntent(R.id.widget_layout_parent, pendingIntent);
            final DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

            Display[] displays = displayManager.getDisplays();
            boolean displaySet = false;
            int currentDisplay = -1;
            for(int j = 0; j < displays.length; j++){
            	Display display = displays[j];
            	if(display.getDisplayId() != Display.DEFAULT_DISPLAY){
                    views.setTextViewText(R.id.widget_text, display.getName());
                    views.setTextColor(R.id.widget_text, context.getResources().getColor(android.R.color.holo_blue_bright));
                    currentDisplay = display.getDisplayId();
                    displaySet = true;
            	}
            }
            
            if(!displaySet){
                views.setTextViewText(R.id.widget_text, "Cast Screen");
                views.setTextColor(R.id.widget_text, context.getResources().getColor(android.R.color.white));
            }

            MiracastDisplayListener displayListener = new MiracastDisplayListener(currentDisplay, views, displayManager, appWidgetManager, appWidgetId, context);
            displayManager.registerDisplayListener(displayListener, null);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
	
	private class MiracastDisplayListener implements DisplayListener {
		int mCurrentDisplay = -1;
		RemoteViews mViews;
		DisplayManager mDisplayManager;
		int mAppWidgetId;
		AppWidgetManager mAppWidgetManager;
		Context mContext;
		
		public MiracastDisplayListener(int currentDisplay, RemoteViews widgetRemoteViews, DisplayManager displayManager, AppWidgetManager appWidgetManager, int appWidgetId, Context context){
			mCurrentDisplay = currentDisplay;
			mViews = widgetRemoteViews;
			mDisplayManager = displayManager;
			mAppWidgetManager = appWidgetManager;
			mAppWidgetId = appWidgetId;
			mContext = context;
		}
				
        @Override
        public void onDisplayRemoved(int displayId) {
            if(displayId == mCurrentDisplay){
                    mCurrentDisplay = -1;
            }
            mViews.setTextViewText(R.id.widget_text, "Cast Screen");
            mViews.setTextColor(R.id.widget_text, mContext.getResources().getColor(android.R.color.white));
            
            // Tell the AppWidgetManager to perform an update on the current app widget
            mAppWidgetManager.updateAppWidget(mAppWidgetId, mViews);
        }
                
        @Override
        public void onDisplayChanged(int displayId) {
                        
        }
                
        @Override
        public void onDisplayAdded(int displayId) {
            mCurrentDisplay = displayId;
            Display display = mDisplayManager.getDisplay(displayId);
            mViews.setTextViewText(R.id.widget_text, display.getName());
            mViews.setTextColor(R.id.widget_text, mContext.getResources().getColor(android.R.color.holo_blue_bright));

            // Tell the AppWidgetManager to perform an update on the current app widget
            mAppWidgetManager.updateAppWidget(mAppWidgetId, mViews);
        }
	}
}
