package com.ryuunoakaihitomi.rebootmenu.util;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.test.filters.RequiresDevice;
import androidx.test.filters.SmallTest;

import com.ryuunoakaihitomi.rebootmenu.test.ContextInit;
import com.ryuunoakaihitomi.rebootmenu.test.ExecBorder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SmallTest
public class SpecialSupportTest {
    private static final String TAG = "SpecialSupportTest";

    @Rule
    public ExecBorder execBorder = new ExecBorder();

    @Rule
    public ContextInit contextInit = ContextInit.INSTANCE;

    @Before
    public void setUp() throws Exception {
        Log.d(TAG, "setUp: MODEL:" + Build.MODEL + " BRAND:" + Build.BRAND);
    }

    @Test
    public void isAndroidTV() {
        assertEquals(SpecialSupport.hasTvFeature(ContextInit.ctx()), SpecialSupport.isAndroidTV(ContextInit.ctx()));
    }

    /**
     * 没有的组件：
     * DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
     * Settings.ACTION_ACCESSIBILITY_SETTINGS   （可以手动打开）
     */
    @Test(expected = AssertionError.class)
    @RequiresDevice
    public void checkTVSupport() {
        if (SpecialSupport.isAndroidTV(ContextInit.ctx())) {
            assertThat(false, allOf(
                    equalTo(isActivityExists(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)),
                    equalTo(isActivityExists(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
        } else fail("Not a TV device.");
    }

    private boolean isActivityExists(String action) {
        Intent i = new Intent(action);
        return i.resolveActivity(ContextInit.ctx().getPackageManager()) != null;
    }
}