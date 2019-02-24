package android.os;

/**
 * This class provides access to the centralized jni bindings for
 * SELinux interaction.
 * {@hide}
 */
public class SELinux {

    /**
     * Determine whether SELinux is disabled or enabled.
     *
     * @return a boolean indicating whether SELinux is enabled.
     */
    public static /*final native*/ boolean isSELinuxEnabled() {
        return false;
    }

    /**
     * Determine whether SELinux is permissive or enforcing.
     *
     * @return a boolean indicating whether SELinux is enforcing.
     */
    public static /*final native*/ boolean isSELinuxEnforced() {
        return false;
    }

    /**
     * Gets the security context of the current process.
     *
     * @return a String representing the security context of the current process.
     */
    public static /*final native*/ String getContext() {
        return null;
    }
}