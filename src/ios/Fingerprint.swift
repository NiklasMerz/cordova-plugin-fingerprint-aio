import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {

  func isAvailable(command: CDVInvokedUrlCommand){
    let authenticationContext = LAContext();
    var error:NSError?;

    let available = authenticationContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error);

    var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not available");
    if available == true {
      pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Available");
    }

    commandDelegate.send(pluginResult, callbackId:command.callbackId);
  }

  func authenticate(command: CDVInvokedUrlCommand){
    let authenticationContext = LAContext();
    var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Something went wrong");
    var reason = "Authentication";
    let data  = command.arguments[0] as AnyObject?;

    if let clientId = data?["clientId"] as! String? {
      reason = clientId;
    }


    authenticationContext.evaluatePolicy(
      .deviceOwnerAuthenticationWithBiometrics,
      localizedReason: reason,
      reply: { [unowned self] (success, error) -> Void in
        if( success ) {
          pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Success");
        }else {
          // Check if there is an error
          if error != nil {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error: \(error)");
          }
        }
        self.commandDelegate.send(pluginResult, callbackId:command.callbackId);
      });
    }
  }
