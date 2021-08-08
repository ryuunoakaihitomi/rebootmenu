# 构建笔记

## 变体

本应用有两个构建变体：`normal`,`floss`

* normal

带统计组件和完整的帮助文件。面向大多数用户的发布版本。

* floss

体积较小，不带统计组件和多余权限，面向**已经深入了解本应用并懂得如何提取错误报告**的进阶用户。仅发布在Github Release。

## 步骤

- 修改[`signInfo_example.properties`](signInfo_example.properties)，填入签名信息，并将其命名为`signInfo.properties`

- 如果需要构建`normal`版本，在`app`目录中加入Firebase配置文件`google-services.json`

- 执行Gradle任务：`app:assembleRelease`