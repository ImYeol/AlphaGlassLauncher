package thealphalabs.alphaglasslauncher.bluetooth;

import android.content.Context;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yeol on 15. 6. 15.
 */
public class ReConnectionService {

    private static ReConnectionService instance;
    private Context mContext;
    private Timer mReConnectiongTimer;
    private final String TAG="ReConnectionService";
    private BluetoothManager mBltManager;


    public static ReConnectionService getInstance(Context paramContext,BluetoothManager paramBltManager)
    {
        if (instance == null)
            instance = new ReConnectionService(paramContext,paramBltManager);
        return instance;
    }

    private ReConnectionService(Context paramContext,BluetoothManager paramBltManager)
    {
        super();
        mContext = paramContext;
        mBltManager=paramBltManager;
    }


    /**
     * 1분 마다 다시 연결요청을 한다
     *
     * @param $context
     */
    public void autoReconnect()
    {
        if(mBltManager.getState() != BluetoothManager.STATE_NONE)
            return ;
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                Log.d(TAG, "autoReconnect");

            }
        };
        mReConnectiongTimer = new Timer();
        mReConnectiongTimer.schedule(task, 5000, 10000);// 매 분마다 다시 연결한다

    }


    /**
     * 자동 연결요청 취소
     */
    public void stopReconnect()
    {
        Log.d(TAG,"stopReconnect");

        if (mReConnectiongTimer != null)
        {
            mReConnectiongTimer.cancel();
        }
    }
}
