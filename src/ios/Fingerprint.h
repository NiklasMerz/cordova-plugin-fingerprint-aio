#import <Cordova/CDVPlugin.h>

@interface Fingerprint :CDVPlugin

- (void) isAvailable:(CDVInvokedUrlCommand*)command;
- (void) authenticate:(CDVInvokedUrlCommand*)command;

- (void) verifyFingerprintWithCustomPasswordFallback:(CDVInvokedUrlCommand*)command;

@end
