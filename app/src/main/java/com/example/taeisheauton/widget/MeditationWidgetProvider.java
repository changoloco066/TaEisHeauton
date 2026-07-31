package com.example.taeisheauton.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.content.Intent;

import com.example.taeisheauton.R;
import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;

    public class MeditationWidgetProvider extends AppWidgetProvider {

        public static final String ACTION_REFRESH = "com.example.taeisheauton.widget.ACTION_REFRESH";

        @Override
        public void onReceive(Context context, Intent intent){
            super.onReceive(context, intent);

            if(ACTION_REFRESH.equals(intent.getAction())){
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, MeditationWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);

            }
        }

        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId);
            }
        }

        private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                MeditationDao dao = db.meditationDao();
                MeditationEntity meditation = dao.getRandom();

                String displayText = (meditation != null)
                        ? "Libro " + meditation.book + ", " + meditation.number + ": " + meditation.text
                        : "Aún no has importado meditaciones";

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_meditation);
                views.setTextViewText(R.id.widgetText, displayText);

                Intent refreshIntent = new Intent(context, MeditationWidgetProvider.class);
                refreshIntent.setAction(ACTION_REFRESH);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.refreshButton, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }).start();
        }
    }
