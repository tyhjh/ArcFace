package com.example.dhht.arcface;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.idcardveri.CompareResult;
import com.arcsoft.idcardveri.DetectFaceResult;
import com.arcsoft.idcardveri.IdCardVerifyError;
import com.arcsoft.idcardveri.IdCardVerifyManager;
import com.arcsoft.liveness.FaceInfo;
import com.example.arclibrary.facerecognition.FaceRecognitionService;
import com.example.arclibrary.facerecognition.FaceSerchListener;
import com.example.arclibrary.liveness.LivenessCheckListener;
import com.example.arclibrary.liveness.LivenessService;
import com.example.arclibrary.util.ImageUtils;
import com.example.dhht.arcface.app.Constants;
import com.example.dhht.arcface.camera.ArcFaceCamera;
import com.example.dhht.arcface.camera.CameraPreviewListener;

import java.util.ArrayList;
import java.util.List;


import permison.PermissonUtil;
import permison.listener.PermissionListener;


public class LivenessActivity extends AppCompatActivity implements CameraPreviewListener {


    public static List<Face> faces = new ArrayList<>();
    SurfaceView surfce_preview, surfce_rect;
    TextView tv_status, tv_name;
    ImageView iv_face;


    //相机的位置
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //相机的方向
    private int cameraOri = 90;
    public static int flag = 0;

    LivenessService livenessService;
    FaceRecognitionService faceRecognitionService;
    boolean isIdCardReady;
    //比对阈值，建议为0.82
    private static final double THRESHOLD = 0.82d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_liveness);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_name = (TextView) findViewById(R.id.tv_name);
        surfce_preview = (SurfaceView) findViewById(R.id.surfce_preview);
        surfce_rect = (SurfaceView) findViewById(R.id.surfce_rect);
        iv_face = (ImageView) findViewById(R.id.iv_face);
        if (flag == 3) {
            //初始化
            int initResult = IdCardVerifyManager.getInstance().init(Constants.IDCARDAPPID, Constants.FRSDKKEY);
            Log.e("LivenessActivity", "init result: " + initResult);
            inputIdCard();
        }

        faceRecognitionService = new FaceRecognitionService();
        livenessService = new LivenessService();

        ArcFaceCamera.getInstance().setCameraPreviewListener(this);
        ArcFaceCamera.getInstance().init(cameraId);

        PermissonUtil.checkPermission(this, new PermissionListener() {
            @Override
            public void havePermission() {
                ArcFaceCamera.getInstance().openCamera(LivenessActivity.this, surfce_preview, surfce_rect);
            }

            @Override
            public void requestPermissionFail() {
                finish();
            }
        }, Manifest.permission.CAMERA);
    }

    private void inputIdCard() {
        Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.file.getPath());
        iv_face.setImageBitmap(bitmap);
        byte[] nv21Data = ImageUtils.getNV21(bitmap);

        DetectFaceResult result = IdCardVerifyManager.getInstance().inputIdCardData(nv21Data, bitmap.getWidth(), bitmap.getHeight());
        Log.e("LivenessActivity", "inputIdCardData result: " + result.getErrCode());
        if (result.getErrCode() == IdCardVerifyError.OK) {
            isIdCardReady = true;
        }
    }

    //开始检测
    public synchronized void detect(final byte[] data, final List<AFT_FSDKFace> fsdkFaces) {


        if (fsdkFaces.size() > 0) {//如果有人脸进行注册、识别
            final AFT_FSDKFace aft_fsdkFace = fsdkFaces.get(0).clone();
            //人脸注册-----------------------------------------------------------------------------------------------------------
            if (flag == 1) {
                flag = -1;
                AFR_FSDKFace afr_fsdkFace = faceRecognitionService.faceData(data, aft_fsdkFace.getRect(), aft_fsdkFace.getDegree());
                faces.add(new Face("用户" + (faces.size() + 1), afr_fsdkFace.getFeatureData()));
                toast("注册成功，姓名为：" + "用户" + faces.size());
                finish();
                //人脸对比----------------------------------------------------------------------------------------------
            } else if (flag == 2) {
                AFR_FSDKFace afr_fsdkFace = faceRecognitionService.faceData(data, aft_fsdkFace.getRect(), aft_fsdkFace.getDegree());

                List<byte[]> faceList = new ArrayList<>();
                for (Face face : faces) {
                    faceList.add(face.getData());
                }

                faceRecognitionService.faceSerch(afr_fsdkFace.getFeatureData(), faceList, new FaceSerchListener() {
                    @Override
                    public void serchFinish(float sorce, int position) {
                        Log.e("LivenessActivity", "sorce：" + sorce + "，position：" + position);
                        if (sorce > 0.7) {
                            tv_name.setText(faces.get(position).getName() + "：相似度：" + sorce);
                            iv_face.setImageBitmap(ImageUtils.cropFace(data, aft_fsdkFace.getRect(), mWidth, mHeight, cameraOri));
                        } else {
                            tv_name.setText("");
                        }
                    }
                });
                flag = -1;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            flag = 2;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                //人证识别----------------------------------------------------------------------------------------------
            } else if (flag == 3) {//人证识别
                if (isIdCardReady) {
                    DetectFaceResult result = IdCardVerifyManager.getInstance().onPreviewData(data, mWidth, mHeight, true);
                    if (result.getErrCode() == IdCardVerifyError.OK) {
                        Log.e("LivenessActivity", "onPreviewData video result: " + result);
                        compare();
                        flag = -1;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    inputIdCard();
                                    Thread.sleep(500);
                                    flag = 3;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        }


        //活体检测------------------------------------------------------------------------------------------------------------------------------
        List<FaceInfo> faceInfos = new ArrayList<>();
        if (fsdkFaces.size() > 0) {
            faceInfos.add(new FaceInfo(fsdkFaces.get(0).getRect(), fsdkFaces.get(0).getDegree()));
        }

        livenessService.isLive(faceInfos, data.clone(), livenessCheckListener);


    }


    LivenessCheckListener livenessCheckListener = new LivenessCheckListener() {
        @Override
        public void noFace() {
            tv_status.setText("没有人脸");
        }

        @Override
        public void notSignleFace() {
            //tv_status.setText("人脸太多");
        }

        @Override
        public void liveness() {
            tv_status.setText("活体通过");
        }

        @Override
        public void livenessNot() {
            tv_status.setText("非活体");
        }

        @Override
        public void unknownEorr() {
            tv_status.setText("未知错误");
        }
    };


    private void compare() {

        CompareResult compareResult = IdCardVerifyManager.getInstance().compareFeature(THRESHOLD);
        Log.e("LivenessActivity", "compareFeature: result " + compareResult.getResult() + ", isSuccess "
                + compareResult.isSuccess() + ", errCode " + compareResult.getErrCode());

        if (compareResult.isSuccess()) {
            tv_name.setText("相似度为：" + compareResult.getResult());
        }
    }


    public void toast(final String test) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LivenessActivity.this, test, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        livenessService.destoryEngine();
        faceRecognitionService.destroyEngine();
        IdCardVerifyManager.getInstance().unInit();
    }

    @Override
    public void onPreviewData(byte[] data, List<AFT_FSDKFace> fsdkFaces) {
        detect(data, fsdkFaces);
    }

    int mWidth, mHeight;

    @Override
    public void onPreviewSize(int width, int height) {
        mHeight = height;
        mWidth = width;
        livenessService.setSize(width, height);
        faceRecognitionService.setSize(width, height);
    }
}
