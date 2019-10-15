package de.niklasmerz.cordova.biometric;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.exxbrain.android.biometric.BiometricManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Fingerprint extends CordovaPlugin {

    private static final String TAG = "Fingerprint";
    private CallbackContext mCallbackContext = null;
    static final String DISABLE_BACKUP = "disableBackup";
    static final String TITLE = "title";
    static final String SUBTITLE = "subtitle";
    static final String DESCRIPTION = "description";
    static final String FALLBACK_BUTTON_TITLE = "fallbackButtonTitle";

    private static boolean mDisableBackup = false;
    private static String mTitle;
    private static String mSubtitle = null;
    private static String mDescription = null;
    private static String mFallbackButtonTitle = "Cancel";

    private static final int REQUEST_CODE_BIOMETRIC = 1;

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

    private void authenticate(JSONArray args) {
        parseArgs(args);
        Intent intent = new Intent(cordova.getActivity().getApplicationContext(), BiometricActivity.class);
        intent.putExtra(DISABLE_BACKUP, mDisableBackup);
        intent.putExtra(TITLE, mTitle);
        intent.putExtra(SUBTITLE, mSubtitle);
        intent.putExtra(DESCRIPTION, mDescription);
        intent.putExtra(FALLBACK_BUTTON_TITLE, mFallbackButtonTitle);
        this.cordova.startActivityForResult(new CordovaPlugin() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                super.onActivityResult(requestCode, resultCode, intent);
                if (requestCode == REQUEST_CODE_BIOMETRIC) {
                    if (resultCode == Activity.RESULT_OK) {
                        sendSuccess("biometric_success");
                    } else {
                        Bundle extras = intent.getExtras();
                        sendError(extras.getInt("code"), extras.getString("message"));
                    }
                }
            }
        }, intent, REQUEST_CODE_BIOMETRIC);
    }

    private void parseArgs(JSONArray args) {
        JSONObject argsObject;
        try {
            argsObject = args.getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse args. Defaults will be used.", e);
            return;
        }
        mDisableBackup = getBooleanArg(argsObject, DISABLE_BACKUP, mDisableBackup);
        mTitle = getStringArg(argsObject, TITLE, mTitle);
        mSubtitle = getStringArg(argsObject, SUBTITLE, mSubtitle);
        mDescription = getStringArg(argsObject, DESCRIPTION, mDescription);
        mFallbackButtonTitle = getStringArg(argsObject, FALLBACK_BUTTON_TITLE,
                mDisableBackup ? "Cancel" : "Use Backup");
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
