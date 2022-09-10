package github.ryuunoakaihitomi.powerpanel.util

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.topjohnwu.superuser.Shell

typealias CC = ContextCompat
typealias RC = ResourcesCompat

/**
 * 本项目的开源地址
 */
const val PROJECT_URL = "https://github.com/ryuunoakaihitomi/rebootmenu"

/**
 * 由于[Shell.isAppGrantedRoot]不会自动申请权限
 * 所以需要先通过[Shell.getShell]尝试获取su shell
 */
fun isRoot() = Shell.isAppGrantedRoot() == true || Shell.getShell().isRoot

fun nox() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
fun lumos() =
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

/**
 * 参考资料：
 * https://www.bilibili.com/video/BV1Ra411G7fp
 * https://www.bilibili.com/video/BV1aS4y1N78d
 * https://pan.baidu.com/s/1LQw_fftvfQzpHS12mtl5tQ?pwd=gf4y
 * https://community.wvbtech.com/d/3027
 * 判断是CracKDroid的依据
 * 根据CPU型号：
 * * [Kindle刷机指南Ver2.0 220831.pdf](https://pan.baidu.com/s/1LQw_fftvfQzpHS12mtl5tQ?pwd=gf4y#list/path=%2FCracKDroid%2F%E6%95%99%E7%A8%8B)
 * ，查看“支持机型”部分
 * * [亚马逊 Kindle 系列产品硬件技术参数大全 中央处理器（CPU）](https://bookfere.com/post/694.html#p03)
 */
@Suppress("SpellCheckingInspection")
val isCrackDroidEnv by lazy {
    Build.BRAND == "Amazon" && Build.HARDWARE == "freescale" && BlackMagic.getSystemProperties("ro.board.platform") == "imx6"
}