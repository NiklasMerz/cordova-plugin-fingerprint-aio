package de.niklasmerz.cordova.biometric;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.exxbrain.android.biometric.BiometricConstants;
import com.exxbrain.android.biometric.BiometricManager;
import com.exxbrain.android.biometric.BiometricPrompt;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

public class Fingerprint extends CordovaPlugin {

    private static final String TAG = "Fingerprint";
    private CallbackContext mCallbackContext = null;
    private static boolean mDisableBackup = false;
    private static String mTitle;
    private static String mSubtitle = null;
    private static String mDescription = null;
    private static String mFallbackButtonTitle = "Cancel";

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;

    public enum PluginError {

        BIOMETRIC_UNKNOWN_ERROR(-100),
        BIOMETRIC_UNAVAILABLE(-101),
        BIOMETRIC_AUTHENTICATION_FAILED(-102),
        BIOMETRIC_SDK_NOT_SUPPORTED(-103),
        BIOMETRIC_HARDWARE_NOT_SUPPORTED(-104),
        BIOMETRIC_PERMISSION_NOT_GRANTED(-105),
        BIOMETRIC_FINGERPRINT_NOT_ENROLLED(-106),
        BIOMETRIC_INTERNAL_PLUGIN_ERROR(-107),
        BIOMETRIC_FINGERPRINT_DISMISSED(-108),
        BIOMETRIC_PIN_OR_PATTERN_DISMISSED(-109),
        BIOMETRIC_SCREEN_GUARD_UNSECURED(-110),
        BIOMETRIC_LOCKED_OUT(-111),
        BIOMETRIC_LOCKED_OUT_PERMANENT(-112);

        private int value;

        PluginError(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    static class Error {
        private final int code;
        private final String message;

        Error(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init Fingerprint");
        PackageManager packageManager = cordova.getActivity().getPackageManager();
        try {
            ApplicationInfo app = packageManager
                    .getApplicationInfo(cordova.getActivity().getPackageName(), 0);
            mTitle = packageManager.getApplicationLabel(app) + " Biometric Sign On";
        } catch (NameNotFoundException e) {
            mTitle = "Biometric Sign On";
        }
    }

    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) {

        this.mCallbackContext = callbackContext;
        Log.v(TAG, "Fingerprint action: " + action);

        if (action.equals("authenticate")) {
            Error error = canAuthenticate();
            if (error != null) {
                sendError(error);
                return true;
            }
            cordova.getActivity().runOnUiThread(() -> authenticate(args));
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            this.mCallbackContext.sendPluginResult(pluginResult);
            return true;

        } else if (action.equals("isAvailable")){
            Error error = canAuthenticate();
            if (error != null) {
                sendError(error);
            } else {
                /**
                 * There is no method to get biometry type in Android, so
                 * for compatibility always return "finger"
                 */
                sendSuccess("finger");
            }
            return true;
        }

        return false;
    }

    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback =
            new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {

                    super.onAuthenticationError(errorCode, errString);

                    switch (errorCode)
                    {
                        case BiometricPrompt.ERROR_CANCELED:
                            break;
                        case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                            if(!mDisableBackup){
                                showAuthenticationScreen();
                            } else{
                                sendError(new Error(
                                        PluginError.BIOMETRIC_FINGERPRINT_DISMISSED.getValue(),
                                        PluginError.BIOMETRIC_FINGERPRINT_DISMISSED.name()));
                            }
                            break;
                        case BiometricPrompt.ERROR_LOCKOUT:
                        case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                            if(!mDisableBackup) {
                                showAuthenticationScreen();
                            } else {
                                sendError(new Error(errorCode == BiometricPrompt.ERROR_LOCKOUT
                                        ? PluginError.BIOMETRIC_LOCKED_OUT.getValue()
                                        : PluginError.BIOMETRIC_LOCKED_OUT_PERMANENT.getValue(),
                                        errString.toString()));
                            }
                            break;

                        default:
                            sendError(new Error(errorCode, errString.toString()));
                            break;
                    }

                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.e(TAG, "biometric_success");
                    sendSuccess("biometric_success");
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    sendError(
                            new Error(PluginError.BIOMETRIC_AUTHENTICATION_FAILED.getValue(),
                                    "Authentication failed"));
                }
            };

    private void authenticate(JSONArray args) {
        try {
            JSONObject argsObject = args.getJSONObject(0);
            if (argsObject.has("disableBackup")) {
                mDisableBackup = argsObject.getBoolean("disableBackup");
            }
            if (argsObject.optString("title") != null
                    && !argsObject.optString("title").isEmpty()){
                mTitle = argsObject.getString("title");
            }
            if (argsObject.optString("subtitle") != null
                    && !argsObject.optString("subtitle").isEmpty()){
                mSubtitle = argsObject.getString("subtitle");
            }
            if (argsObject.optString("description") != null
                    && !argsObject.optString("description").isEmpty()){
                mDescription = argsObject.getString("description");
            }
            if (argsObject.optString("fallbackButtonTitle") != null
                    && !argsObject.optString("fallbackButtonTitle").isEmpty()){
                mFallbackButtonTitle = argsObject.getString("fallbackButtonTitle");
            } else if (!mDisableBackup){
                mFallbackButtonTitle = "Use Backup";
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse args. Default parameters will be used.", e);
        }


        Executor executor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            executor = cordova.getActivity().getMainExecutor();
        } else {
            final Handler handler = new Handler(Looper.getMainLooper());
            executor = handler::post;
        }


        BiometricPrompt biometricPrompt =
                new BiometricPrompt(cordova.getActivity(), executor, mAuthenticationCallback);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(mTitle)
                .setSubtitle(mSubtitle)
                .setDescription(mDescription)
                .setNegativeButtonText(mFallbackButtonTitle)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private Error canAuthenticate() {
        int error = BiometricManager.from(cordova.getContext()).canAuthenticate();
        switch (error) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return new Error(
                        PluginError.BIOMETRIC_HARDWARE_NOT_SUPPORTED.getValue(),
                        PluginError.BIOMETRIC_HARDWARE_NOT_SUPPORTED.name());
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return new Error(
                        PluginError.BIOMETRIC_FINGERPRINT_NOT_ENROLLED.getValue(),
                        PluginError.BIOMETRIC_FINGERPRINT_NOT_ENROLLED.name());
            default:
                return null;
        }
    }

    private void showAuthenticationScreen() {
        KeyguardManager keyguardManager = ContextCompat
                .getSystemService(cordova.getActivity(), KeyguardManager.class);
        if (keyguardManager == null
                || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        if (keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
            cordova.setActivityResultCallback(this);
            cordova.getActivity().startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        } else {
            // Show a message that the user hasn't set up a lock screen.
            sendError(
                    new Error(
                            PluginError.BIOMETRIC_SCREEN_GUARD_UNSECURED.getValue(),
                            "Go to 'Settings -> Security -> Screenlock' to set up a lock screen")
            );
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if (resultCode == Activity.RESULT_OK) {
                sendSuccess();
            } else {
                sendError(
                        new Error(
                                PluginError.BIOMETRIC_PIN_OR_PATTERN_DISMISSED.getValue(),
                                PluginError.BIOMETRIC_PIN_OR_PATTERN_DISMISSED.name())
                );
            }
        }
    }

    private void sendError(Error error) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("code", error.code);
            resultJson.put("message", error.message);

            PluginResult result = new PluginResult(PluginResult.Status.ERROR, resultJson);
            result.setKeepCallback(true);
            cordova.getActivity().runOnUiThread(() ->
                    Fingerprint.this.mCallbackContext.sendPluginResult(result));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void sendSuccess(String message) {
        cordova.getActivity().runOnUiThread(() ->
                this.mCallbackContext.success(message));
    }
}
