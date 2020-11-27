# 构建笔记

在构建之前需要进行以下工作

- 在`app`目录中加入Firebase配置文件`google-services.json`

构建

- 修改[`signInfo_example.properties`](signInfo_example.properties)，填入签名信息，并将其命名为`signInfo.properties`

- 执行Gradle任务：`resguardRelease`