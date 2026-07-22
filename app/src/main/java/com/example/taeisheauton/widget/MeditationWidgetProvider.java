package com.example.taeisheauton.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.taeisheauton.R;
import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;

    public class MeditationWidgetProvider extends AppWidgetProvider {

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

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }).start();
        }
    }
