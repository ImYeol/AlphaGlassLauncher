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
    private RemoteSensorListener mRemoteSensorListener;
    private int ListenerType=RemoteSensor.NONE;

    private ServiceConnection mConn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder=(BluetoothTransferService.BluetoothServiceBinder)service;
            mBinder.registerListener(mRemoteSensorListener,ListenerType);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder=null;
        }
    };
    public BluetoothTransferHelper(Context context){
        this.context=context;
    }

    public void StartConnection() {
        Intent localIntent=new Intent(context,BluetoothTransferService.class);
        IntentSender.getInstance().bindService(context,localIntent,mConn,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void StopConnection() {
        IntentSender.getInstance().unbindService(context, mConn);
    }

    public void registerRemoteSensorListener(RemoteSensorListener paramListener,int paramType) {
        this.mRemoteSensorListener=paramListener;
        this.ListenerType=paramType;
        StartConnection();
    }

}
