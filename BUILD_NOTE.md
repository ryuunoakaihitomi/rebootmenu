## 构建笔记

在构建之前需要进行以下工作

- 在`app`目录中加入Firebase配置文件`google-services.json`

### 备注

如果及其注重隐私或者没有注册Firebase服务，可以不去准备`google-services.json`并删除掉[应用层`build.gradle`](/app/build.gradle)中Firebase的相关依赖项，注释中有详细说明。