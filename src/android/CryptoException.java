package de.niklasmerz.cordova.biometric;

class CryptoException extends Exception {
    private PluginError error;

    CryptoException(String message, Exception cause) {
        this(PluginError.BIOMETRIC_UNKNOWN_ERROR, message, cause);
    }

    CryptoException(PluginError error) {
        this(error, error.getMessage(), null);
    }

    CryptoException(PluginError error, Exception cause) {
        this(error, error.getMessage(), cause);
    }

    private CryptoException(PluginError error, String message, Exception cause) {
        super(message, cause);
        this.error = error;
    }

    public PluginError getError() {
        return error;
    }
}
