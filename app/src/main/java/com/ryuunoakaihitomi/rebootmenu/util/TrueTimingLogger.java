package com.ryuunoakaihitomi.rebootmenu.util;

import android.util.Log;
import android.util.TimingLogger;

import com.ryuunoakaihitomi.rebootmenu.util.hook.ReflectionOnPie;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * 不受限制永远可输出的TimingLogger
 * Created by ZQY on 2018/10/29.
 * <p>
 * https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/util/TimingLogger.java
 */

public class TrueTimingLogger {

    private static final String TAG = "TrueTimingLogger";
    private Field mDisabledField, mSplitsField, mSplitLabelsField, mTagField, mLabelField;
    private final TimingLogger logger;

    /**
     * Create and initialize a TimingLogger object that will log using
     * the specific tag.
     *
     * @param tag   the log tag to use while logging the timings
     * @param label a string to be displayed with each log
     */
    public TrueTimingLogger(String tag, String label) {
        ReflectionOnPie.clearClassLoaderInClass(TrueTimingLogger.class);
        logger = new TimingLogger(tag, label);
        initFields();
        reset(tag, label);
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the specific tag.
     *
     * @param tag   the log tag to use while logging the timings
     * @param label a string to be displayed with each log
     */
    public void reset(String tag, String label) {
        setField(mTagField, tag);
        setField(mLabelField, label);
        reset();
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the tag and label that was specified previously, either via
     * the constructor or a call to reset(tag, label).
     */
    @SuppressWarnings("ConstantConditions")
    public void reset() {
        if (getField(mSplitsField) == null) {
            setField(mSplitsField, new ArrayList<Long>());
            setField(mSplitLabelsField, new ArrayList<String>());
        } else {
            try {
                ((ArrayList) getField(mSplitsField)).clear();
                ((ArrayList) getField(mSplitLabelsField)).clear();
            } catch (NullPointerException e) {
                Log.w(TAG, "reset: ", e);
            }
        }
        addSplit(null);
    }

    /**
     * Add a split for the current time, labeled with splitLabel.
     *
     * @param splitLabel a label to associate with this split.
     */
    public void addSplit(String splitLabel) {
        setField(mDisabledField, false);
        logger.addSplit(splitLabel);
    }

    /**
     * Dumps the timings to the log using Log.d().
     */
    public void dumpToLog() {
        setField(mDisabledField, false);
        logger.dumpToLog();
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private void initFields() {
        try {
            Class tlClz = TimingLogger.class;
            mDisabledField = tlClz.getDeclaredField("mDisabled");
            mDisabledField.setAccessible(true);
            mSplitsField = tlClz.getDeclaredField("mSplits");
            mSplitsField.setAccessible(true);
            mSplitLabelsField = tlClz.getDeclaredField("mSplitLabels");
            mSplitLabelsField.setAccessible(true);
            mTagField = tlClz.getDeclaredField("mTag");
            mTagField.setAccessible(true);
            mLabelField = tlClz.getDeclaredField("mLabel");
            mLabelField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.w(TAG, "initFields: ", e);
        }
    }

    private void setField(Field field, Object value) {
        if (field == null)
            return;
        try {
            field.set(logger, value);
        } catch (IllegalAccessException e) {
            Log.w(TAG, "setField: ", e);
        }
    }

    private Object getField(Field field) {
        try {
            return field.get(logger);
        } catch (IllegalAccessException | NullPointerException e) {
            Log.w(TAG, "getField: ", e);
        }
        return null;
    }
}
