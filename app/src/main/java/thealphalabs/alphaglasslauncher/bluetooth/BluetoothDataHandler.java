package thealphalabs.alphaglasslauncher.bluetooth;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.home.GlassApplication;
import thealphalabs.alphaglasslauncher.home.MainActivity;
import thealphalabs.alphaglasslauncher.util.EventDataType;

/**
 * Created by yeol on 15. 6. 12.
 */
public class BluetoothDataHandler implements Runnable {

    private final String TAG="BluetoothDataHandler";
    private BluetoothTransferService mService;
    private DataInputStream mDataInputStream;
    private DataOutputStream mDataOutputStream;
    private Instrumentation mInstrumentation;
    private Context mContext;

    public BluetoothDataHandler(Service paramService, DataInputStream paramInputStream, DataOutputStream paramOutputStream){
        mService=(BluetoothTransferService)paramService;
        mDataInputStream=paramInputStream;
        mDataOutputStream = paramOutputStream;
        mInstrumentation=new Instrumentation();

        this.mContext = MainActivity.context;
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
                    case EventDataType.EventFileTransfer:
                        sendFileData();
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
        Log.d(TAG, "text data:" + text);
        //mInstrumentation.sendStringSync(text);
    }
    public void sendNotificationData(){
        String notification=null;
        try {
            notification=mDataInputStream.readUTF();
        } catch (IOException e) {
            Log.d(TAG,"sendNotificationData: "+e.getMessage());
        }
        Log.d(TAG, "Notification data:" + notification);

        // Notification 생성
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification nt = new Notification(R.drawable.notification_template_icon_bg, "Nomal Notification", System.currentTimeMillis());
        nt.flags = Notification.FLAG_AUTO_CANCEL;
        nt.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE ;
        nt.number = 13;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        nt.setLatestEventInfo(mContext, "Nomal Title", "Nomal Summary", pendingIntent);

        nm.notify(1234, nt);

//        mInstrumentation.sendStringSync(notification);
    }

    public void sendFileData() {
        long file_size;
        int loop_cnt;
        int bytesRead;
        int count = 0;
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File target = new File(path, "/test3.apk");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(target);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] bytes;

        try {
            file_size=mDataInputStream.readLong();
            loop_cnt = (int) (file_size % 1024 == 0 ? file_size / 1024 : file_size / 1024 + 1);

            Log.d(TAG, "file size = " + file_size + ", loop_cnt = " + loop_cnt);
            bytes = new byte[(int)file_size];

            for(int i = 0; i < loop_cnt; i++)
            {
                bytesRead = mDataInputStream.read(bytes);
                fos.write(bytes, 0, bytesRead);
                count++;
                Log.d(TAG, i + " / " + loop_cnt);
            }
            Log.d(TAG, "Succeed to read file data" + target.getPath() + "(" + target.length() + ")");

            fos.close();

            Intent promptInstall = new Intent(Intent.ACTION_VIEW);
            promptInstall.setDataAndType(Uri.fromFile(target), "application/vnd.andriod.package-archive");
            promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "app.apk")), "application/vnd.android.package-archive");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);

        } catch (IOException e) {
            Log.d(TAG,"sendMouseData: "+e.getMessage());
        }
    }

    public MotionEvent buildMotionEvent(float x,float y,int paramPressure) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        Log.d(TAG, "buildMotionEvent");
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, paramPressure, x,y, 0);

        return event;
    }

    // getter and setter
    public void setContext(Context context) {
        this.mContext = context;
    }
    public Context getContext() {
        return mContext;
    }
}
