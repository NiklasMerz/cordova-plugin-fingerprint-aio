import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {

  func isAvailable(command: CDVInvokedUrlCommand){
    let authenticationContext = LAContext();
    var error:NSError?;

    let available = authenticationContext.canEvaluatePolicy(.DeviceOwnerAuthenticationWithBiometrics, error: &error);

    var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "Not available");
    if available == true {
      pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Available");
    }

    commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
  }

  func authenticate(command: CDVInvokedUrlCommand){
    let authenticationContext = LAContext();
    var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "Something went wrong");
    var reason = "Authentication";

    do {
      let json = try NSJSONSerialization.JSONObjectWithData(data, options: .AllowFragments);
      if let clientId = json["clientId"] as? String {
        reason = clientId;
      }
    } catch {
      pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "error serializing JSON: \(error)");
    }

    authenticationContext.evaluatePolicy(
      .DeviceOwnerAuthenticationWithBiometrics,
      localizedReason: reason,
      reply: { [unowned self] (success, error) -> Void in
        if success == true {
          pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Success");
        }else {
          // Check if there is an error
          if let error = error {
            let erroCode = String(error.code);
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "Error: \(errorCode)");
          }
        }
      });
      commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
    }
  }
