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

public interface IPowerManager extends IInterface {

    //16
    @TargetApi(JELLY_BEAN)
    void goToSleep(long time) throws RemoteException;

    //17 -> 20
    @TargetApi(JELLY_BEAN_MR1)
    void goToSleep(long time, int reason) throws RemoteException;

    //21+
    @TargetApi(LOLLIPOP)
    void goToSleep(long time, int reason, int flags) throws RemoteException;

    //17 -> 23
    @TargetApi(JELLY_BEAN_MR1)
    void shutdown(boolean confirm, boolean wait);

    //24+
    @TargetApi(N)
    void shutdown(boolean confirm, String reason, boolean wait);

    @SuppressWarnings({"UnnecessaryInterfaceModifier", "unused"})
    public abstract static class Stub extends Binder implements IPowerManager {

        public static IPowerManager asInterface(IBinder var0) {
            throw new RuntimeException("Stub!");
        }
    }
}
