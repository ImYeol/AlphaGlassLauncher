package thealphalabs.alphaglasslauncher.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.RemoteSensorEvent;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferHelper;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferService;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensor;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensorListener;
import thealphalabs.alphaglasslauncher.util.EventDataType;


public class MainActivity extends Activity {

    private BluetoothTransferHelper helper;
    private static final String TAG="main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper=((GlassApplication)getApplication()).getBluetoothHelper();
        helper.registerRemoteSensorListener(new RemoteSensorListener() {
            @Override
            public void onRemoteSensorChanged(RemoteSensorEvent event) {
                Log.d(TAG,"gyro x:"+event.getX() + " y:"+event.getY()+" z:"+event.getZ());
            }

            @Override
            public void onRemoteSensorAccuracyChanged(int accuracy) {

            }
        }, RemoteSensor.GYRO);

        helper.registerRemoteSensorListener(new RemoteSensorListener() {
            @Override
            public void onRemoteSensorChanged(RemoteSensorEvent event) {
                Log.d(TAG,"accel x:"+event.getX() + " y:"+event.getY()+" z:"+event.getZ());
            }

            @Override
            public void onRemoteSensorAccuracyChanged(int accuracy) {

            }
        }, RemoteSensor.ACCEL);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
