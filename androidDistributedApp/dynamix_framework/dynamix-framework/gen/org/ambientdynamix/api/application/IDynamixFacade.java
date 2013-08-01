/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/panos/Downloads/dynamix/dynamix-framework/src/org/ambientdynamix/api/application/IDynamixFacade.aidl
 */
package org.ambientdynamix.api.application;
/**
 * IDynamixFacade provides a set of methods for Dynamix applications (running in separate processes) to interact with the
 * Dynamix Framework.
 *
 * @author Darren Carlson
 */
public interface IDynamixFacade extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.ambientdynamix.api.application.IDynamixFacade
{
private static final java.lang.String DESCRIPTOR = "org.ambientdynamix.api.application.IDynamixFacade";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.ambientdynamix.api.application.IDynamixFacade interface,
 * generating a proxy if needed.
 */
public static org.ambientdynamix.api.application.IDynamixFacade asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.ambientdynamix.api.application.IDynamixFacade))) {
return ((org.ambientdynamix.api.application.IDynamixFacade)iin);
}
return new org.ambientdynamix.api.application.IDynamixFacade.Stub.Proxy(obj);
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
case TRANSACTION_addDynamixListener:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
this.addDynamixListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeDynamixListener:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
this.removeDynamixListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_openSession:
{
data.enforceInterface(DESCRIPTOR);
this.openSession();
reply.writeNoException();
return true;
}
case TRANSACTION_closeSession:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.Result _result = this.closeSession();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_isSessionOpen:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isSessionOpen();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getListenerId:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
org.ambientdynamix.api.application.IdResult _result = this.getListenerId(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_addContextSupport:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
org.ambientdynamix.api.application.Result _result = this.addContextSupport(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_addConfiguredContextSupport:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
android.os.Bundle _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
org.ambientdynamix.api.application.Result _result = this.addConfiguredContextSupport(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getContextSupport:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
org.ambientdynamix.api.application.ContextSupportResult _result = this.getContextSupport(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_removeContextSupport:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
org.ambientdynamix.api.application.ContextSupportInfo _arg1;
if ((0!=data.readInt())) {
_arg1 = org.ambientdynamix.api.application.ContextSupportInfo.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
org.ambientdynamix.api.application.Result _result = this.removeContextSupport(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_removeContextSupportForContextType:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
org.ambientdynamix.api.application.Result _result = this.removeContextSupportForContextType(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_removeAllContextSupportForListener:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
org.ambientdynamix.api.application.Result _result = this.removeAllContextSupportForListener(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_removeAllContextSupport:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.Result _result = this.removeAllContextSupport();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getAllContextPluginInformation:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformationResult _result = this.getAllContextPluginInformation();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getContextPluginInformation:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.ambientdynamix.api.application.ContextPluginInformationResult _result = this.getContextPluginInformation(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_resendAllCachedContextEvents:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
org.ambientdynamix.api.application.Result _result = this.resendAllCachedContextEvents(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_resendAllTypedCachedContextEvents:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
org.ambientdynamix.api.application.Result _result = this.resendAllTypedCachedContextEvents(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_resendCachedContextEvents:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
org.ambientdynamix.api.application.Result _result = this.resendCachedContextEvents(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_resendTypedCachedContextEvents:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
org.ambientdynamix.api.application.Result _result = this.resendTypedCachedContextEvents(_arg0, _arg1, _arg2);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_contextRequest:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
org.ambientdynamix.api.application.IdResult _result = this.contextRequest(_arg0, _arg1, _arg2);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_configuredContextRequest:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
android.os.Bundle _arg3;
if ((0!=data.readInt())) {
_arg3 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg3 = null;
}
org.ambientdynamix.api.application.IdResult _result = this.configuredContextRequest(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getSessionId:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IdResult _result = this.getSessionId();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_requestContextPluginInstallation:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
org.ambientdynamix.api.application.Result _result = this.requestContextPluginInstallation(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_requestContextPluginUninstall:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
org.ambientdynamix.api.application.Result _result = this.requestContextPluginUninstall(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_isDynamixActive:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isDynamixActive();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isPanosMaster:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPanosMaster();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_stopPlugin:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.stopPlugin(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_openContextPluginConfigurationView:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.IDynamixListener _arg0;
_arg0 = org.ambientdynamix.api.application.IDynamixListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
org.ambientdynamix.api.application.Result _result = this.openContextPluginConfigurationView(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.ambientdynamix.api.application.IDynamixFacade
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
	 * Adds the IDynamixListener to the Dynamix Framework. This method may be called at any time, regardless of the application's
	 * security authorization.
	 * @param listener The listener to add.
	 */
@Override public void addDynamixListener(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_addDynamixListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Removes the IDynamixListener from the Dynamix Framework. This method may be called at any time, regardless of the application's
	 * security authorization.
	 * @param listener The listener to remove.
	 */
@Override public void removeDynamixListener(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeDynamixListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Indicates that the calling application wishes to open a session with the Dynamix framework. After calling 'openSession', applications 
	 * must wait for the 'onSessionOpened' event before they can call additional IDynamixFacade methods (except for 
	 * 'closeSession'). Applications will only receive 'onSessionOpened' if they have been authorized by Dynamix's context firewall, which 
	 * may not be immediate, since users must create a privacy policy for the application before Dynamix interaction is 
	 * allowed. Note that once 'onSessionOpened' has been received, applications must call 'addContextSupport'
	 * for a particular listener and context type before they will be able to perform context sensing or acting. 
	 */
@Override public void openSession() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_openSession, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Immediately closes the application's Dynamix session, removing all of the application's context support.
	 * Once the application's session is closed, it will receive an 'onSessionClosed' event. To re-open a Dynamix session, 
	 * the application must call 'openSession' again (and wait for the 'onSessionOpened' event). Any registered IDynamixListeners
	 * are maintained, even when the session is closed.
	 * @return A Result indicating success or failure. 
	 */
@Override public org.ambientdynamix.api.application.Result closeSession() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_closeSession, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns true if the application's session is open; false otherwise.
	 * @return True if the application's session is open; false otherwise.
	 */
@Override public boolean isSessionOpen() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isSessionOpen, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns the listener's id.
	 * @return An IdResult indicating success or failure. On success, the id is provided in the IdResult.
	 */
@Override public org.ambientdynamix.api.application.IdResult getListenerId(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.IdResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getListenerId, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.IdResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Adds context support for the specified listener and context type.
	 * The return values for this method are provided asynchronously using events from the IContextListener interface. 
	 * If the context support was successfully added, the 'onContextSupportAdded' event is raised.
	 * If the context support was not successfully added, the 'onContextTypeNotSupported' event is raised.
	 * If the requested context support was not available, and will be installed, the
	 * 'onInstallingContextSupport' will be raised, followed by 'onInstallingContextPlugin' and events related to plugin
	 * installation.
	 *
	 * @param listener The IDynamixListener to add.
	 * @param contextType A String describing the requested context type. Note that
	 * the contextType string must reference a valid context type string (as described in the developer documentation).
	 * @return A Result indicating success or failure..
	 */
@Override public org.ambientdynamix.api.application.Result addContextSupport(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(contextType);
mRemote.transact(Stub.TRANSACTION_addContextSupport, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Adds context support for the specified listener, context type and support configuration.
	 * The return values for this method are provided asynchronously using events from the IContextListener interface. 
	 * If the context support was successfully added, the 'onContextSupportAdded' event is raised.
	 * If the context support was not successfully added, the 'onContextTypeNotSupported' event is raised.
	 * If the specified context support was not available, and will be installed, the
	 * 'onInstallingContextSupport' and the 'onInstallingContextPlugin' events will be raised.
	 *
	 * @param listener The IDynamixListener to add.
	 * @param contextSupportConfig A Bundle describing the requested context support. 
	          See the developer documentation for a description of available configuration options.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result addConfiguredContextSupport(org.ambientdynamix.api.application.IDynamixListener listener, android.os.Bundle contextSupportConfig) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
if ((contextSupportConfig!=null)) {
_data.writeInt(1);
contextSupportConfig.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_addConfiguredContextSupport, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns the context support that has been registered by the specified IDynamixListener.
	 *
	 * @param listener The IDynamixListener.
	 * @return A ContextSupportResult.
	 */
@Override public org.ambientdynamix.api.application.ContextSupportResult getContextSupport(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.ContextSupportResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getContextSupport, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.ContextSupportResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Removes previously added context support for the specified listener. 
	 *
	 * @param listener The listener owning the context support.
	 * @param info The context support to remove. 
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result removeContextSupport(org.ambientdynamix.api.application.IDynamixListener listener, org.ambientdynamix.api.application.ContextSupportInfo info) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
if ((info!=null)) {
_data.writeInt(1);
info.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_removeContextSupport, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Removes all context support for the specified listener and contextType.  
	 *
	 * @param listener The listener owning the context support.
	 * @param contextType The context support type to remove. 
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result removeContextSupportForContextType(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(contextType);
mRemote.transact(Stub.TRANSACTION_removeContextSupportForContextType, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Removes all previously added context support for the specified listener, regardless of contextType. 
	 *
	 * @param listener The listener owning the context support.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result removeAllContextSupportForListener(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeAllContextSupportForListener, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Removes all previously added context support for all listeners, regardless of contextType. 
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result removeAllContextSupport() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_removeAllContextSupport, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns a List of both installed and pending ContextPlugins available from Dynamix. You can check
	 * the installation status of a plug-in by calling 'getInstallStatus()' on each ContextPluginInformation entity
	 * contained in the ContextPluginInformationResult.
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the ContextPluginInformationResult contains the List of context plug-ins.
	 */
@Override public org.ambientdynamix.api.application.ContextPluginInformationResult getAllContextPluginInformation() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.ContextPluginInformationResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAllContextPluginInformation, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.ContextPluginInformationResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns a ContextPluginInformation object for the specified pluginId. You can check
	 * the installation status of the plug-in by calling 'getInstallStatus()' on the ContextPluginInformation entity.
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the ContextPluginInformationResult 
	 * contains the requested context plug-in as the only List object.
	 */
@Override public org.ambientdynamix.api.application.ContextPluginInformationResult getContextPluginInformation(java.lang.String pluginId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.ContextPluginInformationResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pluginId);
mRemote.transact(Stub.TRANSACTION_getContextPluginInformation, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.ContextPluginInformationResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Resends all ContextEvents that have been cached by Dynamix for the specified listener. ContextEvents are provided to 
	 * applications according to Dynamix authentication and security policies defined by Dynamix users.
	 * Note that the requesting application will only received context info they are subscribed to.
	 *
	 * @param listener The listener wishing to receive cached events.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result resendAllCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_resendAllCachedContextEvents, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Resends all ContextEvents (of the specified contextType) that have been cached by Dynamix for the specified listener. 
	 * ContextEvents are provided to applications according to Dynamix authentication and security policies defined by Dynamix users.
	 * Note that the requesting application will only received context info they are subscribed to.
	 *
	 * @param listener The listener wishing to receive cached events.
	 * @param contextType The type of ContextEvent to return.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result resendAllTypedCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(contextType);
mRemote.transact(Stub.TRANSACTION_resendAllTypedCachedContextEvents, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Resends the ContextEvent entities that have been cached for the listener within the specified 
	 * past number of milliseconds. If the number of milliseconds is longer than the max cache time, then all cached 
	 * events are returned. ContextEvents are provided to applications according to Dynamix authentication 
	 * and security policies. Note that the requesting application will only received context info they are subscribed to.
	 * 
	 * @param listener The listener wishing to receive the results.
	 * @param previousMills The time (in milliseconds) to retrieve past events.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result resendCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener, int previousMills) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeInt(previousMills);
mRemote.transact(Stub.TRANSACTION_resendCachedContextEvents, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Resends the ContextEvent entities (of the specified contextType) that have been cached for the listener within the specified 
	 * past number of milliseconds. If the number of milliseconds is longer than the max cache time, then all appropriate cached
	 * events are returned. ContextEvents are provided to applications according to Dynamix authentication 
	 * and security policies. Note that the requesting application will only received context info they are subscribed to.
	 * 
	 * @param listener The listener wishing to receive the results.
	 * @param contextType The type of ContextEvent to return.
	 * @param previousMills The time (in milliseconds) to retrieve past events.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result resendTypedCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType, int previousMills) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(contextType);
_data.writeInt(previousMills);
mRemote.transact(Stub.TRANSACTION_resendTypedCachedContextEvents, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Requests that Dynamix perform a dedicated context interaction (sensing or acting) using the specified plugin and contextType.
	 * Note that this method is only available for ContextPlugins that programmatic access (i.e., reactive context plug-in types).
	 *	
	 * @param listener The listener wishing to receive the results.
	 * @param pluginId The id of the plugin that should perform the context scan.
	 * @param contextType The type of context info to scan for.
	 * @return An IdResult indicating success or failure. On success, the request id is provided in the IdResult.
	 */
@Override public org.ambientdynamix.api.application.IdResult contextRequest(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String pluginId, java.lang.String contextType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.IdResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(pluginId);
_data.writeString(contextType);
mRemote.transact(Stub.TRANSACTION_contextRequest, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.IdResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Requests that Dynamix perform a dedicated context interaction using the specified plugin, contextType and contextConfig.
	 * Context requests may be of several types and are plug-in specific (see the plug-in documentation for details). 
	 * For some plug-ins, a context request may returns specific contextual information obtained from the environment. 
	 * For other plug-ins, a context request may be a change in the underlying contextual situation (e.g., playing a media file on a nearby media renderer).
	 * See the plugin's documentation for configuration options that can be included in the interactionConfig Bundle.
	 *	
	 * @param listener The listener wishing to receive the results.
	 * @param pluginId The id of the plugin that should handle the context request.
	 * @param contextType The type of context to interact with.
	 * @param contextConfig A plug-in specific Bundle of context request configuration options.
	 * @return An IdResult indicating success or failure. On success, the request id is provided in the IdResult.
	 */
@Override public org.ambientdynamix.api.application.IdResult configuredContextRequest(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String pluginId, java.lang.String contextType, android.os.Bundle contextConfig) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.IdResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(pluginId);
_data.writeString(contextType);
if ((contextConfig!=null)) {
_data.writeInt(1);
contextConfig.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_configuredContextRequest, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.IdResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns the session id for this application, which is used for some secure interactions with Dynamix, such as
	 * launching context acquisition interfaces for Context Plug-ins of type pull interactive.
	 * @return An IdResult indicating success or failure. On success, the session id is provided in the IdResult.
	 */
@Override public org.ambientdynamix.api.application.IdResult getSessionId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.IdResult _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSessionId, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.IdResult.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Request that Dynamix install a specific ContextPlugin on behalf of the Application. Such a request might be made 
	 * if an application has a dependency on a specific ContextPlugin. If the installation request is accepted by Dynamix, this 
	 * method returns its results asynchronously using 'onInstallingContextPlugin' and events related to plugin installation. 
	 * 
	 * @param plugInfo The plugin to install.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result requestContextPluginInstallation(org.ambientdynamix.api.application.ContextPluginInformation plugInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugInfo!=null)) {
_data.writeInt(1);
plugInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_requestContextPluginInstallation, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Request that Dynamix uninstall a specific ContextPlugin on behalf of the Application.
	 * 
	 * @param plugInfo The plugin to uninstall.
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result requestContextPluginUninstall(org.ambientdynamix.api.application.ContextPluginInformation plugInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugInfo!=null)) {
_data.writeInt(1);
plugInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_requestContextPluginUninstall, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Returns true if Dynamix is active; false otherwise.
	 * @return True if Dynamix is active; false otherwise.
	 */
@Override public boolean isDynamixActive() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isDynamixActive, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isPanosMaster() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPanosMaster, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void stopPlugin(java.lang.String pluginId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pluginId);
mRemote.transact(Stub.TRANSACTION_stopPlugin, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Opens the specified plug-in's configuration view (if it has one).
	 * @return A Result indicating success or failure.
	 */
@Override public org.ambientdynamix.api.application.Result openContextPluginConfigurationView(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String pluginId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.ambientdynamix.api.application.Result _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(pluginId);
mRemote.transact(Stub.TRANSACTION_openContextPluginConfigurationView, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.ambientdynamix.api.application.Result.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_addDynamixListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_removeDynamixListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_openSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_closeSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_isSessionOpen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getListenerId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_addContextSupport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_addConfiguredContextSupport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getContextSupport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_removeContextSupport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_removeContextSupportForContextType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_removeAllContextSupportForListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_removeAllContextSupport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getAllContextPluginInformation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_getContextPluginInformation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_resendAllCachedContextEvents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_resendAllTypedCachedContextEvents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_resendCachedContextEvents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_resendTypedCachedContextEvents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_contextRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_configuredContextRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_getSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_requestContextPluginInstallation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_requestContextPluginUninstall = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_isDynamixActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_isPanosMaster = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_stopPlugin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_openContextPluginConfigurationView = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
}
/**
	 * Adds the IDynamixListener to the Dynamix Framework. This method may be called at any time, regardless of the application's
	 * security authorization.
	 * @param listener The listener to add.
	 */
public void addDynamixListener(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException;
/**
	 * Removes the IDynamixListener from the Dynamix Framework. This method may be called at any time, regardless of the application's
	 * security authorization.
	 * @param listener The listener to remove.
	 */
public void removeDynamixListener(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException;
/**
	 * Indicates that the calling application wishes to open a session with the Dynamix framework. After calling 'openSession', applications 
	 * must wait for the 'onSessionOpened' event before they can call additional IDynamixFacade methods (except for 
	 * 'closeSession'). Applications will only receive 'onSessionOpened' if they have been authorized by Dynamix's context firewall, which 
	 * may not be immediate, since users must create a privacy policy for the application before Dynamix interaction is 
	 * allowed. Note that once 'onSessionOpened' has been received, applications must call 'addContextSupport'
	 * for a particular listener and context type before they will be able to perform context sensing or acting. 
	 */
public void openSession() throws android.os.RemoteException;
/**
	 * Immediately closes the application's Dynamix session, removing all of the application's context support.
	 * Once the application's session is closed, it will receive an 'onSessionClosed' event. To re-open a Dynamix session, 
	 * the application must call 'openSession' again (and wait for the 'onSessionOpened' event). Any registered IDynamixListeners
	 * are maintained, even when the session is closed.
	 * @return A Result indicating success or failure. 
	 */
public org.ambientdynamix.api.application.Result closeSession() throws android.os.RemoteException;
/**
	 * Returns true if the application's session is open; false otherwise.
	 * @return True if the application's session is open; false otherwise.
	 */
public boolean isSessionOpen() throws android.os.RemoteException;
/**
	 * Returns the listener's id.
	 * @return An IdResult indicating success or failure. On success, the id is provided in the IdResult.
	 */
public org.ambientdynamix.api.application.IdResult getListenerId(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException;
/**
	 * Adds context support for the specified listener and context type.
	 * The return values for this method are provided asynchronously using events from the IContextListener interface. 
	 * If the context support was successfully added, the 'onContextSupportAdded' event is raised.
	 * If the context support was not successfully added, the 'onContextTypeNotSupported' event is raised.
	 * If the requested context support was not available, and will be installed, the
	 * 'onInstallingContextSupport' will be raised, followed by 'onInstallingContextPlugin' and events related to plugin
	 * installation.
	 *
	 * @param listener The IDynamixListener to add.
	 * @param contextType A String describing the requested context type. Note that
	 * the contextType string must reference a valid context type string (as described in the developer documentation).
	 * @return A Result indicating success or failure..
	 */
public org.ambientdynamix.api.application.Result addContextSupport(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType) throws android.os.RemoteException;
/**
	 * Adds context support for the specified listener, context type and support configuration.
	 * The return values for this method are provided asynchronously using events from the IContextListener interface. 
	 * If the context support was successfully added, the 'onContextSupportAdded' event is raised.
	 * If the context support was not successfully added, the 'onContextTypeNotSupported' event is raised.
	 * If the specified context support was not available, and will be installed, the
	 * 'onInstallingContextSupport' and the 'onInstallingContextPlugin' events will be raised.
	 *
	 * @param listener The IDynamixListener to add.
	 * @param contextSupportConfig A Bundle describing the requested context support. 
	          See the developer documentation for a description of available configuration options.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result addConfiguredContextSupport(org.ambientdynamix.api.application.IDynamixListener listener, android.os.Bundle contextSupportConfig) throws android.os.RemoteException;
/**
	 * Returns the context support that has been registered by the specified IDynamixListener.
	 *
	 * @param listener The IDynamixListener.
	 * @return A ContextSupportResult.
	 */
public org.ambientdynamix.api.application.ContextSupportResult getContextSupport(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException;
/**
	 * Removes previously added context support for the specified listener. 
	 *
	 * @param listener The listener owning the context support.
	 * @param info The context support to remove. 
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result removeContextSupport(org.ambientdynamix.api.application.IDynamixListener listener, org.ambientdynamix.api.application.ContextSupportInfo info) throws android.os.RemoteException;
/**
	 * Removes all context support for the specified listener and contextType.  
	 *
	 * @param listener The listener owning the context support.
	 * @param contextType The context support type to remove. 
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result removeContextSupportForContextType(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType) throws android.os.RemoteException;
/**
	 * Removes all previously added context support for the specified listener, regardless of contextType. 
	 *
	 * @param listener The listener owning the context support.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result removeAllContextSupportForListener(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException;
/**
	 * Removes all previously added context support for all listeners, regardless of contextType. 
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result removeAllContextSupport() throws android.os.RemoteException;
/**
	 * Returns a List of both installed and pending ContextPlugins available from Dynamix. You can check
	 * the installation status of a plug-in by calling 'getInstallStatus()' on each ContextPluginInformation entity
	 * contained in the ContextPluginInformationResult.
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the ContextPluginInformationResult contains the List of context plug-ins.
	 */
public org.ambientdynamix.api.application.ContextPluginInformationResult getAllContextPluginInformation() throws android.os.RemoteException;
/**
	 * Returns a ContextPluginInformation object for the specified pluginId. You can check
	 * the installation status of the plug-in by calling 'getInstallStatus()' on the ContextPluginInformation entity.
	 * @return A ContextPluginInformationResult indicating success or failure. On success, the ContextPluginInformationResult 
	 * contains the requested context plug-in as the only List object.
	 */
public org.ambientdynamix.api.application.ContextPluginInformationResult getContextPluginInformation(java.lang.String pluginId) throws android.os.RemoteException;
/**
	 * Resends all ContextEvents that have been cached by Dynamix for the specified listener. ContextEvents are provided to 
	 * applications according to Dynamix authentication and security policies defined by Dynamix users.
	 * Note that the requesting application will only received context info they are subscribed to.
	 *
	 * @param listener The listener wishing to receive cached events.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result resendAllCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener) throws android.os.RemoteException;
/**
	 * Resends all ContextEvents (of the specified contextType) that have been cached by Dynamix for the specified listener. 
	 * ContextEvents are provided to applications according to Dynamix authentication and security policies defined by Dynamix users.
	 * Note that the requesting application will only received context info they are subscribed to.
	 *
	 * @param listener The listener wishing to receive cached events.
	 * @param contextType The type of ContextEvent to return.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result resendAllTypedCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType) throws android.os.RemoteException;
/**
	 * Resends the ContextEvent entities that have been cached for the listener within the specified 
	 * past number of milliseconds. If the number of milliseconds is longer than the max cache time, then all cached 
	 * events are returned. ContextEvents are provided to applications according to Dynamix authentication 
	 * and security policies. Note that the requesting application will only received context info they are subscribed to.
	 * 
	 * @param listener The listener wishing to receive the results.
	 * @param previousMills The time (in milliseconds) to retrieve past events.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result resendCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener, int previousMills) throws android.os.RemoteException;
/**
	 * Resends the ContextEvent entities (of the specified contextType) that have been cached for the listener within the specified 
	 * past number of milliseconds. If the number of milliseconds is longer than the max cache time, then all appropriate cached
	 * events are returned. ContextEvents are provided to applications according to Dynamix authentication 
	 * and security policies. Note that the requesting application will only received context info they are subscribed to.
	 * 
	 * @param listener The listener wishing to receive the results.
	 * @param contextType The type of ContextEvent to return.
	 * @param previousMills The time (in milliseconds) to retrieve past events.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result resendTypedCachedContextEvents(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String contextType, int previousMills) throws android.os.RemoteException;
/**
	 * Requests that Dynamix perform a dedicated context interaction (sensing or acting) using the specified plugin and contextType.
	 * Note that this method is only available for ContextPlugins that programmatic access (i.e., reactive context plug-in types).
	 *	
	 * @param listener The listener wishing to receive the results.
	 * @param pluginId The id of the plugin that should perform the context scan.
	 * @param contextType The type of context info to scan for.
	 * @return An IdResult indicating success or failure. On success, the request id is provided in the IdResult.
	 */
public org.ambientdynamix.api.application.IdResult contextRequest(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String pluginId, java.lang.String contextType) throws android.os.RemoteException;
/**
	 * Requests that Dynamix perform a dedicated context interaction using the specified plugin, contextType and contextConfig.
	 * Context requests may be of several types and are plug-in specific (see the plug-in documentation for details). 
	 * For some plug-ins, a context request may returns specific contextual information obtained from the environment. 
	 * For other plug-ins, a context request may be a change in the underlying contextual situation (e.g., playing a media file on a nearby media renderer).
	 * See the plugin's documentation for configuration options that can be included in the interactionConfig Bundle.
	 *	
	 * @param listener The listener wishing to receive the results.
	 * @param pluginId The id of the plugin that should handle the context request.
	 * @param contextType The type of context to interact with.
	 * @param contextConfig A plug-in specific Bundle of context request configuration options.
	 * @return An IdResult indicating success or failure. On success, the request id is provided in the IdResult.
	 */
public org.ambientdynamix.api.application.IdResult configuredContextRequest(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String pluginId, java.lang.String contextType, android.os.Bundle contextConfig) throws android.os.RemoteException;
/**
	 * Returns the session id for this application, which is used for some secure interactions with Dynamix, such as
	 * launching context acquisition interfaces for Context Plug-ins of type pull interactive.
	 * @return An IdResult indicating success or failure. On success, the session id is provided in the IdResult.
	 */
public org.ambientdynamix.api.application.IdResult getSessionId() throws android.os.RemoteException;
/**
	 * Request that Dynamix install a specific ContextPlugin on behalf of the Application. Such a request might be made 
	 * if an application has a dependency on a specific ContextPlugin. If the installation request is accepted by Dynamix, this 
	 * method returns its results asynchronously using 'onInstallingContextPlugin' and events related to plugin installation. 
	 * 
	 * @param plugInfo The plugin to install.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result requestContextPluginInstallation(org.ambientdynamix.api.application.ContextPluginInformation plugInfo) throws android.os.RemoteException;
/**
	 * Request that Dynamix uninstall a specific ContextPlugin on behalf of the Application.
	 * 
	 * @param plugInfo The plugin to uninstall.
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result requestContextPluginUninstall(org.ambientdynamix.api.application.ContextPluginInformation plugInfo) throws android.os.RemoteException;
/**
	 * Returns true if Dynamix is active; false otherwise.
	 * @return True if Dynamix is active; false otherwise.
	 */
public boolean isDynamixActive() throws android.os.RemoteException;
public boolean isPanosMaster() throws android.os.RemoteException;
public void stopPlugin(java.lang.String pluginId) throws android.os.RemoteException;
/**
	 * Opens the specified plug-in's configuration view (if it has one).
	 * @return A Result indicating success or failure.
	 */
public org.ambientdynamix.api.application.Result openContextPluginConfigurationView(org.ambientdynamix.api.application.IDynamixListener listener, java.lang.String pluginId) throws android.os.RemoteException;
}
