package android.hardware.display;

/**
 * Manager communication with the display manager service on behalf of
 * an application process.  You're probably looking for {@link DisplayManager}.
 *
 * @hide
 */
public final class DisplayManagerGlobal {

    /**
     * Gets an instance of the display manager global singleton.
     *
     * @return The display manager instance, may be null early in system startup
     * before the display manager has been fully initialized.
     */
    public static DisplayManagerGlobal getInstance() {
        return null;
    }

    /**
     * Set the level of color saturation to apply to the display.
     */
    public void setSaturationLevel(float level) {
    }
}