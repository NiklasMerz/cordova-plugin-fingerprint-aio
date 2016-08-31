#import <Cordova/CDVPlugin.h>

@interface TouchID :CDVPlugin

- (void) isAvailable:(CDVInvokedUrlCommand*)command;

- (void) didFingerprintDatabaseChange:(CDVInvokedUrlCommand*)command;

- (void) authenticate:(CDVInvokedUrlCommand*)command;
- (void) verifyFingerprintWithCustomPasswordFallback:(CDVInvokedUrlCommand*)command;
- (void) verifyFingerprintWithCustomPasswordFallbackAndEnterPasswordLabel:(CDVInvokedUrlCommand*)command;

@end
