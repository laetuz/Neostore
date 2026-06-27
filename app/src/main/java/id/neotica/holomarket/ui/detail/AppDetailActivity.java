package id.neotica.holomarket.ui.detail;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.network.DownloadTask;
import id.neotica.holomarket.ui.VersionAdapter;

public class AppDetailActivity extends Activity {

    private TextView tvTitle, tvDesc;
    private ImageView ivIcon;
    private ListView lvVersions;
    private VersionAdapter adapter;
    private Button btDownload;

    private static final String INTENT_PACKAGE_NAME = "PACKAGE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        tvTitle = (TextView) findViewById(R.id.tv_detail_title);
        tvDesc = (TextView) findViewById(R.id.tv_detail_desc);
        ivIcon = (ImageView) findViewById(R.id.iv_detail_icon);
        lvVersions = (ListView) findViewById(R.id.lv_versions);
        btDownload = (Button) findViewById(R.id.bt_download);

        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getCount() == 0) {
                    Toast.makeText(AppDetailActivity.this, "No versions available.", Toast.LENGTH_SHORT).show();
                    return;
                }

                VersionModel latestVersion = null;
                int maxVersionCode = -1;

                // find the highest version_code
                for (int i = 0; i < adapter.getCount(); i++) {
                    VersionModel current = adapter.getItem(i);
                    if (current != null && current.versionCode > maxVersionCode) {
                        maxVersionCode = current.versionCode;
                        latestVersion = current;
                    }
                }

                // Download when found the highest version_code
                if (latestVersion != null && latestVersion.fileUrl != null && latestVersion.fileUrl.length() > 0) {

                    String downloadUrl = BuildConfig.FILE_BASE_URL + latestVersion.fileUrl;
                    String fileName = latestVersion.fileUrl.substring(latestVersion.fileUrl.lastIndexOf('/') + 1);

                    if (fileName.length() == 0 || !fileName.endsWith(".apk")) {
                        fileName = "update_v" + latestVersion.versionCode + ".apk";
                    }

                    Toast.makeText(AppDetailActivity.this, "Downloading v" + latestVersion.versionName + "...", Toast.LENGTH_SHORT).show();

                    AnalyticsTracker.track(AppDetailActivity.this, "download", "app_downloaded");

                    // start downloading.
                    new DownloadTask(AppDetailActivity.this, fileName).execute(downloadUrl);

                } else {
                    Toast.makeText(AppDetailActivity.this, "Download link not available for the latest version.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter = new VersionAdapter(this, new ArrayList<VersionModel>());
        lvVersions.setAdapter(adapter);

        lvVersions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VersionModel clickedVersion = adapter.getItem(position);

                if (clickedVersion != null && clickedVersion.fileUrl != null && clickedVersion.fileUrl.length() > 0) {

                    String downloadUrl = BuildConfig.FILE_BASE_URL + clickedVersion.fileUrl;

                    String fileName = clickedVersion.fileUrl.substring(clickedVersion.fileUrl.lastIndexOf('/') + 1);

                    if (fileName.length() == 0 || !fileName.endsWith(".apk")) {
                        fileName = "update_v" + clickedVersion.versionCode + ".apk";
                    }

                    AnalyticsTracker.track(AppDetailActivity.this, "download", "app_downloaded");

                    // start downloading.
                    new DownloadTask(AppDetailActivity.this, fileName).execute(downloadUrl);

                } else {
                    Toast.makeText(AppDetailActivity.this, "Download link not available for this version.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String packageName = getIntent().getStringExtra(INTENT_PACKAGE_NAME);

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

                    String iconUrl = root.optString("icon_url", "");
                    if (!TextUtils.isEmpty(iconUrl)) {
                        String fullImageUrl = BuildConfig.FILE_BASE_URL + "/buckets" + iconUrl;
                        ImageLoader.getInstance().displayImage(fullImageUrl, ivIcon);
                    } else {
                        ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                    }

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
