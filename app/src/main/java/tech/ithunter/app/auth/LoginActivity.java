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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import mehdi.sakout.fancybuttons.FancyButton;
import tech.ithunter.app.FormActivity;
import tech.ithunter.app.R;
import tech.ithunter.app.check.CheckNetwork;
import tech.ithunter.app.notice.Notice;

import static tech.ithunter.app.check.CheckData.isEmailValid;
import static tech.ithunter.app.check.CheckData.isPasswordValid;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FancyButton loginButton;
    private TextView resetPasswordTextView, signUpTextView, signUpQuestionTextView, backTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            initializeComponents();

            // Configure Firebase
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(LoginActivity.this, loginButton, e.toString());
        }
    }

    private void updateUI(FirebaseUser user) {
        try {
            if (user != null) {
                //Toast.makeText(getApplicationContext(), "Is connected", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, FormActivity.class));
            }
        } catch (Exception e) {
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(LoginActivity.this, loginButton, e.toString());
        }
    }

    private void loginWithEmailAndPassword(String email, String password) {
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCanceledListener(this, new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Notice.showWarningToast(LoginActivity.this, R.string.warning_auth_canceled);
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
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(LoginActivity.this, loginButton, e.toString());
        }
    }

    private void attemptLogin() {
        try {
            emailEditText.setError(null);
            passwordEditText.setError(null);
            if (!new CheckNetwork(getApplicationContext()).isConnected()) {
                Notice.showErrorInTSnackBar(LoginActivity.this, loginButton, R.string.error_no_network_connection);
                clearEditTextFocus();
                return;
            }
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            boolean cancel = false;
            View focusView = null;

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
                loginWithEmailAndPassword(email, password);
                //showProgress(true);
            }
        } catch (Exception e) {
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(LoginActivity.this, loginButton, e.toString());
        }
    }

    private void clearEditTextFocus() {
        try {
            emailEditText.clearFocus();
            passwordEditText.clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
        } catch (Exception e) {
            clearEditTextFocus();
            Notice.showErrorInTSnackBar(LoginActivity.this, loginButton, e.toString());
        }
    }

    private void clearEditTextErrors() {
        emailEditText.setError(null);
        passwordEditText.setError(null);
    }

    private void initializeComponents() throws Exception {
        emailEditText = (EditText) findViewById(R.id.email_editText);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        loginButton = (FancyButton) findViewById(R.id.login_with_email_button);
        resetPasswordTextView = (TextView) findViewById(R.id.reset_password_textView);
        signUpTextView = (TextView) findViewById(R.id.sign_up_textView);
        signUpQuestionTextView = (TextView) findViewById(R.id.sign_up_question_textView);
        backTextView = (TextView) findViewById(R.id.back_textView);

        setListenersForComponents();
    }

    private void setListenersForComponents() throws Exception {
        resetPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
        signUpTextView.setOnClickListener(signUpButtonOnClickListener);
        signUpQuestionTextView.setOnClickListener(signUpButtonOnClickListener);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private View.OnClickListener signUpButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clearEditTextErrors();
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        }
    };
}