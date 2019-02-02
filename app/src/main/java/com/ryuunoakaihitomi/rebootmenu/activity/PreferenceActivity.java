package com.ryuunoakaihitomi.rebootmenu.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.base.Constants;
import com.ryuunoakaihitomi.rebootmenu.activity.base.MyActivity;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.ui.UIUtils;

import androidx.annotation.NonNull;

import static com.ryuunoakaihitomi.rebootmenu.util.ConfigManager.CANCELABLE;
import static com.ryuunoakaihitomi.rebootmenu.util.ConfigManager.DO_NOT_CHECK_ROOT;
import static com.ryuunoakaihitomi.rebootmenu.util.ConfigManager.NO_NEED_TO_COMFIRM;
import static com.ryuunoakaihitomi.rebootmenu.util.ConfigManager.UNROOT_MODE;
import static com.ryuunoakaihitomi.rebootmenu.util.ConfigManager.WHITE_THEME;

/**
 * 偏好设置入口
 * Created by ZQY on 2018/11/24.
 */

@TargetApi(Build.VERSION_CODES.N)
public class PreferenceActivity extends MyActivity {

    private static final String TAG = "PreferenceActivity";
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.transparentStatusBar(this);
        //设置底色默认是白色主题,用黑色更显眼
        AlertDialog.Builder builder = UIUtils.LoadDialog(false, this);
        builder.setTitle(R.string.preference_title)
                .setMultiChoiceItems(new String[]{
                                getString(R.string.r_whitetheme),
                                getString(R.string.r_cancelable),
                                getString(R.string.r_no_root_check),
                                getString(R.string.r_normal_do),
                                getString(R.string.r_unroot_mode)
                        },
                        new boolean[]{
                                ConfigManager.get(WHITE_THEME),
                                ConfigManager.get(CANCELABLE),
                                ConfigManager.get(DO_NOT_CHECK_ROOT),
                                ConfigManager.get(NO_NEED_TO_COMFIRM),
                                ConfigManager.get(UNROOT_MODE)
                        },
                        (dialogInterface, i, b) -> {
                            String ret = "";
                            switch (i) {
                                case 0:
                                    ret += ConfigManager.set(WHITE_THEME, b);
                                    break;
                                case 1:
                                    ret += ConfigManager.set(CANCELABLE, b);
                                    break;
                                case 2:
                                    ret += ConfigManager.set(DO_NOT_CHECK_ROOT, b);
                                    break;
                                case 3:
                                    ret += ConfigManager.set(NO_NEED_TO_COMFIRM, b);
                                    break;
                                case 4:
                                    ret += ConfigManager.set(UNROOT_MODE, b);
                            }
                            new DebugLog(TAG + " ret=" + ret + i, DebugLog.LogLevel.V);
                        })
                .setOnCancelListener(dialogInterface -> finish());
        dialog = builder.create();
        UIUtils.addMagnifier(dialog.getListView());
        //权限检查
        if (hasWriteExternalStoragePermission())
            UIUtils.alphaShow(dialog, UIUtils.TransparentLevel.PREFERENCES);
        else {
            requestCode = Constants.PREFERENCE_WRITE_EXTERNAL_STORAGE_REQUEST_CODE;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        }
    }

    @Override
    protected void onPause() {
        dialog.dismiss();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Constants.PREFERENCE_WRITE_EXTERNAL_STORAGE_REQUEST_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                UIUtils.alphaShow(dialog, UIUtils.TransparentLevel.PREFERENCES);
                //AppOps?
                if (!hasWriteExternalStoragePermission())
                    new TextToast(this, getString(R.string.pref_wes_perm_denied));
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    requestPermissions(permissions, requestCode);
                    //不再询问
                else {
                    new TextToast(this, true, getString(R.string.pref_wes_perm_denied));
                    UIUtils.alphaShow(dialog, UIUtils.TransparentLevel.PREFERENCES);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        dialog.show();
        super.onRestart();
    }

    private boolean hasWriteExternalStoragePermission() {
        try {
            boolean fromCtx = PackageManager.PERMISSION_GRANTED ==
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            @SuppressWarnings("ConstantConditions")
            boolean fromAppOps = AppOpsManager.MODE_ALLOWED == getSystemService(AppOpsManager.class)
                    .checkOpNoThrow(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, Process.myUid(), getPackageName());
            return fromCtx && fromAppOps;
        } catch (NullPointerException ignored) {
            return false;
        }
    }
}
