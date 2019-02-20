package com.ryuunoakaihitomi.rebootmenu.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.ryuunoakaihitomi.rebootmenu.MyApplication;
import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.NetUtils;
import com.ryuunoakaihitomi.rebootmenu.util.StringUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Github发布版本下载服务
 * <p>
 * Created by ZQY on 2019/2/18.
 */

public class ReleaseDownloadService extends IntentService implements Handler.Callback {

    private static final String TAG = "ReleaseDownloadService";
    boolean runningStatus = false;
    private Handler handler = new Handler(this);

    //manifest
    public ReleaseDownloadService() {
        super(TAG);
    }

    /*
     * {
     *     "edges": [
     *         {
     *             "node": {
     *                ...
     *             }
     *         }
     *     ]
     * }
     */
    private static JSONObject graphQLGetFirstEdgesNode(@NonNull JSONObject src) {
        JSONArray edgesArray = src.optJSONArray("edges");
        new DebugLog(TAG, "graphQLGetFirstEdgesNode: len=" + edgesArray.length(), DebugLog.LogLevel.V);
        return edgesArray.optJSONObject(0).optJSONObject("node");
    }

    @Override
    public boolean handleMessage(Message message) {
        DebugLog.d(TAG, "onHandlerReached");
        Context ctx = getApplicationContext();
        switch ((UIActions) message.obj) {
            case ACTION_LINK_ANALYSE_SUCCESS:
                new TextToast(ctx, getString(R.string.dl_serv_hint_link_analyse_success));
                break;
            case ACTIONS_FAILED:
                new TextToast(ctx, getString(R.string.dl_serv_hint_link_auto_dl_failed));
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (runningStatus) {
            new DebugLog(TAG, "Already in running status!", DebugLog.LogLevel.W);
            return;
        }
        runningStatus = true;
        try {
            String rawData = NetUtils.githubConnectModel0("graphql", "bearer " + NetUtils.GITHUB_API_KEY
                    , new JSONObject().put("query",
                            /*
                             * GraphQL v4格式化代码：
                             * query ReleaseUpdateCheck($owner: String!, $name: String!, $releasesLast: Int = 1, $releaseAssetsFirst: Int = 1) {
                             *   repository(owner: $owner, name: $name) {
                             *     releases(last: $releasesLast) {
                             *       totalCount
                             *       edges {
                             *         node {
                             *           tagName
                             *           releaseAssets(first: $releaseAssetsFirst) {
                             *             edges {
                             *               node {
                             *                 downloadUrl
                             *                 downloadCount
                             *               }
                             *             }
                             *           }
                             *         }
                             *       }
                             *     }
                             *   }
                             * }
                             * 查询变量：
                             * {
                             *   "owner": "ryuunoakaihitomi",
                             *   "name": "rebootmenu"
                             * }
                             */
                            "query ReleaseUpdateCheck($owner:String!,$name:String!,$releasesLast:Int=1,$releaseAssetsFirst:Int=1)" +
                                    "{repository(owner:$owner,name:$name)" +
                                    "{releases(last:$releasesLast)" +
                                    "{totalCount edges{node{tagName releaseAssets(first:$releaseAssetsFirst)" +
                                    "{edges{node{downloadUrl downloadCount}}}}}}}}")
                            .put("variables", new JSONObject()
                                    .put("owner", "ryuunoakaihitomi")
                                    .put("name", "rebootmenu"))
                            .put("operationName", "ReleaseUpdateCheck").toString());
            //解析响应结果
            JSONObject rawJSONObject = new JSONObject(rawData);
            if (MyApplication.isDebug) {
                new DebugLog(TAG, "helpDialog: Github response...");
                for (String line : rawJSONObject.toString(2).split("\n"))
                    DebugLog.d(TAG, line);
            }

            //分析JSON取下载相关信息
            JSONObject releases = rawJSONObject.getJSONObject("data").getJSONObject("repository").getJSONObject("releases");
            int totalCount = releases.getInt("totalCount");
            JSONObject releaseNode = graphQLGetFirstEdgesNode(releases);
            String tagName = releaseNode.getString("tagName");
            JSONObject releaseAssetsNode = graphQLGetFirstEdgesNode(releaseNode.getJSONObject("releaseAssets"));
            int downloadCount = releaseAssetsNode.getInt("downloadCount");
            String downloadUrl = releaseAssetsNode.getString("downloadUrl");
            new DebugLog(TAG, "dlInfo=" + StringUtils.varArgsToString(totalCount, tagName, downloadCount, downloadUrl));

            //下载
            handlerMessenger(UIActions.ACTION_LINK_ANALYSE_SUCCESS);
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl))
                    .setDescription(getString(R.string.dl_info_description, tagName, downloadCount, totalCount))
                    .setTitle(getString(R.string.app_name) + tagName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    //（再设置一遍默认值以增强兼容性）
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(true)
                    .setAllowedOverMetered(true)
                    .setVisibleInDownloadsUi(true)
                    //外部下载目录
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            //开启扫描保证在最近文件列表中
            request.allowScanningByMediaScanner();
            //默认值
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.setRequiresCharging(false).setRequiresDeviceIdle(false);
            }
            //noinspection ConstantConditions
            long downloadID = manager.enqueue(request);
            new DebugLog(TAG, "id=" + downloadID, null);
            ConfigManager.setPrivateLong(this, ConfigManager.LASTEST_RELEASE_DOWNLOAD_ID, downloadID);
        } catch (Throwable throwable) {
            new DebugLog(throwable, "DownloadRelease", true);
            handlerMessenger(UIActions.ACTIONS_FAILED);

            //打开链接
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(NetUtils.GITHUB_RELEASE_WEB_LINK))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException ignored) {
            }
        } finally {
            DebugLog.i(TAG, "onHandleIntent: FINISH!");
            runningStatus = false;
        }
    }

    private void handlerMessenger(Object argObj) {
        Message message = new Message();
        message.obj = argObj;
        handler.sendMessage(message);
    }

    private enum UIActions {
        ACTION_LINK_ANALYSE_SUCCESS,
        ACTIONS_FAILED,
    }
}
