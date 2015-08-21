package thealphalabs.alphaglasslauncher.voice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.util.AppInfoProvider;

/**
 * Created by yeol on 15. 8. 21.
 */
public class UIAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private GridAdapter iconAdapter;
    private static final String TAG="UIAdapter";

    public UIAdapter(Context context,AppInfoProvider appInfoProvider) {
        inflater = LayoutInflater.from(context);
        iconAdapter= new GridAdapter(context,appInfoProvider);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = convertView;
        if (convertView == null) {
            if(position == 1){
                layout = inflater.inflate(R.layout.voice_main_view, null);
            }
            else if(position == 2) {
                layout = inflater.inflate(R.layout.icon_view, null);
                ((GridView)layout).setAdapter(iconAdapter);
            }
            Log.d(TAG,"created new view from adapter: "+position);
        }

        return layout;
    }
}
