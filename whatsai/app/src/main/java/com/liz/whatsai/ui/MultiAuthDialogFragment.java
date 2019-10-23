package com.liz.whatsai.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.liz.whatsai.R;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@TargetApi(23)
public class MultiAuthDialogFragment extends DialogFragment {
    private static final String DEFAULT_KEY_NAME = "default_key";
    KeyStore keyStore;
    private FragmentManager fragmentManager;

    private FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private Cipher mCipher;
    private Context mContext;
    private TextView mErrorMsg;

    private boolean mSelfCancel;
    private MultiAuthCallback mMultiAuthCallback;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Functions

    public interface MultiAuthCallback {
        boolean onCheckPassword(String passwordInput);
        void onAuthenticationSucceeded();
        void onAuthenticationFailed();
        void onAuthenticationCancel();
        void onAuthenticationError();
    }

    public void openDlg(Activity activity, MultiAuthCallback callback) {
        mMultiAuthCallback = callback;
        if (MultiAuthDialogFragment.supportFingerprint(activity)) {
            initKey();
            initCipher();
        }
        fragmentManager = activity.getFragmentManager();
        this.show(fragmentManager, "");
    }

    // Interface Functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

//    public void setCipher(Cipher cipher) {
//        mCipher = cipher;
//    }

    public static boolean supportFingerprint(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(context, "您的系统版本过低，不支持指纹功能", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (!fingerprintManager.isHardwareDetected()) {
                Toast.makeText(context, "您的手机不支持指纹功能", Toast.LENGTH_SHORT).show();
                return false;
            } else if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(context, "您还未设置锁屏，请先设置锁屏并添加一个指纹", Toast.LENGTH_SHORT).show();
                return false;
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(context, "您至少需要在系统设置中添加一个指纹", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    private void initKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(23)
    private void initCipher() {
        try {
            SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            //Cipher mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            //showFingerPrintDialog(mCipher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFingerprintManager = getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.multiauth_dialog, container, false);
        mErrorMsg = v.findViewById(R.id.error_msg);
        final EditText editPassword = v.findViewById(R.id.edit_password);
        v.findViewById(R.id.Cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                stopListening();
                if (mMultiAuthCallback != null) {
                    mMultiAuthCallback.onAuthenticationCancel();
                }
            }
        });
        v.findViewById(R.id.OK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMultiAuthCallback == null) {
                    dismiss();
                    stopListening();
                }
                else {
                    String passwordInput = editPassword.getText().toString();
                    if (mMultiAuthCallback.onCheckPassword(passwordInput)) {
                        dismiss();
                        stopListening();
                        mMultiAuthCallback.onAuthenticationSucceeded();
                    }
                    else {
                        Toast.makeText(mContext, "Password Incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        startListening(mCipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }

    private void startListening(Cipher cipher) {
        mSelfCancel = false;
        mCancellationSignal = new CancellationSignal();
        mFingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (!mSelfCancel) {
                    Toast.makeText(mContext, errorCode + ":" + errString, Toast.LENGTH_LONG).show();
                    stopListening();
                    if (mMultiAuthCallback != null) {
                        mMultiAuthCallback.onAuthenticationError();
                    }
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                mErrorMsg.setText(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                Toast.makeText(mContext, "Fingerprint Authentication Successfully", Toast.LENGTH_SHORT).show();
                dismiss();
                stopListening();
                if (mMultiAuthCallback != null) {
                    mMultiAuthCallback.onAuthenticationSucceeded();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(mContext, "Fingerprint Authentication Failed", Toast.LENGTH_SHORT).show();
                mErrorMsg.setText("Fingerprint Authenticate Failed");
                if (mMultiAuthCallback != null) {
                    mMultiAuthCallback.onAuthenticationFailed();
                }
            }
        }, null);
    }

    private void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            mSelfCancel = true;
        }
    }
}
