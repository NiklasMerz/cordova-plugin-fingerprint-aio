#import <Cordova/CDVPlugin.h>

@interface Test :CDVPlugin

- (void) isAvailable:(CDVInvokedUrlCommand*)command;
- (void) authenticate:(CDVInvokedUrlCommand*)command;

- (void) verifyFingerprintWithCustomPasswordFallback:(CDVInvokedUrlCommand*)command;

@end
