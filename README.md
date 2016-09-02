# Cordova Plugin Fingerprint All-In-One

[![npm version](https://badge.fury.io/js/cordova-plugin-fingerprint-aio.svg)](https://badge.fury.io/js/cordova-plugin-fingerprint-aio)
[![GitHub version](https://badge.fury.io/gh/niklasmerz%2Fcordova-plugin-fingerprint-aio.svg)](https://badge.fury.io/gh/niklasmerz%2Fcordova-plugin-fingerprint-aio)

This plugin is my attempt to provide a single interface for accessing fingerprint hardware on both Android 6+ and iOS.

There are some great cordova plugins out there that make use of the fingerprint APIs provided by Android and iOS. But I could not find a project which supports both platforms (correct me if I am wrong). So I decided to take the native code and bundle it together in one plugin.

## Work in Progress
Use with caution at the moment. Still under development..

ngCordova support planed for the future.

### Current status
Android authentication seems to be working fine with the demo app (see below). iOS needs to tested ( lack of Apple hardware).
The API is still in a constant stage of change but it will be similar to the implementation of Matthew Wheatley (@mjwheatly).

## Thanks to the authors of the original fingerprint plugins:

https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth

https://github.com/EddyVerbruggen/cordova-plugin-touch-id

## How to use

For now refer to my demo app: https://github.com/NiklasMerz/fingerprint-aio-demo
