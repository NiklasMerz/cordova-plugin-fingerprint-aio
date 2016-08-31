function FingerprintAuth() {
}

FingerprintAuth.prototype.show = function (params, successCallback, errorCallback) {
    cordova.exec(
        successCallback,
        errorCallback,
        "FingerprintAuth",  // Java Class
        "authenticate", // action
        [ // Array of arguments to pass to the Java class
            params
        ]
    );
};

FingerprintAuth.prototype.isAvailable = function (successCallback, errorCallback) {
    cordova.exec(
        successCallback,
        errorCallback,
        "FingerprintAuth",  // Java Class
        "availability", // action
        [{}]
    );
};

FingerprintAuth = new FingerprintAuth();
module.exports = FingerprintAuth;