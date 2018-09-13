package tech.ithunter.app.auth;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;

import tech.ithunter.app.form.FormActivity;
import tech.ithunter.app.R;
import tech.ithunter.app.notice.Notice;

public class StartActivity extends AppCompatActivity {

    private static final int GOOGLE_RC_SIGN_IN = 9005;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private CallbackManager facebookCallbackManager;
    private LoginButton originalFacebookButton;

    private LinearLayout facebookLayout, googleLayout, emailLayout;
    private Button facebookButton, googleButton, emailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setupWindowAnimations();
        initializeComponents();

        // Configure Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configure Google
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configure Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        facebookCallbackManager = CallbackManager.Factory.create();
        originalFacebookButton.setReadPermissions("email", "public_profile");
        originalFacebookButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override public void onCancel() {
                Notice.showWarningToast(StartActivity.this, R.string.warning_facebook_auth_canceled);
            }
            @Override public void onError(FacebookException error) {
                Notice.showWarningToast(StartActivity.this, R.string.error_facebook_auth);
            }
        });
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide();
            slide.setDuration(20000);
            getWindow().setExitTransition(slide);

            Fade fade = new Fade();
            fade.setDuration(20000);
            getWindow().setEnterTransition(fade);
        }
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == GOOGLE_RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    updateUI(null);
                }
            }
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
        }
    }

    private void updateUI(FirebaseUser user) {
        try {
            if (user != null) {
                //Toast.makeText(getApplicationContext(), "Is connected", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, FormActivity.class));
                //Bungee.swipeLeft(getApplicationContext());
            }
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        try {
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            mAuth.signInWithCredential(credential)
                    .addOnCanceledListener(StartActivity.this, new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Notice.showWarningToast(StartActivity.this, R.string.warning_facebook_auth_canceled);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception e) {
                            Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
                        }
                    })
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                updateUI(null);
                            }
                        }
                    });
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        try {
            final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.fetchProvidersForEmail(acct.getEmail())
                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.getResult().getProviders().isEmpty() || task.getResult().getProviders().contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                                mAuth.signInWithCredential(credential)
                                        .addOnCanceledListener(StartActivity.this, new OnCanceledListener() {
                                            @Override
                                            public void onCanceled() {
                                                Notice.showWarningToast(StartActivity.this, R.string.warning_google_auth_canceled);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
                                            }
                                        })
                                        .addOnCompleteListener(StartActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    updateUI(user);
                                                } else {
                                                    //clearEditTextFocus();
                                                }
                                            }
                                        });
                            } else {
                                GoogleSignIn.getClient(StartActivity.this, gso).signOut();
                                Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, R.string.error_email_is_exist);
                            }
                        }
                    });
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
        }
    }

    private void initializeComponents() {
        originalFacebookButton = (LoginButton) findViewById(R.id.original_facebook_button);

        facebookLayout = (LinearLayout) findViewById(R.id.layout_facebook_button);
        facebookButton = (Button) findViewById(R.id.facebook_button);
        googleLayout = (LinearLayout) findViewById(R.id.layout_google_button);
        googleButton = (Button) findViewById(R.id.google_button);
        emailLayout = (LinearLayout) findViewById(R.id.layout_email_button);
        emailButton = (Button) findViewById(R.id.email_button);

        facebookButton.post(new Runnable() {
            @Override
            public void run() {
                facebookButton.setWidth(facebookButton.getWidth());
                googleButton.setWidth(facebookButton.getWidth());
                emailButton.setWidth(facebookButton.getWidth());
            }
        });

        setListenersForComponents();
    }

    private void setListenersForComponents() {
        facebookLayout.setOnClickListener(facebookButtonOnClickListener);
        facebookButton.setOnClickListener(facebookButtonOnClickListener);
        googleLayout.setOnClickListener(googleButtonOnClickListener);
        googleButton.setOnClickListener(googleButtonOnClickListener);
        emailLayout.setOnClickListener(emailButtonOnClickListener);
        emailButton.setOnClickListener(emailButtonOnClickListener);
    }

    private View.OnClickListener facebookButtonOnClickListener = new View.OnClickListener() {
        @Override public void onClick(View view) { originalFacebookButton.performClick(); }
    };

    private View.OnClickListener googleButtonOnClickListener = new View.OnClickListener() {
        @Override public void onClick(View view) {
            try {
                startActivityForResult(mGoogleSignInClient.getSignInIntent(), GOOGLE_RC_SIGN_IN);
            } catch (Exception e) {
                Notice.showErrorInTSnackBar(StartActivity.this, emailLayout, e.toString());
            }
        }
    };

    private View.OnClickListener emailButtonOnClickListener = new View.OnClickListener() {
        @Override public void onClick(View view) { startActivity(new Intent(StartActivity.this, LoginActivity.class)); }
    };

}