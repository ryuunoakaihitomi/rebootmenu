package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Android N 磁贴入口
 * Created by ZQY on 2018/3/8.
 */

@TargetApi(Build.VERSION_CODES.N)
public class TileEntry extends TileService {
    @Override
    public void onClick() {
        startActivityAndCollapse(getPackageManager().getLaunchIntentForPackage(getPackageName()));
    }

    //这是一个随时可用的磁贴，因此应该在可见时时刻保持“活跃状态”（白色图标）
    @Override
    public void onStartListening() {
        getQsTile().setState(Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }
}
