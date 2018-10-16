package com.example.arclibrary.facerecognition;

import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;

import java.util.List;


/**
 * 人脸对比，人脸识别，获取人脸特征
 */
public class FaceRecognitionService {

    private static String APPID;
    private static String FR_SDKKEY;

    //传入的视频数据的长宽
    private int width = 1080, height = 1920;

    //人脸对比引擎
    private AFR_FSDKEngine frEngine;

    public FaceRecognitionService() {
        frEngine = new AFR_FSDKEngine();
        //初始化人脸识别引擎，使用时请替换申请的APPID 和FR_SDKKEY
        AFR_FSDKError error = frEngine.AFR_FSDK_InitialEngine(APPID, FR_SDKKEY);
        Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error.getCode());
    }



    /**
     * 获取人脸特征
     *
     * @param data   图像数据
     * @param rect   人脸位置
     * @param degree 人脸角度
     * @return
     */
    public AFR_FSDKFace faceData(byte[] data, Rect rect, int degree) {
        AFR_FSDKFace afr_fsdkFace = new AFR_FSDKFace();
        frEngine.AFR_FSDK_ExtractFRFeature(data, width, height, AFR_FSDKEngine.CP_PAF_NV21, rect, degree, afr_fsdkFace);
        return afr_fsdkFace;
    }


    /**
     * 人脸对比
     *
     * @param faceData1 人脸特征
     * @param faceData2
     * @return
     */
    public float faceRecognition(byte[] faceData1, byte[] faceData2) {
        //score用于存放人脸对比的相似度值
        AFR_FSDKMatching score = new AFR_FSDKMatching();
        //用来存放提取到的人脸信息, face_1是注册的人脸，face_2是要识别的人脸
        AFR_FSDKFace face1 = new AFR_FSDKFace();
        AFR_FSDKFace face2 = new AFR_FSDKFace();
        face1.setFeatureData(faceData1);
        face2.setFeatureData(faceData2);
        frEngine.AFR_FSDK_FacePairMatching(face1, face2, score);
        return score.getScore();
    }


    /**
     * 人脸搜索
     */
    public void faceSerch(byte[] faceData, List<byte[]> faceDataList, FaceSerchListener listener) {
        AFR_FSDKMatching score = new AFR_FSDKMatching();
        AFR_FSDKFace face1 = new AFR_FSDKFace();
        face1.setFeatureData(faceData);
        AFR_FSDKFace face2 = new AFR_FSDKFace();
        int positon = 0;
        float max = 0.0f;
        for (int i = 0; i < faceDataList.size(); i++) {
            float like = 0.0f;
            face2.setFeatureData(faceDataList.get(i));
            frEngine.AFR_FSDK_FacePairMatching(face1, face2, score);
            like = score.getScore();
            if (like > max) {
                max = like;
                positon = i;
            }
        }
        listener.serchFinish(max, positon);
    }


    public void destroyEngine() {
        frEngine.AFR_FSDK_UninitialEngine();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static String getAPPID() {
        return APPID;
    }

    public static void setAPPID(String APPID) {
        FaceRecognitionService.APPID = APPID;
    }

    public static String getFrSdkkey() {
        return FR_SDKKEY;
    }

    public static void setFrSdkkey(String frSdkkey) {
        FR_SDKKEY = frSdkkey;
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
