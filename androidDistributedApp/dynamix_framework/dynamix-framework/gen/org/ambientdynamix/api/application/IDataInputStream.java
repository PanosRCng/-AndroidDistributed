/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/panos/Downloads/dynamix/dynamix-framework/src/org/ambientdynamix/api/application/IDataInputStream.aidl
 */
package org.ambientdynamix.api.application;
/**
 * IDataInputStream is used to support streaming large objects across Android's process boundary using IPC.
 *
 * @author Darren Carlson
 */
public interface IDataInputStream extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.ambientdynamix.api.application.IDataInputStream
{
private static final java.lang.String DESCRIPTOR = "org.ambientdynamix.api.application.IDataInputStream";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.ambientdynamix.api.application.IDataInputStream interface,
 * generating a proxy if needed.
 */
public static org.ambientdynamix.api.application.IDataInputStream asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.ambientdynamix.api.application.IDataInputStream))) {
return ((org.ambientdynamix.api.application.IDataInputStream)iin);
}
return new org.ambientdynamix.api.application.IDataInputStream.Stub.Proxy(obj);
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
case TRANSACTION_read:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
int _arg0_length = data.readInt();
if ((_arg0_length<0)) {
_arg0 = null;
}
else {
_arg0 = new byte[_arg0_length];
}
int _result = this.read(_arg0);
reply.writeNoException();
reply.writeInt(_result);
reply.writeByteArray(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.ambientdynamix.api.application.IDataInputStream
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
@Override public int read(byte[] b) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((b==null)) {
_data.writeInt(-1);
}
else {
_data.writeInt(b.length);
}
mRemote.transact(Stub.TRANSACTION_read, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
_reply.readByteArray(b);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_read = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public int read(byte[] b) throws android.os.RemoteException;
}
