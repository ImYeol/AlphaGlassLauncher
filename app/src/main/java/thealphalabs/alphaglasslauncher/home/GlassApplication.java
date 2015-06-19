package thealphalabs.alphaglasslauncher.home;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import thealphalabs.alphaglasslauncher.bluetooth.BluetoothManager;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferHelper;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferService;
import thealphalabs.alphaglasslauncher.wifi.WifiTransferHelper;

/**
 * Created by yeol on 15. 6. 8.
 */
public class GlassApplication extends Application {

    private final String TAG="AlphaApplication";
    private BluetoothTransferHelper mBluetoothHelper;
    private WifiTransferHelper mWifiHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    public void init() {
        startService(new Intent(this, BluetoothTransferService.class));
        mBluetoothHelper=new BluetoothTransferHelper(getBaseContext());
        mWifiHelper=new WifiTransferHelper(getBaseContext());
        mBluetoothHelper.StartConnection();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, BluetoothTransferService.class));
    }

    public BluetoothTransferHelper getBluetoothHelper() {
        return mBluetoothHelper;
    }
}
