# Cordova Plugin Fingerprint All-In-One
## **A** ndroid and **IO** s

[![npm](https://img.shields.io/npm/v/cordova-plugin-fingerprint-aio.svg?maxAge=2592000)](https://www.npmjs.com/package/cordova-plugin-fingerprint-aio)
[![npm](https://img.shields.io/npm/dt/express.svg?maxAge=2592000)](https://www.npmjs.com/package/cordova-plugin-fingerprint-aio)
[![GitHub stars](https://img.shields.io/github/stars/badges/shields.svg?style=social&label=Star&maxAge=2592000)](https://github.com/NiklasMerz/cordova-plugin-fingerprint-aio)

This plugin is an attempt to provide a single interface for accessing fingerprint hardware on both Android 6+ and iOS.

**Contributors welcome for iOS testing and development**

There are some great cordova plugins out there that make use of the fingerprint APIs provided by Android and iOS. But I could not find a project which supports both platforms (correct me if I am wrong). I decided to take their native code and bundle it together in one plugin.

## Features - Work in Progress
Use with caution at the moment. Still under development..

ngCordova support planned for the future.

### Current status

**iOS Work in Progress, Android Testing in Progress**

Android authentication seems to be working fine with the demo app (see below). iOS needs to tested ( lack of Apple hardware).
The API is still in a constant stage of change but it will be similar to the implementation by Matthew Wheatley (@mjwheatly).

## Disclaimer
This is just an simpler implementation of the original plugins. At the moment the API could change every day and the plugin build could crash or have secutity issues. If you use fingerprint authentication in production use the plugins below.

## Thanks to the authors of the original fingerprint plugins:

https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth

https://github.com/EddyVerbruggen/cordova-plugin-touch-id

## How to use

For now refer to my demo app: https://github.com/NiklasMerz/fingerprint-aio-demo
