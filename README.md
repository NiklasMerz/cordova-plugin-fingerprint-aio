# Cordova Plugin Fingerprint All-In-One
## **A** ndroid and **IO** s

[![npm](https://img.shields.io/npm/v/cordova-plugin-fingerprint-aio.svg?maxAge=2592000)](https://www.npmjs.com/package/cordova-plugin-fingerprint-aio)
[![npm](https://img.shields.io/npm/dt/cordova-plugin-fingerprint-aio.svg?maxAge=2592000)](https://www.npmjs.com/package/cordova-plugin-fingerprint-aio)


This plugin is an attempt to provide a single interface for accessing fingerprint hardware on both Android 6+ and iOS.

There are some great cordova plugins out there that make use of the fingerprint APIs provided by Android and iOS. But I could not find a project which supports both platforms (correct me if I am wrong). I decided to take their native code and bundle it together in one plugin.

## Features - Work in Progress
Use with caution at the moment. Still under development..

ngCordova support planned for the future.

### Current status

**iOS Work in Progress, Android Testing in Progress**

The new iOS implementation in Swift is currently under development. Android authentication is borrowed from the original plugin and seems to be working fine with the demo app (see below). The API is still in a constant stage of change but it will be similar to the implementation by Matthew Wheatley (@mjwheatly).

## Disclaimer
This is just an simpler implementation of the original plugins. At the moment the API could change every day and the plugin build could crash or have secutity issues. If you use fingerprint authentication in production use the plugins below.

## Thanks to the authors of the original fingerprint plugins:

https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth

https://github.com/EddyVerbruggen/cordova-plugin-touch-id

## How to use

[Example](https://github.com/NiklasMerz/fingerprint-aio-demo/blob/master/www/js/controllers.js)

For now refer to my demo app: https://github.com/NiklasMerz/fingerprint-aio-demo
