package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yeol on 15. 6. 11.
 */
public class BluetoothManager {

    private UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");
    private BluetoothAdapter mBluetoothAdapter;
    private final String TAG="BluetoothManager";
    private int mState;
    private Handler BltServiceHandler;
    private BluetoothTransferService mBluetoothService;
    private ReConnectionService mReConnectionService;
    private AcceptThread mAcceptThread;

    private final ReadWriteLock lock=new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private int ThreadPoolNum=3;
    private BluetoothServerSocket mServerSocket;
    private BluetoothSocket mClientSocket;
    private DataInputStream mDataInputStream;
    private DataOutputStream mDataOutputStream;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ExecutorService concurrentWorker= Executors.newFixedThreadPool(ThreadPoolNum);

    // State constants
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTENING = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public BluetoothManager(Service paramService,Handler paramHandler){
        mState=STATE_NONE;
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        BltServiceHandler=paramHandler;
        mBluetoothService=(BluetoothTransferService)paramService;
        mReConnectionService=ReConnectionService.getInstance(paramService,this);
    }

    public void Listening() {
        Log.d(TAG, "Listening: ");
        int localState=getState();

        if (localState != STATE_NONE)
            return ;
        if(mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        // Start the thread to connect with the given device
        mAcceptThread=new AcceptThread();
        mAcceptThread.start();
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
        if(mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if(concurrentWorker != null) {
            concurrentWorker.shutdown();
            concurrentWorker=null;
        }
        setState(STATE_NONE);
    }

    private class AcceptThread extends Thread {

        public AcceptThread() {
            try {
                mServerSocket = mBluetoothAdapter
                        .listenUsingRfcommWithServiceRecord(
                                "ClassicBluetoothServer", MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "get listend sock is error");
            }
        }

        public void run() {
            Log.d(TAG, "AcceptThread run()");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mServerSocket.accept(); // blocking call
                    mClientSocket=socket;
                } catch (IOException e) {
                    Log.v(TAG, e.getMessage());
                    break;
                }
                Log.d(TAG, "socket is accepted");
                if (socket != null) {
                    // Do work in a separate thread
                    setState(STATE_CONNECTED);
                    getStream();
                    startConcurrentWorker();
                    Log.d(TAG, "conncurrent Thread is called");
                }
            }
        }

        public void getStream() {
            try {
                mInputStream=mClientSocket.getInputStream();
                mOutputStream=mClientSocket.getOutputStream();
                mDataInputStream=new DataInputStream(mInputStream);
                mDataOutputStream = new DataOutputStream(mOutputStream);
            } catch (IOException e) {
                Log.d(TAG,"socket.getInputStream : "+e.getMessage());
            }
        }

        public void startConcurrentWorker() {
            if(concurrentWorker == null)
                concurrentWorker=Executors.newFixedThreadPool(ThreadPoolNum);

            try {
                concurrentWorker.execute(new BluetoothDataHandler(mBluetoothService, mDataInputStream, mDataOutputStream));
            } catch (RejectedExecutionException e) {
                Log.d(TAG,"StartConcurrentWorker() rejected :"+e.getMessage());
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.d(TAG,"failed to close server socket "+ e.getMessage() );
            }
        }
    }


}
