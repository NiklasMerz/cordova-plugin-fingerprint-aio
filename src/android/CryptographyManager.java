package de.niklasmerz.cordova.biometric;

import android.content.Context;

import javax.crypto.Cipher;

interface CryptographyManager {

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [ENCRYPT_MODE][Cipher.ENCRYPT_MODE] is used.
     */
    Cipher getInitializedCipherForEncryption(String keyName, boolean invalidateOnEnrollment, Context context) throws CryptoException;

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [DECRYPT_MODE][Cipher.DECRYPT_MODE] is used.
     */
    Cipher getInitializedCipherForDecryption(String keyName, byte[] initializationVector, Context context) throws CryptoException;

    /**
     * The Cipher created with [getInitializedCipherForEncryption] is used here
     */
    EncryptedData encryptData(String plaintext, Cipher cipher) throws CryptoException;

    /**
     * The Cipher created with [getInitializedCipherForDecryption] is used here
     */
    String decryptData(byte[] ciphertext, Cipher cipher) throws CryptoException;

}
