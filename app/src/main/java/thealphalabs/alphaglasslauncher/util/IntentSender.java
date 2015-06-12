package thealphalabs.alphaglasslauncher.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import junit.framework.Assert;

/**
 * Created by yeol on 15. 6. 9.
 */
public abstract class IntentSender {
    private static IntentSender instance=null;

    public static void clearInstance()
    {
        instance=null;
    }

    public static IntentSender getInstance()
    {
        if(instance == null){
            instance=new DefaultIntentSender();
        }
        return instance;
    }

    public static void setInstance(IntentSender paramIntentSender)
    {
        Assert.assertNotNull(paramIntentSender);
        instance=paramIntentSender;
    }
    public abstract boolean bindService(Context paramContext, Intent paramIntent, ServiceConnection paramServiceConnection, int paramInt);

    public abstract void sendBroadcast(Context paramContext, Intent paramIntent);

    public abstract void sendBroadcast(Context paramContext, Intent paramIntent, String paramString);

    public abstract void startActivity(Context paramContext, Intent paramIntent);

    public abstract void startActivityForResult(Activity paramActivity, Intent paramIntent, int paramInt);

    public abstract ComponentName startService(Context paramContext, Intent paramIntent);

    public abstract boolean stopService(Context paramContext, Intent paramIntent);

    public abstract void unbindService(Context paramContext, ServiceConnection paramServiceConnection);

    public static class DefaultIntentSender
            extends IntentSender
    {
        public boolean bindService(Context paramContext, Intent paramIntent, ServiceConnection paramServiceConnection, int paramInt)
        {
            return paramContext.bindService(paramIntent, paramServiceConnection, paramInt);
        }

        public void sendBroadcast(Context paramContext, Intent paramIntent)
        {
            paramContext.sendBroadcast(paramIntent);
        }

        public void sendBroadcast(Context paramContext, Intent paramIntent, String paramString)
        {
            paramContext.sendBroadcast(paramIntent, paramString);
        }


        public void startActivity(Context paramContext, Intent paramIntent)
        {
            paramContext.startActivity(paramIntent);
        }

        public void startActivityForResult(Activity paramActivity, Intent paramIntent, int paramInt)
        {
            paramActivity.startActivityForResult(paramIntent, paramInt);
        }

        public ComponentName startService(Context paramContext, Intent paramIntent)
        {
            return paramContext.startService(paramIntent);
        }

        public boolean stopService(Context paramContext, Intent paramIntent)
        {
            return paramContext.stopService(paramIntent);
        }

        public void unbindService(Context paramContext, ServiceConnection paramServiceConnection)
        {
            paramContext.unbindService(paramServiceConnection);
        }
    }
}


