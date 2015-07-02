package thealphalabs.alphaglasslauncher.bluetooth;

import thealphalabs.alphaglasslauncher.RemoteSensorEvent;

/**
 * Created by yeol on 15. 6. 12.
 */
public interface RemoteSensorListener {

    void onRemoteSensorChanged(RemoteSensorEvent event);

    void onRemoteSensorAccuracyChanged(int accuracy);
}
