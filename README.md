# Cordova Plugin Fingerprint All-In-One
## **A** ndroid and **IO** s

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

[Example](https://github.com/NiklasMerz/fingerprint-aio-demo)

[ngCordova Example](https://github.com/NiklasMerz/fingerprint-aio-demo/tree/ng-cordova)

[Ionic Native Example](https://github.com/NiklasMerz/fingerprint-aio-demo/tree/ionic-native)


Demo app: https://github.com/NiklasMerz/fingerprint-aio-demo

## Thanks to the authors of the original fingerprint plugin:

[Android](https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth)

[iOS](https://github.com/EddyVerbruggen/cordova-plugin-touch-id)

## License

* Project and iOS source -> MIT
* Android source -> MIT and Apache 2.0
