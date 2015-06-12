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
    public void run() {

        while(true) {
            int type=mDataInputStream.readInt();
            switch(type){
                case EventDataType.EventAccel:
                    sendSensorData();
                    break;
                case EventDataType.EventGyro:
                    sendSensorData();
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
    }

    public void sendSensorData(){
        float x=-1,y=-1,z=-1;
        try {
            x=mDataInputStream.readFloat();
            y=mDataInputStream.readFloat();
            z=mDataInputStream.readFloat();
        } catch (IOException e) {
            Log.d(TAG, "sendSensorData: " + e.getMessage());
        }
        mService.sendSensorData(x,y,z);
    }
    public void sendMouseData(){
        float x=-1,y=-1;
        try {
            x=mDataInputStream.readFloat();
            y=mDataInputStream.readFloat();
        } catch (IOException e) {
            Log.d(TAG,"sendMouseData: "+e.getMessage());
        }
        mInstrumentation.sendPointerSync(buildMotionEvent(x,y));
    }
    public void sendTextData(){
        String text=null;
        try {
            text=mDataInputStream.readUTF();
        } catch (IOException e) {
            Log.d(TAG,"sendTextData: "+e.getMessage());
        }
        mInstrumentation.sendStringSync(text);
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

    public MotionEvent buildMotionEvent(float x,float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x,y, 0);
        return event;
    }
}
