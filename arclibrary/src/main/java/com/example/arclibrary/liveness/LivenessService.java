package com.example.arclibrary.liveness;

import android.util.Log;

import com.arcsoft.liveness.ErrorInfo;
import com.arcsoft.liveness.FaceInfo;
import com.arcsoft.liveness.LivenessEngine;
import com.arcsoft.liveness.LivenessInfo;
import com.example.arclibrary.manager.ArcFaceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;



/**
 * 活体检测
 */
public class LivenessService {

    LivenessEngine arcFaceEngine;

    private static String APPID, LIVENESS_SDKKEY;

    //传入的视频数据的长宽
    private int width = 1080, height = 1920;


    public LivenessService() {
        initEngine();
    }

    /**
     * @param activeEngineListener
     */
    public static void activeEngine(final LivenessActiveListener activeEngineListener) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //活体检测引擎
                LivenessEngine livenessEngine = new LivenessEngine();
                long activeCode = livenessEngine.activeEngine(ArcFaceManager.getContext(), APPID, LIVENESS_SDKKEY).getCode();
                if (activeEngineListener == null)
                    return;
                if (activeCode == ErrorInfo.MOK) {
                    activeEngineListener.activeSucceed();
                } else if (activeCode == ErrorInfo.MERR_AL_BASE_ALREADY_ACTIVATED) {
                    activeEngineListener.activeSucceed();
                } else {
                    activeEngineListener.activeFail("活体引擎激活失败，errorcode：" + activeCode);
                }
            }
        });
    }

    /**
     * 活体引擎初始化(视频)
     */
    private void initEngine() {
        arcFaceEngine = new LivenessEngine();
        ErrorInfo error = arcFaceEngine.initEngine(ArcFaceManager.getContext(), LivenessEngine.AL_DETECT_MODE_VIDEO);
        if (error.getCode() != 0) {
            Log.d("活体初始化失败，errorcode：" , error.getCode()+"");
        }
    }

    /**
     * 检测是否为活体
     *
     * @param faceInfos 传入的人脸位置信息数据，通过人脸检测API获取
     * @param data      传入的NV21数据
     * @return
     */
    public void isLive(List<FaceInfo> faceInfos, byte[] data, LivenessCheckListener listener) {
        //活体检测(目前只支持单人脸，且无论有无人脸都需调用)
        List<LivenessInfo> livenessInfos = new ArrayList<>();
        ErrorInfo livenessError = arcFaceEngine.startLivenessDetect(data, width, height,
                LivenessEngine.CP_PAF_NV21, faceInfos, livenessInfos);
        if (livenessError.getCode() == ErrorInfo.MOK) {
            if (livenessInfos.size() == 0) {
                listener.noFace();
                return;
            }
            final int liveness = livenessInfos.get(0).getLiveness();
            if (liveness == LivenessInfo.NOT_LIVE) {
                listener.livenessNot();
            } else if (liveness == LivenessInfo.LIVE) {
                listener.liveness();
            } else if (liveness == LivenessInfo.MORE_THAN_ONE_FACE) {
                listener.notSignleFace();
            } else {
                listener.unknownEorr();
            }
        }
    }


    public boolean isLive(List<FaceInfo> faceInfos, byte[] data) {
        //活体检测(目前只支持单人脸，且无论有无人脸都需调用)
        List<LivenessInfo> livenessInfos = new ArrayList<>();
        ErrorInfo livenessError = arcFaceEngine.startLivenessDetect(data, width, height,
                LivenessEngine.CP_PAF_NV21, faceInfos, livenessInfos);
        if (livenessError.getCode() == ErrorInfo.MOK) {
            if (livenessInfos.size() != 0) {
                int liveness = livenessInfos.get(0).getLiveness();
                if (liveness == LivenessInfo.LIVE) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * 销毁引擎
     */
    public void destoryEngine() {
        arcFaceEngine.unInitEngine();
    }


    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static String getAPPID() {
        return APPID;
    }

    public static void setAPPID(String APPID) {
        LivenessService.APPID = APPID;
    }

    public static String getLivenessSdkkey() {
        return LIVENESS_SDKKEY;
    }

    public static void setLivenessSdkkey(String livenessSdkkey) {
        LIVENESS_SDKKEY = livenessSdkkey;
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
