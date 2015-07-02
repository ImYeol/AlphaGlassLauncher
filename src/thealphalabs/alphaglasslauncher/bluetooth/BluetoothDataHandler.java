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
    private static long mTwoTouchDown;
    private static float ScreenX;
    private static float ScreenY;

    public BluetoothDataHandler(Service paramService, DataInputStream paramInputStream){
        mService=(BluetoothTransferService)paramService;
        mDataInputStream=paramInputStream;
        mInstrumentation=new Instrumentation();
        ScreenX=mService.getViewWidth();
        ScreenY=mService.getViewHeight();
        Log.d(TAG,"screen X:"+ScreenX + " Screen Y:" + ScreenY);
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
        if( flag == EventDataType.EventDataFlag.Motion_Two_Touch_Move) {
            Log.d(TAG,"sendTwoTouchData Move ");
            mInstrumentation.sendPointerSync(buildMotionEvent(x * ScreenX, y * ScreenY, MotionEvent.ACTION_MOVE));
        }
        else if( flag == EventDataType.EventDataFlag.Motion_Two_Touch_UP) {
            Log.d(TAG,"sendTwoTouchData up ");
            mInstrumentation.sendPointerSync(buildMotionEvent(x * ScreenX, y * ScreenY, MotionEvent.ACTION_UP));
        }
        else if( flag == EventDataType.EventDataFlag.Motion_Two_Touch_Down) {
            Log.d(TAG,"sendTwoTouchData down ");
            mTwoTouchDown=SystemClock.uptimeMillis();
            mInstrumentation.sendPointerSync(buildMotionEvent(x*ScreenX,y*ScreenY,MotionEvent.ACTION_DOWN));
        }
        else if(flag == MotionEvent.ACTION_UP)
            mService.SendClickData();
        else
            mService.SendMouseData(x,y);
    }
 /*   private void SyncClickEvent(float x,float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x ,y, 0);
        MotionEvent event2 = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, x, y, 0);
        mInstrumentation.sendPointerSync(event);
        mInstrumentation.sendPointerSync(event2);
    } */
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
        long downTime=SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        Log.d(TAG, "buildMotionEvent:"+mTwoTouchDown);
        Log.d(TAG,"buildMotionEvent x:"+x+" y:"+y);
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, paramPressure, x,y, 0);

        return event;
    }

}
