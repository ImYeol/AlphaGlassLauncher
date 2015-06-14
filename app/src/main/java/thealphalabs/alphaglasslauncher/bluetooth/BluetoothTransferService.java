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

import java.util.Set;

import thealphalabs.alphaglasslauncher.ISensorDataInterface;
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
        mConnectionInfo = ConnectionInfo.getInstance(this);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()) {
            // BT is not on, need to turn on manually.
            // Activity will do this.
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent, 100);
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
        BluetoothDevice localDevice=getBondedDevices();
        if(localDevice != null)
            mBltManager.connect(localDevice);
    }

    private BluetoothDevice getBondedDevices() {
        String localDeviceName=mConnectionInfo.getDeviceName();
        if(localDeviceName == null)
            return null;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.v(TAG, "bonded device - " + device.getName() + ": "
                        + device.getAddress());
                if (device.getName().equalsIgnoreCase(localDeviceName)) {
                    return device;
                }
            }
        } else {
            Log.d(TAG,"getBondedDevices failed");
        }
        return null;
    }
    public void registerSensorChangeReceiver() {
        SensorChangeReceiver=new BluetoothConnectionReceiver(this,mBltManager);
        IntentFilter localIntentFilter=new IntentFilter();
        localIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        localIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        localIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
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

    public void sendSensorData(float x,float y,float z) {
        mSensorDataHandler.post(new Runnable() {
            @Override
            public void run() {
                RemoteSensorEvent event=new RemoteSensorEvent();
                event.setEventData(x,y,z);
                event.setAccuracy(0);
                mCallback.onRemoteSensorChanged();
            }
        });
    }

}
