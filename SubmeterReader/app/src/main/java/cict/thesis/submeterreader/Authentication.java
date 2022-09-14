package cict.thesis.submeterreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.Executor;

public class Authentication extends AppCompatActivity {
    private TextView authStat;
    private Button authBtn;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.authentication);

        authStat = findViewById(R.id.authStat);
        authBtn = findViewById(R.id.authbutton);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(Authentication.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                authStat.setText("Authentication Error: "+errString);
            }
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
               super.onAuthenticationSucceeded(result);
               authStat.setText("Authentication Succeed!... ");
                Intent a = new Intent(getApplicationContext(),SplashScreen.class);
                startActivity(a);
                finish();
            }
            @Override
            public void onAuthenticationFailed(){
                super.onAuthenticationFailed();
                authStat.setText("Authentication Failed!... ");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication").setSubtitle("Access using fingerprint authentication.")
                .setNegativeButtonText("Cancel")
                .build();

        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);

            }
        });
    }
}
