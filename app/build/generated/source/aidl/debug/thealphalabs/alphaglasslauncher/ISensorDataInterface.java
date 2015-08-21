/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/yeol/AndroidStudioProjects/AlphaGlassLauncher/app/src/main/aidl/thealphalabs/alphaglasslauncher/ISensorDataInterface.aidl
 */
package thealphalabs.alphaglasslauncher;
public interface ISensorDataInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements thealphalabs.alphaglasslauncher.ISensorDataInterface
{
private static final java.lang.String DESCRIPTOR = "thealphalabs.alphaglasslauncher.ISensorDataInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an thealphalabs.alphaglasslauncher.ISensorDataInterface interface,
 * generating a proxy if needed.
 */
public static thealphalabs.alphaglasslauncher.ISensorDataInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof thealphalabs.alphaglasslauncher.ISensorDataInterface))) {
return ((thealphalabs.alphaglasslauncher.ISensorDataInterface)iin);
}
return new thealphalabs.alphaglasslauncher.ISensorDataInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onRemoteSensorChanged:
{
data.enforceInterface(DESCRIPTOR);
thealphalabs.alphaglasslauncher.RemoteSensorEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = thealphalabs.alphaglasslauncher.RemoteSensorEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onRemoteSensorChanged(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements thealphalabs.alphaglasslauncher.ISensorDataInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
@Override public void onRemoteSensorChanged(thealphalabs.alphaglasslauncher.RemoteSensorEvent event) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((event!=null)) {
_data.writeInt(1);
event.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onRemoteSensorChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onRemoteSensorChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void onRemoteSensorChanged(thealphalabs.alphaglasslauncher.RemoteSensorEvent event) throws android.os.RemoteException;
}
