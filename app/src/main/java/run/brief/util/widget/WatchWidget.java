package run.brief.util.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import run.brief.R;
import run.brief.util.log.BLog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class WatchWidget extends AppWidgetProvider
{
    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
    	BLog.e("Widget update", "called");
        RemoteViews remoteViews;
        ComponentName watchWidget;
        DateFormat format = SimpleDateFormat.getTimeInstance( SimpleDateFormat.MEDIUM, Locale.getDefault() );

        remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget );
        watchWidget = new ComponentName( context, WatchWidget.class );
        remoteViews.setTextViewText( R.id.widget_textview, "Time = " + format.format( new Date()));
        appWidgetManager.updateAppWidget( watchWidget, remoteViews );
    }
}