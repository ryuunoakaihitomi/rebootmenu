package com.ryuunoakaihitomi.rebootmenu.util.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import androidx.annotation.NonNull;

import static com.ryuunoakaihitomi.rebootmenu.util.DebugLog.LogLevel.D;
import static com.ryuunoakaihitomi.rebootmenu.util.DebugLog.LogLevel.I;

/**
 * 快速生成默认文本烤面包片的工具类
 * Created by ZQY on 2018/2/8.
 *
 * @author ZQY
 * @version 1.2
 * @see android.widget.Toast
 */

@SuppressLint("ShowToast")
@SuppressWarnings("JavaReflectionMemberAccess")
public class TextToast {
    private static final String TAG = "TextToast";

    /**
     * 文本toast生成
     *
     * @param context 上下文
     * @param isLong  是否是持续时间较长的toast
     * @param message 显示的文本内容
     */
    public TextToast(Context context, boolean isLong, String message) {
        toastCompat(context, message, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
    }

    /**
     * 短暂文本toast生成
     *
     * @param context 上下文
     * @param message 文本内容
     */
    public TextToast(Context context, String message) {
        toastCompat(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * toast兼容适配
     *
     * @param context  {@link Context}
     * @param text     文本信息
     * @param duration 持续时间
     */
    private static void toastCompat(Context context, CharSequence text, int duration) {
        boolean isMI = SpecialSupport.isMIUI();
        //MIUI的Toast在文本前面添加应用名称很不合理，因为Toast显示时间有限，需要尽快让用户注意最重要的内容
        Toast toast = Toast.makeText(context.getApplicationContext(), isMI ? null : text, duration);
        if (isMI) toast.setText(text);
        fixBadTokenException(context, toast);
        toast.show();
    }

    /**
     * https://blog.csdn.net/qq331710168/article/details/85320098
     * 伪装成系统Toast以躲避通知权限检查...
     * 调用影响全局
     */
    @SuppressLint("PrivateApi")
    public static void defineSystemToast() {
        try {
            Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
            getServiceMethod.setAccessible(true);
            Object iNotificationManagerObj = getServiceMethod.invoke(null);
            Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
            Object iNotificationManagerProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iNotificationManagerCls}, (proxy, method, args) -> {
                //强制使用系统Toast
                if ("enqueueToast".equals(method.getName())
                        || "enqueueToastEx".equals(method.getName())) {  //华为p20 pro上为enqueueToastEx
                    //enqueueToastEx确认用，暂时没有以上设备
                    new DebugLog(TAG, "methodName:" + method.getName() + " D" + args[2], null);
                    args[0] = "android";
                }
                return method.invoke(iNotificationManagerObj, args);
            });
            Field sServiceField = Toast.class.getDeclaredField("sService");
            sServiceField.setAccessible(true);
            sServiceField.set(null, iNotificationManagerProxy);
        } catch (Exception e) {
            new DebugLog(e, TAG + "defineSystemToast: ", false);
        }
    }

    /**
     * --------------------------------------
     * https://github.com/drakeet/ToastCompat
     */
    private static void fixBadTokenException(Context context, Toast toast) {
        if (Build.VERSION.SDK_INT == 25) {
            try {
                Field field = View.class.getDeclaredField("mContext");
                field.setAccessible(true);
                field.set(toast.getView(), new SafeToastContext(context));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private final static class SafeToastContext extends ContextWrapper {


        SafeToastContext(@NonNull Context base) {
            super(base);
        }


        @Override
        public Context getApplicationContext() {
            return new ApplicationContextWrapper(getBaseContext().getApplicationContext());
        }

        private final class ApplicationContextWrapper extends ContextWrapper {

            private ApplicationContextWrapper(@NonNull Context base) {
                super(base);
            }


            @Override
            public Object getSystemService(@NonNull String name) {
                if (Context.WINDOW_SERVICE.equals(name)) {
                    // noinspection ConstantConditions
                    return new WindowManagerWrapper((WindowManager) getBaseContext().getSystemService(name));
                }
                return super.getSystemService(name);
            }
        }


        private final class WindowManagerWrapper implements WindowManager {

            private final @NonNull
            WindowManager base;


            private WindowManagerWrapper(@NonNull WindowManager base) {
                this.base = base;
            }


            @Override
            public Display getDefaultDisplay() {
                return base.getDefaultDisplay();
            }


            @Override
            public void removeViewImmediate(View view) {
                base.removeViewImmediate(view);
            }


            @Override
            public void addView(View view, ViewGroup.LayoutParams params) {
                try {
                    new DebugLog(TAG, "WindowManager's addView(view, params) has been hooked.", D);
                    base.addView(view, params);
                } catch (BadTokenException e) {
                    new DebugLog(TAG, e.getMessage(), I);
                } catch (Throwable throwable) {
                    new DebugLog(throwable, TAG + "[addView]", true);
                }
            }


            @Override
            public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
                base.updateViewLayout(view, params);
            }


            @Override
            public void removeView(View view) {
                base.removeView(view);
            }
        }
    }
}
