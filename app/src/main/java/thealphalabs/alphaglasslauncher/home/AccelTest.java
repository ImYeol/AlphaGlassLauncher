package thealphalabs.alphaglasslauncher.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import thealphalabs.alphaglasslauncher.R;

/**
 * Created by yeol on 15. 6. 22.
 */
public class AccelTest extends Activity implements SensorEventListener {
    private static final String LOG_TAG = "BABUKUMA";
    private SensorManager sensorManager;
    private SensorView sensorView;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sensorView = new SensorView(this);
        setContentView(sensorView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensors = sensorManager

                .getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) {

            sensorManager.registerListener(this, sensors.get(0),

                    SensorManager.SENSOR_DELAY_GAME);

        }
    }

    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(final SensorEvent event) {
        Sensor sensor = event.sensor;
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorView.move(event.values[0], event.values[1]);
                break;
            default:
                break;
        }
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

            this.x -= (mx * 4f);

            //    this.x2-=(mx*3.5f);

            this.y += (my * 4f);

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

            invalidate();
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

            canvas.drawBitmap(kuma, x+1, y+1, null);

            canvas.drawBitmap(kuma, x+2, y+2, null);

        }

    }

}
