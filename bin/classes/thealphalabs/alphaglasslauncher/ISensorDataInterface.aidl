package thealphalabs.alphaglasslauncher;


import thealphalabs.alphaglasslauncher.RemoteSensorEvent;

interface ISensorDataInterface {
    void onRemoteSensorChanged(in RemoteSensorEvent event);
}
