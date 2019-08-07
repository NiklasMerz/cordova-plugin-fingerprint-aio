import Foundation
import LocalAuthentication

@objc(Fingerprint) class Fingerprint: CDVPlugin {
    
    override func pluginInitialize() {
        super.pluginInitialize()
    }
    
    /// Checks what kind of biometric authentication is available on the current device.
    ///
    /// - Parameter command: The command containing the callbackId that is being notified about the result of the availability check.
    @objc func isAvailable(_ command: CDVInvokedUrlCommand) {
        let authenticationContext = LAContext()
        var error: NSError?
        if authenticationContext.canEvaluatePolicy(LAPolicy.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            if #available(iOS 11.0, *) {
                switch(authenticationContext.biometryType) {
                case .none:
                    send(BiometricAuthenticationMethod.none.rawValue, as: .success, to: command.callbackId)
                case .touchID:
                    send(BiometricAuthenticationMethod.touchID.rawValue, as: .success, to: command.callbackId)
                case .faceID:
                    send(BiometricAuthenticationMethod.faceID.rawValue, as: .success, to: command.callbackId)
                }
            } else {
                send(BiometricAuthenticationMethod.touchID.rawValue, as: .success, to: command.callbackId)
            }
        } else {
            send(error?.localizedDescription, as: .error, to: command.callbackId)
        }
    }
    
    /// Authenticates the user via biometric authentication.
    ///
    /// - Parameter command: The command containing the callbackId that is being notified about the result of the authentication and optional parameters (localizedFallbackTitle and localizedReason).
    @objc func authenticate(_ command: CDVInvokedUrlCommand) {
        var reason = "Authentication"
        let authenticationContext = LAContext()
        var policy = LAPolicy.deviceOwnerAuthentication
        let customParameters = command.arguments[0] as AnyObject?
        
        // Use localized fallback title from command if available
        if let localizedFallbackTitle = customParameters?["localizedFallbackTitle"] as? String {
            authenticationContext.localizedFallbackTitle = localizedFallbackTitle
        }
        
        // Disable localized fallback title if requested in command
        if let disableBackup = customParameters?["disableBackup"] as? Bool, disableBackup {
            authenticationContext.localizedFallbackTitle = ""
            policy = .deviceOwnerAuthenticationWithBiometrics
        }
        
        // Use localized reason or clientID from command if available
        if let localizedReason = customParameters?["localizedReason"] as? String {
            reason = localizedReason
        } else if let clientId = customParameters?["clientId"] as? String {
            reason = clientId;
        }
        
        // Show the biometric authentication dialog
        authenticationContext.evaluatePolicy(policy, localizedReason: reason) { (success, error) -> Void in
            if success {
                self.send("Success", as: .success, to: command.callbackId)
            } else {
                let errorMessage = error?.localizedDescription ?? "Unknown error."
                self.send(errorMessage, as: .error, to: command.callbackId)
            }
        }
    }
    
    // MARK: - Private Stuff
    
    /// Sends the callback message to the given callback ID of the command.
    ///
    /// - Parameters:
    ///   - message: The message to be sent.
    ///   - resultCase: The result case (success or error) which will be converted to the corresponding CDVCommandStatus.
    ///   - callbackID: The callback ID of the command which will be used to send the callback to the correct receiver.
    private func send(_ message: String?, as resultCase: PluginResultCase, to callbackID: String) {
        let pluginResult = resultCase.isSuccess ? CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message) : CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: message)
        commandDelegate.send(pluginResult, callbackId:callbackID)
    }
    
    // MARK: - Biometric Authentication Method Options
    
    private enum BiometricAuthenticationMethod: String {
        // Attention: The raw value (String) has to be identical to the Android callback!
        case none
        case touchID = "finger"
        case faceID = "face"
    }
    
    // MARK: - Plugin Result Cases
    
    private enum PluginResultCase {
        case success
        case error
        
        var isSuccess: Bool {
            switch self {
            case .success: return true
            case .error: return false
            }
        }
    }
    
}
