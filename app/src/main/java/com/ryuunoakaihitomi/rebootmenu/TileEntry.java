package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;

/**
 * Android N 磁贴入口（打开应用）
 * Created by ZQY on 2018/3/8.
 */

@TargetApi(Build.VERSION_CODES.N)
public class TileEntry extends TileService {
    @Override
    public void onClick() {
        boolean isLocked = isLocked();
        new DebugLog("TileEntry.onClick: isLocked=" + isLocked, DebugLog.LogLevel.V);
        if (!isLocked)
            startActivityAndCollapse(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        else {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().updateTile();
        }
    }

    //这是一个随时可用的磁贴，因此应该在可见时时刻保持“活跃状态”（对比突出图标），但在锁屏时没有使用的理由
    @Override
    public void onStartListening() {
        boolean isLocked = isLocked();
        int qsTileState = getQsTile().getState();
        new DebugLog("TileEntry.onStartListening: isLocked=" + isLocked + " qsTileState=" + qsTileState, DebugLog.LogLevel.I);
        if (!isLocked && qsTileState != Tile.STATE_ACTIVE) {
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().updateTile();
        }
    }
}
