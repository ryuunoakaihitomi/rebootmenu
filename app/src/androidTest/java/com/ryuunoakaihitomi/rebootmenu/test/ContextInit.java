package com.ryuunoakaihitomi.rebootmenu.test;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;

/**
 * 单元测试取Context
 * Created by ZQY on 2019/8/18.
 *
 * @author ZQY
 */

public enum ContextInit implements TestRule {

    INSTANCE;

    private static final String TAG = "ContextInit";

    private static Context ctx, appCtx;
    private Instrumentation instrumentation;

    /**
     * {@link InstrumentationRegistry#getInstrumentation()}
     * {@link Instrumentation#getContext()}
     *
     * @return {@link Context}
     */
    public static @NonNull
    Context ctx() {
        assertNotNull(ctx);
        return ctx;
    }

    /**
     * {@link InstrumentationRegistry#getInstrumentation()}
     * {@link Instrumentation#getTargetContext()}
     *
     * @return {@link Context}
     */
    public static @NonNull
    Context appCtx() {
        assertNotNull(appCtx);
        return appCtx;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (instrumentation == null) {
            Log.v(TAG, "apply: Load instrumentation.");
            instrumentation = InstrumentationRegistry.getInstrumentation();
        }
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                if (ctx == null || appCtx == null) {
                    Log.v(TAG, "apply: Preload context.");
                    ctx = instrumentation.getContext();
                    appCtx = instrumentation.getTargetContext();
                }
                base.evaluate();
            }
        };
    }

    @Override
    public String toString() {
        return "ContextInit{" +
                "ctx=" + ctx +
                ", appCtx=" + appCtx +
                ", instrumentation=" + instrumentation +
                '}';
    }

}
