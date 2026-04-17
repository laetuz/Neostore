package id.neotica.neostore.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import id.neotica.neostore.R;
import id.neotica.neostore.utils.CrashCatcher;

public class MainActivity extends Activity {

    private ListView listView;
    private SectionAdapter adapter;

    private static final String INTENT_TOPIC = "URL_TOPIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        CrashCatcher.showCrashLogIfAny(this);

        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText("Welcome User!");

        listView = (ListView) findViewById(R.id.lv_main);
        List<String> topicList = Arrays.asList("APPLICATION", "GAME");

        adapter = new SectionAdapter(this, topicList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedApp = adapter.getItem(position);
                if (clickedApp != null) {
                    Intent intent = new Intent(MainActivity.this, AppListActivity.class);
                    intent.putExtra(INTENT_TOPIC, clickedApp);
                    startActivity(intent);
                }
            }
        });

    }

}