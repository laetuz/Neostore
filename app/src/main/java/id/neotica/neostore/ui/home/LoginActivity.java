package id.neotica.neostore.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.neotica.neostore.R;
import id.neotica.neostore.network.ApiCallback;
import id.neotica.neostore.network.ApiTask;
import id.neotica.neostore.utils.AuthManager;
import id.neotica.neostore.utils.CrashCatcher;

public class LoginActivity extends Activity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private AuthManager authManager;

    private static final String LOGIN_URL = "http://dev.neotica.id/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_login);
        CrashCatcher.showCrashLogIfAny(this);

        authManager = new AuthManager(this);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("username", username);
        headers.put("password", password);

        new ApiTask(this, "GET", LOGIN_URL, null, "Logging in...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String token = json.optString("token", null);
                    String refreshToken = json.optString("refreshToken", null);
                    long expirationTime = json.optLong("expirationTime", 0);

                    if (token != null) {
                        authManager.saveToken(token);
                        authManager.saveRefreshToken(refreshToken);
                        authManager.saveExpirationTime(expirationTime);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: no token in response", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }, headers).execute();
    }
}
