# 构建笔记

## 变体

本应用有两个构建变体：`normal`,`floss`

* normal

带统计组件和完整的帮助文件。面向大多数用户的发布版本。

* floss

体积较小，不带统计组件和多余权限，面向**已经深入了解本应用并懂得如何提取错误报告**的进阶用户。仅发布在Github Release。

## 步骤

- 需要事先配置好[Android Studio](https://developer.android.google.cn/studio)并导入本项目

- 修改[`secret_example.properties`](secret_example.properties)，填入签名信息，并将其重命名为`secret.properties`

- **推荐构建floss变体，normal变体的步骤部分只作为开发者的备忘录。**
  如果需要构建`normal`变体，在`app`目录中加入Firebase配置文件`google-services.json`， 在`secret.properties`中补充Visual
  Studio App Center的API密钥于`APP_CENTER`字段

- 执行Gradle任务：`app:assembleRelease`