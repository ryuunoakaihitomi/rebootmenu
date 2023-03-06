# Command Line Build Tutorial

[中文](BUILD_CLI.md)/[EN](BUILD_CLI-EN.md)
* Using the GUI-based build in Android Studio is convenient and highly recommended, but there may be scenarios where a command-line interface is necessary, in which case this tutorial may come in handy.
* This tutorial can also be used as reference material for command-line builds of other applications.
* Over time, this tutorial may become outdated; please seek out solutions on your own if that happens, and the tutorial includes some reference links.

## System Requirements

Item|Parameter|Explanation
---|---|---
Architecture|`amd64`|`arm64` cannot be built (Android device, using [Termux](https://termux.com))
Minimum memory|`4GB`|
Minimum storage|`10GB`|
Operating system|`Ubuntu Server 22.04.1 LTS`|Use [AOSP-required software environment](https://source.android.google.cn/setup/build/requirements#software-requirements) to ensure successful builds, use the server edition to reduce resource consumption

## Steps

### Prepare Software Packages

Update and restart to increase the success rate of the build process.

```shell
sudo apt update && sudo apt upgrade -y
sudo reboot # optional
```

Install the following software packages:

* java: using java 11, as AndResGuard does not currently support java 17.
* zip: used to extract files.
* 7z: optional, AndResGuard depends on it to further compress APKs.

```shell
sudo apt install openjdk-11-jre-headless zip p7zip-full -y
```


## Clone the repository 
Clone the code in the user directory. Since it is only used for building, only clone the most current version of the repo to save on download time (see depth parameter).

```shell
cd ~
git clone --depth=1 https://github.com/ryuunoakaihitomi/rebootmenu.git
```
## Configure the Building Environment
Download the Android Command-line Tools
The download link can be obtained by scrolling down to the Command line tools only heading on the Android Developer's [Android Studio download page](https://developer.android.google.cn/studio#downloads), finding the Linux item, and agreeing to the terms before copying the link.

```shell
wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip # if the download link changes, modify it here
unzip commandlinetools-linux-*_latest.zip
```

Configure the Android SDK
Create a new directory as the SDK root directory.

```shell
mkdir android_sdk
export ANDROID_SDK_ROOT=$HOME/android_sdk
```

Before using the SDK, you also need to accept the license agreement. Refer to these:

* sdkmanager User Guide
* Automatically accept all SDK licences

```shell
cd cmdline-tools/bin # command line tool directory
yes | ./sdkmanager --sdk_root=$HOME/android_sdk --licenses
```

### Install Gradle
Refer to:

* https://gradle.org/install
* https://sdkman.io/install

Installed version 7.5.1 (tested and working at the time of writing this tutorial).

```shell
cd ~
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 7.5.1
```

## Building
Prepare the Keystore 

Generate a keystore ([refer to the documentation if needed](https://docs.oracle.com/en/java/javase/11/tools/keytool.html)).

```shell
cd rebootmenu/app
keytool -genkey -alias a -dname CN=_ -storepass passwd -keypass passwd -keyalg RSA -keystore android.keystore
```
Write to the configuration file secret.properties

```shell
cd ..
echo KEY_ALIAS=a >> secret.properties
echo KEY_PWD=passwd >> secret.properties
echo STORE_PWD=passwd >> secret.properties
echo STORE_FILE=android.keystore >> secret.properties
```

If you would like, instead of generating a new keystore you can use your own and change the information in the configuration file to your keystore's alias, password, and path.


Execute the Gradle task for building

```shell
gradle resguardFlossRelease
```
Wait a moment (depending on your hardware and network speed it may take 5 to 20 minutes). The output will show BUILD SUCCESSFUL, indicating that the build is completed without any errors. The APK file will then be located at app/build/outputs/apk/floss/release.

