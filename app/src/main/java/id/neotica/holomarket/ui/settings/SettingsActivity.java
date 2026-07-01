package id.neotica.holomarket.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import id.neotica.holomarket.R;
import id.neotica.holomarket.ui.home.MainActivity;
import id.neotica.holomarket.utils.AuthManager;
import id.neotica.holomarket.utils.CrashCatcher;

public class SettingsActivity extends Activity {

    private AuthManager authManager;
    private boolean ignoreCheckedChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_settings);
        CrashCatcher.showCrashLogIfAny(this);

        authManager = new AuthManager(this);

        TextView tvUsername = (TextView) findViewById(R.id.tv_settings_username);
        Button btnLogout = (Button) findViewById(R.id.btn_logout);

        String username = authManager.getUsernameFromToken();
        if (username != null) {
            tvUsername.setText(username);
        } else {
            tvUsername.setText("User");
        }

        final CheckBox cbAdultContent = (CheckBox) findViewById(R.id.cb_adult_content);
        cbAdultContent.setChecked(authManager.isAdultContentEnabled());
        cbAdultContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (ignoreCheckedChange) return;

                if (isChecked) {
                    final EditText input = new EditText(SettingsActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle("18+ Content")
                            .setMessage("Enter password to enable 18+ content:")
                            .setView(input)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = input.getText().toString();
                                    if ("adult".equals(password)) {
                                        authManager.saveAdultContentEnabled(true);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        ignoreCheckedChange = true;
                                        cbAdultContent.setChecked(false);
                                        ignoreCheckedChange = false;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ignoreCheckedChange = true;
                                    cbAdultContent.setChecked(false);
                                    ignoreCheckedChange = false;
                                    dialog.cancel();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    ignoreCheckedChange = true;
                                    cbAdultContent.setChecked(false);
                                    ignoreCheckedChange = false;
                                }
                            })
                            .show();
                } else {
                    authManager.saveAdultContentEnabled(false);
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authManager.clear();
                Toast.makeText(SettingsActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
