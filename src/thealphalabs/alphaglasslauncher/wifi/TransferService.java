package thealphalabs.alphaglasslauncher.wifi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by yeol on 15. 6. 30.
 */
public class TransferService extends IntentService {

    private static final String TAG="TransferService";
    private boolean serviceEnabled=true;

    private int port;
    private File saveLocation;
    private ResultReceiver serverResult;
    private String dirPath;

    public TransferService(String name) {
        super(name);
    }

    public TransferService() {
        super("TransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        port = ((Integer) intent.getExtras().get("port")).intValue();
    //    saveLocation = (File) intent.getExtras().get("saveLocation");
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");
        dirPath= Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d(TAG,"dirPath:"+dirPath);

        ServerSocket welcomeSocket = null;
        Socket socket = null;

        try {
            welcomeSocket = new ServerSocket(port);

            while(true && serviceEnabled)
            {
                Log.d(TAG,"server started");
                socket = welcomeSocket.accept();
                InputStream is = socket.getInputStream();
                DataInputStream datainputstream=new DataInputStream(is);
                Log.d(TAG,"client accepted");
                byte[] buffer = new byte[4096];
                int bytesRead;

                String fileName=datainputstream.readUTF();
                Long fileLength=datainputstream.readLong();

                String savedAs = dirPath+File.separator+fileName+System.currentTimeMillis();
                File file = new File(savedAs);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                while((bytesRead = datainputstream.read(buffer, 0, buffer.length))>0)
                {
                    if(bytesRead == -1)
                    {
                        Log.d(TAG,"file transfer error");
                        break;
                    }
                    bos.write(buffer, 0, bytesRead);
                    bos.flush();
                    Log.d(TAG, "file data received:"+bytesRead);
                }
                bos.close();
                socket.close();
               // signalActivity("File Transfer Complete, saved as: " + savedAs);
                //Start writing to file
                signalActivity(savedAs);
            }


        } catch (IOException e) {
          //  signalActivity(e.getMessage());
            Log.d(TAG,"IO error:"+e.getMessage());

        }
        catch(Exception ex)
        {
            Log.d(TAG,"exception : "+ex.getMessage());
            //signalActivity(ex.getMessage());

        }

        //Signal that operation is complete
        serverResult.send(port, null);

    }


    public void signalActivity(String message)
    {
        Bundle b = new Bundle();
        b.putString("message", message);
        serverResult.send(port, b);
    }


    public void onDestroy()
    {
        serviceEnabled = false;
        stopSelf();
    }

}
