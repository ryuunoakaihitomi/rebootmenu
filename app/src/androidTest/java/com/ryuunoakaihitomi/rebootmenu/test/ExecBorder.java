package com.ryuunoakaihitomi.rebootmenu.test;

import android.util.Log;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * 单元测试运行边界
 * Created by ZQY on 2019/8/13.
 *
 * @author ZQY
 */

public class ExecBorder implements TestRule {
    private static final String TAG = "ExecBorder";
    private static final String BORDER = "-------------";

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Log.i(TAG, "! " + description.getClassName());
                String methodName = description.getMethodName();
                Log.v(TAG, BORDER + ' ' + methodName + " start");
                base.evaluate();
                Log.v(TAG, BORDER + ' ' + methodName + " end");
            }
        };
    }
}
