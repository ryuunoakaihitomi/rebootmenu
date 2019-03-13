package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.ryuunoakaihitomi.rebootmenu.MyApplication;
import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.RootMode;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * 对A广告组件的二次封装，方便以后更换及调整
 * Created by ZQY on 2018/6/3.
 */

public class AdImpl {
    private static final String TAG = "AdImpl";

    //AdMob应用密钥
    private static final String AD_APP_KEY = "ca-app-pub-3887749646735208~8115523935";

    //AdMob广告单元密钥
    private static final String InterstitialAdUnitKey = "ca-app-pub-3887749646735208/2003920462";
    //发布ID
    private static final String AdViewUnitKey = "ca-app-pub-3887749646735208/6916393844";

    //测试用广告ID
    private static final String DEBUG_INTERSTITIAL_AD_UNIT_KEY = "ca-app-pub-3940256099942544/1033173712";
    private static final String DEBUG_AD_VIEW_UNIT_KEY = "ca-app-pub-3940256099942544/6300978111";

    //AD状态码表
    private static final String[] adStatusCode =
            {"ERROR_CODE_INTERNAL_ERROR", "ERROR_CODE_NETWORK_ERROR", "ERROR_CODE_INVALID_REQUEST", "ERROR_CODE_NO_FILL",
                    "LISTENER_ON_AD_OPENED", "LISTENER_ON_AD_CLICKED", "SIGNAL_NO_AD_AUTO"};
    //AD操作对象
    private static AdView adView;
    private static InterstitialAd interstitialAd;
    private static AdRequest adRequest;

    //初始化广告组件
    public static void initialize(Activity activity) {
        if (!MainCompat.shouldLoadCSCModule()) return;
        // ConfigManager.set() 有可能有权限建立目录却没权删除目录
        if (adSwitchGetter(activity)) {
            hideBottomUIMenu(activity);
            //加载广告背景
            activity.setContentView(R.layout.ad_background);
            boolean isRootMode = activity.getClass().equals(RootMode.class);
            new DebugLog("isRootMode:" + isRootMode);
            MobileAds.initialize(activity, AD_APP_KEY);
            //增加横幅广告视图
            LinearLayout adContainer = activity.findViewById(R.id.layout);
            adView = new AdView(activity);
            adView.setAdSize(AdSize.SMART_BANNER);
            //至于View中顶层
            adView.bringToFront();
            //只有在root模式才有必要初始化插页广告
            if (isRootMode)
                //初始化插页广告
                interstitialAd = new InterstitialAd(activity);
            //测试广告请求
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            if (MyApplication.isDebug) {
                adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                //横幅广告测试
                adView.setAdUnitId(DEBUG_AD_VIEW_UNIT_KEY);
                if (isRootMode)
                    //插页广告测试
                    interstitialAd.setAdUnitId(DEBUG_INTERSTITIAL_AD_UNIT_KEY);
            } else {
                adView.setAdUnitId(AdViewUnitKey);
                if (isRootMode)
                    interstitialAd.setAdUnitId(InterstitialAdUnitKey);
            }
            adContainer.addView(adView);
            //addView之后要重新给子view设置宽高。
            adView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            adContainer.setGravity(Gravity.BOTTOM);
            adRequest = adRequestBuilder.build();
            if (isRootMode) {
                interstitialAd.loadAd(adRequest);
                //插页监听
                interstitialAd.setAdListener(new AdListener() {

                    @Override
                    public void onAdClicked() {
                        EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[5]);
                    }

                    @Override
                    public void onAdOpened() {
                        adSwitchSetter(activity, false);
                        EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[4]);
                        new TextToast(activity.getApplicationContext(), true, activity.getString(R.string.ad_opened_toast), true);
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        adErrorCodeReport(i);
                        //清掉背景监听
                        activity.setContentView(new View(activity));
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    }

                    @Override
                    public void onAdClosed() {
                        // Load the next interstitial.
                        interstitialAd.loadAd(new AdRequest.Builder().build());
                        activity.finish();
                    }
                });
            }
            //横幅监听
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClicked() {
                    EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[5]);
                }

                @Override
                public void onAdOpened() {
                    adSwitchSetter(activity, false);
                    EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[4]);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    adErrorCodeReport(errorCode);
                    //在免root模式下使用，在root模式下会报view not added异常。在返回键退出设置为假的时候使用，防止窗口无退出按钮亦凝滞
                    if (!isRootMode && !ConfigManager.get(ConfigManager.CANCELABLE)) {
                        //删除广告背景
                        activity.setContentView(new View(activity));
                        //取消去焦点
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    }
                }
            });
        } else {
            double random = Math.random();
            String log = "AdReshowProbability r:" + random;
            if (random < 0.2) adSwitchSetter(activity, true);
            DebugLog.i(TAG, log);
            EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[6]);
        }
    }

    //显示插页广告
    public static void showInterstitialAd(Activity activity) {
        //广告 展示
        if (interstitialAd != null && interstitialAd.isLoaded())
            interstitialAd.show();
        else new DebugLog("The interstitial wasn't loaded yet.");
        activity.finish();
    }

    //统计广告错误信息
    private static void adErrorCodeReport(int errorCode) {
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[0]);
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[1]);
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[2]);
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                EventStatistics.record(EventStatistics.AD_STATUS_CODE, adStatusCode[3]);
                break;
        }
    }

    //显示横幅广告
    public static void showAdView() {
        if (adView != null) adView.loadAd(adRequest);
    }

    private static void adSwitchSetter(Context context, boolean val) {
        ConfigManager.setPrivateBoolean(context, ConfigManager.AUTO_NOAD, !val);
    }

    private static boolean adSwitchGetter(Context context) {
        return !ConfigManager.getPrivateBoolean(context, ConfigManager.AUTO_NOAD, false);
    }

    /**
     * 设置去焦点Flag，使广告在对话框后亦可点击
     *
     * @param dialog 对话框
     */
    public static void setFlagNotFocusable(Dialog dialog) {
        //去焦点，设置外部可点击，使得adView可点击
        if (adView != null)
            //无需更改，返回键设置本来就不对二次确认生效
            //https://blog.csdn.net/ID19870510/article/details/50828501
            //https://blog.csdn.net/u012255016/article/details/49888881
            //??? mainDialogCreate.setCanceledOnTouchOutside(true);
            Objects.requireNonNull(dialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /**
     * 隐藏虚拟按键，并且全屏，使广告不受遮挡
     *
     * @param activity a
     */
    @SuppressLint("ObsoleteSdkInt")
    private static void hideBottomUIMenu(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            //https://developer.android.com/training/system-ui/navigation
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
