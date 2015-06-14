package thealphalabs.alphaglasslauncher.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import thealphalabs.alphaglasslauncher.util.ConnectionInfo;
import thealphalabs.alphaglasslauncher.util.Constants;

/**
 * Created by yeol on 15. 6. 12.
 */
public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private final String TAG="BluetoothConnectionReceiver";
    private BluetoothManager mBltManager;
    private ConnectionInfo mConnectionInfo;
    private Context context;

    public BluetoothConnectionReceiver(Context paramContext,BluetoothManager paramBltManager) {
        mBltManager=paramBltManager;
        context=paramContext;
        mConnectionInfo=ConnectionInfo.getInstance(paramContext);
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"ConnectionReceiver.java | onReceive");

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String address = device.getAddress();

        if (TextUtils.isEmpty(address))
            return;

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
        {
            mBltManager.Destroy();
            reconnect(context, address);
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
        {
            String lastRequestAddress = mConnectionInfo.getDeviceAddress();
            if(ShouldBeConnected(lastRequestAddress,address)) {
                mConnectionInfo.setDeviceAddress(address);
                mConnectionInfo.setDeviceName(device.getName());
            }
            mBltManager.setState(BluetoothManager.STATE_CONNECTED);
        }
        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice localDevice = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String localName = device.getName();

            // found another Android device of mine and start communication
            if (localName != null ) {
                mBltManager.connect(localDevice);
            }
        }
    }

    private boolean ShouldBeConnected(String paramLastRequestAddress,String paramCurAddress) {
        if (TextUtils.isEmpty(paramLastRequestAddress)
                || paramLastRequestAddress.equals(paramCurAddress) == false)
            return true;
    }

    private void reconnect(Context paramContext, String paramAddress) {
        String lastConnectAddress = ConnectionInfo.getInstance(context).getDeviceAddress();
        if (TextUtils.isEmpty(lastConnectAddress))
            return;

        // 연결이 끊기면 1분 마다 스캔을 다시 한다.
        if (paramAddress.equals(lastConnectAddress)) {
            Log.i("DisconnectedReceiver.java | onReceive", "|==" + "스캔 다시하기" + "|");
          //  ReConnectService.instance($context).autoReconnect();
        }
    }
}
