/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
@TargetApi(23)
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    static final long ERROR_TIMEOUT_MILLIS = 1600;
    static final long SUCCESS_DELAY_MILLIS = 1300;

    private final Context mContext;
    private final FingerprintManager mFingerprintManager;
    private final ImageView mIcon;
    private final TextView mErrorTextView;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    boolean mSelfCancelled;

    /**
     * Builder class for {@link FingerprintUiHelper} in which injected fields from Dagger
     * holds its fields and takes other arguments in the {@link #build} method.
     */
    public static class FingerprintUiHelperBuilder {
        private final FingerprintManager mFingerPrintManager;
        private final Context mContext;

        public FingerprintUiHelperBuilder(Context context, FingerprintManager fingerprintManager) {
            mFingerPrintManager = fingerprintManager;
            mContext = context;
        }

        public FingerprintUiHelper build(ImageView icon, TextView errorTextView, Callback callback) {
            return new FingerprintUiHelper(mContext, mFingerPrintManager, icon, errorTextView,
                    callback);
        }
    }

    /**
     * Constructor for {@link FingerprintUiHelper}. This method is expected to be called from
     * only the {@link FingerprintUiHelperBuilder} class.
     */
    private FingerprintUiHelper(Context context, FingerprintManager fingerprintManager,
            ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
        mContext = context;
    }

    public boolean isFingerprintAuthAvailable() {
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null);

        int ic_fp_40px_id = mContext.getResources()
                .getIdentifier("ic_fp_40px", "drawable", Fingerprint.packageName);
        mIcon.setImageResource(ic_fp_40px_id);
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            showError(errString);
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        int fingerprint_not_recognized_id = mContext.getResources()
                .getIdentifier("fingerprint_not_recognized", "string", Fingerprint.packageName);
        showError(mIcon.getResources().getString(
                fingerprint_not_recognized_id));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        int ic_fingerprint_success_id = mContext.getResources()
                .getIdentifier("ic_fingerprint_success", "drawable", Fingerprint.packageName);
        mIcon.setImageResource(ic_fingerprint_success_id);
        int success_color_id = mContext.getResources()
                .getIdentifier("success_color", "color", Fingerprint.packageName);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(success_color_id, null));
        int fingerprint_success_id = mContext.getResources()
                .getIdentifier("fingerprint_success", "string", Fingerprint.packageName);
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(fingerprint_success_id));
        mIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onAuthenticated();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        int ic_fingerprint_error_id = mContext.getResources()
                .getIdentifier("ic_fingerprint_error", "drawable", Fingerprint.packageName);
        mIcon.setImageResource(ic_fingerprint_error_id);
        mErrorTextView.setText(error);
        int warning_color_id = mContext.getResources()
                .getIdentifier("warning_color", "color", Fingerprint.packageName);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(warning_color_id, null));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            int hint_color_id = mContext.getResources()
                    .getIdentifier("hint_color", "color", Fingerprint.packageName);
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(hint_color_id, null));
            int fingerprint_hint_id = mContext.getResources()
                    .getIdentifier("fingerprint_hint", "string", Fingerprint.packageName);
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(fingerprint_hint_id));
            int ic_fp_40px_id = mContext.getResources()
                    .getIdentifier("ic_fp_40px", "drawable", Fingerprint.packageName);
            mIcon.setImageResource(ic_fp_40px_id);
        }
    };

    public interface Callback {

        void onAuthenticated();

        void onError();
    }
}
