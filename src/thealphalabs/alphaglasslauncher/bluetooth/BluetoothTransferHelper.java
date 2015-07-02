package thealphalabs.alphaglasslauncher.bluetooth;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import thealphalabs.alphaglasslauncher.util.Constants;
import thealphalabs.alphaglasslauncher.util.IntentSender;

/**
 * Created by yeol on 15. 6. 9.
 */
public class BluetoothTransferHelper {

    private Context context;
    private final String TAG="BlueToothTransferHelper";
    private BluetoothTransferService.BluetoothServiceBinder mBinder;
    private RemoteSensorListener mRemoteGyroListener;
    private RemoteSensorListener mRemoteAccelListener;

    private int ListenerType=RemoteSensor.NONE;
    private int ClientGyroID;
    private int ClientAccelID;

    private ServiceConnection mConn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"Service is connected");
            mBinder=(BluetoothTransferService.BluetoothServiceBinder)service;
            if(mRemoteAccelListener != null)
                ClientAccelID=mBinder.registerListener(mRemoteAccelListener,RemoteSensor.ACCEL);
            if(mRemoteGyroListener != null)
                ClientGyroID=mBinder.registerListener(mRemoteGyroListener,RemoteSensor.GYRO);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Intent localIntent=new Intent(Constants.DISCONNECTION_BROADCAST);
            localIntent.putExtra(Constants.DISCONNECTION_BROADCAST_ID,ClientAccelID);
            IntentSender.getInstance().sendBroadcast(context, localIntent);
            ClientAccelID=-1;
            mRemoteAccelListener=null;
            localIntent.removeExtra(Constants.DISCONNECTION_BROADCAST_ID);
            localIntent.putExtra(Constants.DISCONNECTION_BROADCAST_ID,ClientGyroID);
            IntentSender.getInstance().sendBroadcast(context, localIntent);
            ClientGyroID=-1;
            mRemoteGyroListener=null;
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

    public void StopConnection() {
        IntentSender.getInstance().unbindService(context, mConn);
    }

    public void registerRemoteSensorListener(RemoteSensorListener paramListener,int paramType) {
        Log.d(TAG,"registerRemoteSensorListener");
        if (paramType == RemoteSensor.ACCEL) {
            mRemoteAccelListener=paramListener;
            if(mBinder != null)
                ClientAccelID=mBinder.registerListener(mRemoteAccelListener,RemoteSensor.ACCEL);
        } else if (paramType == RemoteSensor.GYRO) {
            mRemoteGyroListener=paramListener;
            if(mBinder != null)
                ClientGyroID=mBinder.registerListener(mRemoteGyroListener,RemoteSensor.GYRO);
        }

    }

}
