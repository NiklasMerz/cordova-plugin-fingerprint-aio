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
import android.support.v4.content.ContextCompat;
import android.util.Log;

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
            executeAuthenticate(args);
            return true;

        } else if (action.equals("isAvailable")){
            executeIsAvailable();
            return true;
        }

        return false;
    }

    private void executeIsAvailable() {
        PluginError error = canAuthenticate();
        if (error != null) {
            sendError(error);
        } else {
            sendSuccess("finger");
        }
    }

    private void executeAuthenticate(JSONArray args) {
        PluginError error = canAuthenticate();
        if (error != null) {
            sendError(error);
            return;
        }
        cordova.getActivity().runOnUiThread(() -> this.authenticate(args));
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        this.mCallbackContext.sendPluginResult(pluginResult);
    }

    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback =
            new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    onError(errorCode, errString);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    sendSuccess("biometric_success");
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    sendError(PluginError.BIOMETRIC_AUTHENTICATION_FAILED);
                }
            };

    private void onError(int errorCode, @NonNull CharSequence errString) {

        if (errorCode == BiometricPrompt.ERROR_CANCELED) {
            return;
        }

        if (!mDisableBackup && (
                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        || errorCode == BiometricPrompt.ERROR_LOCKOUT
                        || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT)) {
            showAuthenticationScreen();
            return;
        }

        switch (errorCode)
        {
            case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                sendError(PluginError.BIOMETRIC_FINGERPRINT_DISMISSED);
                break;
            case BiometricPrompt.ERROR_LOCKOUT:
                sendError(PluginError.BIOMETRIC_LOCKED_OUT.getValue(), errString.toString());
                break;
            case BiometricPrompt.ERROR_LOCKOUT_PERMANENT:
                sendError(PluginError.BIOMETRIC_LOCKED_OUT_PERMANENT.getValue(), errString.toString());
                break;
            default:
                sendError(errorCode, errString.toString());
        }
    }

    private void authenticate(JSONArray args) {
        parseArgs(args);

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

    private void parseArgs(JSONArray args) {
        JSONObject argsObject;
        try {
            argsObject = args.getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse args. Defaults will be used.", e);
            return;
        }
        mDisableBackup = getBooleanArg(argsObject, "disableBackup", mDisableBackup);
        mTitle = getStringArg(argsObject, "title", mTitle);
        mSubtitle = getStringArg(argsObject, "subtitle", mSubtitle);
        mDescription = getStringArg(argsObject, "description", mDescription);
        mFallbackButtonTitle = getStringArg(argsObject, "fallbackButtonTitle",
                !mDisableBackup ? "Use Backup" : mFallbackButtonTitle);
    }

    private Boolean getBooleanArg(JSONObject argsObject, String name, Boolean defaultValue) {
        if (argsObject.has(name)){
            try {
                return argsObject.getBoolean(name);
            } catch (JSONException e) {
                Log.e(TAG, "Can't parse '" + name + "'. Default will be used.", e);
            }
        }
        return defaultValue;
    }

    private String getStringArg(JSONObject argsObject, String name, String defaultValue) {
        if (argsObject.optString(name) != null
                && !argsObject.optString(name).isEmpty()){
            try {
                return argsObject.getString(name);
            } catch (JSONException e) {
                Log.e(TAG, "Can't parse '" + name + "'. Default will be used.", e);
            }
        }
        return defaultValue;
    }

    private PluginError canAuthenticate() {
        int error = BiometricManager.from(cordova.getContext()).canAuthenticate();
        switch (error) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return PluginError.BIOMETRIC_HARDWARE_NOT_SUPPORTED;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return PluginError.BIOMETRIC_FINGERPRINT_NOT_ENROLLED;
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
            sendError(PluginError.BIOMETRIC_SCREEN_GUARD_UNSECURED);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if (resultCode == Activity.RESULT_OK) {
                sendSuccess("biometric_success");
            } else {
                sendError(PluginError.BIOMETRIC_PIN_OR_PATTERN_DISMISSED);
            }
        }
    }

    private void sendError(int code, String message) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("code", code);
            resultJson.put("message", message);

            PluginResult result = new PluginResult(PluginResult.Status.ERROR, resultJson);
            result.setKeepCallback(true);
            cordova.getActivity().runOnUiThread(() ->
                    Fingerprint.this.mCallbackContext.sendPluginResult(result));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void sendError(PluginError error) {
        sendError(error.getValue(), error.getMessage());
    }

    private void sendSuccess(String message) {
        Log.e(TAG, message);
        cordova.getActivity().runOnUiThread(() ->
                this.mCallbackContext.success(message));
    }
}
