package id.neotica.neostore.ui.detail;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import id.neotica.neostore.BuildConfig;
import id.neotica.neostore.R;
import id.neotica.neostore.model.VersionModel;
import id.neotica.neostore.network.ApiCallback;
import id.neotica.neostore.network.ApiTask;
import id.neotica.neostore.ui.VersionAdapter;

public class AppDetailActivity extends Activity {

    private TextView tvTitle, tvDesc;
    private ListView lvVersions;
    private VersionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        tvTitle = (TextView) findViewById(R.id.tv_detail_title);
        tvDesc = (TextView) findViewById(R.id.tv_detail_desc);
        lvVersions = (ListView) findViewById(R.id.lv_versions);

        adapter = new VersionAdapter(this, new ArrayList<VersionModel>());
        lvVersions.setAdapter(adapter);

        String packageName = getIntent().getStringExtra("PACKAGE_NAME");

        if (packageName != null) {
            fetchAppDetails(packageName);
        } else {
            Toast.makeText(this, "Error: No package provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchAppDetails(String packageName) {
        String targetUrl = BuildConfig.BASE_URL + "/apps/" + packageName;

        new ApiTask(this, "GET", targetUrl, null, "Loading details...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);

                    tvTitle.setText(root.optString("title", "Unknown App"));
                    tvDesc.setText(root.optString("description", "No description available."));

                    JSONArray versionsArray = root.optJSONArray("versions");
                    if (versionsArray != null) {
                        adapter.clear();
                        for (int i = 0; i < versionsArray.length(); i++) {
                            JSONObject vObj = versionsArray.getJSONObject(i);
                            adapter.add(new VersionModel(
                                    vObj.optString("version_name", ""),
                                    vObj.optInt("version_code", 0),
                                    vObj.optString("file_url", ""),
                                    vObj.optString("changelog", "")
                            ));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AppDetailActivity.this, "Error parsing app details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (errorMessage.contains("HTTP_ERROR_404|")) {
                    String actualError = errorMessage.substring(errorMessage.indexOf("|") + 1);

                    Toast.makeText(AppDetailActivity.this, actualError, Toast.LENGTH_LONG).show();
                    finish();

                } else if (errorMessage.contains("|")) {
                    // Handle any other HTTP errors (500, 403, etc) that have custom messages
                    String actualError = errorMessage.substring(errorMessage.indexOf("|") + 1);
                    Toast.makeText(AppDetailActivity.this, actualError, Toast.LENGTH_LONG).show();

                } else {
                    // Fallback for standard network errors (e.g. timeout, no internet)
                    Toast.makeText(AppDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }).execute();
    }
}
