package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Instrumentation;
import android.app.Service;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.IOException;

import thealphalabs.alphaglasslauncher.util.EventDataType;

/**
 * Created by yeol on 15. 6. 12.
 */
public class BluetoothDataHandler implements Runnable {

    private final String TAG="BluetoothDataHandler";
    private BluetoothTransferService mService;
    private DataInputStream mDataInputStream;
    private Instrumentation mInstrumentation;

    public BluetoothDataHandler(Service paramService, DataInputStream paramInputStream){
        mService=(BluetoothTransferService)paramService;
        mDataInputStream=paramInputStream;
        mInstrumentation=new Instrumentation();
    }
    @Override
    public synchronized void run() {
        int type= 0;
        try {
            while ((type = mDataInputStream.readInt()) > 0) {
                switch (type) {
                    case EventDataType.EventAccel:
                        sendSensorData(type);
                        break;
                    case EventDataType.EventGyro:
                        sendSensorData(type);
                        break;
                    case EventDataType.EventMouse:
                        sendMouseData();
                        break;
                    case EventDataType.EventText:
                        sendTextData();
                        break;
                    case EventDataType.EventNotification:
                        sendNotificationData();
                        break;
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "error to readInt : " + e.getMessage());
        }
    }

    public void sendSensorData(int type){
        float x=-1,y=-1,z=-1;
    //    Log.d(TAG, "sendSensorData: ");
        try {
            x=mDataInputStream.readFloat();
            y=mDataInputStream.readFloat();
            z=mDataInputStream.readFloat();
        } catch (IOException e) {
            Log.d(TAG, "sendSensorData: " + e.getMessage());
        }
        mService.sendSensorData(x, y, z, type);
    }
    public void sendMouseData(){
        float x=-1,y=-1;
        int flag=-1;
        try {
            flag=mDataInputStream.readInt();
            x=mDataInputStream.readFloat();
            y=mDataInputStream.readFloat();
        } catch (IOException e) {
            Log.d(TAG,"sendMouseData: "+e.getMessage());
        }
        mService.SendMouseData(x,y);
      //  Log.d(TAG,"mouse data: x:"+x+" y:"+y);
      //  mInstrumentation.sendPointerSync(buildMotionEvent(x, y, flag));
    }
    public void sendTextData(){
        String text=null;
        try {
            text=mDataInputStream.readUTF();
        } catch (IOException e) {
            Log.d(TAG,"sendTextData: "+e.getMessage());
        }
        Log.d(TAG,"text data:"+text);
        //mInstrumentation.sendStringSync(text);
    }
    public void sendNotificationData(){
        String notification=null;
        try {
            notification=mDataInputStream.readUTF();
        } catch (IOException e) {
            Log.d(TAG,"sendNotificationData: "+e.getMessage());
        }
        mInstrumentation.sendStringSync(notification);
    }

    public MotionEvent buildMotionEvent(float x,float y,int paramPressure) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, paramPressure, x,y, 0);
        return event;
    }

}
