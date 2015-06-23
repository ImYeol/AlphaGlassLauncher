package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import thealphalabs.alphaglasslauncher.R;
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

    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mManager;
    @Override
    public void onCreate() {
        super.onCreate();
        Init();

        mView=new MouseView(this);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.TOP | Gravity.LEFT;

        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mManager.addView(mView, mParams);
    }
    private Handler handler=new Handler(Looper.getMainLooper());
    public void SendMouseData(float x,float y) {

        ((MouseView)mView).move(x, y);
    }
    class MouseView extends View {

        private static final int KUMA_SIZE = 50;
        private Bitmap kuma;
        private int w;
        private int h;
        private float x;
        private float y;
        public MouseView(Context context) {

            super(context);

        //    requestWindowFeature(Window.FEATURE_NO_TITLE);

            kuma = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.accel_img);
        }

        public void move(float mx, float my) {

           // this.x -= (mx * 4f);

           // this.y += (my * 4f);
            this.x+=mx * w;
            this.y+=my * h;

            if (this.x < 0) {

                this.x = 0;

            } else if ((this.x + KUMA_SIZE) > this.w) {

                this.x = this.w - KUMA_SIZE;

            }

            if (this.y < 0) {

                this.y = 0;

            } else if ((this.y + KUMA_SIZE) > this.h) {

                this.y = this.h - KUMA_SIZE;

            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
          //  invalidate();
        }

        @Override

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {

            this.w = w;
            this.h = h;
            this.x = (w - KUMA_SIZE) / 2f;
            this.y = (h - KUMA_SIZE) / 2f;
        }

        @Override

        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(kuma, x, y, null);

         //   canvas.drawBitmap(kuma, x+1, y+1, null);

        //    canvas.drawBitmap(kuma, x+2, y+2, null);

        }

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

        if(mView != null) {
            mManager.removeView(mView);
            mView = null;
        }
    }

    public void sendSensorData(float x,float y,float z,int type) {
        RemoteSensorEvent event=new RemoteSensorEvent();
        event.setEventData(x, y, z);
        event.setAccuracy(0);
        if(type == EventDataType.EventGyro) {
            Collection<RemoteSensorListener> cols=mGyroCallback.values();
            for(RemoteSensorListener listener : cols) {
             //   Log.d(TAG,"sendToGyroCallback");
                listener.onRemoteSensorChanged(event);
            }
        }
        else if(type == EventDataType.EventAccel) {
            Collection<RemoteSensorListener> cols=mAccelCallback.values();
         //   Log.d(TAG,"cols33333:"+ cols);
            for(RemoteSensorListener listener : cols) {
            //    Log.d(TAG,"sendToAccelCallback");
                listener.onRemoteSensorChanged(event);
            }
        }
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
