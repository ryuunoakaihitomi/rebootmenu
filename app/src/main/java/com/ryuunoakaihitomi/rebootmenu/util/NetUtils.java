package com.ryuunoakaihitomi.rebootmenu.util;


import android.os.NetworkOnMainThreadException;

import com.ryuunoakaihitomi.rebootmenu.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 网络连接杂物箱
 * Created by ZQY on 2019/2/17.
 */

public class NetUtils {

    /**
     * Personal access tokens: 保证只持有公共权限
     */
    public static final String GITHUB_API_KEY = BuildConfig.GITHUB_PAT;
    /**
     * 本应用的源代码网址
     */
    public static final String GITHUB_LINK = "https://github.com/ryuunoakaihitomi/rebootmenu";
    /**
     * 本应用的Github Release版本网址
     */
    public static final String GITHUB_RELEASE_WEB_LINK = GITHUB_LINK + "/releases";
    private static final int MAX_DELAY = 3000;

    private NetUtils() {
    }

    /**
     * 连接Github API的一个公共模型模型
     *
     * @param apiEndpoint api端点
     * @param authParam   认证请求头参数
     * @param postData    发送数据
     * @return 响应
     * @throws IOException                  +{@link java.net.MalformedURLException, java.net.ProtocolException}
     * @throws SecurityException            权限问题，{@see android.Manifest.permission.INTERNET}
     * @throws NetworkOnMainThreadException 不能在主线程发起网络链接
     */
    public static String githubConnectModel0(String apiEndpoint, String authParam, String postData) throws IOException, SecurityException, NetworkOnMainThreadException {
        URL url = new URL("https://api.github.com/" + apiEndpoint);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", authParam);
        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes());
        os.flush();
        os.close();
        //设置超时
        conn.setConnectTimeout(MAX_DELAY);
        conn.setReadTimeout(MAX_DELAY);
        //包装成字符串
        InputStream is = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        br.close();
        String response = sb.toString();
        conn.disconnect();
        return response;
    }
}
