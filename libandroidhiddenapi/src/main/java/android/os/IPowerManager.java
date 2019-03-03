package android.os;

import android.annotation.TargetApi;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;

/**
 * Android 隐藏私有API 存根
 * <p>
 * -> 28
 */

//IPowerManager.aidl
public interface IPowerManager extends IInterface {

    //16
    @TargetApi(JELLY_BEAN)
    void goToSleep(long time);

    //17 -> 20
    @TargetApi(JELLY_BEAN_MR1)
    void goToSleep(long time, int reason);

    //21+
    @TargetApi(LOLLIPOP)
    void goToSleep(long time, int reason, int flags);

    //17 -> 23
    @TargetApi(JELLY_BEAN_MR1)
    void shutdown(boolean confirm, boolean wait);

    //24+
    @TargetApi(N)
    void shutdown(boolean confirm, String reason, boolean wait);

    @SuppressWarnings({"UnnecessaryInterfaceModifier", "unused"})
    public abstract static class Stub extends Binder implements IPowerManager {

        public static IPowerManager asInterface(IBinder var0) {
            // 备忘录：因为libandroidhiddenapi这个项目是compileOnly进app中的，
            // 所以存根异常就失去了作用（无法抛出），所以一律改为return null。
            /*throw new RuntimeException("Stub!");*/
            return null;
        }
    }
}
