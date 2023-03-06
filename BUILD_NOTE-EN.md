# Build Notes

[中文](BUILD_NOTE.md)/[EN](BUILD_NOTE-EN.md)

## Variants

There are two build variants: `normal`, `floss`.

* normal

Includes the statistics component and complete help files. It is the release version for most users.

* floss

Smaller in size, does not include statistics component and unnecessary permissions. It is intended for advanced users who have deep knowledge of this app and know how to extract error reports. It is only released on Github Release.

## Steps

- Android Studio needs to be configured in advance and this project needs to be imported (if you cannot configure Android Studio, you can try [command line build](BUILD_CLI.md)).

- Execute the Gradle task: `app:resguardFlossRelease`.

- APK file path: `app/build/outputs/apk/floss/release/rebootmenu-<version information>-floss_release.apk`.

## 🈲 Steps for normal build variant ⚠

**It is recommended that users build the floss variant using the steps above. The following steps are only for the maintainer's reference.**

- Modify [`secret_example.properties`](secret_example.properties), fill in the signing information, and rename it to `secret.properties`.

- If you need to build the `normal` variant, add the Firebase configuration file `google-services.json` in the `app` directory, supplement the Visual Studio App Center API key in the `APP_CENTER` field in `secret.properties`. Run the Gradle task `app:resguardRelease` to build both normal and floss versions simultaneously.
