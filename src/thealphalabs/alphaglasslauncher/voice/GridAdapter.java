package thealphalabs.alphaglasslauncher.voice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.util.AppInfoProvider;

/**
 * Created by yeol on 15. 8. 21.
 */
public class GridAdapter extends BaseAdapter {

    private Context context;
    private AppInfoProvider mAppInfoProvider;

    public GridAdapter(Context context, AppInfoProvider appInfoProvider) {
        this.context        = context;
        this.mAppInfoProvider = appInfoProvider;
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View iconView;

        if (convertView == null) {

            iconView = inflater.inflate( R.layout.icon_view , null);

            TextView textView = (TextView) iconView
                    .findViewById(R.id.icon_name);

            textView.setText(mAppInfoProvider.getName(position));

            ImageView imageView = (ImageView) iconView
                    .findViewById(R.id.icon_image);

            imageView.setImageDrawable(mAppInfoProvider.getIcon(position));

        } else {
            iconView = (View) convertView;
        }

        return iconView;
    }
}
