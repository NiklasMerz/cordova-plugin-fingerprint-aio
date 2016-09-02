#import <Cordova/CDVPlugin.h>

@interface Fingerprint :CDVPlugin

- (void) isAvailable:(CDVInvokedUrlCommand*)command;

- (void) didFingerprintDatabaseChange:(CDVInvokedUrlCommand*)command;

- (void) authenticate:(CDVInvokedUrlCommand*)command;
- (void) verifyFingerprintWithCustomPasswordFallback:(CDVInvokedUrlCommand*)command;
- (void) verifyFingerprintWithCustomPasswordFallbackAndEnterPasswordLabel:(CDVInvokedUrlCommand*)command;

@end
