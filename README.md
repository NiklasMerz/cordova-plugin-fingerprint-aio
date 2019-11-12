# Cordova Plugin Fingerprint All-In-One
## For **Android** and **iOS**

[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/cordova-plugin-fingerprint-aio)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/NiklasMerz/cordova-plugin-fingerprint-aio/master/LICENSE)
[![Build Status](https://travis-ci.org/NiklasMerz/cordova-plugin-fingerprint-aio.svg?branch=master)](https://travis-ci.org/NiklasMerz/cordova-plugin-fingerprint-aio)
[![Issue Count](https://codeclimate.com/github/NiklasMerz/cordova-plugin-fingerprint-aio/badges/issue_count.svg)](https://codeclimate.com/github/NiklasMerz/cordova-plugin-fingerprint-aio)

[![NPM](https://nodei.co/npm/cordova-plugin-fingerprint-aio.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/cordova-plugin-fingerprint-aio/)


**This plugin provides a single and simple interface for accessing fingerprint APIs on both Android 6+ and iOS.**

## Features

* Check if a fingerprint scanner is available
* Fingerprint authentication
* Ionic Native support
* ngCordova support
* Fallback options
* Now with **FaceID** on iPhone X
* **⚡️ Works with [Capacitor](https://capacitor.ionicframework.com/). [Try it out](https://github.com/NiklasMerz/capacitor-fingerprint-app) ⚡️**

### Platforms

* Android - Minimum SDK 23
* iOS - **XCode 9.2 or higher** required
  * _Please set `<preference name="UseSwiftLanguageVersion" value="4.0" />` in your config.xml_


## How to use

**[Tutorial about using this plugin with Ionic](https://www.youtube.com/watch?v=tQDChMJ6er8)** thanks to Paul Halliday

[Examples](https://github.com/NiklasMerz/fingerprint-aio-demo)

[ngCordova Example](https://github.com/NiklasMerz/fingerprint-aio-demo/tree/ng-cordova)

[Ionic Native Example](https://github.com/NiklasMerz/fingerprint-aio-demo/tree/ionic-native)

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
Fingerprint.isAvailable(isAvailableSuccess, isAvailableError);

    function isAvailableSuccess(result) {
      /*
      result depends on device and os. 
      iPhone X will return 'face' other Android or iOS devices will return 'finger'  
      */
      alert("Fingerprint available");
    }

    function isAvailableError(error) {
      // 'error' will be an object with an error code and message
      alert(error.message);
    }
```

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
***

Thanks to the authors of the original fingerprint plugins

Some code is refactored from their projects and I learned how to make Cordova plugins from their great plugins:

@EddyVerbruggen and @mjwheatley

[Android](https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth)

[iOS](https://github.com/EddyVerbruggen/cordova-plugin-touch-id)

Starting with version 3.0.0 the iOS and Android parts are written from scratch.

## License

The project is MIT licensed: [MIT](https://opensource.org/licenses/MIT).

