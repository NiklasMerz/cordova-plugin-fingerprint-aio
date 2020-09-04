import Foundation
import LocalAuthentication

enum PluginError:Int {
    case BIOMETRIC_UNKNOWN_ERROR = -100
    case BIOMETRIC_UNAVAILABLE = -101
    case BIOMETRIC_AUTHENTICATION_FAILED = -102
    case BIOMETRIC_PERMISSION_NOT_GRANTED = -105
    case BIOMETRIC_NOT_ENROLLED = -106
    case BIOMETRIC_DISMISSED = -108
    case BIOMETRIC_SCREEN_GUARD_UNSECURED = -110
    case BIOMETRIC_LOCKED_OUT = -111
    case BIOMETRIC_SECRET_NOT_FOUND = -113
}

@objc(Fingerprint) class Fingerprint : CDVPlugin {

    struct ErrorCodes {
        var code: Int
    }


    @objc(isAvailable:)
    func isAvailable(_ command: CDVInvokedUrlCommand){
        let authenticationContext = LAContext();
        var biometryType = "finger";
        var errorResponse: [AnyHashable: Any] = [
            "code": 0,
            "message": "Not Available"
        ];
        var error:NSError?;
        let params = command.argument(at: 0) as? [AnyHashable: Any] ?? [:]
        let allowBackup = params["allowBackup"] as? Bool ?? false
        let policy:LAPolicy = allowBackup ? .deviceOwnerAuthentication : .deviceOwnerAuthenticationWithBiometrics;
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not available");
        let available = authenticationContext.canEvaluatePolicy(policy, error: &error);

        var results: [String : Any]

        if(error != nil){
            biometryType = "none";
            errorResponse["code"] = error?.code;
            errorResponse["message"] = error?.localizedDescription;
        }

        if (available == true) {
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
        }else{
            var code: Int;
            switch(error!._code) {
                case Int(kLAErrorBiometryNotAvailable):
                    code = PluginError.BIOMETRIC_UNAVAILABLE.rawValue;
                    break;
                case Int(kLAErrorBiometryNotEnrolled):
                    code = PluginError.BIOMETRIC_NOT_ENROLLED.rawValue;
                    break;

                default:
                    code = PluginError.BIOMETRIC_UNKNOWN_ERROR.rawValue;
                    break;
            }
            results = ["code": code, "message": error!.localizedDescription];
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: results);
        }

        commandDelegate.send(pluginResult, callbackId:command.callbackId);
    }

    func justAuthenticate(_ command: CDVInvokedUrlCommand) {
        let authenticationContext = LAContext();
        var errorResponse: [AnyHashable: Any] = [
            "message": "Something went wrong"
        ];
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResponse);
        var reason = "Authentication";
        var policy:LAPolicy = .deviceOwnerAuthentication;
        let data  = command.arguments[0] as? [String: Any];

        if let disableBackup = data?["disableBackup"] as! Bool? {
            if disableBackup {
                authenticationContext.localizedFallbackTitle = "";
                policy = .deviceOwnerAuthenticationWithBiometrics;
            } else {
                if let fallbackButtonTitle = data?["fallbackButtonTitle"] as! String? {
                    authenticationContext.localizedFallbackTitle = fallbackButtonTitle;
                }else{
                    authenticationContext.localizedFallbackTitle = "Use Pin";
                }
            }
        }

        // Localized reason
        if let description = data?["description"] as! String? {
            reason = description;
        }

        authenticationContext.evaluatePolicy(
            policy,
            localizedReason: reason,
            reply: { [unowned self] (success, error) -> Void in
                if( success ) {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Success");
                }else {
                    if (error != nil) {

                        var errorCodes = [Int: ErrorCodes]()
                        var errorResult: [String : Any] = ["code":  PluginError.BIOMETRIC_UNKNOWN_ERROR.rawValue, "message": error?.localizedDescription ?? ""];

                        errorCodes[1] = ErrorCodes(code: PluginError.BIOMETRIC_AUTHENTICATION_FAILED.rawValue)
                        errorCodes[2] = ErrorCodes(code: PluginError.BIOMETRIC_DISMISSED.rawValue)
                        errorCodes[5] = ErrorCodes(code: PluginError.BIOMETRIC_SCREEN_GUARD_UNSECURED.rawValue)
                        errorCodes[6] = ErrorCodes(code: PluginError.BIOMETRIC_UNAVAILABLE.rawValue)
                        errorCodes[7] = ErrorCodes(code: PluginError.BIOMETRIC_NOT_ENROLLED.rawValue)
                        errorCodes[8] = ErrorCodes(code: PluginError.BIOMETRIC_LOCKED_OUT.rawValue)

                        let errorCode = abs(error!._code)
                        if let e = errorCodes[errorCode] {
                           errorResult = ["code": e.code, "message": error!.localizedDescription];
                        }

                        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResult);
                    }
                }
                self.commandDelegate.send(pluginResult, callbackId:command.callbackId);
            }
        );
    }

    func saveSecret(_ secretStr: String, command: CDVInvokedUrlCommand) {
        let data  = command.arguments[0] as AnyObject?;
        var pluginResult: CDVPluginResult
        do {
            let secret = Secret()
            try? secret.delete()
            let invalidateOnEnrollment = (data?["invalidateOnEnrollment"] as? Bool) ?? false
            try secret.save(secretStr, invalidateOnEnrollment: invalidateOnEnrollment)
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Success");
        } catch {
            let errorResult = ["code": PluginError.BIOMETRIC_UNKNOWN_ERROR.rawValue, "message": error.localizedDescription] as [String : Any];
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResult);
        }
        self.commandDelegate.send(pluginResult, callbackId:command.callbackId)
        return
    }


    func loadSecret(_ command: CDVInvokedUrlCommand) {
        let data  = command.arguments[0] as AnyObject?;
        var prompt = "Authentication"
        if let description = data?["description"] as! String? {
            prompt = description;
        }
        var pluginResult: CDVPluginResult
        do {
            let result = try Secret().load(prompt)
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result);
        } catch {
            var code = PluginError.BIOMETRIC_UNKNOWN_ERROR.rawValue
            var message = error.localizedDescription
            if let err = error as? KeychainError {
                code = err.pluginError.rawValue
                message = err.localizedDescription
            }
            let errorResult = ["code": code, "message": message] as [String : Any]
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorResult);
        }
        self.commandDelegate.send(pluginResult, callbackId:command.callbackId)
    }

    @objc(authenticate:)
    func authenticate(_ command: CDVInvokedUrlCommand){
        justAuthenticate(command)
    }

    @objc(registerBiometricSecret:)
    func registerBiometricSecret(_ command: CDVInvokedUrlCommand){
        let data  = command.arguments[0] as AnyObject?;
        if let secret = data?["secret"] as? String {
            self.saveSecret(secret, command: command)
            return
        }
    }

    @objc(loadBiometricSecret:)
    func loadBiometricSecret(_ command: CDVInvokedUrlCommand){
        self.loadSecret(command)
    }

    override func pluginInitialize() {
        super.pluginInitialize()
    }

}

/// Keychain errors we might encounter.
struct KeychainError: Error {
    var status: OSStatus

    var localizedDescription: String {
        if #available(iOS 11.3, *) {
            if let result = SecCopyErrorMessageString(status, nil) as String? {
                return result
            }
        }
        switch status {
            case errSecItemNotFound:
                return "Secret not found"
            case errSecUserCanceled:
                return "Biometric dissmissed"
            case errSecAuthFailed:
                return "Authentication failed"
            default:
                return "Unknown error \(status)"
        }
    }

    var pluginError: PluginError {
        switch status {
        case errSecItemNotFound:
            return PluginError.BIOMETRIC_SECRET_NOT_FOUND
        case errSecUserCanceled:
            return PluginError.BIOMETRIC_DISMISSED
        case errSecAuthFailed:
                return PluginError.BIOMETRIC_AUTHENTICATION_FAILED
        default:
            return PluginError.BIOMETRIC_UNKNOWN_ERROR
        }
    }
}

class Secret {

    private static let keyName: String = "__aio_key"

    private func getBioSecAccessControl(invalidateOnEnrollment: Bool) -> SecAccessControl {
        var access: SecAccessControl?
        var error: Unmanaged<CFError>?

        if #available(iOS 11.3, *) {
            access = SecAccessControlCreateWithFlags(nil,
                kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
                invalidateOnEnrollment ? .biometryCurrentSet : .userPresence,
                &error)
        } else {
            access = SecAccessControlCreateWithFlags(nil,
                kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
                invalidateOnEnrollment ? .touchIDCurrentSet : .userPresence,
                &error)
        }
        precondition(access != nil, "SecAccessControlCreateWithFlags failed")
        return access!
    }

    func save(_ secret: String, invalidateOnEnrollment: Bool) throws {
        let password = secret.data(using: String.Encoding.utf8)!

        // Allow a device unlock in the last 10 seconds to be used to get at keychain items.
        // let context = LAContext()
        // context.touchIDAuthenticationAllowableReuseDuration = 10

        // Build the query for use in the add operation.
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrAccount as String: Secret.keyName,
                                    kSecAttrAccessControl as String: getBioSecAccessControl(invalidateOnEnrollment: invalidateOnEnrollment),
                                    kSecValueData as String: password]

        let status = SecItemAdd(query as CFDictionary, nil)
        guard status == errSecSuccess else { throw KeychainError(status: status) }
    }

    func load(_ prompt: String) throws -> String {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrAccount as String: Secret.keyName,
                                    kSecMatchLimit as String: kSecMatchLimitOne,
                                    kSecReturnData as String : kCFBooleanTrue,
                                    kSecAttrAccessControl as String: getBioSecAccessControl(invalidateOnEnrollment: true),
                                    kSecUseOperationPrompt as String: prompt]

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess else { throw KeychainError(status: status) }

        guard let passwordData = item as? Data,
            let password = String(data: passwordData, encoding: String.Encoding.utf8)
            // let account = existingItem[kSecAttrAccount as String] as? String
            else {
                throw KeychainError(status: errSecInternalError)
        }

        return password
    }

    func delete() throws {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrAccount as String: Secret.keyName]

        let status = SecItemDelete(query as CFDictionary)
        guard status == errSecSuccess else { throw KeychainError(status: status) }
    }
}
