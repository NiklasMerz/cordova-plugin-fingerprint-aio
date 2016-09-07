import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {
  let authenticationContext = LAContext();

  func isAvailable(command: CDVInvokedUrlCommand){
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
    //TODO params

    authenticationContext.evaluatePolicy(
      .DeviceOwnerAuthenticationWithBiometrics,
      localizedReason: "authenticate",
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
