package de.niklasmerz.cordova.biometric;

public enum PluginError {

    BIOMETRIC_AUTHENTICATION_FAILED(-102, "Authentication failed"),
    BIOMETRIC_HARDWARE_NOT_SUPPORTED(-104),
    BIOMETRIC_NOT_ENROLLED(-106),
    BIOMETRIC_DISMISSED(-108),
    BIOMETRIC_PIN_OR_PATTERN_DISMISSED(-109),
    BIOMETRIC_SCREEN_GUARD_UNSECURED(-110,
            "Go to 'Settings -> Security -> Screenlock' to set up a lock screen"),
    BIOMETRIC_LOCKED_OUT(-111),
    BIOMETRIC_LOCKED_OUT_PERMANENT(-112),
    BIOMETRIC_ARGS_PARSING_FAILED(-113, "Couldn't parse args"),
    BIOMETRIC_NO_SECRET(-114, "No secret");

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
