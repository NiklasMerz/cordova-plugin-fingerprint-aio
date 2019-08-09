/*
 * Copyright (C) https://github.com/mjwheatley/cordova-plugin-android-fingerprint-auth
 * Modifications copyright (C) 2016 Niklas Merz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
 
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

import com.an.biometric.BiometricManager;
import com.an.biometric.BiometricCallback;
import android.hardware.biometrics.BiometricPrompt;

public class Fingerprint extends CordovaPlugin implements BiometricCallback {
	
    public static final String TAG = "Fingerprint";
    public static String packageName;
    public static Context mContext;
    public static CallbackContext mCallbackContext;
    public static PluginResult mPluginResult;
    private static boolean mDisableBackup = false;
    public KeyguardManager mKeyguardManager;
    BiometricManager mBiometricManager;
    
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;

	
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init Fingerprint");
        packageName = cordova.getActivity().getApplicationContext().getPackageName();
        mPluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        mContext = cordova.getActivity().getApplicationContext();
        mKeyguardManager = cordova.getActivity().getSystemService(KeyguardManager.class);
    }
    
    public boolean execute(final String action,
                           JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;
        Log.v(TAG, "Fingerprint action: " + action);
        
        final JSONObject arg_object = args.getJSONObject(0);

        if (action.equals("authenticate")) {
	        
            cordova.getActivity().runOnUiThread(new Runnable() {
	            public void run() {
		            
		            if (arg_object.has("disableBackup")) {
			            try {
							mDisableBackup = arg_object.getBoolean("disableBackup");
			            } catch (JSONException e) {
				            
			            }
		            }

					//set up the builder
					mBiometricManager = new BiometricManager.BiometricBuilder(mContext)
						.setTitle("Title")
						.setSubtitle("Subtitle")
						.setDescription("Description")
						.setNegativeButtonText("Cancel")
						.build();
					
					//start authentication
					mBiometricManager.authenticate(Fingerprint.this);
	            }
	        });
	        
	        mPluginResult.setKeepCallback(true);
            return true;
        }else if(action.equals("isAvailable")){
	        showAuthenticationScreen();
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
	         onBiometricAuthenticationInternalError("Go to 'Settings -> Security -> Screenlock' to set up a lock screen");
        }
    }
    

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    //onBiometricAuthenticationInternalError("requestCode: " + requestCode);
	    Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
              onAuthenticationSuccessful();
            } else {
              onBiometricAuthenticationInternalError("Pin/pattern dismissed.");
            }
        }
    }
    
    @Override
    public void onSdkVersionNotSupported() {
        mCallbackContext.error("biometric_error_sdk_not_supported");
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {
        mCallbackContext.error("biometric_error_hardware_not_supported");
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
        mCallbackContext.error("biometric_error_fingerprint_not_available");
    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
        mCallbackContext.error("biometric_error_permission_not_granted");
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
        mCallbackContext.error("InternalError" + error);
    }

    @Override
    public void onAuthenticationFailed() {
		mCallbackContext.error("biometric_failure");
    }

    @Override
    public void onAuthenticationCancelled() {
        //mCallbackContext.error("biometric_cancelled");
        mBiometricManager.cancelAuthentication();
        if(!mDisableBackup){
	        showAuthenticationScreen();
        }
    }

    @Override
    public void onAuthenticationSuccessful() {
        mCallbackContext.error("biometric_success");
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
		mCallbackContext.error(helpString + " | helpCode: " + helpCode);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
		mCallbackContext.error(errString + " | errorCode: " + errorCode);
		if (errorCode == 9) {
        	if(!mDisableBackup){
	        	mPluginResult.setKeepCallback(false);
	        	showAuthenticationScreen();
        	}
	    }
    }
    
/*
    public void onAuthenticationActivityFinished(){
	    mCallbackContext.error("biometric_activity_finished");
    }
*/
	
}
