package github.ryuunoakaihitomi.powerpanel.util

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