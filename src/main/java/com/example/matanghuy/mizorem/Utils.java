package com.example.matanghuy.mizorem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by matanghuy on 3/6/15.
 */
public class Utils {


    public static final String FB_ID = "fbID";
    public static final String FULL_NAME_KEY = "fullName";
    private static final SimpleDateFormat clockDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault());

    public static Date updateDate(Date date, int hourOfDay, int minute){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTime();
    }

    public static Date updateDate(Date date, int dayOfMonth, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        return cal.getTime();
    }

    public static String getDateAndTimeAsString(Date date) {
        return fullDateFormat.format(date);
    }
    public static String getDateAsString(Date date) {
        return dateFormat.format(date);
    }

    public static String getTimeAsString(Date date) {
        return clockDateFormat.format(date);
    }


}
