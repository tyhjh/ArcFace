package com.example.dhht.arcface;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.example.arclibrary.facefind.FaceFindService;
import com.example.arclibrary.liveness.LivenessActiveListener;
import com.example.arclibrary.liveness.LivenessService;
import com.example.arclibrary.util.DrawUtils;
import com.example.arclibrary.util.ImageUtils;
import com.yorhp.picturepick.OnPickListener;
import com.yorhp.picturepick.PicturePickUtil;

import java.io.File;
import java.util.List;

import permison.PermissonUtil;
import permison.listener.PermissionListener;


public class MainActivity extends AppCompatActivity {

    Button btn_liveness, btn_register, btn_recognition, btn_idCard, btn_picture;
    ImageView imageView;

    public static File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_liveness = (Button) findViewById(R.id.btn_liveness);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_recognition = (Button) findViewById(R.id.btn_recognition);
        btn_idCard = (Button) findViewById(R.id.btn_idCard);
        btn_picture = (Button) findViewById(R.id.btn_picture);
        setClick();

        PermissonUtil.checkPermission(this, new PermissionListener() {
            @Override
            public void havePermission() {

                //激活活体检测
                LivenessService.activeEngine(new LivenessActiveListener() {
                    @Override
                    public void activeSucceed() {
                        toast("激活成功");
                    }

                    @Override
                    public void activeFail(String massage) {
                        Log.d("激活活体检测失败", massage);
                        toast("激活失败：" + massage);
                    }
                });
            }

            @Override
            public void requestPermissionFail() {
                toast("活体检测激活失败");
                finish();
            }
        }, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void setClick() {
        btn_liveness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LivenessActivity.flag = 0;
                startActivity(new Intent(MainActivity.this, LivenessActivity.class));
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LivenessActivity.flag = 1;
                startActivity(new Intent(MainActivity.this, LivenessActivity.class));
            }
        });

        btn_recognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LivenessActivity.flag = 2;
                startActivity(new Intent(MainActivity.this, LivenessActivity.class));
            }
        });

        btn_idCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LivenessActivity.flag = 3;
                PicturePickUtil.pick(MainActivity.this, new OnPickListener() {
                    @Override
                    public void pickPicture(File file) {
                        MainActivity.file = file;
                        startActivity(new Intent(MainActivity.this, LivenessActivity.class));
                    }
                });
            }
        });

        btn_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicturePickUtil.pick(MainActivity.this, new OnPickListener() {
                    @Override
                    public void pickPicture(final File file) {
                        final Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        showDialog();
                        imageView.setImageBitmap(bitmap);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                byte[] pictureData = ImageUtils.getNV21(bitmap);
                                FaceFindService faceFindService = new FaceFindService();
                                faceFindService.setSize(bitmap.getWidth(), bitmap.getHeight());
                                final Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                Canvas canvas = new Canvas(bitmap1);
                                List<AFD_FSDKFace> afd_fsdkFaceList = faceFindService.findFace(pictureData);
                                for (AFD_FSDKFace afd_fsdkFace : afd_fsdkFaceList) {
                                    DrawUtils.drawFaceRect(canvas, afd_fsdkFace.getRect(), Color.GREEN, 4);
                                }
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bitmap1);
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }
        });

    }

    private void showDialog() {
        AlertDialog.Builder di = new AlertDialog.Builder(MainActivity.this);
        di.setCancelable(true);
        imageView = new ImageView(MainActivity.this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        di.setView(imageView);
        di.show();
    }


    public void toast(final String test) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, test, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
