package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

import thealphalabs.alphaglasslauncher.util.ConnectionInfo;

/**
 * Created by yeol on 15. 6. 9.
 */
public class BluetoothTransferService extends Service{

    public final static String TAG="BLTService";
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectionInfo mConnectionInfo=null;
    private BluetoothManager mBltManager;
    private BluetoothConnectionReceiver SensorChangeReceiver;
    private ISensorDataInterface mCallback;
    private final IBinder mBinder= new BluetoothServiceBinder();
    private int SensorEventType=RemoteSensor.NONE;
    private Handler mSensorDataHandler=new Handler();

    public class BluetoothServiceBinder extends Binder {
        public void registerListener(ISensorDataInterface paramCallback,int paramType) {
            BluetoothTransferService.this.mCallback=paramCallback;
            SensorEventType=paramType;
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

        // Get connection info instance
        //      mConnectionInfo = ConnectionInfo.getInstance(mContext);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            // BT is not on, need to turn on manually.
            // Activity will do this.
        } else {
            SetupBTManager();
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
        SensorChangeReceiver=new BluetoothConnectionReceiver(getBaseContext(),mBltManager);
        IntentFilter localIntentFilter=new IntentFilter();
        localIntentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        localIntentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
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
        unRegisterSensorChangeReceiver();

    }
    public void connectDevice(String address) {
        Log.d(TAG, "Service - connect to " + address);

        // Get the BluetoothDevice object
        if(mBluetoothAdapter != null) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            if(device != null && mBltManager != null) {
                mBltManager.connect(device);
            }
        }
    }
    public void connectDevice(BluetoothDevice device) {
        if(device != null && mBtManager != null) {
            mBltManager.connect(device);
        }
    }

    public void sendSensorData(float x,float y,float z) {
        mSensorDataHandler.post(new Runnable() {
            @Override
            public void run() {
                RemoteSensorEvent event=new RemoteSensorEvent();
                event.setEventData(x,y,z);
                mCallback.onRemoteSensorChanged();
            }
        });
    }

}
