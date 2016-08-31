function Fingerprint() {
}

Fingerprint.prototype.show = function (params, successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "Fingerprint",
    "authenticate",
    [ params ]
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

//TODO Android
Fingerprint.prototype.didFingerprintDatabaseChange = function (successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "Fingerprint",
    "didFingerprintDatabaseChange",
    []
  );
};

//TODO Android
Fingerprint.prototype.verifyFingerprintWithCustomPasswordFallback = function (message, successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "Fingerprint",
    "verifyFingerprintWithCustomPasswordFallback",
    [message]
  );
};

//TODO Android
Fingerprint.prototype.verifyFingerprintWithCustomPasswordFallbackAndEnterPasswordLabel = function (message, enterPasswordLabel, successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "Fingerprint",
    "verifyFingerprintWithCustomPasswordFallbackAndEnterPasswordLabel",
    [message, enterPasswordLabel]
  );
};


Fingerprint = new Fingerprint();
module.exports = Fingerprint;
