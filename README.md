# Cordova Plugin Fingerprint All-In-One
## **Android** and **iOS**

[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/cordova-plugin-fingerprint-aio)

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/NiklasMerz/cordova-plugin-fingerprint-aio/master/LICENSE)



[![NPM](https://nodei.co/npm/cordova-plugin-fingerprint-aio.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/cordova-plugin-fingerprint-aio/)


This plugin is an attempt to provide a single interface for accessing fingerprint hardware on both Android 6+ and iOS.

There are some great cordova plugins out there that make use of the fingerprint APIs provided by Android and iOS. But I could not find a project which supports both platforms (correct me if I am wrong). I decided to take their native code and bundle it together in one plugin.

## Features

* Check if fingerprint scanner is available
* Fingerprint authentication
* ngCordova support - [Pull request](https://github.com/driftyco/ng-cordova/pull/1347)
* Ionic Native support - [Pull request](https://github.com/driftyco/ionic-native/pull/845)

### Platforms

* Android
* iOS - **XCode 8** required, plugin uses Swift 3


## How to use

---

**[Tutorial about using this plugin with Ionic](https://www.youtube.com/watch?v=tQDChMJ6er8)** thanks to Paul Halliday

[Examples](https://github.com/NiklasMerz/fingerprint-aio-demo)

[ngCordova Example](https://github.com/NiklasMerz/fingerprint-aio-demo/tree/ng-cordova)

[Ionic Native Example](https://github.com/NiklasMerz/fingerprint-aio-demo/tree/ionic-native)

---

### Install

Install from NPM

```
cordova plugin add cordova-plugin-fingerprint-aio --save
```

or use this Github repo

### Check if fingerprint authentication is available
```javascript
Fingerprint.isAvailable(isAvailableSuccess, isAvailableError);

    function isAvailableSuccess(result) {
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

__disableBackup__: If true remove backup option on authentication dialogue for Android. Default false.

## Thanks to the authors of the original fingerprint plugin:

[Android](https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth)

[iOS](https://github.com/EddyVerbruggen/cordova-plugin-touch-id)

## License

* Project and iOS source -> MIT
* Android source -> MIT and Apache 2.0
