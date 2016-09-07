import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {
  func sayHello(command: CDVInvokedUrlCommand) {
    let message = "Hello !";

    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: message);
    commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
  }

  func isAvailable(command: CDVInvokedUrlCommand){
    var authenticationContext = LAContext();
    var error:NSError?

    var available = authenticationContext.canEvaluatePolicy(.DeviceOwnerAuthenticationWithBiometrics, error: &error);

    var statusCode = CDVCommandStatus_ERROR;
    var message = "isAvailable?"
    if(available){
      statusCode = CDVCommandStatus_OK;
    }

    let pluginResult = CDVPluginResult(status: statusCode, messageAsString: message);
    commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
  }

  func authenticate(command: CDVInvokedUrlCommand){
    //TODO class
    var authenticationContext = LAContext();

    authenticationContext.evaluatePolicy(
      .DeviceOwnerAuthenticationWithBiometrics,
      localizedReason: "Only awesome people are allowed",
      reply: { [unowned self] (success, error) -> Void in

      var statusCode = CDVCommandStatus_ERROR;
      var message = "auth?"
      if( success ) {
          statusCode = CDVCommandStatus_OK;
      }else {
          // Check if there is an error
          if let error = error {
              let message = error.code
          }
      }
    })
    let pluginResult = CDVPluginResult(status: statusCode, messageAsString: message);
    commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
  }
}
