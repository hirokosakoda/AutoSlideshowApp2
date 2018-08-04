package jp.techacademy.hiroko.sakoda.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
//import android.support.design.widget.Snackbar;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Timer mTimer;
    double mTimerSec = 0.0;

    Handler mHandler = new Handler();

    Button mBackButton;
    Button mStartPauseButton;
    Button mNextButton;
    ImageView mImageView;
    TextView mTextView;
    LinearLayout mainLayout;

    int imageCount = 1;
    ArrayList<Uri> uriArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackButton = (Button) findViewById(R.id.back_button);
        mStartPauseButton = (Button) findViewById(R.id.startPause_button);
        mNextButton = (Button) findViewById(R.id.next_button);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.textView);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        //再生・停止ボタン
        mStartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int imageNum = uriArray.size();

                if (imageNum != 0) {

                    mBackButton.setEnabled(true);
                    mNextButton.setEnabled(true);
                    mStartPauseButton.setEnabled(true);

                    if (mTimer == null) {

                        mStartPauseButton.setText("停止");
                        mTimer = new Timer();
                        setButtonsState(mTimer);

                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                mTimerSec += 2.0;

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (imageCount >= uriArray.size()) {

                                            mTimer.cancel();
                                            mTimer = null;
                                            mStartPauseButton.setText("再生");
                                            imageCount = 1;

                                        } else {

                                            imageCount += 1;
                                        }

                                        mImageView.setImageURI(uriArray.get(imageCount - 1));
                                        showNumberToTextView(imageCount);
                                        setButtonsState(mTimer);
                                    }
                                });
                            }
                        }, 2000, 2000);

                    } else {

                        mStartPauseButton.setText("再生");
                        mTimer.cancel();
                        mTimer = null;
                        setButtonsState(mTimer);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "写真を追加してください", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        //戻るボタン
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageCount <= 1) {
                    imageCount = uriArray.size() + 1;
                }
                imageCount -= 1;
                mImageView.setImageURI(uriArray.get(imageCount - 1));
                showNumberToTextView(imageCount);
            }
        });

        //進むボタン
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageCount >= uriArray.size()) {
                    imageCount = 0;
                }
                imageCount += 1;
                mImageView.setImageURI(uriArray.get(imageCount - 1));
                showNumberToTextView(imageCount);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {

                    Toast.makeText(this, "アクセスを許可してください", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);

                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                uriArray.add(imageUri);

            } while (cursor.moveToNext());
        }

        mImageView.setImageURI(uriArray.get(0));
        showNumberToTextView(1);

        cursor.close();
    }

    private void showNumberToTextView(int num) {
        mTextView.setText(String.valueOf(num) + "/" + String.valueOf(uriArray.size()));
    }

    private void setButtonsState(Timer mTimer) {

        boolean b;

        if (mTimer == null) {
            b = true;
        } else {
            b = false;
        }

        mBackButton.setEnabled(b);
        mNextButton.setEnabled(b);
    }
}

