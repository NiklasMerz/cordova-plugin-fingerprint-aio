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
import java.security.UnrecoverableKeyException;
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

    private SecretKey createSecretKey(String keyName, boolean invalidateOnEnrollment, Context context) throws CryptoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return createSecretKeyNew(keyName, invalidateOnEnrollment);
        } else {
            return getOrCreateSecretKeyOld(keyName, context);
        }
    }

    private SecretKey getSecretKey(String keyName, Context context) throws CryptoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getSecretKey(keyName);
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

    private SecretKey getSecretKey(String keyName) throws  CryptoException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null); // Keystore must be loaded before it can be accessed
            return (SecretKey) keyStore.getKey(keyName, null);
        } catch (Exception e){
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey createSecretKeyNew(String keyName, boolean invalidateOnEnrollment) throws CryptoException {
        try {
            // if you reach here, then a new SecretKey must be generated for that keyName
            KeyGenParameterSpec.Builder keyGenParamsBuilder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationValidityDurationSeconds(-1)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                keyGenParamsBuilder.setInvalidatedByBiometricEnrollment(invalidateOnEnrollment);
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
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
            SecretKey secretKey;
            try {
                secretKey = getSecretKey(keyName, context);
                if(secretKey == null) {
                    secretKey = createSecretKey(keyName, invalidateOnEnrollment, context);
                }
            } catch (CryptoException e) {
                EncryptedData.remove(context);
                secretKey = createSecretKey(keyName, invalidateOnEnrollment, context);
            }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && e instanceof KeyPermanentlyInvalidatedException || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && e.getCause() instanceof UnrecoverableKeyException) {
            removeKey(keyName);
            throw new KeyInvalidatedException();
        }
    }

    @Override
    public Cipher getInitializedCipherForDecryption(String keyName, byte[] initializationVector, Context context) throws CryptoException {
        try {
            Cipher cipher = getCipher();
            SecretKey secretKey = getSecretKey(keyName, context);
            if(secretKey == null){
                throw new KeyInvalidatedException();
            }
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
            keyStore.deleteEntry(keyName); //TODO Check why can't delete a key was previously invalidated by new enrollment or disable security pattern
        } catch (Exception e) {
            throw new KeyInvalidatedException(); //TODO Manage proper exception after deal with deleteEntry issue instead return always BIOMETRIC_NO_SECRET_FOUND and retry flow
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
