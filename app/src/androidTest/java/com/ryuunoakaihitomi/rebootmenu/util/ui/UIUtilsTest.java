package com.ryuunoakaihitomi.rebootmenu.util.ui;

import android.content.res.Resources;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.BuildConfig;
import com.ryuunoakaihitomi.rebootmenu.test.ContextInit;
import com.ryuunoakaihitomi.rebootmenu.test.ExecBorder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class UIUtilsTest {
    private static final String TAG = "UIUtilsTest";

    @Rule
    public TestRule r0 = new ExecBorder(), r1 = ContextInit.INSTANCE;

    @Test(timeout = 500/*, expected = AssertionError.class*/)
    public void visibleHint() {
        String[] packs = {"android", BuildConfig.APPLICATION_ID};
        for (String pack : packs) {
            String strResClsName = pack + ".R$string";
            Class strResCls = null;
            try {
                strResCls = Class.forName(strResClsName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                fail("String resource class not found.");
            }
            for (Field strRes : strResCls.getFields()) {
                int id = 0;
                try {
                    id = strRes.getInt(strResCls);
                    String resName = strRes.getName();
                    String resBody = ContextInit.appCtx().getString(id);
                    assertNotEquals(0, id);
                    assertThat(Arrays.asList(resName, resBody), hasItem(is(notNullValue())));
                    Log.d(TAG, "visibleHint: " + pack + Arrays.toString(new Object[]{id, resName, resBody}));
                } catch (IllegalAccessException e) {
                    fail("Resource cannot access.");
                    break;
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "visibleHint: Context.getString(" + id + ") not found.", e);
                }
            }
        }
    }
}
