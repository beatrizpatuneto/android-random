/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/mnovak/Projects/android-random/RemoteLogger/client/src/com/androidnerds/tools/RemoteLogger/LogProcessorInterface.aidl
 */
package com.androidnerds.tools.RemoteLogger;
import java.lang.String;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface LogProcessorInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.androidnerds.tools.RemoteLogger.LogProcessorInterface
{
private static final java.lang.String DESCRIPTOR = "com.androidnerds.tools.RemoteLogger.LogProcessorInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an LogProcessorInterface interface,
 * generating a proxy if needed.
 */
public static com.androidnerds.tools.RemoteLogger.LogProcessorInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
com.androidnerds.tools.RemoteLogger.LogProcessorInterface in = (com.androidnerds.tools.RemoteLogger.LogProcessorInterface)obj.queryLocalInterface(DESCRIPTOR);
if ((in!=null)) {
return in;
}
return new com.androidnerds.tools.RemoteLogger.LogProcessorInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags)
{
try {
switch (code)
{
case TRANSACTION_startTheCat:
{
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.startTheCat(_arg0, _arg1);
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_closeServer:
{
boolean _result = this.closeServer();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
}
catch (android.os.DeadObjectException e) {
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.androidnerds.tools.RemoteLogger.LogProcessorInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public boolean startTheCat(java.lang.String serverAddress, java.lang.String serverPort) throws android.os.DeadObjectException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeString(serverAddress);
_data.writeString(serverPort);
mRemote.transact(Stub.TRANSACTION_startTheCat, _data, _reply, 0);
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean closeServer() throws android.os.DeadObjectException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
mRemote.transact(Stub.TRANSACTION_closeServer, _data, _reply, 0);
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_startTheCat = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_closeServer = (IBinder.FIRST_CALL_TRANSACTION + 1);
}
public boolean startTheCat(java.lang.String serverAddress, java.lang.String serverPort) throws android.os.DeadObjectException;
public boolean closeServer() throws android.os.DeadObjectException;
}
