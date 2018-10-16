package com.example.arclibrary.liveness;

public interface LivenessActiveListener {
    //激活成功
    void activeSucceed();

    //激活失败
    void activeFail(String massage);
}
