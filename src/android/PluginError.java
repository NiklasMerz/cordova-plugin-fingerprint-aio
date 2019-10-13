package de.niklasmerz.cordova.biometric;

public enum PluginError {

    BIOMETRIC_UNKNOWN_ERROR(-100),
    BIOMETRIC_UNAVAILABLE(-101),
    BIOMETRIC_AUTHENTICATION_FAILED(-102, "Authentication failed"),
    BIOMETRIC_SDK_NOT_SUPPORTED(-103),
    BIOMETRIC_HARDWARE_NOT_SUPPORTED(-104),
    BIOMETRIC_PERMISSION_NOT_GRANTED(-105),
    BIOMETRIC_FINGERPRINT_NOT_ENROLLED(-106),
    BIOMETRIC_INTERNAL_PLUGIN_ERROR(-107),
    BIOMETRIC_FINGERPRINT_DISMISSED(-108),
    BIOMETRIC_PIN_OR_PATTERN_DISMISSED(-109),
    BIOMETRIC_SCREEN_GUARD_UNSECURED(-110,
            "Go to 'Settings -> Security -> Screenlock' to set up a lock screen"),
    BIOMETRIC_LOCKED_OUT(-111),
    BIOMETRIC_LOCKED_OUT_PERMANENT(-112);

    private int value;
    private String message;

    PluginError(int value) {
        this.value = value;
        this.message = this.name();
    }

    PluginError(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
