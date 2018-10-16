package com.example.arclibrary.facetrack;

import android.util.Log;

import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;

import java.util.ArrayList;
import java.util.List;


/**
 * 人脸追踪SDK，用于获取人脸的位置和角度
 */
public class FaceTrackService {

    private static String APPID, FT_SDKKEY;

    //传入的视频数据的长宽
    private int width = 1080, height = 1920;

    //人脸追踪SDK
    private AFT_FSDKEngine ftEngine;


    public FaceTrackService() {
        ftEngine = new AFT_FSDKEngine();
        int ftInitErrorCode = ftEngine.AFT_FSDK_InitialFaceEngine(APPID,
                FT_SDKKEY, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT,
                16, 5).getCode();
        if (ftInitErrorCode != 0) {
            Log.d("FT初始化失败，errorcode：" , ftInitErrorCode+"");
        }
    }



    /**
     * 获取人脸的位置角度信息
     *
     * @param data
     * @return
     */
    public List<AFT_FSDKFace> getFtfaces(byte[] data) {
        List<AFT_FSDKFace> ftFaceList = new ArrayList<>();
        //视频FT检测人脸
        int ftCode = ftEngine.AFT_FSDK_FaceFeatureDetect(data, width, height,
                AFT_FSDKEngine.CP_PAF_NV21, ftFaceList).getCode();
        if (ftCode != AFT_FSDKError.MOK) {
            Log.d("FaceTrackService","AFT_FSDK_FaceFeatureDetect: errorcode "+ftCode);
        }
        return ftFaceList;
    }


    //销毁引擎
    public void destoryEngine() {
        ftEngine.AFT_FSDK_UninitialFaceEngine();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static String getAPPID() {
        return APPID;
    }

    public static void setAPPID(String APPID) {
        FaceTrackService.APPID = APPID;
    }

    public static String getFtSdkkey() {
        return FT_SDKKEY;
    }

    public static void setFtSdkkey(String ftSdkkey) {
        FT_SDKKEY = ftSdkkey;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
