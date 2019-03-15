package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.os.Build;

/**
 * MainCompat
 * Created by ZQY on 2019/3/11.
 */

class MainCompat {

    /**
     * 是否应该加载闭源组件
     * 注意：闭源组件行为无法控制，
     * 要加载在常用的系统平台上以便保持稳定
     *
     * @return {@link Boolean}
     */
    static boolean shouldLoadCSC() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }
}
