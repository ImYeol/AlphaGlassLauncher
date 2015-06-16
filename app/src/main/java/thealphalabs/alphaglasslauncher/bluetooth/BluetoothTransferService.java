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

import thealphalabs.alphaglasslauncher.RemoteSensorEvent;
import thealphalabs.alphaglasslauncher.util.ConnectionInfo;


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
    private int SensorEventType=RemoteSensor.NONE;
    private Handler mSensorDataHandler=new Handler();
    private ConnectionInfo mConnectionInfo;

    private ArrayList<RemoteSensorListener> mSensorListenerList
            = new ArrayList<RemoteSensorListener>();

    public class BluetoothServiceBinder extends Binder {
        public void registerListener(RemoteSensorListener paramCallback,int paramType) {
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
        final float finalX=x;
        final float finalY=y;
        final float finalZ=z;

        mSensorDataHandler.post(new Runnable() {
            @Override
            public void run() {
                RemoteSensorEvent event=new RemoteSensorEvent();
                event.setEventData(finalX,finalY,finalZ);
                event.setAccuracy(0);
                mCallback.onRemoteSensorChanged(event);
            }
        });
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
                Log.d(TAG,"connected:"+mBltManager);
                mBltManager.setState(BluetoothManager.STATE_CONNECTED);
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
