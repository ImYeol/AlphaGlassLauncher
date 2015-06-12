package thealphalabs.alphaglasslauncher.bluetooth;

import android.hardware.SensorEvent;

/**
 * Created by yeol on 15. 6. 12.
 */
public interface RemoteSensorListener {

    void onRemoteSensorChanged(SensorEvent event);

    void onRemoteSensorAccuracyChanged(int accuracy);
}
