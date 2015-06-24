package thealphalabs.alphaglasslauncher.wifi;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import thealphalabs.alphaglasslauncher.util.IntentSender;

/**
 * Created by yeol on 15. 6. 9.
 */
public class WifiTransferService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WifiTransferService(String name) {
        super(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
