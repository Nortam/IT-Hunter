package tech.ithunter.app.auth;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import mehdi.sakout.fancybuttons.FancyButton;
import tech.ithunter.app.form.FormActivity;
import tech.ithunter.app.R;
import tech.ithunter.app.check.CheckNetwork;
import tech.ithunter.app.notice.Notice;

import static tech.ithunter.app.check.CheckData.isEmailValid;
import static tech.ithunter.app.check.CheckData.isPasswordValid;

public class SignUpActivity extends AppCompatActivity {

    private FancyButton signUpButton;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private TextView backTextView, loginTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_up);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            initializeComponents();

            // Configure Firebase
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, e.toString());
        }
    }

    private void attemptSignUp() {
        try {
            emailEditText.setError(null);
            passwordEditText.setError(null);
            confirmPasswordEditText.setError(null);
            if (!new CheckNetwork(getApplicationContext()).isConnected()) {
                Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, R.string.error_no_network_connection);
                clearEditTextFocus();
                return;
            }
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            boolean cancel = false;
            View focusView = null;

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError(getString(R.string.error_passwords_do_not_match));
                focusView = confirmPasswordEditText;
                cancel = true;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                confirmPasswordEditText.setError(getString(R.string.error_invalid_password));
                focusView = confirmPasswordEditText;
                cancel = true;
            }
            if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                passwordEditText.setError(getString(R.string.error_invalid_password));
                focusView = passwordEditText;
                cancel = true;
            }
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError(getString(R.string.error_field_required));
                focusView = emailEditText;
                cancel = true;
            } else if (!isEmailValid(email)) {
                emailEditText.setError(getString(R.string.error_invalid_email));
                focusView = emailEditText;
                cancel = true;
            }
            if (cancel) {
                focusView.requestFocus();
            } else {
                createUser(email, password);
                //showProgress(true);
            }
        } catch (Exception e) {
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, e.toString());
        }
    }

    private void createUser(final String email, final String password) {
        try {
            mAuth.fetchProvidersForEmail(email)
                    .addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Notice.showWarningToast(SignUpActivity.this, R.string.warning_sign_up_canceled);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            try {
                                if (task.getResult().getProviders().isEmpty()) {
                                    mAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
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
                                } else {
                                    emailEditText.setError(SignUpActivity.this.getString(R.string.error_email_taken));
                                    emailEditText.requestFocus();
                                }
                            } catch (Exception e) {
                                Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, e.toString());
                            }
                        }
                    });
        } catch (Exception e) {
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, e.toString());
        }
    }

    private void updateUI(FirebaseUser user) {
        try {
            if (user != null) {
                startActivity(new Intent(this, FormActivity.class));
            } else {
                Notice.showErrorToast(SignUpActivity.this, R.string.error_sign_up_failed);
            }
        } catch (Exception e) {
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, e.toString());
        }
    }

    private void clearEditTextFocus() {
        try {
            emailEditText.clearFocus();
            passwordEditText.clearFocus();
            confirmPasswordEditText.clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(SignUpActivity.this, signUpButton, e.toString());
        }
    }

    private void clearEditTextErrors() {
        emailEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);
    }

    private void initializeComponents() throws Exception {
        emailEditText = (EditText) findViewById(R.id.email_editText);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirm_password_editText);
        signUpButton = (FancyButton) findViewById(R.id.sign_up_button);
        backTextView = (TextView) findViewById(R.id.back_textView);
        loginTextView = (TextView) findViewById(R.id.login_textView);

        setListenersForComponents();
    }

    private void setListenersForComponents() throws Exception {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}