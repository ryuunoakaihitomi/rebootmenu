## 构建笔记

在构建之前需要进行以下工作

- 在`app`目录中加入Firebase配置文件`google-services.json`

### 备注

如果想打包不含Firebase组件的版本，可以不去准备`google-services.json`并前往[应用层`build.gradle`](/app/build.gradle)中禁用Firebase组件，注释中有详细说明。