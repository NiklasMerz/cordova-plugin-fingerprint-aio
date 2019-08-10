import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint : CDVPlugin {

    @objc(isAvailable:)
    func isAvailable(_ command: CDVInvokedUrlCommand){
        let authenticationContext = LAContext();
        var biometryType = "finger";
        var errorResponse: [AnyHashable: Any] = [
            "code": 0,
            "message": "Not Available"
        ];
        var error:NSError?;
        let policy:LAPolicy = .deviceOwnerAuthenticationWithBiometrics;

        let available = authenticationContext.canEvaluatePolicy(policy, error: &error);

        if(error != nil){
            biometryType = "none";
            errorResponse["code"] = error?.code;
            errorResponse["message"] = error?.localizedDescription;
        }

        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResponse);
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


    @objc(authenticate:)
    func authenticate(_ command: CDVInvokedUrlCommand){
        let authenticationContext = LAContext();
        var errorResponse: [AnyHashable: Any] = [
            "message": "Something went wrong"
        ];
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResponse);
        var reason = "Authentication";
        var policy:LAPolicy = .deviceOwnerAuthentication;
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
                        errorResponse["message"] = error?.localizedDescription;
                        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResponse)
                    }
                }
                self.commandDelegate.send(pluginResult, callbackId:command.callbackId);
        });
    }

    override func pluginInitialize() {
        super.pluginInitialize()
    }
}

