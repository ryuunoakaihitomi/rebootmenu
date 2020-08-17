package github.ryuunoakaihitomi.powerpanel;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class BlackMagic {
    private static final String TAG = "BlackMagic";

    private BlackMagic() {
    }

    // 在受限模式中，用户屏蔽通知的同时可能会导致toast无法显示
    public static void toastBugsFix() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
                Method getService = Toast.class.getDeclaredMethod("getService");
                getService.setAccessible(true);
                // Do not be inline to avoid endless loop.
                final Object iNotificationManager = getService.invoke(null);
                @SuppressLint("PrivateApi")
                Object iNotificationManagerProxy = Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{Class.forName("android.app.INotificationManager")},
                        (proxy, method, args) -> {
                            if ("enqueueToast".equals(method.getName())) {
                                // duration: LENGTH_SHORT = 0; LENGTH_LONG = 1;
                                Log.d(TAG, "toastBugsFix: pkg = " + args[0] + ", duration = " + args[2]);
                                args[0] = "android";
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                    Object tn = args[1];
                                    Field mHandler = tn.getClass().getDeclaredField("mHandler");
                                    mHandler.setAccessible(true);
                                    final class HandlerProxy extends Handler {
                                        private final Handler mHandler;

                                        public HandlerProxy(Handler handler) {
                                            mHandler = handler;
                                        }

                                        @Override
                                        public void handleMessage(Message msg) {
                                            // SHOW = 0; HIDE = 1; CANCEL = 2;
                                            Log.d(TAG, "toastBugsFix: handleMessage action = " + msg.what);
                                            try {
                                                mHandler.handleMessage(msg);
                                            } catch (WindowManager.BadTokenException e) {
                                                Log.i(TAG, "toastBugsFix: handleMessage BTE caught!", e);
                                            }
                                        }
                                    }
                                    mHandler.set(tn, new HandlerProxy((Handler) mHandler.get(tn)));
                                }
                            }
                            return method.invoke(iNotificationManager, args);
                        });
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field sService = Toast.class.getDeclaredField("sService");
                sService.setAccessible(true);
                sService.set(null, iNotificationManagerProxy);
            } catch (Exception e) {
                Log.e(TAG, null, e);
            }
        }
    }

    /**
     * 使AlertDialog的信息可交互
     * 注意：系统自带的{@link android.app.AlertDialog}已经不允许这样做，必须使用AppCompat里的。
     *
     * @param alertDialog {@link AlertDialog}
     */
    @SuppressWarnings("ConstantConditions")
    public static @NonNull
    TextView getAlertDialogMessageView(AlertDialog alertDialog) {
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(alertDialog);
            Field mMessageView = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessageView.setAccessible(true);
            return (TextView) mMessageView.get(mAlertController);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
