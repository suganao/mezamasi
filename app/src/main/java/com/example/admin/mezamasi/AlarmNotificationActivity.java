package com.example.admin.mezamasi;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.example.admin.mezamasi.Constants.FILE_NAME;

/**
 * Created by admin on 2017/02/25.
 */

public class AlarmNotificationActivity extends Activity {
    private MediaPlayer mp;
    private MyAlarmManager mam;
    private Vibrator vib;
    private long pattern[] = {500, 200};

    private ImageView imageView;
    private TextView textView;
    private UrlDto dto;

    private int defaultVol;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_notification);
        load();
        textView = (TextView) findViewById(R.id.memo2);
        textView.setText(dto.getMemo());
        imageView = (ImageView) findViewById(R.id.back_view);
        Uri uri = null;
        if (dto != null) {
            uri = Uri.parse(dto.getUrl());
            Log.i("", "Uri: " + uri.toString());

            try {
                Bitmap bmp = getBitmapFromUri(uri);
                imageView.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mam = new MyAlarmManager(this);
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // スクリーンロックを解除する
        // 権限が必要
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        Toast.makeText(this, "アラーム！", Toast.LENGTH_SHORT).show();

        final Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mam.stopAlarm();
                vib.cancel();
                Intent intent = new Intent(AlarmNotificationActivity.this, MainActivity.class);
                intent.putExtra("stop", true);
                startActivity(intent);
                finish();
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(Constants.mNotificationId);
                clearMemo();
                stopAndRelaese();
            }
        });
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        //読み込み用のオプションオブジェクトを生成
        BitmapFactory.Options options = new BitmapFactory.Options();
       //この値をtrueにすると実際には画像を読み込まず、
       //画像のサイズ情報だけを取得することができます。
        options.inJustDecodeBounds = true;

        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        Bitmap image;
        int imageSizeMax = 500;
        float imageScaleWidth = (float)options.outWidth / imageSizeMax;
        float imageScaleHeight = (float)options.outHeight / imageSizeMax;
        if (imageScaleWidth > 2 && imageScaleHeight > 2) {
            BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();
            // 縦横、小さい方に縮小するスケールを合わせる
            int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

            // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
            for (int i = 2; i <= imageScale; i *= 2) {
                imageOptions2.inSampleSize = i;
            }

            image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, imageOptions2);
            Log.v("image", "Sample Size: 1/" + imageOptions2.inSampleSize);
        } else {
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        }

        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(getApplicationContext(), "アラームスタート！", Toast.LENGTH_LONG).show();
        // 音を鳴らす
        if (mp == null)
            // resのrawディレクトリにtest.mp3を置いてある
            mp = MediaPlayer.create(this, R.raw.test);

        mp.setLooping(true);

        AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        defaultVol = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 最大音量値を取得
        int maxVol = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 音量を設定
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(maxVol/1.5), 0);
//        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        mp.setVolume(1.0f, 1.0f);

//        mp.start();
        vib.vibrate(pattern, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void stopAndRelaese() {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // 音量を元に戻す
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVol, 0);
    }

    /**
     * データを読み込む
     */
    private void load() {
        // ファイルに保存したデータを読み込む
        Object o = TempDataUtil.load(this, FILE_NAME);
        if (o == null)
            dto = new UrlDto("","");
        else {
            dto = (UrlDto) o;
        }
        // 読み込んだデータを画面に反映する
    }

    private void clearMemo() {
        dto.setMemo("");

        // Dtoに詰め込んだデータをファイルに保存する
        TempDataUtil.store(this, dto, FILE_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        alarmNowText = (TextView) findViewById(R.id.alarm_now_time);
//        handler.sendEmptyMessage(WHAT);
        // mam.stopAlarm();
    }
}
