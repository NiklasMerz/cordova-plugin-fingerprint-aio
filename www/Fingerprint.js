/*global cordova */

function Fingerprint() {
}

//@todo add to ionic native wrapper
//Fingerprint.BIOMETRIC_UNKNOWN_ERROR = -100;
//Fingerprint.BIOMETRIC_UNAVAILABLE = -101;
//Fingerprint.BIOMETRIC_AUTHENTICATION_FAILED = -102;
//Fingerprint.BIOMETRIC_SDK_NOT_SUPPORTED = -103;
//Fingerprint.BIOMETRIC_HARDWARE_NOT_SUPPORTED = -104;
//Fingerprint.BIOMETRIC_PERMISSION_NOT_GRANTED(-105),
//Fingerprint.BIOMETRIC_FINGERPRINT_NOT_ENROLLED = -106;
//Fingerprint.BIOMETRIC_INTERNAL_PLUGIN_ERROR = -107;
//Fingerprint.BIOMETRIC_FINGERPRINT_DISMISSED = -108;
//Fingerprint.BIOMETRIC_PIN_OR_PATTERN_DISMISSED = -109;
//Fingerprint.BIOMETRIC_SCREEN_GUARD_UNSECURED = -110;
//Fingerprint.BIOMETRIC_LOCKED_OUT = -111;
//Fingerprint.BIOMETRIC_LOCKED_OUT_PERMANENT = -112;


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
