package android.os;

/**
 * Controls and utilities for low-level {@code init} services.
 *
 * @hide
 */
@SuppressWarnings("unused")
public class SystemService {

    /**
     * Request that the init daemon restart a named service.
     */
    public static void restart(String name) {
        throw new RuntimeException("Stub!");
    }
}