package com.example.quizzapp.auth;

import android.app.Activity;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Helper bọc Credential Manager – dùng GetSignInWithGoogleOption
 * (luôn hiện bottom sheet chọn account, đáng tin cậy hơn GetGoogleIdOption).
 */
public class GoogleAuthHelper {

    private static final String TAG = "GoogleAuthHelper";

    public interface SignInCallback {
        void onSuccess(String idToken);
        void onError(String message);
    }

    // PHẢI là Activity (không phải Context) – Credential Manager cần Activity để hiện UI
    private final Activity activity;
    private final CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GoogleAuthHelper(Activity activity) {
        this.activity = activity;
        this.credentialManager = CredentialManager.create(activity);
    }

    /**
     * Mở bottom sheet Sign in with Google → trả idToken qua callback.
     * @param serverClientId Web Client ID (oauth_client/client_type=3 trong google-services.json)
     */
    public void signIn(@NonNull String serverClientId, @NonNull SignInCallback callback) {
        // GetSignInWithGoogleOption luôn hiện bottom sheet chọn account
        // (khác GetGoogleIdOption chỉ tìm passive credential → dễ bị NoCredentialException)
        GetSignInWithGoogleOption signInOption =
                new GetSignInWithGoogleOption.Builder(serverClientId)
                        .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,       // PHẢI là Activity, không dùng getApplicationContext()
                request,
                new CancellationSignal(),
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse result) {
                        handleSignInResult(result, callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "GetCredential failed", e);
                        callback.onError(e.getLocalizedMessage());
                    }
                }
        );
    }

    private void handleSignInResult(GetCredentialResponse response, SignInCallback callback) {
        androidx.credentials.Credential cred = response.getCredential();
        if (cred instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(cred.getType())) {
            try {
                GoogleIdTokenCredential googleIdCred =
                        GoogleIdTokenCredential.createFrom(cred.getData());
                callback.onSuccess(googleIdCred.getIdToken());
            } catch (Exception e) {
                Log.e(TAG, "Parse Google ID token failed", e);
                callback.onError("Token Google không hợp lệ");
            }
        } else {
            callback.onError("Credential trả về không phải Google ID Token");
        }
    }

    /**
     * Xoá credential state để lần login sau hiện popup chọn account lại.
     * GỌI SAU FirebaseAuth.signOut().
     */
    public void clearCredentialState() {
        ClearCredentialStateRequest clearReq = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                clearReq,
                new CancellationSignal(),
                executor,
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(@NonNull Void v) {
                        Log.d(TAG, "Cleared credential state");
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e(TAG, "Clear credential failed", e);
                    }
                }
        );
    }
}