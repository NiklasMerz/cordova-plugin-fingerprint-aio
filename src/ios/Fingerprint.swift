import Foundation

@objc(Fingerprint) class Fingerprint : CDVPlugin {
  func sayHello(command: CDVInvokedUrlCommand) {
    let message = "Hello !";

    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: message);
    commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId);
  }

  func isAvailable(command: CDVInvokedUrlCommand){

  }

  func authenticate(command: CDVInvokedUrlCommand){
    
  }
}
