package com.example.admin.mezamasi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by admin on 2017/02/25.
 */

public class MyAlarmManager {
    Context c;
    AlarmManager am;
    private PendingIntent mAlarmSender;

    private static final String TAG = MyAlarmManager.class.getSimpleName();

    public MyAlarmManager(Context c){
        // 初期化
        this.c = c;
        am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Log.v(TAG,"初期化完了");
    }

    public void addAlarm(int alarmHour, int alarmMinute){
        // アラームを設定する
        mAlarmSender = this.getPendingIntent();

        // アラーム時間設定
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        // 設定した時刻をカレンダーに設定
        cal.set(Calendar.HOUR_OF_DAY, alarmHour);
        cal.set(Calendar.MINUTE, alarmMinute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 過去だったら明日にする
        if(cal.getTimeInMillis() < System.currentTimeMillis()){
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        Toast.makeText(c, String.format("%02d時%02d分に起こします", alarmHour, alarmMinute), Toast.LENGTH_LONG).show();

        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), mAlarmSender);
        Log.v(TAG, cal.getTimeInMillis()+"ms");
        Log.v(TAG, "アラームセット完了");
    }

    public void stopAlarm() {
        // アラームのキャンセル
        mAlarmSender = this.getPendingIntent();
        Log.d(TAG, "stopAlarm()");
        am.cancel(mAlarmSender);
//        spm.updateToRevival();
    }

    public void cancelAlarm() {
        // アラームのキャンセル
        mAlarmSender = this.getPendingIntent();
        Log.d(TAG, "cancelAlarm()");
        am.cancel(mAlarmSender);
//        spm.updateToRevival();
    }

    public void getAlarmSetting() {

    }

    List<PendingIntent> list = new ArrayList<>();

    private PendingIntent getPendingIntent() {
        // アラーム時に起動するアプリケーションを登録
        Intent intent = new Intent(c, MyAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(c, PendingIntent.FLAG_ONE_SHOT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
