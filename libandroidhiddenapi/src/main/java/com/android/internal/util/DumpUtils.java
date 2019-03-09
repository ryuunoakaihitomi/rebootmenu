package com.android.internal.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.io.PrintWriter;

/**
 * Helper functions for dumping the state of system services.
 */
public final class DumpUtils {

    /**
     * Verify that caller holds {@link android.Manifest.permission#DUMP}.
     *
     * @return true if access should be granted.
     * @hide
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static boolean checkDumpPermission(Context context, String tag, PrintWriter pw) {
        return false;
    }
}
