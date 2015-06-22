package thealphalabs.alphaglasslauncher.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferHelper;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensor;
import thealphalabs.alphaglasslauncher.bluetooth.remoteSenListeners.AccelSensorListener;


public class MainActivity extends Activity implements View.OnTouchListener{
    private final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothTransferHelper transferHelper =
                ((GlassApplication) getApplication()).getBluetoothTransferHelper();

        transferHelper.registerRemoteSensorListener(new AccelSensorListener(), RemoteSensor.ACCEL);
        ImageView image = (ImageView) findViewById(R.id.image);
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        Log.d(TAG, "onTouch!!!!!!");
        return true;
    }
}
