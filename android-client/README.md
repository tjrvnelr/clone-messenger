# Android client

This is a native Android client for the Java Messenger server. The original
Maven project is a JavaFX desktop client and cannot be packaged as an Android
APK directly.

## Build the APK

Open the `android-client` folder in Android Studio, let it install the Gradle
and Android SDK components, then choose **Build > Build APK(s)**. The debug
APK will be created at:

```text
android-client/app/build/outputs/apk/debug/app-debug.apk
```

Install it on the phone with Android Studio, or copy the APK to the phone and
open it. Android may ask you to allow installation from that file manager.

## Connect a phone to the server

1. Start MySQL and the Java Messenger server on the computer.
2. Find the computer's local IPv4 address, for example `192.168.1.25`.
3. Ensure the phone and computer use the same Wi-Fi network.
4. Enter that address in **Server address** in the app. Do not use
   `localhost` on a physical phone; `localhost` means the phone itself.
5. Allow TCP port `5555` through the computer firewall if the phone cannot
   connect.

For an Android emulator, the default `10.0.2.2` address points to the host
computer.
