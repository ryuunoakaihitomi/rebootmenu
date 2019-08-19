package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.test.ContextInit;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GmsApiWrapperTest {
    private static final String TAG = "GmsApiWrapperTest";

    @Rule
    public ContextInit init = ContextInit.INSTANCE;

    @Test
    public void fetchStateString() {
        String description = GmsApiWrapper.fetchStateString(ContextInit.appCtx());
        assertNotNull(description);
        Log.d(TAG, "fetchStateString: " + description);
    }
}
