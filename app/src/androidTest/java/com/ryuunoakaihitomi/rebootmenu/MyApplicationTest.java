package com.ryuunoakaihitomi.rebootmenu;

import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.test.ContextInit;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class MyApplicationTest {
    private static final String TAG = "MyApplicationTest";

    @Rule
    public ContextInit init = ContextInit.INSTANCE;

    @After
    public void tearDown() throws Exception {
        Log.i(TAG, "tearDown: " + init);
    }

    @Test
    public void emptyMethod() {
        //DO NOT ADD ANY CODE HERE!
    }
}
