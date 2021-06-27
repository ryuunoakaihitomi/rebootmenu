# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android
-keepattributes SourceFile,LineNumberTable
# 从R3版本开始更新以来，一直没有报告自定义的Exception，因此禁用以进一步缩小体积
#-keepnames public class * extends java.lang.Exception