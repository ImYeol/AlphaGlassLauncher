// ISensorDataInterface.aidl
package thealphalabs.alphaglasslauncher;

// Declare any non-default types here with import statements
import thealphalabs.alphaglasslauncher.RemoteSensorEvent;

interface ISensorDataInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onRemoteSensorChanged(in RemoteSensorEvent event);
}
