package tech.ithunter.app.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import mehdi.sakout.fancybuttons.FancyButton;
import tech.ithunter.app.R;
import tech.ithunter.app.notice.Notice;

import static tech.ithunter.app.check.CheckData.isEmailValid;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private FancyButton resetPasswordButton;
    private TextView backTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.reset_password);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            initializeComponents();

            // Configure Firebase
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(ResetPasswordActivity.this, resetPasswordButton, e.toString());
        }
    }

    private void clearEditTextFocus() {
        try {
            emailEditText.clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
        } catch (Exception e) {
            Notice.showErrorInTSnackBar(ResetPasswordActivity.this, resetPasswordButton, e.toString());
        }
    }

    private void clearEditTextErrors() {
        emailEditText.setError(null);
    }

    private void initializeComponents() throws Exception {
        emailEditText = (EditText) findViewById(R.id.email_reset_password_editText);
        resetPasswordButton = (FancyButton) findViewById(R.id.reset_password_button);
        backTextView = (TextView) findViewById(R.id.back_textView);

        setListenersForComponents();
    }

    private void setListenersForComponents() throws Exception {
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                    if (isEmailValid(email)) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Notice.showSuccessToast(ResetPasswordActivity.this, R.string.email_sent);
                                        } else {
                                            Notice.showErrorToast(ResetPasswordActivity.this, R.string.error_email_sent);
                                        }
                                    }
                                });
                    } else {
                        emailEditText.setError(getString(R.string.error_invalid_email));
                    }
                } else {
                    emailEditText.setError(getString(R.string.error_field_required));
                }
            }
        });
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
