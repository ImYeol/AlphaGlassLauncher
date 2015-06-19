package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import thealphalabs.alphaglasslauncher.RemoteSensorEvent;
import thealphalabs.alphaglasslauncher.util.ConnectionInfo;
import thealphalabs.alphaglasslauncher.util.Constants;
import thealphalabs.alphaglasslauncher.util.EventDataType;


/**
 * Created by yeol on 15. 6. 9.
 */
public class BluetoothTransferService extends Service{

    public final static String TAG="BLTService";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBltManager;
    private BluetoothConnectionReceiver SensorChangeReceiver;
    private RemoteSensorListener mCallback;
    private final IBinder mBinder= new BluetoothServiceBinder();
    private Handler mSensorDataHandler=new Handler();
    private ConnectionInfo mConnectionInfo;
    private Hashtable<Integer,RemoteSensorListener> mGyroCallback
            =new Hashtable<>();
    private Hashtable<Integer,RemoteSensorListener> mAccelCallback
            =new Hashtable<>();

    public class BluetoothServiceBinder extends Binder {
        public int registerListener(RemoteSensorListener paramCallback,int paramType) {
            Random random=new Random();
            int ID=random.nextInt(100);
            if(paramType == RemoteSensor.ACCEL) {
                Log.d(TAG,"accel register");
                mAccelCallback.put(ID, paramCallback);
            }
            else if(paramType == RemoteSensor.GYRO) {
                Log.d(TAG,"gyro register");
                mGyroCallback.put(ID, paramCallback);
            }
            return ID;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Init();
    }

    private void Init(){
        Log.d(TAG, "# Service : initialize ---");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Log.d(TAG,"init - bluetoothAdapter failed");
            return;
        }
        mConnectionInfo = ConnectionInfo.getInstance(this);
        if(!mBluetoothAdapter.isEnabled()) {
            // BT is not on, need to turn on manually.
            // Activity will do this.
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        } else {
            SetupBTManager();
            mBltManager.Listening();
        }
        registerSensorChangeReceiver();
    }
    public void SetupBTManager() {
        Log.d(TAG, "make bluetooth manager");
        if(mBltManager == null){
            mBltManager = new BluetoothManager(this,mSensorDataHandler);
        }
    }

    public void registerSensorChangeReceiver() {
        SensorChangeReceiver=new BluetoothConnectionReceiver();
        IntentFilter localIntentFilter=new IntentFilter();
        localIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        localIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        localIntentFilter.addAction(Constants.DISCONNECTION_BROADCAST);
        this.registerReceiver(SensorChangeReceiver, localIntentFilter);
    }

    public void unRegisterSensorChangeReceiver() {
        this.unregisterReceiver(SensorChangeReceiver);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DestroyService();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        DestroyService();
        return super.onUnbind(intent);
    }

    public void DestroyService() {
        Log.d(TAG,"Destroy Service --");
        mBluetoothAdapter=null;
        if(mBltManager != null)
            mBltManager.Destroy();
        mBltManager = null;
        mGyroCallback.clear();
        mGyroCallback=null;
        mAccelCallback.clear();
        mAccelCallback=null;
        unRegisterSensorChangeReceiver();
    }

    public void sendSensorData(float x,float y,float z,int type) {
        RemoteSensorEvent event=new RemoteSensorEvent();
        event.setEventData(x, y, z);
        event.setAccuracy(0);
        if(type == EventDataType.EventGyro) {
            Collection<RemoteSensorListener> cols=mGyroCallback.values();
            for(RemoteSensorListener listener : cols) {
                Log.d(TAG,"sendToGyroCallback");
                listener.onRemoteSensorChanged(event);
            }
        }
        else if(type == EventDataType.EventAccel) {
            Collection<RemoteSensorListener> cols=mAccelCallback.values();
            Log.d(TAG,"cols33333:"+ cols);
            for(RemoteSensorListener listener : cols) {
                Log.d(TAG,"sendToAccelCallback");
                listener.onRemoteSensorChanged(event);
            }
        }
       /* mSensorDataHandler.post(new Runnable() {
            @Override
            public void run() {
                RemoteSensorEvent event=new RemoteSensorEvent();
                event.setEventData(finalX,finalY,finalZ);
                event.setAccuracy(0);
                mCallback.onRemoteSensorChanged(event);
            }
        });*/
    }

    public class BluetoothConnectionReceiver extends BroadcastReceiver {

        private final String TAG="BluetoothConnectionReceiver";

        public BluetoothConnectionReceiver() {

        }
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG,"onReceive");

            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String address = device.getAddress();

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
                mBltManager.setState(BluetoothManager.STATE_CONNECTED);
            }
            else if (Constants.DISCONNECTION_BROADCAST.equals(action)) {
                int localID=intent.getExtras().getInt(Constants.DISCONNECTION_BROADCAST_ID);
                if(mGyroCallback.remove(localID) == null) {
                    mAccelCallback.remove(localID);
                }
            }
        }

        private boolean ShouldBeConnected(String paramLastRequestAddress,String paramCurAddress) {
           return TextUtils.isEmpty(paramLastRequestAddress)
                    || !paramCurAddress.equals(paramLastRequestAddress);

        }

        private void reconnect() {
            mBltManager.setState(BluetoothManager.STATE_NONE);
            mBltManager.Listening();
        }

    }

}
