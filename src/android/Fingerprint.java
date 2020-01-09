package de.niklasmerz.cordova.biometric;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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

    private static final int REQUEST_CODE_BIOMETRIC = 1;
    private PromptInfo.Builder mPromptInfoBuilder;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init Fingerprint");
        mPromptInfoBuilder = new PromptInfo.Builder(cordova.getActivity());
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            sendSuccess("biometric");
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
        cordova.getActivity().runOnUiThread(() -> {
            mPromptInfoBuilder.parseArgs(args);
            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), BiometricActivity.class);
            intent.putExtras(mPromptInfoBuilder.build().getBundle());
            this.cordova.startActivityForResult(this, intent, REQUEST_CODE_BIOMETRIC);
        });
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        this.mCallbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode != REQUEST_CODE_BIOMETRIC) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            sendSuccess("biometric_success");
        } else if (intent != null) {
            Bundle extras = intent.getExtras();
            sendError(extras.getInt("code"), extras.getString("message"));
        } else {
            sendError(PluginError.BIOMETRIC_DISMISSED);
        }
    }

    private PluginError canAuthenticate() {
        int error = BiometricManager.from(cordova.getContext()).canAuthenticate();
        switch (error) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return PluginError.BIOMETRIC_HARDWARE_NOT_SUPPORTED;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return PluginError.BIOMETRIC_NOT_ENROLLED;
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
