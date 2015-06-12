package thealphalabs.alphaglasslauncher.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yeol on 15. 6. 12.
 */
public class RemoteSensorEvent implements Parcelable{

    private float x;
    private float y;
    private float z;

    public RemoteSensorEvent(){
        x=0;y=0;z=0;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public void setEventData(float x,float y,float z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }
}
