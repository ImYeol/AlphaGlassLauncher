package thealphalabs.alphaglasslauncher;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yeol on 15. 6. 12.
 */
public class RemoteSensorEvent implements Parcelable {

    private float x;
    private float y;
    private float z;
    private int accuracy;

    public static final Parcelable.Creator<RemoteSensorEvent> CREATOR
            = new Parcelable.Creator<RemoteSensorEvent>() {
        public RemoteSensorEvent createFromParcel(Parcel in) {
            return new RemoteSensorEvent(in);
        }

        @Override
        public RemoteSensorEvent[] newArray(int size) {
            return new RemoteSensorEvent[size];
        }
    };

    public RemoteSensorEvent(Parcel in) {
        readFromParcel(in);
    }

    public RemoteSensorEvent(){
        x=0;y=0;z=0;accuracy=0;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(z);
        dest.writeInt(accuracy);
    }
    private void readFromParcel(Parcel in) {
        // TODO Auto-generated method stub
        x=in.readFloat();
        y=in.readFloat();
        z=in.readFloat();
        accuracy=in.readInt();
    }

    public void setEventData(float x,float y,float z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public void setAccuracy(int paramAccuracy) {
        accuracy=paramAccuracy;
    }

    public float getX(){
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
