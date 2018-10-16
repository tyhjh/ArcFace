package com.example.dhht.arcface.app;

import android.app.Application;

import com.example.arclibrary.builder.AcrFaceManagerBuilder;
import com.yorhp.picturepick.PicturePickUtil;


public class MyApplication extends Application {

    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        initArcFace();
        PicturePickUtil.init("com.yorhp.arcface.fileProvider");
    }

    private void initArcFace() {

        new AcrFaceManagerBuilder().setContext(this)
                .setFreeSdkAppId(Constants.FREESDKAPPID)
                .setFdSdkKey(Constants.FDSDKKEY)
                .setFtSdkKey(Constants.FTSDKKEY)
                .setFrSdkKey(Constants.FRSDKKEY)
                .setLivenessAppId(Constants.LIVENESSAPPID)
                .setLivenessSdkKey(Constants.LIVENESSSDKKEY)
                .create();
    }

}



