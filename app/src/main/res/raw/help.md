# 📇

## About

This android app is used to control the power state.

There are 3 operation modes:
* **RESTRICTED MODE**: Functionality is limited in rootless environments.
* **PRIVILEGED MODE**: All functions are available with root access.
* **~~FORCE MODE~~**: Force power off for some items under root access. 
  (Only as a backup mode for compatibility with earlier non-standard systems, which is **not recommended** in most cases because of some security risks)

To circumvent the inherent flaws and limitations of the regular mechanism, starting from Android 6.0, it is **strongly recommended** to use this app with [**Shizuku Manager**](https://shizuku.rikka.app/zh-hans/download.html)! Granting Shizuku permission to this app to enjoy better operating experiences.

![`loading Shizuku's icon...`](https://shizuku.rikka.app/logo.png)

With root access, use the *Switch mode* button to switch between PRIVILEGED MODE and FORCE MODE. The items with corresponding force mode will **change color and bold**.

RESTRICTED MODE may requires persistent accessibility service, so notification will be left on to maintain foreground process status when the mode is enabled, it can be blocked manually. In some systems, you may have to use other means to keep the service running. (See [Don't kill my app!](https://dontkillmyapp.com))

**Long press** an item to create a **launcher shortcut**. (Some custom systems may need to manually grant permission to add launcher icons in advance) Starting from Android 7.0, provide *Power menu* and *Lock screen* tiles for the quick settings panel. (On CyanogenMod 12.1+, just *Power menu* tile is provided)

In order to prevent misoperation, except for *Lock screen* and *Sys power menu*, other items need to be confirmed again in PRIVILEGED MODE.

## ⚠ Unexpected situation

* Accessibility service

On Android 11, accessibility service has a potential bug that will cause it to stop working.

Starting from Android 13, accessibility service is classified as ["restricted settings"](https://blog.esper.io/android-13-sideloading-restriction-harder-malware-abuse-accessibility-apis/#android-13s-restricted-access-to-accessibility-services).
Users need to allow restricted settings to use it, which is one more troublesome step.

In restricted mode, please use Shizuku instead of accessibility service if possible.

* Launcher shortcut

In some environments, the launcher shortcut may not work after the application has been updated. Just delete and re-add it.

## Author

[@ryuunoakaihitomi github.com](https://github.com/ryuunoakaihitomi)

## Privacy statement

The following references are the legal statements of the statistics SDK used in this app.

In some environments, the launcher shortcut may not work after an app update. Just delete and re-add it.

> [Privacy and Security in Firebase](https://firebase.google.com/support/privacy)
>
> [Visual Studio App Center Security and Compliance](https://docs.microsoft.com/en-us/appcenter/general/app-center-security)

The app is free and open-source, which means that its behavior will never infringe your privacy within the developer's control.