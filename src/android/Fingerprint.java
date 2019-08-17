package de.niklasmerz.cordova.fingerprint;

import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.content.Intent;
import android.content.Context;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.an.biometric.BiometricUtils;
import com.an.biometric.BiometricManager;
import com.an.biometric.BiometricCallback;
import android.hardware.biometrics.BiometricPrompt;

import java.util.UUID;

public class Fingerprint extends CordovaPlugin implements BiometricCallback {

    public static final String TAG = "Fingerprint";
    public static String packageName;
    public static Context mContext;
    private CallbackContext mCallbackContext = null;
    public static PluginResult mPluginResult;
    private static boolean mDisableBackup = false;
    private static String mTitle;
    private static String mSubtitle = null;
    private static String mDescription = null;
    private static String mFallbackButtonTitle = "Cancel";
    public KeyguardManager mKeyguardManager;
    public BiometricManager mBiometricManager;
    public BiometricUtils mBiometricUtils;

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

        private PluginError(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init Fingerprint");
        packageName = cordova.getActivity().getApplicationContext().getPackageName();
        mContext = cordova.getActivity().getApplicationContext();
        mKeyguardManager = cordova.getActivity().getSystemService(KeyguardManager.class);

        PackageManager packageManager = cordova.getActivity().getPackageManager();
        ApplicationInfo app = null;
        try {
            app = packageManager.getApplicationInfo(cordova.getActivity().getPackageName(), 0);
            mTitle = (String)packageManager.getApplicationLabel(app) + " Biometric Sign On";
        } catch (NameNotFoundException e) {
            mTitle = "Biometric Sign On";
        }
    }

    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.mCallbackContext = callbackContext;
        Log.v(TAG, "Fingerprint action: " + action);

        final JSONObject arg_object = args.getJSONObject(0);


        if (action.equals("authenticate")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
	            public void run() {

                    try {
                        if (arg_object.has("disableBackup")) {
                            mDisableBackup = arg_object.getBoolean("disableBackup");
                        }
                        if(arg_object.optString("title") != null && !arg_object.optString("title").isEmpty()){
                            mTitle = arg_object.getString("title");
                        }else{

                        }
                        if(arg_object.optString("subtitle") != null && !arg_object.optString("subtitle").isEmpty()){
                            mSubtitle = arg_object.getString("subtitle");
                        }
                        if(arg_object.optString("description") != null && !arg_object.optString("description").isEmpty()){
                            mDescription = arg_object.getString("description");
                        }
                        if(arg_object.optString("fallbackButtonTitle") != null && !arg_object.optString("fallbackButtonTitle").isEmpty()){
                            mFallbackButtonTitle = arg_object.getString("fallbackButtonTitle");
                        }else{
                            if(!mDisableBackup){
                                mFallbackButtonTitle = "Use Backup";
                            }
                        }
                    } catch (JSONException e) {

                    }

					//set up the builder
					mBiometricManager = new BiometricManager.BiometricBuilder(mContext)
						.setTitle(mTitle)
						.setSubtitle(mSubtitle)
						.setDescription(mDescription)
						.setNegativeButtonText(mFallbackButtonTitle)
						.build();

					//start authentication
					mBiometricManager.authenticate(Fingerprint.this);
	            }
	        });

            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            this.mCallbackContext.sendPluginResult(pluginResult);

            return true;
        }else if(action.equals("isAvailable")){

            /**
             * NOTE:
             * Android 9 only includes fingerprint integration for BiometricPrompt. However, integrated
             * support for other biometric modalities are forthcoming. So right now can't return anything
             * other than "finger" if it's available even if face dection opens up.
             * @link https://source.android.com/security/biometric
             */
            boolean passedAllChecks = true;

	        if(!mBiometricUtils.isSdkVersionSupported()){
                onSdkVersionNotSupported();
                passedAllChecks = true;
            }

	        if(!mBiometricUtils.isHardwareSupported(mContext)){
                onBiometricAuthenticationNotSupported();
                passedAllChecks = true;
            }

	        if(!mBiometricUtils.isFingerprintAvailable(mContext)){
                onBiometricAuthenticationNotAvailable();
                passedAllChecks = true;
            }

	        if(!mBiometricUtils.isPermissionGranted(mContext)){
                onBiometricAuthenticationPermissionNotGranted();
                passedAllChecks = true;
            }

            if (passedAllChecks) {
                sendSuccess("finger");
            }
            return true;
        }
        return false;
    }

    public void showAuthenticationScreen() {
	    if(mKeyguardManager.isKeyguardSecure()){
	        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
	        if (intent != null) {
	          cordova.setActivityResultCallback(this);
	          cordova.getActivity().startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
	        }
        }else{
	         // Show a message that the user hasn't set up a lock screen.
             sendError(PluginError.BIOMETRIC_SCREEN_GUARD_UNSECURED.getValue(), "Go to 'Settings -> Security -> Screenlock' to set up a lock screen");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
              onAuthenticationSuccessful();
            } else {
              sendError(PluginError.BIOMETRIC_PIN_OR_PATTERN_DISMISSED.getValue(), PluginError.BIOMETRIC_PIN_OR_PATTERN_DISMISSED.name());
            }
        }
    }

    @Override
    public void onSdkVersionNotSupported() {
        sendError(PluginError.BIOMETRIC_SDK_NOT_SUPPORTED.getValue(), PluginError.BIOMETRIC_SDK_NOT_SUPPORTED.name());
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {
        sendError(PluginError.BIOMETRIC_HARDWARE_NOT_SUPPORTED.getValue(), PluginError.BIOMETRIC_HARDWARE_NOT_SUPPORTED.name());
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
        sendError(PluginError.BIOMETRIC_FINGERPRINT_NOT_ENROLLED.getValue(), PluginError.BIOMETRIC_FINGERPRINT_NOT_ENROLLED.name());
    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
        sendError(PluginError.BIOMETRIC_PERMISSION_NOT_GRANTED.getValue(), PluginError.BIOMETRIC_PERMISSION_NOT_GRANTED.name());
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
        sendError(PluginError.BIOMETRIC_INTERNAL_PLUGIN_ERROR.getValue(), error);
    }


    @Override
    public void onAuthenticationCancelled() {
        mBiometricManager.cancelAuthentication();
        if(!mDisableBackup){
	        showAuthenticationScreen();
        }else{
	        sendError(PluginError.BIOMETRIC_FINGERPRINT_DISMISSED.getValue(), PluginError.BIOMETRIC_FINGERPRINT_DISMISSED.name());
        }
    }

    @Override
    public void onAuthenticationSuccessful() {
        Log.e(TAG, "biometric_success");
        //mCallbackContext.success("biometric_success");
        sendSuccess("biometric_success");
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Log.e(TAG, "onAuthenticationHelp: " + helpCode + " | " + helpString);
        sendError(helpCode, helpString != null ? helpString.toString() : "Error has no description.");
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        switch (errorCode)
        {
            case 7:
            case 9:
                if(!mDisableBackup) {
                    showAuthenticationScreen();
                }else{
                    sendError(errorCode == 7 ? PluginError.BIOMETRIC_LOCKED_OUT.getValue() : PluginError.BIOMETRIC_LOCKED_OUT_PERMANENT.getValue(), errString.toString());
                }
                break;

            default:
                sendError(errorCode, (errString != null ? errString.toString() : "Error has no description."));
                break;
        }
    }

    public void sendError(int code, String message) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("code", code);
            resultJson.put("message", message);

            PluginResult result = new PluginResult(PluginResult.Status.ERROR, resultJson);
            result.setKeepCallback(true);
            this.mCallbackContext.sendPluginResult(result);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void sendSuccess(String message){
        this.mCallbackContext.success(message);
    }
}
