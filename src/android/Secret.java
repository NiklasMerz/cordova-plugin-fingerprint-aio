package de.niklasmerz.cordova.biometric;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;

public class Secret {

    private static final String KEY_NAME = "key_for_secret";
    private static final String KEY_STORE = "AndroidKeyStore";

    private static final String SECRET_KEY_NAME = "__biometric-aio-secret";
    private static final String IV_KEY_NAME = "__biometric-aio-iv";

    public static void save(String secret, Context context) throws CryptoException, KeyInvalidatedException {
        if (secret == null) {
            return;
        }
        Cipher cipher = getEncryptionCipher(context);
        try {
            byte[] bytes = cipher.doFinal(secret.getBytes());
            String encrypted = Base64.encodeToString(bytes, Base64.NO_WRAP);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit()
                    .putString(SECRET_KEY_NAME, encrypted)
                    .apply();

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException("Couldn't save secret", e);
        }
    }

    public static String load(Cipher cipher, Context context) throws CryptoException, KeyInvalidatedException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String encryptedSecret = preferences.getString(SECRET_KEY_NAME, null);
        if (encryptedSecret == null) {
            return null;
        }
        try {
            if (cipher == null) {
                cipher = getDecryptionCipher(context);
            }
            byte[] bytes = Base64.decode(encryptedSecret, Base64.NO_WRAP);
            return new String(cipher.doFinal(bytes));
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException("Couldn't load secret", e);
        }

    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("AES/CBC/PKCS7Padding");
    }

    private static Key getSecretKey(Context context)
            throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException,
            IOException, UnrecoverableKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_NAME)) {
            generateSecretKey(context);
        }
        return keyStore.getKey(KEY_NAME, null);
    }

    private static void generateSecretKey(Context context) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            generateSecretKeyM();
        } else {
            generateSecretKeyOld(context);
        }
    }

    private static void generateSecretKeyOld(Context context) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_NAME)
                .setSubject(new X500Principal("CN=FINGERPRINT_AIO ," +
                        " O=FINGERPRINT_AIO" +
                        " C=World"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", KEY_STORE);

        generator.initialize(spec);
        generator.generateKeyPair();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void generateSecretKeyM() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, KEY_STORE);

        KeyGenParameterSpec.Builder specBuilder = new KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationValidityDurationSeconds(-1)
                .setUserAuthenticationRequired(true);

        // Invalidate the keys if the user has registered a new biometric
        // credential, such as a new fingerprint. Can call this method only
        // on Android 7.0 (API level 24) or higher. The variable
        // "invalidatedByBiometricEnrollment" is true by default.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            specBuilder.setInvalidatedByBiometricEnrollment(true);
        }

        keyGenerator.init(specBuilder.build());
        keyGenerator.generateKey();
    }

    private static void deleteInvalidKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
            keyStore.load(null);
            keyStore.deleteEntry(KEY_NAME);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    static Cipher getEncryptionCipher(Context context) throws CryptoException, KeyInvalidatedException {
        try {
            Cipher cipher = getCipher();
            Key secretKey = getSecretKey(context);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            String ivString = Base64.encodeToString(iv, Base64.NO_WRAP);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit()
                    .putString(IV_KEY_NAME, ivString)
                    .apply();

            return cipher;
        } catch (InvalidKeyException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (e instanceof KeyPermanentlyInvalidatedException) {
                    deleteInvalidKey();
                    throw new KeyInvalidatedException();
                }
            }
            throw new CryptoException("Couldn't create Crypto Object", e);
        } catch (IOException | CertificateException | UnrecoverableKeyException | NoSuchProviderException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new CryptoException("Couldn't create Crypto Object", e);
        }
    }

    static Cipher getDecryptionCipher(Context context) throws CryptoException, KeyInvalidatedException {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String ivEncoded = preferences.getString(IV_KEY_NAME, null);
            if (ivEncoded == null) {
                throw new InvalidKeyException();
            }
            Cipher cipher = getCipher();
            Key secretKey = getSecretKey(context);
            IvParameterSpec ivSpec = new IvParameterSpec(Base64.decode(ivEncoded, Base64.DEFAULT));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            return cipher;
        } catch (InvalidKeyException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (e instanceof KeyPermanentlyInvalidatedException) {
                    deleteInvalidKey();
                    throw new KeyInvalidatedException();
                }
            }
            throw new CryptoException("Couldn't create Crypto Object", e);
        } catch (IOException | CertificateException | UnrecoverableKeyException | NoSuchProviderException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new CryptoException("Couldn't create Crypto Object", e);
        }
    }

}