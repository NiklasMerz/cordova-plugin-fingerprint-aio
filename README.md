# Cordova Plugin Fingerprint All-In-One
## For **Android** and **iOS**

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/NiklasMerz/cordova-plugin-fingerprint-aio/master/LICENSE)
[![Issue Count](https://codeclimate.com/github/NiklasMerz/cordova-plugin-fingerprint-aio/badges/issue_count.svg)](https://codeclimate.com/github/NiklasMerz/cordova-plugin-fingerprint-aio)

[![NPM](https://nodei.co/npm/cordova-plugin-fingerprint-aio.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/cordova-plugin-fingerprint-aio/)


**This plugin provides a single and simple interface for accessing fingerprint APIs on both Android 6+ and iOS.**

## Features

* Check if a fingerprint scanner is available
* Fingerprint authentication
* Ionic Native support
* Fallback options
* **FaceID** support
* **⚡️ Works with [Capacitor](https://capacitor.ionicframework.com/). [Try it out](https://github.com/NiklasMerz/capacitor-fingerprint-app) ⚡️**
* [Encrypt and save secrets behind a biometric prompt](#show-authentication-dialogue-and-register-secret)

## Version 4.0

Version 4.0 of this plugin is a significant upgrade over the previous versions. Previous versions only allowed a visual fingerprint prompt. Version 4.0 allows **saving an encrypted secret behind the biometric prompt** for true security. Please test it out and report any issues. If this plugin has security issues please check the [security policy](https://github.com/NiklasMerz/cordova-plugin-fingerprint-aio/security/policy). If you do audits using this plugin please let me know the results. My email is on my Github profile.

_Version 4 was developed almost 100% by other people than me (@NiklasMerz)._ **Please thank these awesome people for their work: @exxbrain, @leolio86400**. This is a community driven plugin and I don't do any real development anymore. But triaging issues and rewiewing and testing PRs is cumbersome work. If you depend on this plugin for your product please consider becoming my sponsor on Github to keep it going for a while. Some day I may consider stop working on it and pass it on to somebody interested. 

**Version 4.0 is awesome so please us it and let us fix it:smile:.**

### Platforms

* Android - Minimum SDK 23
* iOS - **latest XCode** is required. Plugin sets Swift version 4.


## How to use

**[Tutorial about using this plugin with Ionic](https://www.youtube.com/watch?v=tQDChMJ6er8)** thanks to Paul Halliday (**old plugin version!!**)

---

### Install

**Install from NPM**

```
cordova plugin add cordova-plugin-fingerprint-aio --save
```

If you want to set a FaceID description use:

```
cordova plugin add cordova-plugin-fingerprint-aio --variable FACEID_USAGE_DESCRIPTION="Login now...."
```

**Use the release candidate for testing the latest fixes**

You can use preview versions with the `rc` tag on npm.

```
cordova plugin add cordova-plugin-fingerprint-aio@rc
```

**Use this Github repo**

Get the latest development version. *Not recommended!*

```
cordova plugin add https://github.com/NiklasMerz/cordova-plugin-fingerprint-aio.git
```

### Check if fingerprint authentication is available
```javascript
Fingerprint.isAvailable(isAvailableSuccess, isAvailableError, optionalParams);

    function isAvailableSuccess(result) {
      /*
      result depends on device and os. 
      iPhone X will return 'face' other Android or iOS devices will return 'finger' Android P+ will return 'biometric'
      */
      alert("Fingerprint available");
    }

    function isAvailableError(error) {
      // 'error' will be an object with an error code and message
      alert(error.message);
    }
```
### Optional parameters

* __allowBackup (iOS)__: If `true` checks if backup authentication option is available, e.g. passcode. Default: `false`, which means check for biometrics only.

### Show authentication dialogue
```javascript
Fingerprint.show({
      description: "Some biometric description"
    }, successCallback, errorCallback);

    function successCallback(){
      alert("Authentication successful");
    }

    function errorCallback(error){
      alert("Authentication invalid " + error.message);
    }
```
### Optional parameters

* __title__: Title in authentication dialogue. Default: `"<APP_NAME> Biometric Sign On"`
* __subtitle__: Subtitle in authentication dialogue. Default: `null`
* __description__: Description in authentication dialogue. Defaults:
  * iOS: `"Authenticate"` (iOS' [evaluatePolicy()](https://developer.apple.com/documentation/localauthentication/lacontext/1514176-evaluatepolicy?language=objc) requires this field)
  * Android: `null`
* __fallbackButtonTitle__: Title of fallback button. Defaults:
  * When **disableBackup** is true
     *  `"Cancel"`
  * When **disableBackup** is false
     * iOS: `"Use PIN"`
     * Android: `"Use Backup"` (Because backup could be anything pin/pattern/password ..haven't figured out a reliable way to determine lock type yet [source](https://stackoverflow.com/questions/7768879/check-whether-lock-was-enabled-or-not/18720287))
* __disableBackup__: If `true` remove backup option on authentication dialogue. Default: `false`. This is useful if you want to implement your own fallback.
* __cancelButtonTitle__: For cancel button on Android
* __confirmationRequired__ (**Android**): If `false` user confirmation is NOT required after a biometric has been authenticated . Default: `true`. See [docs](https://developer.android.com/training/sign-in/biometric-auth#no-explicit-user-action).

### Show authentication dialogue and register secret
```javascript
Fingerprint.registerBiometricSecret({
      description: "Some biometric description",
      secret: "my-super-secret",
      invalidateOnEnrollment: true,
      disableBackup: true, // always disabled on Android
    }, successCallback, errorCallback);

    function successCallback(){
      alert("Authentication successful");
    }

    function errorCallback(error){
      alert("Authentication invalid " + error.message);
    }
```
### Optional parameters

* __title__: Title in authentication dialogue. Default: `"<APP_NAME> Biometric Sign On"`
* __subtitle__: Subtitle in authentication dialogue. Default: `null`
* __description__: Description in authentication dialogue. Defaults:
  * iOS: `"Authenticate"` (iOS' [evaluatePolicy()](https://developer.apple.com/documentation/localauthentication/lacontext/1514176-evaluatepolicy?language=objc) requires this field)
  * Android: `null`
* __fallbackButtonTitle__: Title of fallback button. Defaults:
  * When **disableBackup** is true
     *  `"Cancel"`
  * When **disableBackup** is false
     * iOS: `"Use PIN"`
     * Android: `"Use Backup"` (Because backup could be anything pin/pattern/password ..haven't figured out a reliable way to determine lock type yet [source](https://stackoverflow.com/questions/7768879/check-whether-lock-was-enabled-or-not/18720287))
* __disableBackup__: If `true` remove backup option on authentication dialogue. Default: `false`. This is useful if you want to implement your own fallback. NOTE: it will be disabled on Android
* __cancelButtonTitle__: For cancel button on Android
* __confirmationRequired__ (**Android**): If `false` user confirmation is NOT required after a biometric has been authenticated . Default: `true`. See [docs](https://developer.android.com/training/sign-in/biometric-auth#no-explicit-user-action).
* __secret__: String secret to encrypt and save, use simple strings matching the regex [a-zA-Z0-9\-]+
* __invalidateOnEnrollment__: If `true` secret will be deleted when biometry items are deleted or enrolled 

### Show authentication dialogue and load secret
```javascript
Fingerprint.loadBiometricSecret({
      description: "Some biometric description",
      disableBackup: true, // always disabled on Android
    }, successCallback, errorCallback);

    function successCallback(secret){
      alert("Authentication successful, secret: " + secret);
    }

    function errorCallback(error){
      alert("Authentication invalid " + error.message);
    }
```
### Optional parameters

* __title__: Title in authentication dialogue. Default: `"<APP_NAME> Biometric Sign On"`
* __subtitle__: Subtitle in authentication dialogue. Default: `null`
* __description__: Description in authentication dialogue. Defaults:
  * iOS: `"Authenticate"` (iOS' [evaluatePolicy()](https://developer.apple.com/documentation/localauthentication/lacontext/1514176-evaluatepolicy?language=objc) requires this field)
  * Android: `null`
* __fallbackButtonTitle__: Title of fallback button. Defaults:
  * When **disableBackup** is true
     *  `"Cancel"`
  * When **disableBackup** is false
     * iOS: `"Use PIN"`
     * Android: `"Use Backup"` (Because backup could be anything pin/pattern/password ..haven't figured out a reliable way to determine lock type yet [source](https://stackoverflow.com/questions/7768879/check-whether-lock-was-enabled-or-not/18720287))
* __disableBackup__: If `true` remove backup option on authentication dialogue. Default: `false`. This is useful if you want to implement your own fallback. NOTE: it will be disabled on Android
* __cancelButtonTitle__: For cancel button on Android
* __confirmationRequired__ (**Android**): If `false` user confirmation is NOT required after a biometric has been authenticated . Default: `true`. See [docs](https://developer.android.com/training/sign-in/biometric-auth#no-explicit-user-action).

### Constants
- **BIOMETRIC_UNKNOWN_ERROR** = `-100`;
- **BIOMETRIC_UNAVAILABLE** = `-101`;
- **BIOMETRIC_AUTHENTICATION_FAILED** = `-102`;
- **BIOMETRIC_SDK_NOT_SUPPORTED** = `-103`;
- **BIOMETRIC_HARDWARE_NOT_SUPPORTED** = `-104`;
- **BIOMETRIC_PERMISSION_NOT_GRANTED** = `-105`;
- **BIOMETRIC_NOT_ENROLLED** = `-106`;
- **BIOMETRIC_INTERNAL_PLUGIN_ERROR** = `-107`;
- **BIOMETRIC_DISMISSED** = `-108`;
- **BIOMETRIC_PIN_OR_PATTERN_DISMISSED** = `-109`;
- **BIOMETRIC_SCREEN_GUARD_UNSECURED** = `-110`;
- **BIOMETRIC_LOCKED_OUT** = `-111`;
- **BIOMETRIC_LOCKED_OUT_PERMANENT** = `-112`;
- **BIOMETRIC_SECRET_NOT_FOUND** = `-113`;
***

Thanks to the authors of the original fingerprint plugins

Some code is refactored from their projects and I learned how to make Cordova plugins from their great plugins:

@EddyVerbruggen and @mjwheatley

[Android](https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth)

[iOS](https://github.com/EddyVerbruggen/cordova-plugin-touch-id)

Starting with version 3.0.0 the iOS and Android parts are written from scratch.

## License

The project is MIT licensed: [MIT](https://opensource.org/licenses/MIT).
