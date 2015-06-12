package thealphalabs.alphaglasslauncher.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yeol on 15. 6. 9.
 */
public class ConnectionInfo {
    // Constants

    // Instance
    private static ConnectionInfo mInstance = null;
    private Context mContext;

    // Target device's MAC address
    private String mDeviceAddress = null;
    // Name of the connected device
    private String mDeviceName = null;


    private ConnectionInfo(Context c) {
        mContext = c;

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mDeviceAddress = prefs.getString(Constants.PREFERENCE_CONN_INFO_ADDRESS, null);
        mDeviceName = prefs.getString(Constants.PREFERENCE_CONN_INFO_NAME, null);
    }

    public synchronized static ConnectionInfo getInstance(Context c) {
        if(mInstance == null) {
            if(c != null)
                mInstance = new ConnectionInfo(c);
            else
                return null;
        }
        return mInstance;
    }

    public void reSetConnectionInfo() {
        mDeviceAddress = null;
        mDeviceName = null;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String name) {
        mDeviceName = name;

        // At this time, connection is established successfully.
        // Save connection info in shared preference.
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFERENCE_CONN_INFO_ADDRESS, mDeviceAddress);
        editor.putString(Constants.PREFERENCE_CONN_INFO_NAME, mDeviceName);
        editor.commit();
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public void setDeviceAddress(String address) {
        mDeviceAddress = address;
    }
}
