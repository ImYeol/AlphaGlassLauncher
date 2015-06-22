package thealphalabs.alphaglasslauncher.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import thealphalabs.alphaglasslauncher.R;
import thealphalabs.alphaglasslauncher.RemoteSensorEvent;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferHelper;
import thealphalabs.alphaglasslauncher.bluetooth.BluetoothTransferService;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensor;
import thealphalabs.alphaglasslauncher.bluetooth.RemoteSensorListener;
import thealphalabs.alphaglasslauncher.util.EventDataType;


public class MainActivity extends Activity {

    private BluetoothTransferHelper helper;
    private static final String TAG="main";

    private SensorView sensorView;
    private Handler handler=new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorView = new SensorView(this);
     //   setContentView(R.layout.activity_main);
        setContentView(sensorView);
        helper=((GlassApplication)getApplication()).getBluetoothHelper();
        helper.registerRemoteSensorListener(new RemoteSensorListener() {
            @Override
            public void onRemoteSensorChanged(RemoteSensorEvent event) {
                Log.d(TAG,"gyro x:"+event.getX() + " y:"+event.getY()+" z:"+event.getZ());
            }
            @Override
            public void onRemoteSensorAccuracyChanged(int accuracy) {

            }
        }, RemoteSensor.GYRO);

        helper.registerRemoteSensorListener(new RemoteSensorListener() {
            @Override
            public void onRemoteSensorChanged(RemoteSensorEvent event) {
              //  final float finalX=event.getX();
              //  final float finalY=event.getY();
               /* runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sensorView.move(finalX, finalY);
                    }
                }); */
                sensorView.move(event.getX(),event.getY());
            }

            @Override
            public void onRemoteSensorAccuracyChanged(int accuracy) {

            }
        }, RemoteSensor.ACCEL);

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
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"onTouchEvent x:"+event.getX());
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, BluetoothTransferService.class));
        super.onDestroy();
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

                    R.mipmap.accel_img);
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

}
