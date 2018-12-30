package android.os;

/**
 * Android 隐藏私有API 存根
 * <p>
 * -> 28
 */

public final class ServiceManager {

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get
     * @return a reference to the service, or <code>null</code> if the service doesn't exist
     */
    @SuppressWarnings("unused")
    public static IBinder getService(String name) {
        throw new RuntimeException("Stub!");
    }
}
