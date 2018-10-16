package com.example.arclibrary.liveness;

public interface LivenessCheckListener {
    //没有人脸数据
    void noFace();

    //非单人脸信息
    void notSignleFace();

    //活体
    void liveness();

    //非活体
    void livenessNot();

    //未知错误
    void unknownEorr();


}
