package thealphalabs.alphaglasslauncher.wifi;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;


/**
 * Created by yeol on 15. 6. 9.
 */
public class WifiTransferHelper {

    private Context context;

    private ServiceConnection mConn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    public WifiTransferHelper(Context paramContext){
        context=paramContext;
    }


}
