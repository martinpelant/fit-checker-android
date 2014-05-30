package cz.mpelant.fitchecker.utils;

import android.content.Intent;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import cz.mpelant.fitchecker.App;

import java.util.Date;

/**
 * Tools.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.utils
 * @since 5/30/2014
 */
public class Tools {
    public static void addToCalendar(@NonNull String title, @Nullable String location, @Nullable String description, @NonNull Date startDate, @NonNull Date endDate) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, title);
        if (location != null)
            calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        if (description != null)
            calIntent.putExtra(CalendarContract.Events.DESCRIPTION, description);

        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTime());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTime());
        calIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        App.getInstance().startActivity(calIntent);
    }
}
