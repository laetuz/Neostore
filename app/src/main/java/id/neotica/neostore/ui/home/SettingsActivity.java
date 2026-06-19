package id.neotica.neostore.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import id.neotica.neostore.R;
import id.neotica.neostore.utils.AuthManager;
import id.neotica.neostore.utils.CrashCatcher;

public class SettingsActivity extends Activity {

    private AuthManager authManager;

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

        CheckBox cbAdultContent = (CheckBox) findViewById(R.id.cb_adult_content);
        cbAdultContent.setChecked(authManager.isAdultContentEnabled());
        cbAdultContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                authManager.saveAdultContentEnabled(b);
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
