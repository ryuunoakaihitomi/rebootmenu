-optimizationpasses 9

# 保留main入口
-keepclassmembers public class * {
    public static void main(java.lang.String[]);
}

# 保持Android隐藏API存根
-keep class android.** {*;}
-keep class com.android.** {*;}

# 保护Xposed入口 @Keep
-keep public class com.ryuunoakaihitomi.rebootmenu.util.hook.XposedMain

# 保存Xposed检测开关 @Keep
-keep public class com.ryuunoakaihitomi.rebootmenu.util.hook.XposedUtils {
    public static boolean isActive;
}

# 保留行号并且隐藏类名
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# --第三方组建要求--
#
# For faster builds with ProGuard, exclude Crashlytics.
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
# To preserve the info Crashlytics needs for readable crash reports.
-keepattributes *Annotation* # Keep Crashlytics annotations
-keep public class * extends java.lang.Exception # Keep custom exceptions (opt)