package de.niklasmerz.cordova.biometric;

class CryptoException extends RuntimeException {
    private PluginError error;

    CryptoException(String s, Exception e) {
        super(s, e);
    }

    CryptoException(PluginError error) {
        super(error.getMessage());
        this.error = error;
    }

    public PluginError getError() {
        return error;
    }
}
