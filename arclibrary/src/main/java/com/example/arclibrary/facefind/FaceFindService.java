package com.example.arclibrary.facefind;

import android.util.Log;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;

import java.util.ArrayList;
import java.util.List;


/**
 * 人脸检测，用于获取人脸的位置和角度，用于静态图片
 */
public class FaceFindService {

    private static String APPID;
    private static String FD_SDKKEY;

    //传入的视频数据的长宽
    private int width = 1080, height = 1920;

    private AFD_FSDKEngine fdEngine;

    public FaceFindService() {
        fdEngine = new AFD_FSDKEngine();
        //初始化人脸检测引擎，使用时请替换申请的APPID和FD_SDKKEY
        AFD_FSDKError err = fdEngine.AFD_FSDK_InitialFaceEngine(APPID, FD_SDKKEY, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
        Log.d("FaceFindService", "AFD_FSDK_InitialFaceEngine = "+err.getCode());
    }

    public List<AFD_FSDKFace> findFace(byte[] data) {
        // 用来存放检测到的人脸信息列表
        List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
        if (fdEngine != null) {
            fdEngine.AFD_FSDK_StillImageFaceDetection(data, width, height, AFD_FSDKEngine.CP_PAF_NV21, result);
        }
        return result;
    }

    public void destroyEngine() {
        fdEngine.AFD_FSDK_UninitialFaceEngine();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public static String getAPPID() {
        return APPID;
    }

    public static void setAPPID(String APPID) {
        FaceFindService.APPID = APPID;
    }

    public static String getFdSdkkey() {
        return FD_SDKKEY;
    }

    public static void setFdSdkkey(String fdSdkkey) {
        FD_SDKKEY = fdSdkkey;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
