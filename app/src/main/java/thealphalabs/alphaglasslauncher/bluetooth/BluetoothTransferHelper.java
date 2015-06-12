package thealphalabs.alphaglasslauncher.bluetooth;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import thealphalabs.alphaglasslauncher.util.IntentSender;

/**
 * Created by yeol on 15. 6. 9.
 */
public class BluetoothTransferHelper {

    private Context context;
    private final String TAG="BlueToothTransferHelper";
    private BluetoothTransferService.BluetoothServiceBinder mBinder;


    private ServiceConnection mConn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder=(BluetoothTransferService.BluetoothServiceBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder=null;
        }
    };
    public BluetoothTransferHelper(Context context){
        this.context=context;
        //StartConnection();
    }

    public void StartConnection() {
        Intent localIntent=new Intent(context,BluetoothTransferService.class);
        IntentSender.getInstance().startService(context,localIntent);
        IntentSender.getInstance().bindService(context,localIntent,mConn,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void StopConnection() {
        Intent localIntent=new Intent(context,BluetoothTransferService.class);
        IntentSender.getInstance().unbindService(context,mConn);
        IntentSender.getInstance().stopService(context, localIntent);
    }

    public void registerRemoteSensorListener(RemoteSensorListener paramListener,int paramType) {
        mBinder.registerListener(paramListener,paramType);
    }

}
