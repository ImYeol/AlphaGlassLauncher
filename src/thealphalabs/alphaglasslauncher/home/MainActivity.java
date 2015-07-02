package thealphalabs.alphaglasslauncher.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.RemoteSensorEvent;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferHelper;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferService;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensor;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensorListener;
import thealphalabs.alphaglasslauncher.util.EventDataType;
import thealphalabs.alphaglasslauncher.wifi.TransferService;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MainActivity extends Activity {

    private BluetoothTransferHelper helper;
    private static final String TAG="main";

    public static Context context;
    private SensorView sensorView;
    private Handler handler=new Handler(Looper.getMainLooper());

    /** WifiP2pManager */
    private WifiP2pManager mWifiP2pManager;
    /** Channel */
    private WifiP2pManager.Channel mChannel;
    /** peers */
    private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>();
    /** 選択中の子 */
    private String mSelectedDevice;
    private boolean mIsAppBoot;

    private BroadcastReceiver mReceiver;
    private boolean mIsWiFiDirectEnabled;
    private ActionListenerAdapter mActionListenerAdapter;

    /** p2p Control Port */
    private int mP2pControlPort = -1;
    /** p2p interface name */
    private String mP2pInterfaceName;

    private ImageView iv;
    private ListView lv;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;

    private Intent serverServiceIntent;
    private boolean serverThreadActive=false;
    private static final int port=7950;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
      //  sensorView = new SensorView(this);
        setContentView(R.layout.listview);
        iv=(ImageView)findViewById(R.id.imageView);
      //  setContentView(sensorView);
        
        list = new ArrayList<String>();
        for(int i=0;i<20;i++) {
        	list.add(""+i);
        }
        log("hh2");
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, list);
        lv=(ListView)findViewById(R.id.listView1);
        lv.setAdapter(adapter);
        log("hh3");
        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "listView x:" + event.getX() + " y:" + event.getY());
                Log.d(TAG,"listView width:"+v.getWidth()+" height:"+v.getHeight());
                return false;
            }
        });

        log("hh4");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        log("hh5");
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mReceiver = new WiFiDirectBroadcastReceiver();
        registerReceiver(mReceiver, filter);
        Log.d(TAG, "registerBroadcastReceiver() BroadcastReceiver");
    }

    private void unRegisterBroadcastReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
            Log.d(TAG,"unRegisterBroadcastReceiver() BroadcastReceiver");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, BluetoothTransferService.class));
     //   helper.StopConnection();
        unRegisterBroadcastReceiver();
    }

    private void log(String s) {
        Log.d(TAG,s);
    }
    class ActionListenerAdapter implements WifiP2pManager.ActionListener {

        // 成功
        public void onSuccess() {
            log("succes");
        }

        // 失敗
        public void onFailure(int reason) {
            String log = " onFailure("+getReason(reason)+")";
            log(log);
        }

        // 失敗理由intコード -> 文字列変換
        private String getReason(int reason) {
            String[] strs = {"ERROR", "P2P_UNSUPPORTED", "BUSY"};
            try {
                return strs[reason] + "("+reason+")";
            } catch (ArrayIndexOutOfBoundsException e) {
                return "UNKNOWN REASON CODE("+reason+")";
            }
        }
    }

    public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String log = "onReceive() ["+action+"]";
            log(log);

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                mIsWiFiDirectEnabled = false;
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                String sttStr;
                switch (state) {
                    case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                        mIsWiFiDirectEnabled = true;
                        sttStr = "ENABLED";
                        break;
                    case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                        sttStr = "DISABLED";
                        break;
                    default:
                        sttStr = "UNKNOWN";
                        break;
                }
                log("state[" + sttStr + "](" + state + ")");

                if (mIsWiFiDirectEnabled) {
                    onClickGetSystemService(null);
                    onClickInitialize(null);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                log("try requestPeers()");
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                // invoke Sink
                if (networkInfo.isConnected()) {
                    mIsAppBoot = false;
                    invokeSink();
                    startServer(null);
                } else if (!mIsAppBoot) {
                    //finish();
                	  log("system.exit");
                    //System.exit(0); // force finish Sink Screen. TODO FIXME^^;;
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                log("device:"+device.toString() + " status:" +getDeviceStatus(device.status));

                // Search
                if (mIsWiFiDirectEnabled) {
                    onClickDiscoverPeers(null);
                }
            }
        }
    }

    public void startServer(View view) {

        //If server is already listening on port or transfering data, do not attempt to start server service
        if(!serverThreadActive)
        {
            //Create new thread, open socket, wait for connection, and transfer file
            serverServiceIntent = new Intent(this, TransferService.class);
            //serverServiceIntent.putExtra("saveLocation", downloadTarget);
            serverServiceIntent.putExtra("port", new Integer(port));
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {

                    if(resultCode == port )
                    {
                        if (resultData == null) {
                            serverThreadActive = false;
                        }
                        else {
                            final String FileName=resultData.getString("message");
                            final File file=new File(FileName);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap=BitmapFactory.decodeFile(FileName);
                                    iv.setImageBitmap(bitmap);
                                }
                            });
                        }
                        serverThreadActive=false;
                    }
                }
            });
            serverThreadActive = true;
            startService(serverServiceIntent);
        }

    }

    private String getDeviceStatus(int deviceStatus) {
        String status = "";
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                status = "Available";
                break;
            case WifiP2pDevice.INVITED:
                status = "Invited";
                break;
            case WifiP2pDevice.CONNECTED:
                status = "Connected";
                break;
            case WifiP2pDevice.FAILED:
                status = "Failed";
                break;
            case WifiP2pDevice.UNAVAILABLE:
                status = "Unavailable";
                break;
            default:
                status = "Unknown";
                break;
        }
        return status;
    }

    public void onClickGetSystemService(View view) {
        log("getSystemService(Context.WIFI_P2P_SERVICE)");

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        log("　Result[" + (mWifiP2pManager != null) + "]");
    }
    public void onClickDiscoverPeers(View view) {
        log("mWifiP2pManager.discoverPeers()");
        if (isNull(true)) { return; }

        mWifiP2pManager.discoverPeers(mChannel, mActionListenerAdapter);
    }
    public void onClickInitialize(View view) {
        log("mWifiP2pManager.initialize()");
        if (isNull(false)) { return; }

        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            public void onChannelDisconnected() {
                log("mWifiP2pManager.initialize() -> onChannelDisconnected()");
            }
        });

        log("　Result[" + (mChannel != null) + "]");
    }

    private boolean isNull(boolean both) {
        if (mActionListenerAdapter == null) {
            mActionListenerAdapter = new ActionListenerAdapter();
        }

        if (!mIsWiFiDirectEnabled) {
            log(" Wi-Fi Direct is OFF! try Setting Menu");
            return true;
        }

        if (mWifiP2pManager == null) {
            log(" mWifiP2pManager is NULL! try getSystemService");
            return true;
        }
        if (both && (mChannel == null) ) {
            log(" mChannel is NULL!  try initialize");
            return true;
        }

        return false;
    }

    private void invokeSink() {
        log("invokeSink() call requestGroupInfo()");
        if (isNull(true)) { return; }

        mWifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            // requestGroupInfo()実行後、非同期応答あり
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                log("　onGroupInfoAvailable():");
                if (group == null) {
                    log("  group is NULL!");
                    return;
                }

                String log = group.toString();

                // パスワードは、G.O.のみ取得可能
                String pass ="　password: ";
                if (group.isGroupOwner()) {
                    pass += group.getPassphrase();
                } else {
                    pass += "Client Couldn't Get Password";
                }
                log(log);

                mP2pControlPort = -1;
                // Miracast device filtering
                Collection<WifiP2pDevice> p2pdevs = group.getClientList();
                //AssertEqual(p2pdevs.size(), 1); // one device?
                for (WifiP2pDevice dev : p2pdevs) {
                    boolean b = isWifiDisplaySource(dev);

                    log("invokeSink() isWifiDisplaySource(" + dev.deviceName + ")=[" + b + "]");
                    if (!b) {
                        continue;
                        // return; // because not Miracast Source device
                    }
                }
                if (mP2pControlPort == -1) {
                    //final class WifiDisplayController implements DumpUtils.Dump {
                    //    private static final int DEFAULT_CONTROL_PORT = 7236;
                    mP2pControlPort = 7236;
                    log("invokeSink() port=-1?? p2pdevs.size()=["+p2pdevs.size()+"] port assigned=7236");
                }

                // connect
                if (group.isGroupOwner()) { // G.O. don't know client IP, so check /proc/net/arp
                    mP2pInterfaceName = group.getInterface();

                    mArpTableObservationTimer = new Timer();
                    ArpTableObservationTask task = new ArpTableObservationTask();
                    mArpTableObservationTimer.scheduleAtFixedRate(task, 10, 1*1000); // 10ms後から1秒間隔でarpファイルをチェック
                } else { // this device is not G.O. get G.O. address
                    invokeSink2nd();
                }
            }
        });
    }
    private void invokeSink2nd() {
        log("invokeSink2nd() requestConnectionInfo()");
        if (isNull(true)) { return; }

        mWifiP2pManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            // requestConnectionInfo()実行後、非同期応答あり
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                log("　onConnectionInfoAvailable():");
                if (info == null) {
                    log("  info is NULL!");
                    return;
                }

                log("  groupFormed:" + info.groupFormed);
                log("  isGroupOwner:" + info.isGroupOwner);
                log("  groupOwnerAddress:" + info.groupOwnerAddress);

                if (!info.groupFormed) {
                    log("  not yet groupFormed!");
                    return;
                }

                if (info.isGroupOwner) {
                    log("  I'm G.O.? Illegal State!!");
                    return;
                } else {
                    String source_ip = info.groupOwnerAddress.getHostAddress();
                    delayedInvokeSink(source_ip, mP2pControlPort, 3);
                }
            }
        });
    }
    private boolean isWifiDisplaySource(WifiP2pDevice dev) {
        if (dev == null || dev.wfdInfo == null) {
            return false;
        }

        WifiP2pWfdInfo wfd = dev.wfdInfo;
        if (!wfd.isWfdEnabled()) {
            return false;
        }

        int type = wfd.getDeviceType();
        mP2pControlPort = wfd.getControlPort();

        boolean source = (type == WifiP2pWfdInfo.WFD_SOURCE) || (type == WifiP2pWfdInfo.SOURCE_OR_PRIMARY_SINK);
        log("isWifiDisplaySource() type[" + type + "] is-source[" + source + "] port[" + mP2pControlPort + "]");
        return source;
    }

    private Timer mArpTableObservationTimer;
    /** arpファイル読み込みリトライ回数 */
    private int mArpRetryCount = 0;
    /** arpファイル読み込み上限回数 */
    private final int MAX_ARP_RETRY_COUNT = 60;


    class ArpTableObservationTask extends TimerTask {
        @Override
        public void run() {
            // arpテーブル読み込み
            RarpImpl rarp = new RarpImpl();
            String source_ip = rarp.execRarp(mP2pInterfaceName);

            // リトライ
            if (source_ip == null) {
                Log.d(TAG, "retry:" + mArpRetryCount);
                if (++mArpRetryCount > MAX_ARP_RETRY_COUNT) {
                    mArpTableObservationTimer.cancel();
                    return;
                }
                return;
            }

            mArpTableObservationTimer.cancel();
            delayedInvokeSink(source_ip, mP2pControlPort, 3);
        }
    }

    private void delayedInvokeSink(final String ip, final int port, int delaySec) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               nativeSink(ip, port);
            }
        }, delaySec * 1000);
    }

    class SensorView extends View {

        private static final int KUMA_SIZE = 50;
        private Bitmap kuma;
        private int w;
        private int h;
        private float x;
        private float y;
        public SensorView(Context context) {

            super(context);

            requestWindowFeature(Window.FEATURE_NO_TITLE);

            kuma = BitmapFactory.decodeResource(context.getResources(),

                    R.drawable.accel_img);
        }

        public void move(float mx, float my) {

            this.x -= (mx * 8f);

            //    this.x2-=(mx*3.5f);

            this.y += (my * 8f);

            //     this.y2 += (my * 3.5f);

            if (this.x < 0) {

                this.x = 0;

            } else if ((this.x + KUMA_SIZE) > this.w) {

                this.x = this.w - KUMA_SIZE;

            }

            if (this.y < 0) {

                this.y = 0;

            } else if ((this.y + KUMA_SIZE) > this.h) {

                this.y = this.h - KUMA_SIZE;

            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
         //   invalidate();
        }

        @Override

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {

            this.w = w;
            this.h = h;
            this.x = (w - KUMA_SIZE) / 2f;
            this.y = (h - KUMA_SIZE) / 2f;
        }

        @Override

        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(kuma, x, y, null);

         //   canvas.drawBitmap(kuma, x+1, y+1, null);

        //    canvas.drawBitmap(kuma, x+2, y+2, null);

        }

    }

    private void nativeSink(String ip,int port) {
        Log.d(TAG, "invokeSink() Source Addr["+ip+":"+port+"]");
        new AvoidANRThread(false, ip, port).start();
    }

    private static class AvoidANRThread extends Thread {
        private final boolean source;
        private final String ip;
        private final int port;

        AvoidANRThread(boolean _source, String _ip, int _port) {
            source = _source;
            ip = _ip;
            port = _port;
        }

        public void run() {
            if (source) {
             //   nativeInvokeSource(ip, port);
            } else {
                nativeInvokeSink(ip, port);
            }
        }
    }

    /**
     * JNI:invoke Sink
     */
    private static native void nativeInvokeSink(String ip, int port);

    static {
        System.loadLibrary("SinkModule");
    }
}
