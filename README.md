# Android集成虹软人脸、人证对比，活体检测

标签（空格分隔）： Android

---
此文不会更新，建议阅读原文：https://www.jianshu.com/p/8dee89ec4a24

#### 友情提示
> 只适用于v1.2版本，现在官网人脸识别最新为v2.0，因为申请的时候下载的so库和给的APPKEY是关联的，所以一定要把so库和Constants类下面的各种APPKEY和APPID换成自己的才能正常运行，第一次激活活体检测应该是需要联网的；额，活体检测只能检测电子图片，打印图片不行，虹软的SDK还是有问题的

最近虹软新增了**人证识别**、**活体检测**的功能，好像之前的人脸识别也更新过版本，之前一篇文章[用虹软Android SDK做人脸识别](https://www.jianshu.com/p/16dbde19cb39)，写过虹软人脸识别的用法，最近把**人脸识别**、**人证识别**，**活体检测**功能都简单的封装了一下，使用起来可以更简单一点；但是由于appkey是和so库绑定的，所以不能直接依赖，需要下载项目换成自己的so库就能使用或者发布了，还是挺方便的

### 虹软人脸识别库的介绍
#### so库和appkey是绑定的
以人脸识别为例，它包括**人脸检测、人脸追踪、人脸识别、年龄识别、性别识别**这5种引擎，每个引擎都有一个**so库和jar包**，申请的5种**AppKey**和**APPID**是和自己下载的so库是绑定的，不能混淆使用

#### 人脸检测(FD)
用于获取静态图片的人脸的位置和角度，传入格式为**NV21**的图片数据(byte[])，返回一个**AFD_FSDKFace**对象的集合，**AFD_FSDKFace**只储存了一个位置和角度；如果用于视频流里面好像也不报错

```java
public class AFD_FSDKFace {
    Rect mRect;
    int mDegree;
    ...
```

#### 人脸追踪(FT)
和人脸检测一样，也是用来获取人脸的位置和角度，不过只适用于获取视频流的人脸，也就是在相机的**onPreviewFrame**方法里面使用，返回的是**AFT_FSDKFace**对象的集合，也只储存了一个位置和角度；如果用于静态图片好像是会报错的

```java
public class AFT_FSDKFace {
    Rect mRect;
    int mDegree;
    ...
```

#### 人脸识别(FR)
用于获取人脸特征和对比人脸特征的
获取人脸特征，需要传入格式为**NV21**的图片数据(byte[])和人脸的位置、人脸的角度，所以需要先用前面的引擎获取到人脸的信息,返回一个**AFR_FSDKFace**对象，这个对象也只保存了人脸特征(byte[])
对比人脸，需要传入两个**AFR_FSDKFace**对象，返回一个**AFR_FSDKMatching**对象，只保存了相似度

```java
public class AFR_FSDKFace {
    public static final int FEATURE_SIZE = 22020;
    byte[] mFeatureData;
    ...
    
public class AFR_FSDKMatching {
    float mScore = 0.0F;
    ...
```

#### 活体检测
活体检测是检测是不是活人的，也是传入人脸的位置、人脸的角度，又是一个新的**FaceInfo**对象，传入的是FaceInfo的集合，返回**LivenessInfo**集合，但是目前只支持单人脸我们只管第一个数据，**LivenessInfo**里面保存的返回的结果，活体、非活体、人脸超过一个、未知错误(经常返回，问题不大)


#### 人证对比
用来对比人脸和身份证的，传入传入格式为**NV21**的证件照片(byte[])和人脸的照片，还有各自的图片大小和比对阈值；返回一个**CompareResult**对象，包括相似度、是否成功等信息
```java
public class CompareResult {
    private boolean isSuccess;
    private double result;
    ...
```
人证识别其实是人脸识别的那几个引擎（FD，FT，FR）的集合，所以有同时集成肯定包冲突了，可以使用人脸识别的so库，然后把人脸识别的jar包都删了，使用人证的jar包，人证的激活码使用**FR**的激活码就行了
![屏幕快照 2018-10-11 下午3.41.05.png-15.5kB][1]

其他的年龄、性别的引擎应该都差不多

### 封装后的部分功能的展示

#### 初始化AppKey和APPID
```java
new AcrFaceManagerBuilder().setContext(this)
                .setFreeSdkAppId(Constants.FREESDKAPPID)
                .setFdSdkKey(Constants.FDSDKKEY)
                .setFtSdkKey(Constants.FTSDKKEY)
                .setFrSdkKey(Constants.FRSDKKEY)
                .setLivenessAppId(Constants.LIVENESSAPPID)
                .setLivenessSdkKey(Constants.LIVENESSSDKKEY)
                .create();
    }
```

#### 相机预览追踪人脸位置
```java
//初始化人脸追踪引擎
FaceTrackService faceTrackService = new FaceTrackService();
//设置传入的图片的大小
faceTrackService.setSize(previewSize.width, previewSize.height);
 camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    //获取人脸的位置信息
                    List<AFT_FSDKFace> fsdkFaces = faceTrackService.getFtfaces(data);
                    //画出人脸的位置
                    drawFaceRect(fsdkFaces);
                    //输出数据进行其他处理
                    cameraPreviewListener.onPreviewData(data.clone(), fsdkFaces);
                    ...
                }
            });
```
相机自己实现，获取人脸位置的代码非常简单，就一句代码，画出人脸的位置实现是用了两个surfaceView，一个用于相机画面展示，另一个画出人脸的位置

#### 画出人脸的位置
值得注意的是获取的人脸的位置Rect是传入的图片的相对位置，图片大小是相机预览设置的大小，画的时候是画在了surfaceView上面，surfaceView一般和预览大小是不一样的，而且还要考虑画面是否旋转、相机的位置等，所以需要先进行转换
```java
Rect rect1=DrawUtils.adjustRect(rect, previewSizeX, previewSizeY,canvas.getWidth(), canvas.getHeight(), cameraOri, cameraId);
```

#### 获取人脸特征进行注册
```java
//初始化人脸识别引擎
FaceRecognitionService faceRecognitionService = new FaceRecognitionService();
faceRecognitionService.setSize(width, height);
//获取人脸特征
AFR_FSDKFace afr_fsdkFace =faceRecognitionService.faceData(data, aft_fsdkFace.getRect(), aft_fsdkFace.getDegree());
tv_status.setText("人脸特征为：" + afr_fsdkFace.getFeatureData());
```
**aft_fsdkFace**为上一步获取的人脸的位置信息

#### 相机获取的人脸和已保存的人脸进行对比
```java
//获取保存的人脸特征
byte[] faceData=faces.get(0).getData();
//对比人脸特征
float socre=faceRecognitionService.faceRecognition(afr_fsdkFace.getFeatureData(),faceData);
tv_status.setText("相似度为：" + sorce);
```
**afr_fsdkFace**为上一步获取的人脸的特征，**faceData**为已保存的人脸特征，也有提供一个人脸和多个对比获取相似度最高的一个的方法


#### 活体检测
```java
 //激活活体检测
LivenessService.activeEngine(new LivenessActiveListener() {
    @Override
    public void activeSucceed() {
        toast("激活成功");
    }

    @Override
    public void activeFail(String massage) {
        LogUtils.log(massage);
        toast("激活失败：" + massage);
    }
});
LivenessService livenessService = new LivenessService();
//
List<FaceInfo> faceInfos = new ArrayList<>();
faceInfos.add(new FaceInfo(aft_fsdkFace.getRect(), aft_fsdkFace.getDegree()));
//判断是否是活体
boolean isLive=livenessService.isLive(faceInfos,data);
```
**aft_fsdkFace**为上一步获取的人脸的位置信息，第一次激活好像需要联网

#### 人证对比
```java
//初始化
IdCardVerifyManager.getInstance().init(Constants.IDCARDAPPID, Constants.FRSDKKEY);
//bitmap转NV21数据
byte[] nv21Data = ImageUtils.getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
//传入证件照片
DetectFaceResult result = IdCardVerifyManager.getInstance().inputIdCardData(nv21Data, bitmap.getWidth(), bitmap.getHeight());
//传入相机获取的人脸数据
DetectFaceResult result = IdCardVerifyManager.getInstance().onPreviewData(data, mWidth, mHeight, true);
//对比相似度
CompareResult compareResult = IdCardVerifyManager.getInstance().compareFeature(THRESHOLD);
if (compareResult.isSuccess()) {
    tv_status.setText("相似度为：" + compareResult.getResult());
}
```
证件图片是本地的，转成了NV21的格式的byte数组，方法也集成了

#### 图片获取人脸特征
```java
//先把bitmap转NV21格式
byte[] photoData = ImageUtils.getNV21(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1);
//获取人脸位置信息
List<AFD_FSDKFace> afd_fsdkFaces = faceFindService.findFace(photoData);
for (AFD_FSDKFace afdFsdkFace : afd_fsdkFaces) {
//获取每一个人脸的特征
AFR_FSDKFace afr_fsdkFace = faceRecognitionService.faceData(photoData, afdFsdkFace.getRect(), afdFsdkFace.getDegree());
}
```
#### 引擎释放
```java
livenessService.destoryEngine();
faceTrackService.destoryEngine();
faceRecognitionService.destroyEngine();
IdCardVerifyManager.getInstance().unInit();
```

效果都还不错，主要是全部免费，下载源码，替换so库和AppKey、sdkKey，才能运行，可以查看所有功能

![屏幕快照 2018-10-11 下午4.50.30.png-32.4kB][2]

demo下载地址：http://oy5r220jg.bkt.clouddn.com/arcface_v1.0.3.apk

项目地址：https://github.com/tyhjh/Arcface


  [1]: http://static.zybuluo.com/Tyhj/1y4h9mbk7xlf9urpx18oseeq/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202018-10-11%20%E4%B8%8B%E5%8D%883.41.05.png
  [2]: http://static.zybuluo.com/Tyhj/knlu9x9hl1dokgqjvd8l3koe/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202018-10-11%20%E4%B8%8B%E5%8D%884.50.30.png
