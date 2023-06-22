package com.kdang.library.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.logger.Logger;

public class SharedPreferenceUtil {
    static SharedPreferenceUtil instances;
    private static final String preferenceName= "KDang";
    private static final String KEY_DAY_SCHEDULE_LIST = "DayScheduleList";
    private static SharedPreferences pref = null;
    private static SharedPreferences.Editor edit = null;
    private SharedPreferenceUtil(){}
    public static SharedPreferenceUtil getInstance(){
        if(instances == null){
            instances = new SharedPreferenceUtil();
        }
        return instances;
    }

    public static void saveDayScheduleList(Context context, String scheduleListJson){
        Logger.e("saveDayScheduleList  "+scheduleListJson);
        pref = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        edit = pref.edit();
        edit.putString(KEY_DAY_SCHEDULE_LIST, scheduleListJson);
        edit.commit();

    }
    public static String getDayScheduleList(Context context){
        Logger.e("getDayScheduleList");
        pref = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        return pref.getString(KEY_DAY_SCHEDULE_LIST, null);
    }
}
