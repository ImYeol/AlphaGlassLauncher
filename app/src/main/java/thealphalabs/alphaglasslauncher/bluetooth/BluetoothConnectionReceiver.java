package thealphalabs.alphaglasslauncher.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import thealphalabs.alphaglasslauncher.util.ConnectionInfo;


/**
 * Created by yeol on 15. 6. 12.
 */
public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private final String TAG="BluetoothConnectionReceiver";
    private BluetoothManager mBltManager;
    private ConnectionInfo mConnectionInfo;
    private Context context;
    private ReConnectionService mReConnectionService;

    public BluetoothConnectionReceiver(Context paramContext,BluetoothManager paramBltManager) {
        mBltManager=paramBltManager;
        context=paramContext;
        mConnectionInfo=ConnectionInfo.getInstance(paramContext);
        mReConnectionService=ReConnectionService.getInstance(paramContext,paramBltManager);
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"onReceive");

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String address=null;
        if(device !=null)
            address = device.getAddress();

        if (TextUtils.isEmpty(address))
            return;

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
        {
            mBltManager.Destroy();
            reconnect();
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
        {
            String lastRequestAddress = mConnectionInfo.getDeviceAddress();
            if(ShouldBeConnected(lastRequestAddress,address)) {
                mConnectionInfo.setDeviceAddress(address);
                mConnectionInfo.setDeviceName(device.getName());
            }
            Log.d(TAG,"connected:"+mBltManager);
            mBltManager.setState(BluetoothManager.STATE_CONNECTED);
        }

    }

    private boolean ShouldBeConnected(String paramLastRequestAddress,String paramCurAddress) {
        if (TextUtils.isEmpty(paramLastRequestAddress))
            return true;
        if(!paramCurAddress.equals(paramLastRequestAddress))
            return true;
        return false;
    }

    private void reconnect() {
        mBltManager.Listening();
    }

}
