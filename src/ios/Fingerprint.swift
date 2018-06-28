import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {
    
    fileprivate var policy:LAPolicy!
    
    func isAvailable(_ command: CDVInvokedUrlCommand){
        let authenticationContext = LAContext();
        var biometryType = "finger";
        var error:NSError?;
        
        let available = authenticationContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error);
        
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not available");
        if available == true {
            if #available(iOS 11.0, *) {
                switch(authenticationContext.biometryType) {
                case .none:
                    biometryType = "none1";
                case .touchID:
                    biometryType = "finger1";
                case .faceID:
                    biometryType = "face1"
                }
            }

            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: biometryType);
        }else{
            
            if let theError = error {
                
                let messageObject = ["code":"\(theError.code)", "description": theError.localizedDescription]
                
                let data = try? JSONSerialization.data(withJSONObject: messageObject, options: JSONSerialization.WritingOptions.prettyPrinted)
                
                if let jsonData = data {
                    let messageStr = String.init(data: jsonData, encoding: .utf8)
                    if messageStr != nil {
                        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: messageStr)
                    }
                }
                
            }
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
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: reason,
            reply: { [unowned self] (success, error) -> Void in
                if( success ) {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Success");
                }else {
                    
                    let nsError = error as NSError?
                    
                    if let theError = nsError {
                        
                        let messageObject = ["code":"\(theError.code)", "description": theError.localizedDescription]
                        
                        let data = try? JSONSerialization.data(withJSONObject: messageObject, options: JSONSerialization.WritingOptions.prettyPrinted)
                        
                        if let jsonData = data {
                            let messageStr = String.init(data: jsonData, encoding: .utf8)
                            if messageStr != nil {
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: messageStr)
                            }
                        }
                    }
                    
                    // Check if there is an error andy
//                    if error != nil {
//                        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error: \(String(describing: error?.localizedDescription))")
//                    }
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

