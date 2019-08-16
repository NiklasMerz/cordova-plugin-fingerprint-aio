/*global cordova */

function Fingerprint() {
}

//@todo add to ionic native wrapper
//BIOMETRIC_UNKNOWN_ERROR = -100;
//BIOMETRIC_UNAVAILABLE = -101;
//BIOMETRIC_AUTHENTICATION_FAILED = -102;
//BIOMETRIC_SDK_NOT_SUPPORTED = -103;
//BIOMETRIC_HARDWARE_NOT_SUPPORTED = -104;
//BIOMETRIC_PERMISSION_NOT_GRANTED(-105),
//BIOMETRIC_FINGERPRINT_NOT_ENROLLED = -106;
//BIOMETRIC_INTERNAL_PLUGIN_ERROR = -107;
//BIOMETRIC_FINGERPRINT_DISMISSED = -108;
//BIOMETRIC_PIN_OR_PATTERN_DISMISSED = -109;
//BIOMETRIC_SCREEN_GUARD_UNSECURED = -110;
//BIOMETRIC_LOCKED_OUT = -111;
//BIOMETRIC_LOCKED_OUT_PERMANENT = -112;


Fingerprint.prototype.show = function (params, successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "Fingerprint",
    "authenticate",
    [params]
  );
};

Fingerprint.prototype.isAvailable = function (successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "Fingerprint",
    "isAvailable",
    [{}]
  );
};

module.exports = new Fingerprint();
