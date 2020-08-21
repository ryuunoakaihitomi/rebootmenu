## 手册

本应用可以更改设备的电源状态。

有以下几种操作模式：
* **受限模式**：在无root权限环境下，功能受限。
* **特权模式**：在root权限下，可以使用所有功能。
* **强制模式**：在root权限下，对于部分项目，强制切断电源。**（有安全风险）**

在root模式下，使用*切换模式*按钮可以在特权模式和强制模式间相互切换。有对应强制模式的项目会**变色加粗**。

受限模式可能需要**常驻**的**无障碍服务**，所以启用后会留有通知以保持前台状态。用户可以通过手动屏蔽通知，在非原生系统中，用户应该尽可能使用其自带机制~~如白名单，最近任务卡片锁定，电池优化等~~保留后台。

长按项目可以创建启动器快捷方式。从Android7.0开始，下拉任务栏有一个叫*电源菜单*的磁贴。

为防止误操作，在特权模式中，除*锁屏*外，其他项目均需要再次确认。

受限模式和特权模式的锁屏的区别是：受限模式的在Android 9.0下不能用生物传感器解锁，从9.0开始需要常驻无障碍服务；特权模式无此限制，但是会稍有延迟。

## 作者
[@ryuunoakaihitomi github.com](https://github.com/ryuunoakaihitomi)

## 隐私声明
本应用开源，代码可供任何人查看。开发者承诺不会使应用做出任何有损用户隐私权等利益的行为。

使用Firebase相关组件进行崩溃报告收集和使用统计，这些信息有益于开发者改进本应用。详情参阅：[Firebase 中的隐私权和安全性](https://firebase.google.cn/support/privacy)

## 开源引用

* [PowerAct](https://github.com/ryuunoakaihitomi/PowerAct)
Apache License 2.0

* [Toasty](https://github.com/GrenderG/Toasty)
LGPL-3.0 License

* [libsu](https://github.com/topjohnwu/libsu)
Apache License 2.0

* [Apache Commons IO](http://commons.apache.org/proper/commons-io/)
Apache License 2.0

* [Markwon](https://github.com/noties/Markwon) Apache License 2.0

本应用程序作为在Android平台及其相关支持组件下开发的产物，同时也受到[Android Source Licenses](https://source.android.google.cn/setup/start/licenses)的约束。
