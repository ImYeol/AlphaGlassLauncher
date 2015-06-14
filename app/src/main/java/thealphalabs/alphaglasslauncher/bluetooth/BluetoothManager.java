package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;

import thealphalabs.alphaglasslauncher.util.EventDataType;

/**
 * Created by yeol on 15. 6. 11.
 */
public class BluetoothManager {

    private UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");
    private BluetoothAdapter mBluetoothAdapter;
    private final String TAG="BluetoothManager";
    private int mState;
    private DataOutputStream mDataOutputStream;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler BltServiceHandler;
    private ThreadPoolExecutor DataReceiverWorker;
    private BluetoothTransferService mBluetoothService

    private final ReadWriteLock lock=new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    // State constants
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private int ThreadPoolNum=3;

    public BluetoothManager(Service paramService,Handler paramHandler){
        mState=STATE_NONE;
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        BltServiceHandler=paramHandler;
        mBluetoothService=(BluetoothTransferService)paramService;
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "Connecting to: " + device);
        int localState=getState();

        if (localState != STATE_NONE)
            return;

        // Cancel any thread attempting to make a connection
        if (localState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
   /*     if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        } */

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }
    public void connectDevice(String address) {
        Log.d(TAG, "Service - connect to " + address);

        // Get the BluetoothDevice object
        if(mBluetoothAdapter != null) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            if(device != null) {
                connect(device);
            }
        }
    }
    public void connectDevice(BluetoothDevice device) {
        if(device != null) {
            connect(device);
        }
    }
    public void setState(int state){
        writeLock.lock();
        mState=state;
        writeLock.unlock();
    }

    public int getState(){
        int state;
        readLock.lock();
        state=mState;
        readLock.unlock();
        return state;
    }

    public void Destroy() {
        Log.d(TAG, "BltManager Destroy--");
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        setState(STATE_NONE);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private ExecutorService concurrentWorker= Executors.newFixedThreadPool(ThreadPoolNum);
        private DataInputStream mmDataInputStream;
        private InputStream mmInputStream;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
            setState(STATE_CONNECTING);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                //connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }
            setState(STATE_CONNECTED);
            getStream();
            startConcurrentWorker();
            mConnectThread=null;
        }
        public void getStream() {
            InputStream tmpIn;
            try {
                tmpIn = mmSocket.getInputStream();
                mmInputStream = tmpIn;
                mmDataInputStream=new DataInputStream(mmInputStream);
            } catch (IOException e) {
                Log.d(TAG,"socket.getInputStream : "+e.getMessage());
            }
        }

        public void startConcurrentWorker() {
            try {
                concurrentWorker.execute(new BluetoothDataHandler(mBluetoothService, mmDataInputStream));
            } catch (RejectedExecutionException e) {
                Log.d(TAG,"StartConcurrentWorker() rejected :"+e.getMessage());
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }	// End of class ConnectThread

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final DataInputStream mmDataInputStream;
        private BluetoothDataHandler mmWorker;
        private ExecutorService
        private String text;
        private float x,y,z;


        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            try
            {
                tmpIn = socket.getInputStream();
            }
            catch (IOException e)
            {
                Log.d(TAG,"socket.getInputStream : "+e.getMessage());
            }
            mmInStream = tmpIn;
            mmDataInputStream=new DataInputStream(mmInStream);

        }

        public void run()
        {
            int type=0;

            while (true)
            {
                try
                {
                    if((type=mmDataInputStream.readInt()) == EventDataType.EventAccel ||
                            type == EventDataType.EventGyro)
                    {
                        mBluetoothService.sendSensorData();
                    }
                }
                catch (Exception e)
                {
                    Log.d(TAG,"error connectedThread: "+e.getMessage());
                    break;
                }
            }
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
            }
        }
    }

}
