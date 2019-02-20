package com.ryuunoakaihitomi.rebootmenu.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.ryuunoakaihitomi.rebootmenu.BuildConfig;
import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.NetUtils;
import com.ryuunoakaihitomi.rebootmenu.util.StringUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.ui.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.Nullable;

import static com.ryuunoakaihitomi.rebootmenu.util.StringUtils.strArr2String;


/**
 * 回调来解决AsyncTask中处理含Context任务的问题
 */
interface PostTrigger {

    void onStart();

    void onEnd(boolean isSucceeded);
}

/**
 * Crash反馈提交申请
 * Created by ZQY on 2019/2/6.
 */

public class SendBugFeedback extends Activity implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = "SendBugFeedback";

    private static final String EXTRA_TAG_EXP_STACK = "exp_stack",
            EXTRA_TAG_CRASH_TIME = "crash_time",
            BUNDLE_TAG_MORE_DES = "more_des";

    private EditText userNameEdit, passwordEdit, moreDescriptionEdit;
    private CheckBox keepContactChkBx;

    private String buildInfo, exp, time;

    /**
     * 启动action
     *
     * @param context {@link Context#startActivity(Intent)}
     * @param time    Crash发生的时刻
     * @param exp     错误堆栈
     */
    public static void actionStart(Context context, String time, String exp) {
        Log.d(TAG, "actionStart: ");
        Intent intent = new Intent(context, SendBugFeedback.class);
        intent.putExtra(EXTRA_TAG_CRASH_TIME, time);
        intent.putExtra(EXTRA_TAG_EXP_STACK, exp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Build Info
     * <p>
     * {@link Build}
     * {@link StringUtils#strArr2String(String[], String)}
     *
     * @return JSON String
     */
    private static String getRawBuildEnvInfo() {
        Map<String, Object> map = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            map.put("SUPPORTED_ABIS", strArr2String(Build.SUPPORTED_ABIS, ","));
            map.put("SUPPORTED_32_BIT_ABIS", strArr2String(Build.SUPPORTED_32_BIT_ABIS, " "));
            map.put("SUPPORTED_64_BIT_ABIS", strArr2String(Build.SUPPORTED_64_BIT_ABIS, " "));
        } else {
            map.put("CPU_ABI", Build.CPU_ABI);
            map.put("CPU_ABI2", Build.CPU_ABI2);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            map.put("VERSION.BASE_OS", Build.VERSION.BASE_OS);
            map.put("VERSION.PREVIEW_SDK_INT", Build.VERSION.PREVIEW_SDK_INT);
            map.put("VERSION.SECURITY_PATCH", Build.VERSION.SECURITY_PATCH);
        }
        map.put("VERSION.CODENAME", Build.VERSION.CODENAME);
        map.put("VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
        map.put("VERSION.RELEASE", Build.VERSION.RELEASE);
        map.put("VERSION.SDK_INT", Build.VERSION.SDK_INT);
        map.put("getRadioVersion", Build.getRadioVersion());
        map.put("BOARD", Build.BOARD);
        map.put("BOOTLOADER", Build.BOOTLOADER);
        map.put("BRAND", Build.BRAND);
        map.put("DEVICE", Build.DEVICE);
        map.put("DISPLAY", Build.DISPLAY);
        map.put("FINGERPRINT", Build.FINGERPRINT);
        map.put("HARDWARE", Build.HARDWARE);
        map.put("HOST", Build.HOST);
        map.put("ID", Build.ID);
        map.put("MANUFACTURER", Build.MANUFACTURER);
        map.put("MODEL", Build.MODEL);
        map.put("PRODUCT", Build.PRODUCT);
        map.put("TAGS", Build.TAGS);
        map.put("TYPE", Build.TYPE);
        map.put("USER", Build.USER);
        map.put("TIME", Build.TIME);
        Map<String, Object> sort = new TreeMap<>(String::compareTo);
        sort.putAll(map);
        JSONObject jsonObject = new JSONObject(sort);
        try {
            return jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
        setTitle(String.format("❌ %s (X_X)", getTitle()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_report);
        initUIComponents();

        Intent i = getIntent();
        time = i.getStringExtra(EXTRA_TAG_CRASH_TIME);
        exp = i.getStringExtra(EXTRA_TAG_EXP_STACK);
        buildInfo = getRawBuildEnvInfo();

        //1KiB输入长度限制
        moreDescriptionEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1024)});

        //自动填充
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            userNameEdit.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            userNameEdit.setAutofillHints(View.AUTOFILL_HINT_USERNAME);
            passwordEdit.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            passwordEdit.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
            moreDescriptionEdit.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        //恢复紧急的自定义描述
        if (savedInstanceState != null) {
            moreDescriptionEdit.setText(savedInstanceState.getString(BUNDLE_TAG_MORE_DES));
            new DebugLog(TAG, "savedInstanceState != null", DebugLog.LogLevel.W);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_TAG_MORE_DES, moreDescriptionEdit.getText().toString());
    }

    //初始化UI控件的模板代码
    private void initUIComponents() {
        userNameEdit = findViewById(R.id.username);
        passwordEdit = findViewById(R.id.password);
        moreDescriptionEdit = findViewById(R.id.more_des);
        Button viewInfoBtn = findViewById(R.id.view_crash_info);
        Button sendBtn = findViewById(R.id.send);
        keepContactChkBx = findViewById(R.id.contact_req);
        viewInfoBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        userNameEdit.setOnFocusChangeListener(this);
        passwordEdit.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_crash_info:
                AlertDialog.Builder builder = UIUtils.LoadDialog(true, this);
                builder.setTitle(R.string.see_err_info);
                WebView webView = new WebView(this);
                WebSettings webSettings = webView.getSettings();
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webSettings.setLoadWithOverviewMode(true);
                //缩放
                webSettings.setBuiltInZoomControls(true);
                webSettings.setDisplayZoomControls(false);
                webView.loadData("<code style=background-color:white;font-size:8pt;color:#F00;word-break:keep-all>"
                        + ("//crash time\n" + time + "\n//exception stack\n" + exp + "\n//build info\n" + buildInfo)
                        .replace("\n", "<br>").replace(" ", "&nbsp;")
                        //JSON反转义
                        .replace("\\/", "/")
                        + "</code>", null, null);
                builder.setView(webView);
                builder.setNegativeButton(android.R.string.ok, null);
                builder.show();
                break;
            case R.id.send:
                new PostTask(new PostTrigger() {
                    AlertDialog progressDialog;
                    ProgressDialog deprecatedDialog;
                    Activity act;

                    @Override
                    public void onStart() {
                        act = SendBugFeedback.this;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            deprecatedDialog = ProgressDialog.show(act, null, getString(R.string.sending));
                            return;
                        }
                        AlertDialog.Builder pgDialogBuilder = UIUtils.LoadDialog(true, act);
                        ProgressBar bar = new ProgressBar(act, null, android.R.attr.progressBarStyleLarge);
                        pgDialogBuilder.setView(bar);
                        pgDialogBuilder.setCancelable(false);
                        pgDialogBuilder.setTitle(R.string.sending);
                        progressDialog = pgDialogBuilder.show();
                    }

                    @Override
                    public void onEnd(boolean isSucceeded) {
                        if (progressDialog != null) progressDialog.dismiss();
                        else deprecatedDialog.dismiss();
                        if (isSucceeded) {
                            new TextToast(act, true, getString(R.string.send_bug_report_succeeded));
                            UIUtils.restartApp(act);
                        } else
                            new TextToast(act, true, getString(R.string.send_bug_report_failed));
                    }

                }).execute(userNameEdit.getText().toString()
                        , passwordEdit.getText().toString()
                        , time, exp, buildInfo
                        , moreDescriptionEdit.getText().toString()
                        , String.valueOf(keepContactChkBx.isChecked()));
                break;
        }
    }

    @Override
    public void onFocusChange(View view, /*hasFocus*/ boolean b) {
        switch (view.getId()) {
            case R.id.username:
            case R.id.password:
                //自动填充服务积极调用
                if (b && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AutofillManager afm = getSystemService(AutofillManager.class);
                    if (afm != null) afm.requestAutofill(view);
                }
                break;
            default:
        }
    }
}

/**
 * 发送Github Issues的主体
 */
class PostTask extends AsyncTask<String, Integer, Boolean> {

    private static final String TAG = "PostTask";

    @SuppressWarnings("CanBeFinal")
    private PostTrigger mTrigger;

    PostTask(PostTrigger trigger) {
        mTrigger = trigger;
    }

    @Override
    protected void onPreExecute() {
        new DebugLog(TAG, "onPreExecute: ", DebugLog.LogLevel.I);
        mTrigger.onStart();
    }

    /**
     * GitHub
     *
     * @param strings 0.userName
     *                1.password
     *                2.crashTime
     *                3.exceptionStack
     *                4.buildInformation
     *                5.moreDescription
     *                6.ifWantMoreFeedBack
     * @return 是否成功
     */
    @Override
    protected Boolean doInBackground(String... strings) {
        long start = SystemClock.uptimeMillis();
        String issuesLink = null;
        try {
            //replace MarkDown引用
            String post, get, moreDes = "> " + strings[5].replace("\n", "\n> ");
            JSONObject postBody = new JSONObject();
            postBody.put("title", "Auto-generated issues:From SendBugFeedback");
            /*
             * Markdown模板：自动反馈
             *
             *
             * # Time
             *
             * ```
             * 时间
             * ```
             *
             * # Exception Stack
             *
             * ```
             * 堆栈
             * ```
             *
             * # Build Info
             *
             * ```json
             * JSON构建信息
             * ```
             *
             * # More Description
             *
             * > 自定义（更多描述）
             *
             * ## MoreFeedBack: （bool，是否允许之后联系） versionCode
             */
            postBody.put("body", "# Time\n\n```\n" + strings[2]
                    + "\n```\n\n# Exception Stack\n\n```\n" + strings[3]
                    + "\n```\n\n# Build Info\n\n```json\n" + strings[4]
                    + "\n```\n\n# More Description\n\n" + moreDes
                    + "\n\n## MoreFeedBack: " + strings[6] + " " + BuildConfig.VERSION_CODE);
            post = postBody.toString();
            get = NetUtils.githubConnectModel0("repos/ryuunoakaihitomi/rebootmenu/issues",
                    "Basic " + Base64.encodeToString((strings[0] + ":" + strings[1]).getBytes(), Base64.DEFAULT), post);
            JSONObject ret = new JSONObject(get);
            issuesLink = ret.optString("html_url");
            return true;
        } catch (MalformedURLException e) {
            new DebugLog(e, "MalformedURLException", false);
        } catch (ProtocolException e) {
            new DebugLog(e, "ProtocolException", false);
        } catch (IOException e) {
            new DebugLog(e, "IOException", false);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            new DebugLog(e, "SecurityException", false);
        } finally {
            //当try、catch中有return时，finally中的代码依然会继续执行
            new DebugLog(TAG, "delay=" + (SystemClock.uptimeMillis() - start) + " link=" + issuesLink, null);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        new DebugLog(TAG, "post succeed? " + aBoolean, null);
        mTrigger.onEnd(aBoolean);
    }
}
