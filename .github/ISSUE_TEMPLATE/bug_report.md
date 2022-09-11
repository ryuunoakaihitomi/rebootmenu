---
name: 缺陷报告
about: 创建一个对改进本应用有所帮助的报告
title: "[BUG]"
labels: bug, help wanted
assignees: ryuunoakaihitomi

---

* ⚠ 请仔细查看此模板，严格遵守规则填写！
* ⚠ 如果怀疑本应用影响了其他闭源应用的行为，请先向那个闭源应用的开发者反馈！

### 描述缺陷
对缺陷言简意赅的描述。

### 复现
复现所依赖的环境（可选）
1. 需要安装'...'应用
2. 在系统设置中调整了'...'

复现出这种行为的描述
1. 来到 '...'
2. 在 '....' 上点击
3. 向下滑动到 '....'
4. 看到错误（崩溃/闪退，界面错位，操作不执行等等）

请尽可能保证错误能够稳定复现，并最精简化复现步骤和复现所依赖的环境配置

### 期望行为
对你所期望行为的言简意赅的描述。

### 日志
在这里填充以下描述之一所代表的信息：
* [错误报告](https://developer.android.google.cn/studio/debug/bug-report?hl=zh-cn)
* [Logcat](https://developer.android.google.cn/studio/command-line/logcat?hl=zh_cn)
* 如果用的是`floss`版本且报告的问题属于应用崩溃，检查`/sdcard/Android/data/github.ryuunoakaihitomi.powerpanel/cache/CrashReport`目录里最新创建的json文件
  
注意：较大的文件和文本要上传至其他网络服务生成下载链接再上报。在某些系统环境中，获取日志信息可能有特殊步骤和限制，请自行查询解决办法。

### 一些截图
如果可以，添加一些截图以帮助解释你的问题。
如果要报告的问题属于界面错位，这个信息尤其重要。

### 所使用的环境
 - 设备（如果不是知名厂商提供的消费级产品，需要补充更多详尽信息）: [例: Redmi 9A; Tanix TX6，这是我在淘宝上买的一个外贸电视盒子（附上购买链接）]
 - 操作系统（需要注意的是如果不是用的AOSP，**必须**写上它们自己的系统版本）: [例: Android 10, MIUI 12.5; Android 12.1, LineageOS 19.1]
 - 版本 [例: R3.12.4]

### 附加信息
添加任何有关这个问题的其他信息。
