package com.ryuunoakaihitomi.rebootmenu.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ui.UIUtils;

import java.util.Objects;

/**
 * Github发布版本下载完成接收器
 * <p>
 * Created by ZQY on 2019/2/18.
 */

public class DownloadActionReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadActionReceiver";
    private static final long NULL_DOWNLOAD_ID = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = Objects.requireNonNull(intent.getAction());
        DebugLog.d(TAG, "onReceive: action=" + action);
        DownloadManager manager = (DownloadManager) Objects.requireNonNull(context.getSystemService(Context.DOWNLOAD_SERVICE));
        long id = ConfigManager.getPrivateLong(context, ConfigManager.LATEST_RELEASE_DOWNLOAD_ID, NULL_DOWNLOAD_ID);
        DebugLog.i(TAG, "id=" + id);
        switch (action) {
            //下载完成自动打开安装
            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0) == id) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(id);
                    Cursor cursor = manager.query(query);
                    if (!cursor.moveToFirst()) {
                        DebugLog.e(TAG, "moveToFirst failed!");
                        cursor.close();
                        return;
                    }
                    String localFilePath = cursor.getString(cursor.getColumnIndex(
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                    ? DownloadManager.COLUMN_LOCAL_URI : DownloadManager.COLUMN_LOCAL_FILENAME));
                    DebugLog.v(TAG, "Download Completed! -> " + localFilePath);
                    if (localFilePath.startsWith("file://"))
                        localFilePath = localFilePath.replace("file://", "");
                    UIUtils.openFile(context, localFilePath);
                    cursor.close();
                }
                break;
            //安装完成自动删除
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                if (id != NULL_DOWNLOAD_ID) {
                    DebugLog.i(TAG, "delete dl id:" + id + " ret=" + manager.remove(id));
                    ConfigManager.setPrivateLong(context, ConfigManager.LATEST_RELEASE_DOWNLOAD_ID, NULL_DOWNLOAD_ID);
                }
        }
    }
}
