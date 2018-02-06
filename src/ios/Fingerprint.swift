import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {
    
    fileprivate var policy:LAPolicy!
    
    func isAvailable(_ command: CDVInvokedUrlCommand){
        let authenticationContext = LAContext();
        var biometryType = "finger";
        var error:NSError?;
        
        let available = authenticationContext.canEvaluatePolicy(policy, error: &error);
        
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not available");
        if available == true {
            if #available(iOS 11.0, *) {
                switch(authenticationContext.biometryType) {
                case .none:
                    biometryType = "none";
                case .touchID:
                    biometryType = "finger";
                case .faceID:
                    biometryType = "face"
                }
            }

            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: biometryType);
        }
        
        commandDelegate.send(pluginResult, callbackId:command.callbackId);
    }
    
    func authenticate(_ command: CDVInvokedUrlCommand){
        let authenticationContext = LAContext();
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Something went wrong");
        var reason = "Authentication";
        let data  = command.arguments[0] as AnyObject?;
        
        if let disableBackup = data?["disableBackup"] as! Bool? {
            if disableBackup {
                authenticationContext.localizedFallbackTitle = "";
                policy = .deviceOwnerAuthenticationWithBiometrics;
            } else {
                if let localizedFallbackTitle = data?["localizedFallbackTitle"] as! String? {
                    authenticationContext.localizedFallbackTitle = localizedFallbackTitle;
                }
            }
        }
        
        // Localized reason
        if let localizedReason = data?["localizedReason"] as! String? {
            reason = localizedReason;
        }else if let clientId = data?["clientId"] as! String? {
            reason = clientId;
        }
        
        authenticationContext.evaluatePolicy(
            policy,
            localizedReason: reason,
            reply: { [unowned self] (success, error) -> Void in
                if( success ) {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Success");
                }else {
                    // Check if there is an error
                    if error != nil {
                        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error: \(String(describing: error?.localizedDescription))")
                    }
                }
                self.commandDelegate.send(pluginResult, callbackId:command.callbackId);
        });
    }
    
    override func pluginInitialize() {
        super.pluginInitialize()
        
        guard #available(iOS 9.0, *) else {
            policy = .deviceOwnerAuthenticationWithBiometrics
            return
        }
        
        policy = .deviceOwnerAuthentication
    }
}

