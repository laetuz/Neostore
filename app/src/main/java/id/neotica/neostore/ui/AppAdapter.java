package id.neotica.neostore.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import id.neotica.neostore.R;
import id.neotica.neostore.model.AppModel;

/**
 * Created by ryomartin on 21/03/26.
 */

public class AppAdapter extends ArrayAdapter<AppModel> {
    public AppAdapter(Context context, List<AppModel> apps) {
        super(context, 0, apps);
    }

    private static class ViewHolder {
        TextView tvtitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModel app = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);

            viewHolder.tvtitle = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (app != null) {
            viewHolder.tvtitle.setText(app.title);
        }

        return convertView;
    }
}
