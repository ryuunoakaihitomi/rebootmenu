# 黑魔法 getAlertDialogMessageView
-keepclassmembernames class androidx.appcompat.app.AlertDialog {
    androidx.appcompat.app.AlertController mAlert;
}
-keepclassmembernames class androidx.appcompat.app.AlertController {
    android.widget.TextView mMessageView;
}

# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception