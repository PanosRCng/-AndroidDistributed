/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/panos/Downloads/dynamix/dynamix-framework/src/org/ambientdynamix/api/application/IDynamixListener.aidl
 */
package org.ambientdynamix.api.application;
/**
 * IDynamixListener provides a set of methods that remote clients must implement in order to receive
 * context events from the Dynamix Framework. 
 *
 * @author Darren Carlson
 */
public interface IDynamixListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.ambientdynamix.api.application.IDynamixListener
{
private static final java.lang.String DESCRIPTOR = "org.ambientdynamix.api.application.IDynamixListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.ambientdynamix.api.application.IDynamixListener interface,
 * generating a proxy if needed.
 */
public static org.ambientdynamix.api.application.IDynamixListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.ambientdynamix.api.application.IDynamixListener))) {
return ((org.ambientdynamix.api.application.IDynamixListener)iin);
}
return new org.ambientdynamix.api.application.IDynamixListener.Stub.Proxy(obj);
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
case TRANSACTION_onDynamixListenerAdded:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onDynamixListenerAdded(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onDynamixListenerRemoved:
{
data.enforceInterface(DESCRIPTOR);
this.onDynamixListenerRemoved();
reply.writeNoException();
return true;
}
case TRANSACTION_onAwaitingSecurityAuthorization:
{
data.enforceInterface(DESCRIPTOR);
this.onAwaitingSecurityAuthorization();
reply.writeNoException();
return true;
}
case TRANSACTION_onSecurityAuthorizationGranted:
{
data.enforceInterface(DESCRIPTOR);
this.onSecurityAuthorizationGranted();
reply.writeNoException();
return true;
}
case TRANSACTION_onSecurityAuthorizationRevoked:
{
data.enforceInterface(DESCRIPTOR);
this.onSecurityAuthorizationRevoked();
reply.writeNoException();
return true;
}
case TRANSACTION_onSessionOpened:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onSessionOpened(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onSessionClosed:
{
data.enforceInterface(DESCRIPTOR);
this.onSessionClosed();
reply.writeNoException();
return true;
}
case TRANSACTION_onContextEvent:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onContextEvent(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextSupportAdded:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextSupportInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextSupportInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onContextSupportAdded(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextSupportRemoved:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextSupportInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextSupportInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onContextSupportRemoved(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextTypeNotSupported:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onContextTypeNotSupported(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onInstallingContextSupport:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
this.onInstallingContextSupport(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onInstallingContextPlugin:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onInstallingContextPlugin(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginInstallProgress:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _arg1;
_arg1 = data.readInt();
this.onContextPluginInstallProgress(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginInstalled:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onContextPluginInstalled(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginUninstalled:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onContextPluginUninstalled(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginInstallFailed:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
this.onContextPluginInstallFailed(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextRequestFailed:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
this.onContextRequestFailed(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginDiscoveryStarted:
{
data.enforceInterface(DESCRIPTOR);
this.onContextPluginDiscoveryStarted();
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginDiscoveryFinished:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<org.ambientdynamix.api.application.ContextPluginInformation> _arg0;
_arg0 = data.createTypedArrayList(org.ambientdynamix.api.application.ContextPluginInformation.CREATOR);
this.onContextPluginDiscoveryFinished(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onDynamixFrameworkActive:
{
data.enforceInterface(DESCRIPTOR);
this.onDynamixFrameworkActive();
reply.writeNoException();
return true;
}
case TRANSACTION_onDynamixFrameworkInactive:
{
data.enforceInterface(DESCRIPTOR);
this.onDynamixFrameworkInactive();
reply.writeNoException();
return true;
}
case TRANSACTION_onContextPluginError:
{
data.enforceInterface(DESCRIPTOR);
org.ambientdynamix.api.application.ContextPluginInformation _arg0;
if ((0!=data.readInt())) {
_arg0 = org.ambientdynamix.api.application.ContextPluginInformation.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
this.onContextPluginError(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.ambientdynamix.api.application.IDynamixListener
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
	 * A response to the IDynamixFacade 'addDynamixListener' method indicating that the Dynamix listener has been added.
	 * @param listenerId The listener's unique listenerId.
	 */
@Override public void onDynamixListenerAdded(java.lang.String listenerId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(listenerId);
mRemote.transact(Stub.TRANSACTION_onDynamixListenerAdded, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * A response to the IDynamixFacade 'removeDynamixListener' method indicating that the Dynamix listener has been removed.
	 */
@Override public void onDynamixListenerRemoved() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onDynamixListenerRemoved, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the application is awaiting security authorization by the Dynamix Framework. Sent in response 
	 * to a call to the IDynamixFacade's 'openSession' method if the application has not yet been granted 
	 * security authorization. If authorization is granted by Dynamix, the 'onSecurityAuthorizationGranted' event will 
	 * be raised.
	 */
@Override public void onAwaitingSecurityAuthorization() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onAwaitingSecurityAuthorization, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the application has been granted security authorization by the Dynamix Framework.
	 * Once security authorization has been granted, the 'onSecurityAuthorizationGranted' event will always
	 * be raised in response to calls to the IDynamixFacade's 'openSession' method; however, Applications must 
	 * wait for the 'onSessionOpened' event before they can interact with Dynamix (e.g. add context  support)
	 * or expect context events. Note that the 'onSessionOpened' implies that security authorization has
	 * been granted.
	 */
@Override public void onSecurityAuthorizationGranted() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSecurityAuthorizationGranted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the application's security authorization has been revoked by the Dynamix Framework.
	 * Once 'onSecurityAuthorizationRevoked' is received, the Dynamix Framework will immediately stop sending events to 
	 * the application and further interaction with Dynamix is not allowed until security authorization is granted again.
	 * Once security authorization is revoked, applications may only call the IDynamixFacade's 'openSession', 'addDynamixListener' and
	 * 'removeDynamixListener' methods.
	 */
@Override public void onSecurityAuthorizationRevoked() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSecurityAuthorizationRevoked, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the Dynamix session has been opened. Once received, applications can safely interact with Dynamix 
	 * (e.g. add context support) and expect context events. Note that 'onSessionOpened' will only be 
	 * sent to an application once security authorization has been granted by the Dynamix Framework. This event is 
	 * typically sent in response to a call to the IDynamixFacade's 'openSession' method. Note that if the
	 * Dynamix Framework is deactivated after 'onSessionOpened' has been received, the application will receive the 
	 * 'onSessionClosed' event. In this case it is NOT necessary to call the IDynamixFacade's 'openSession' 
	 * method again, since Dynamix maintains the application's session status, even while inactive. Once the 
	 * Dynamix Framework becomes active again, the 'onSessionOpened' event will be raised again. However, if the 
	 * application loses contact with Dynamix (as indicated by an 'onServiceDisconnected' provided 
	 * by the ServiceConnection object), Dynamix has been shut down by Android (or crashed). In this case, the 
	 * application will need to call the IDynamixFacade's 'openSession' method again.
	 *
	 * @param sessionId The application's sessionId.
	 */
@Override public void onSessionOpened(java.lang.String sessionId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sessionId);
mRemote.transact(Stub.TRANSACTION_onSessionOpened, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the Dynamix session has been closed.
	 */
@Override public void onSessionClosed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSessionClosed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification of an incoming ContextEvent.
	 * @see ContextEvent
	 */
@Override public void onContextEvent(org.ambientdynamix.api.application.ContextEvent event) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_onContextEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the listener that context support for the given context type has been added.
	 * Note that applications may receive more than one of this type of event for the same contextType if multiple 
	 * plugins are able to provide support for the requested type.
	 *
	 * @param supportInfo Information about the context support that was added.
	 */
@Override public void onContextSupportAdded(org.ambientdynamix.api.application.ContextSupportInfo supportInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((supportInfo!=null)) {
_data.writeInt(1);
supportInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onContextSupportAdded, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the listener that context support for the given context type has been removed.
	 *
	 * @param supportInfo Information about the context support that was removed.
	 */
@Override public void onContextSupportRemoved(org.ambientdynamix.api.application.ContextSupportInfo supportInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((supportInfo!=null)) {
_data.writeInt(1);
supportInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onContextSupportRemoved, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that the requested context type is not supported, and that the requested context 
	 * support was not added. This result implies that Dynamix is not able to install support for the specified
	 * context type at the moment.
	 *
	 * @param contextType The requested context type that is not supported.
	 */
@Override public void onContextTypeNotSupported(java.lang.String contextType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(contextType);
mRemote.transact(Stub.TRANSACTION_onContextTypeNotSupported, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a plugin installation has begun for the specified contextType. This 
	 * event is only raised if the application previously called 'addContextSupport' for a context 
	 * plugin that was not already installed (or was being installed) and the application has permission to install context support. 
	 * If the context support is eventually installed, the caller will receive 'onContextSupportAdded'. If the context support 
	 * was not installed, the caller will receive 'onContextTypeNotSupported'.
	 *
	 * @param ContextPluginInformation The Context Plug-in being installed.
	 * @param contextType The type of context data support being installed.
	 */
@Override public void onInstallingContextSupport(org.ambientdynamix.api.application.ContextPluginInformation plugin, java.lang.String contextType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugin!=null)) {
_data.writeInt(1);
plugin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(contextType);
mRemote.transact(Stub.TRANSACTION_onInstallingContextSupport, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a new Context Plug-in is being installed.
	 * @param plugin The Context Plug-in being installed.
	 */
@Override public void onInstallingContextPlugin(org.ambientdynamix.api.application.ContextPluginInformation plugin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugin!=null)) {
_data.writeInt(1);
plugin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onInstallingContextPlugin, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a new Context Plug-in installation has progressed.
	 * @param plugin The Context Plug-in being installed.
	 * @param plugin The Context Plug-in installation percent complete.
	 */
@Override public void onContextPluginInstallProgress(org.ambientdynamix.api.application.ContextPluginInformation plugin, int percentComplete) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugin!=null)) {
_data.writeInt(1);
plugin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(percentComplete);
mRemote.transact(Stub.TRANSACTION_onContextPluginInstallProgress, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a new Context Plug-in has been installed.
	 * @param plugin The Context Plug-in that was installed. 
	 */
@Override public void onContextPluginInstalled(org.ambientdynamix.api.application.ContextPluginInformation plugin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugin!=null)) {
_data.writeInt(1);
plugin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onContextPluginInstalled, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a previously installed Context Plug-in has been uninstalled.
	 * @param plugin The Context Plug-in that was uninstalled. 
	 */
@Override public void onContextPluginUninstalled(org.ambientdynamix.api.application.ContextPluginInformation plugin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plugin!=null)) {
_data.writeInt(1);
plugin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onContextPluginUninstalled, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a Context Plug-in has failed to install. 
	 *
	 * @param plugin The Context Plug-in that failed to install.
	 * @param message The message associated with the failure.
	 */
@Override public void onContextPluginInstallFailed(org.ambientdynamix.api.application.ContextPluginInformation plug, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plug!=null)) {
_data.writeInt(1);
plug.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_onContextPluginInstallFailed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notifies the application that a context request has failed.
	 *
	 * @param requestId The id of the context request.
	 * @param message The message associated with the failure.
	 * @param errorCode The error code associated with the failure.
	 */
@Override public void onContextRequestFailed(java.lang.String requestId, java.lang.String message, int errorCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(requestId);
_data.writeString(message);
_data.writeInt(errorCode);
mRemote.transact(Stub.TRANSACTION_onContextRequestFailed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that context plug-in discovery has started.
	 */
@Override public void onContextPluginDiscoveryStarted() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onContextPluginDiscoveryStarted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that context plug-in discovery has finished.
	 * @param discoveredPlugins The context plug-ins discovered (may be empty)
	 */
@Override public void onContextPluginDiscoveryFinished(java.util.List<org.ambientdynamix.api.application.ContextPluginInformation> discoveredPlugins) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(discoveredPlugins);
mRemote.transact(Stub.TRANSACTION_onContextPluginDiscoveryFinished, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the Dynamix Framework is active.
	 */
@Override public void onDynamixFrameworkActive() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onDynamixFrameworkActive, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that the Dynamix Framework is inactive.
	 */
@Override public void onDynamixFrameworkInactive() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onDynamixFrameworkInactive, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Notification that a Context Plug-in has encountered an error (and mostly likely has been deactivated).
	 */
@Override public void onContextPluginError(org.ambientdynamix.api.application.ContextPluginInformation plug, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((plug!=null)) {
_data.writeInt(1);
plug.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_onContextPluginError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onDynamixListenerAdded = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDynamixListenerRemoved = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onAwaitingSecurityAuthorization = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onSecurityAuthorizationGranted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onSecurityAuthorizationRevoked = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_onSessionOpened = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_onSessionClosed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_onContextEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_onContextSupportAdded = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_onContextSupportRemoved = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_onContextTypeNotSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_onInstallingContextSupport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_onInstallingContextPlugin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_onContextPluginInstallProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_onContextPluginInstalled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_onContextPluginUninstalled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_onContextPluginInstallFailed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_onContextRequestFailed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_onContextPluginDiscoveryStarted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_onContextPluginDiscoveryFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_onDynamixFrameworkActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_onDynamixFrameworkInactive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_onContextPluginError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
}
/**
	 * A response to the IDynamixFacade 'addDynamixListener' method indicating that the Dynamix listener has been added.
	 * @param listenerId The listener's unique listenerId.
	 */
public void onDynamixListenerAdded(java.lang.String listenerId) throws android.os.RemoteException;
/**
	 * A response to the IDynamixFacade 'removeDynamixListener' method indicating that the Dynamix listener has been removed.
	 */
public void onDynamixListenerRemoved() throws android.os.RemoteException;
/**
	 * Notification that the application is awaiting security authorization by the Dynamix Framework. Sent in response 
	 * to a call to the IDynamixFacade's 'openSession' method if the application has not yet been granted 
	 * security authorization. If authorization is granted by Dynamix, the 'onSecurityAuthorizationGranted' event will 
	 * be raised.
	 */
public void onAwaitingSecurityAuthorization() throws android.os.RemoteException;
/**
	 * Notification that the application has been granted security authorization by the Dynamix Framework.
	 * Once security authorization has been granted, the 'onSecurityAuthorizationGranted' event will always
	 * be raised in response to calls to the IDynamixFacade's 'openSession' method; however, Applications must 
	 * wait for the 'onSessionOpened' event before they can interact with Dynamix (e.g. add context  support)
	 * or expect context events. Note that the 'onSessionOpened' implies that security authorization has
	 * been granted.
	 */
public void onSecurityAuthorizationGranted() throws android.os.RemoteException;
/**
	 * Notification that the application's security authorization has been revoked by the Dynamix Framework.
	 * Once 'onSecurityAuthorizationRevoked' is received, the Dynamix Framework will immediately stop sending events to 
	 * the application and further interaction with Dynamix is not allowed until security authorization is granted again.
	 * Once security authorization is revoked, applications may only call the IDynamixFacade's 'openSession', 'addDynamixListener' and
	 * 'removeDynamixListener' methods.
	 */
public void onSecurityAuthorizationRevoked() throws android.os.RemoteException;
/**
	 * Notification that the Dynamix session has been opened. Once received, applications can safely interact with Dynamix 
	 * (e.g. add context support) and expect context events. Note that 'onSessionOpened' will only be 
	 * sent to an application once security authorization has been granted by the Dynamix Framework. This event is 
	 * typically sent in response to a call to the IDynamixFacade's 'openSession' method. Note that if the
	 * Dynamix Framework is deactivated after 'onSessionOpened' has been received, the application will receive the 
	 * 'onSessionClosed' event. In this case it is NOT necessary to call the IDynamixFacade's 'openSession' 
	 * method again, since Dynamix maintains the application's session status, even while inactive. Once the 
	 * Dynamix Framework becomes active again, the 'onSessionOpened' event will be raised again. However, if the 
	 * application loses contact with Dynamix (as indicated by an 'onServiceDisconnected' provided 
	 * by the ServiceConnection object), Dynamix has been shut down by Android (or crashed). In this case, the 
	 * application will need to call the IDynamixFacade's 'openSession' method again.
	 *
	 * @param sessionId The application's sessionId.
	 */
public void onSessionOpened(java.lang.String sessionId) throws android.os.RemoteException;
/**
	 * Notification that the Dynamix session has been closed.
	 */
public void onSessionClosed() throws android.os.RemoteException;
/**
	 * Notification of an incoming ContextEvent.
	 * @see ContextEvent
	 */
public void onContextEvent(org.ambientdynamix.api.application.ContextEvent event) throws android.os.RemoteException;
/**
	 * Notifies the listener that context support for the given context type has been added.
	 * Note that applications may receive more than one of this type of event for the same contextType if multiple 
	 * plugins are able to provide support for the requested type.
	 *
	 * @param supportInfo Information about the context support that was added.
	 */
public void onContextSupportAdded(org.ambientdynamix.api.application.ContextSupportInfo supportInfo) throws android.os.RemoteException;
/**
	 * Notifies the listener that context support for the given context type has been removed.
	 *
	 * @param supportInfo Information about the context support that was removed.
	 */
public void onContextSupportRemoved(org.ambientdynamix.api.application.ContextSupportInfo supportInfo) throws android.os.RemoteException;
/**
	 * Notifies the application that the requested context type is not supported, and that the requested context 
	 * support was not added. This result implies that Dynamix is not able to install support for the specified
	 * context type at the moment.
	 *
	 * @param contextType The requested context type that is not supported.
	 */
public void onContextTypeNotSupported(java.lang.String contextType) throws android.os.RemoteException;
/**
	 * Notifies the application that a plugin installation has begun for the specified contextType. This 
	 * event is only raised if the application previously called 'addContextSupport' for a context 
	 * plugin that was not already installed (or was being installed) and the application has permission to install context support. 
	 * If the context support is eventually installed, the caller will receive 'onContextSupportAdded'. If the context support 
	 * was not installed, the caller will receive 'onContextTypeNotSupported'.
	 *
	 * @param ContextPluginInformation The Context Plug-in being installed.
	 * @param contextType The type of context data support being installed.
	 */
public void onInstallingContextSupport(org.ambientdynamix.api.application.ContextPluginInformation plugin, java.lang.String contextType) throws android.os.RemoteException;
/**
	 * Notifies the application that a new Context Plug-in is being installed.
	 * @param plugin The Context Plug-in being installed.
	 */
public void onInstallingContextPlugin(org.ambientdynamix.api.application.ContextPluginInformation plugin) throws android.os.RemoteException;
/**
	 * Notifies the application that a new Context Plug-in installation has progressed.
	 * @param plugin The Context Plug-in being installed.
	 * @param plugin The Context Plug-in installation percent complete.
	 */
public void onContextPluginInstallProgress(org.ambientdynamix.api.application.ContextPluginInformation plugin, int percentComplete) throws android.os.RemoteException;
/**
	 * Notifies the application that a new Context Plug-in has been installed.
	 * @param plugin The Context Plug-in that was installed. 
	 */
public void onContextPluginInstalled(org.ambientdynamix.api.application.ContextPluginInformation plugin) throws android.os.RemoteException;
/**
	 * Notifies the application that a previously installed Context Plug-in has been uninstalled.
	 * @param plugin The Context Plug-in that was uninstalled. 
	 */
public void onContextPluginUninstalled(org.ambientdynamix.api.application.ContextPluginInformation plugin) throws android.os.RemoteException;
/**
	 * Notifies the application that a Context Plug-in has failed to install. 
	 *
	 * @param plugin The Context Plug-in that failed to install.
	 * @param message The message associated with the failure.
	 */
public void onContextPluginInstallFailed(org.ambientdynamix.api.application.ContextPluginInformation plug, java.lang.String message) throws android.os.RemoteException;
/**
	 * Notifies the application that a context request has failed.
	 *
	 * @param requestId The id of the context request.
	 * @param message The message associated with the failure.
	 * @param errorCode The error code associated with the failure.
	 */
public void onContextRequestFailed(java.lang.String requestId, java.lang.String message, int errorCode) throws android.os.RemoteException;
/**
	 * Notification that context plug-in discovery has started.
	 */
public void onContextPluginDiscoveryStarted() throws android.os.RemoteException;
/**
	 * Notification that context plug-in discovery has finished.
	 * @param discoveredPlugins The context plug-ins discovered (may be empty)
	 */
public void onContextPluginDiscoveryFinished(java.util.List<org.ambientdynamix.api.application.ContextPluginInformation> discoveredPlugins) throws android.os.RemoteException;
/**
	 * Notification that the Dynamix Framework is active.
	 */
public void onDynamixFrameworkActive() throws android.os.RemoteException;
/**
	 * Notification that the Dynamix Framework is inactive.
	 */
public void onDynamixFrameworkInactive() throws android.os.RemoteException;
/**
	 * Notification that a Context Plug-in has encountered an error (and mostly likely has been deactivated).
	 */
public void onContextPluginError(org.ambientdynamix.api.application.ContextPluginInformation plug, java.lang.String message) throws android.os.RemoteException;
}
