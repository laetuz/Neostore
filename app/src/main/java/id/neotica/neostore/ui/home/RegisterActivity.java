package id.neotica.neostore.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import id.neotica.neostore.BuildConfig;
import id.neotica.neostore.R;
import id.neotica.neostore.network.ApiCallback;
import id.neotica.neostore.network.ApiTask;
import id.neotica.neostore.utils.CrashCatcher;

public class RegisterActivity extends Activity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    private static final String REGISTER_URL = BuildConfig.AUTH_BASE_URL + "/auth/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_register);
        CrashCatcher.showCrashLogIfAny(this);

        etUsername = (EditText) findViewById(R.id.et_register_username);
        etEmail = (EditText) findViewById(R.id.et_register_email);
        etPassword = (EditText) findViewById(R.id.et_register_password);
        btnRegister = (Button) findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
            }
        });
    }

    private void performRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("password", password);
            payload.put("email", email);

            new ApiTask(this, "POST", REGISTER_URL, payload.toString(), "Registering...", new ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        String message = json.optString("message", "Account created.");
                        String registeredEmail = json.optString("email", "");
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }).execute();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_LONG).show();
        }
    }
}
