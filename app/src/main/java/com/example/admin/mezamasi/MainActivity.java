package com.example.admin.mezamasi;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.example.admin.mezamasi.Constants.FILE_NAME;

public class MainActivity extends AppCompatActivity {
    MyAlarmManager mam;
    TextView textView;

    private ImageView imageView;
    private static final int RESULT_PICK_IMAGEFILE = 1001;

    private UrlDto dto;

    NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this);

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mBuilder.setSmallIcon(R.mipmap.lp_ic_alpha_only);
        }
        else {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }
        mBuilder.setAutoCancel(false);

        dto = new UrlDto();
        load();

        imageView = (ImageView) findViewById(R.id.image_view);
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

        textView = (TextView) findViewById(R.id.text);
        boolean stop = getIntent().getBooleanExtra("stop", false);
        if (stop) {
            textView.setText("");
        }
        String hour = getIntent().getStringExtra("hour");
        String minute = getIntent().getStringExtra("minute");
        String memo = getIntent().getStringExtra("memo");
        if (hour != null) {
            textView.append(hour);
            textView.append(":");
            textView.append(minute);
            textView.append("にセットしました。");
            if (!memo.isEmpty())
            {
                textView.append(" : ");
                textView.append(memo);
                dto.setMemo(memo);
                store(dto.getUrl(), memo);
            }
            mBuilder.setContentText(textView.getText());
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_NO_CLEAR;
            // Builds the notification and issues it.
            mNotifyMgr.notify(Constants.mNotificationId, notification);
        }
        mam = new MyAlarmManager(this);
        Button addButton = (Button) findViewById(R.id.add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });
        Button deleteButton = (Button) findViewById(R.id.delete_btn);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mam.cancelAlarm();
                textView.setText("");
                clearMemo();
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(Constants.mNotificationId);
            }
        });

        Button button = (Button) findViewById(R.id.imgset_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // it would be "*/*".
                intent.setType("image/*");

                startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("", "Uri: " + uri.toString());

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    Bitmap bmp = getBitmapFromUri(uri);
                    imageView.setImageBitmap(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                store(uri.toString(), dto.getMemo());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
//        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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

    /**
     * データを書き込む
     */
    private void store(String url, String memo) {
        // 画面の情報をDtoに詰め込む
        dto.setUrl(url);

        dto.setMemo(memo);

        // Dtoに詰め込んだデータをファイルに保存する
        TempDataUtil.store(this, dto, FILE_NAME);
    }

    /**
     * データを読み込む
     */
    private void load() {
        // ファイルに保存したデータを読み込む
        Object o = TempDataUtil.load(this, FILE_NAME);
        if (o == null)
            dto = new UrlDto("", "");
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
}