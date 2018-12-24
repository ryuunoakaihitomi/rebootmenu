package android.os;

/**
 * Android 隐藏私有API 存根
 * <p>
 * -> 28
 */

public final class ServiceManager {

    @SuppressWarnings("unused")
    public static IBinder getService(String name) {
        throw new RuntimeException("Stub!");
    }
}
