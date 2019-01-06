-optimizationpasses 9

# 保留main入口
-keepclassmembers public class * {
    public static void main(java.lang.String[]);
}

# 保持Android隐藏API存根
-keep class android.** {*;}
-keep class com.android.** {*;}

# 保护Xposed入口
-keep public class com.ryuunoakaihitomi.rebootmenu.util.hook.XposedMain

# 保存Xposed检测开关
-keep public class com.ryuunoakaihitomi.rebootmenu.util.hook.XposedUtils {
    public static boolean isActive;
}