import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {
  let authenticationContext = LAContext();

  func isAvailable(command: CDVInvokedUrlCommand){
    var error:NSError?

    let available = authenticationContext.canEvaluatePolicy(.DeviceOwnerAuthenticationWithBiometrics, error: &error);

    var statusCode = CDVCommandStatus_ERROR;
    var message = "Not available"
    if(available){
      statusCode = CDVCommandStatus_OK;
      message = "available";
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

        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "No success");
        if( success ) {
          pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Success");
        }else {
          // Check if there is an error
          if let error = error {
            //TODO error result
            print(error.code);
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "Error");
          }
        }
      });
      commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
    }
  }
