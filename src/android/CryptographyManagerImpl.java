package de.niklasmerz.cordova.biometric;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import androidx.annotation.RequiresApi;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.security.auth.x500.X500Principal;

class CryptographyManagerImpl implements CryptographyManager {

    private static final int KEY_SIZE = 256;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ENCRYPTION_PADDING = "NoPadding"; // KeyProperties.ENCRYPTION_PADDING_NONE
    private static final String ENCRYPTION_ALGORITHM = "AES"; // KeyProperties.KEY_ALGORITHM_AES
    private static final String KEY_ALGORITHM_AES = "AES"; // KeyProperties.KEY_ALGORITHM_AES
    private static final String ENCRYPTION_BLOCK_MODE = "GCM"; // KeyProperties.BLOCK_MODE_GCM

    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        String transformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_BLOCK_MODE + "/" + ENCRYPTION_PADDING;
        return Cipher.getInstance(transformation);
    }

    private SecretKey getOrCreateSecretKey(String keyName, boolean invalidateOnEnrollment, Context context) throws CryptoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getOrCreateSecretKeyNew(keyName, invalidateOnEnrollment);
        } else {
            return getOrCreateSecretKeyOld(keyName, context);
        }
    }

    private SecretKey getOrCreateSecretKeyOld(String keyName, Context context) throws CryptoException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);
        try {
            KeyPairGeneratorSpec keySpec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(keyName)
                    .setSubject(new X500Principal("CN=FINGERPRINT_AIO ," +
                            " O=FINGERPRINT_AIO" +
                            " C=World"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            kg.init(keySpec);
            return kg.generateKey();
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey getOrCreateSecretKeyNew(String keyName, boolean invalidateOnEnrollment) throws CryptoException {
        try {
            // If Secretkey was previously created for that keyName, then grab and return it.
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null); // Keystore must be loaded before it can be accessed


            SecretKey key = (SecretKey) keyStore.getKey(keyName, null);
            if (key != null) {
                return key;
            }

            // if you reach here, then a new SecretKey must be generated for that keyName
            KeyGenParameterSpec.Builder keyGenParamsBuilder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationRequired(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                keyGenParamsBuilder.setInvalidatedByBiometricEnrollment(invalidateOnEnrollment);
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE);
            keyGenerator.init(keyGenParamsBuilder.build());

            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public Cipher getInitializedCipherForEncryption(String keyName, boolean invalidateOnEnrollment, Context context) throws CryptoException {
        try {
            Cipher cipher = getCipher();
            SecretKey secretKey = getOrCreateSecretKey(keyName, invalidateOnEnrollment, context);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher;
        } catch (Exception e) {
            try {
                handleException(e, keyName);
            } catch (KeyInvalidatedException kie) {
                return getInitializedCipherForEncryption(keyName, invalidateOnEnrollment, context);
            }
            throw new CryptoException(e.getMessage(), e);
        }
    }

    private void handleException(Exception e, String keyName) throws CryptoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && e instanceof KeyPermanentlyInvalidatedException) {
            removeKey(keyName);
            throw new KeyInvalidatedException();
        }
    }

    @Override
    public Cipher getInitializedCipherForDecryption(String keyName, byte[] initializationVector, Context context) throws CryptoException {
        try {
            Cipher cipher = getCipher();
            SecretKey secretKey = getOrCreateSecretKey(keyName, true, context);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, initializationVector));
            return cipher;
        } catch (Exception e) {
            handleException(e, keyName);
            throw new CryptoException(e.getMessage(), e);
        }
    }

    private void removeKey(String keyName) throws CryptoException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null); // Keystore must be loaded before it can be accessed
            keyStore.deleteEntry(keyName);
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public EncryptedData encryptData(String plaintext, Cipher cipher) throws CryptoException {
        try {
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedData(ciphertext, cipher.getIV());
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public String decryptData(byte[] ciphertext, Cipher cipher) throws CryptoException {
        try {
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }
}
