package de.niklasmerz.cordova.biometric;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

class EncryptedData {

    private static final String CIPHERTEXT_KEY_NAME = "__biometric-aio-ciphertext";
    private static final String IV_KEY_NAME = "__biometric-aio-iv";

    private byte[] ciphertext;
    private byte[] initializationVector;

    EncryptedData(byte[] ciphertext, byte[] initializationVector) {
        this.ciphertext = ciphertext;
        this.initializationVector = initializationVector;
    }

    static byte[] loadInitializationVector(Context context) throws CryptoException {
        return load(IV_KEY_NAME, context);
    }

    static byte[] loadCiphertext(Context context) throws CryptoException {
        return load(CIPHERTEXT_KEY_NAME, context);
    }

    void save(Context context) {
        save(IV_KEY_NAME, initializationVector, context);
        save(CIPHERTEXT_KEY_NAME, ciphertext, context);
    }

    private void save(String key, byte[] value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit()
                .putString(key, Base64.encodeToString(value, Base64.DEFAULT))
                .apply();
    }

    private static byte[] load(String key, Context context) throws CryptoException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String res = preferences.getString(key, null);
        if (res == null) throw new CryptoException(PluginError.BIOMETRIC_NO_SECRET_FOUND);
        return Base64.decode(res, Base64.DEFAULT);
    }
}
