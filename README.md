# Cordova Plugin Fingerprint All-In-One
## **Android** and **iOS**

[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/cordova-plugin-fingerprint-aio)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/NiklasMerz/cordova-plugin-fingerprint-aio/master/LICENSE)
[![Build Status](https://travis-ci.org/NiklasMerz/cordova-plugin-fingerprint-aio.svg?branch=master)](https://travis-ci.org/NiklasMerz/cordova-plugin-fingerprint-aio)
[![Issue Count](https://codeclimate.com/github/NiklasMerz/cordova-plugin-fingerprint-aio/badges/issue_count.svg)](https://codeclimate.com/github/NiklasMerz/cordova-plugin-fingerprint-aio)

[![NPM](https://nodei.co/npm/cordova-plugin-fingerprint-aio.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/cordova-plugin-fingerprint-aio/)


**This plugin provides a single and simple interface for accessing fingerprint APIs on both Android 6+ and iOS.**

## Features

* Check if fingerprint scanner is available
* Fingerprint authentication
* Ionic Native support
* ngCordova support
* Fallback options
* Now with **FaceID** on iPhone X

### Platforms

* Android - Minimum SDK 23
* iOS - **XCode 9.2 or higher** required
  * _Please set `<preference name="UseSwiftLanguageVersion" value="3.2" />` in your config.xml_


## How to use

---

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

**Use Release candidate**

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

    function isAvailableError(message) {
      alert(message);
    }
```

### Show authentication dialogue
```javascript
Fingerprint.show({
      clientId: "Fingerprint-Demo",
      clientSecret: "password" //Only necessary for Android
    }, successCallback, errorCallback);

    function successCallback(){
      alert("Authentication successfull");
    }

    function errorCallback(err){
      alert("Authentication invalid " + err);
    }
```
**Optional parameters**

* __disableBackup__: If true remove backup option on authentication dialogue for Android. Default false.
* __localizedFallbackTitle__ (iOS only): Title of fallback button.
* __localizedReason__ (iOS only): Description in authentication dialogue.

## Thanks to the authors of the original fingerprint plugins

Some code is refactored from their projects and I learned how to make Cordova plugins from their great plugins:

@EddyVerbruggen and @mjwheatley

[Android](https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth)

[iOS](https://github.com/EddyVerbruggen/cordova-plugin-touch-id)

## License

* Project and iOS source -> MIT
* Android source -> MIT and Apache 2.0
