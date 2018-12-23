-optimizationpasses 9

# 保留main入口
-keepclassmembers public class * {
    public static void main(java.lang.String[]);
}

# 保持Android隐藏API存根
-keep class android.** {*;}