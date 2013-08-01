/*
 * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambientdynamix.api.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.Deflater;

import org.ambientdynamix.api.contextplugin.PluginConstants;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

/**
 * ContextEvent represents a parameterized result of a Dynamix context acquisition and modeling process. It contains the
 * 'native' object-based event data (as an IContextInfo attachment), the time that the ContextEvent was generated, how
 * long the ContextEvent is valid (including forever), and (optionally) various string representations of the
 * IContextInfo. String-based context representations are used by clients who may not have access to a given
 * IContextInfo data-type JAR on their class-path, but are still interested in working with the context information. <br/>
 * This class also provides low-level interprocess communication (IPC) features. First, it provides transparent
 * (in-memory) compression and decompression of both string-based and (optionally) object-based context representations
 * to help improve IPC performance. String-based context representations are always compressed. Second, it provides
 * high-performance IPC streaming, which is able to transport arbitrary amounts of data across Android's IPC transport
 * mechanism, which normally has a 100KB hard limit. A StreamController is used to guard against excessive memory
 * consumption during streaming.
 * 
 * @author Darren Carlson
 * @see IContextInfo
 */
public class ContextEvent extends Expirable implements Serializable, Parcelable {
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<ContextEvent> CREATOR = new Parcelable.Creator<ContextEvent>() {
		public ContextEvent createFromParcel(Parcel in) {
			try {
				return new ContextEvent(in);
			} catch (Exception e) {
				Log.w(TAG, e);
				/*
				 * We must return null if we can't create the ContextEvent because this indicates to Android that we
				 * were not able to deserialize the embedded data, which signals Dynamix to perform a string-only
				 * resend.
				 */
				return null;
			}
		}

		public ContextEvent[] newArray(int size) {
			return new ContextEvent[size];
		}
	};
	// Private variables
	private static final long serialVersionUID = 5110811252364172334L;
	private final static String TAG = ContextEvent.class.getSimpleName();
	private ContextPluginInformation eventSource;
	private Map<String, String> contextRepresentationStrings = new HashMap<String, String>();
	private Map<String, byte[]> contextRepresentationStringsBytes = new HashMap<String, byte[]>();
	private IContextInfo contextInfo;
	private byte[] contextInfoBytes;
	private String contextType;
	private boolean attachContextInfo;
	private int targetAppId;
	private String responseId;
	private IStreamController streamController;
	private UUID eventUUID = UUID.randomUUID();
	private boolean useStreaming = false;
	private boolean DEBUG = false;
	private boolean autoWebEncode = true;
	private String webEncodingFormat = PluginConstants.JSON_WEB_ENCODING;

	/**
	 * Creates a ContextEvent using the incoming IContextInfo. Automatically Provides the current local time as a
	 * timestamp and specifies no expiration (i.e., the context event is valid forever).
	 */
	public ContextEvent(IContextInfo eventInfo) {
		this(eventInfo, new Date(), -1);
	}

	/**
	 * Creates a ContextEvent using the incoming IContextInfo. Automatically provides the current local time as a
	 * timestamp and specifies a specific expireMills (in milliseconds).
	 */
	public ContextEvent(IContextInfo eventInfo, int expireMills) {
		this(eventInfo, new Date(), expireMills);
	}

	/**
	 * Creates a ContextEvent using the incoming IContextInfo, timeStamp and expireMills (in milliseconds).
	 */
	public ContextEvent(IContextInfo eventInfo, Date timeStamp, int expireMills) {
		super(timeStamp, expireMills);
		this.contextInfo = eventInfo;
		this.contextType = eventInfo.getContextType();
		if (eventInfo != null)
			attachContextInfo = true;
		else
			attachContextInfo = false;
		if (eventInfo.getStringRepresentationFormats() != null) {
			for (String format : eventInfo.getStringRepresentationFormats()) {
				contextRepresentationStrings.put(format, eventInfo.getStringRepresentation(format));
			}
		}
	}

	/**
	 * Setups up this event for streaming.
	 * 
	 * @param streamController
	 */
	public void prepStreaming(IStreamController streamController) {
		/*
		 * Include methods for prep here
		 */
		this.streamController = streamController;
		if (contextInfo != null) {
			Parcel p = Parcel.obtain();
			p.writeParcelable(contextInfo, 0);
			contextInfoBytes = p.marshall();
			p.recycle();
		}
		try {
			// Compress the string-based context representations
			for (String format : contextRepresentationStrings.keySet()) {
				contextRepresentationStringsBytes.put(format,
						Utils.zipString(contextRepresentationStrings.get(format), Deflater.BEST_SPEED));
			}
		} catch (Exception e) {
			Log.w(TAG, "Error in ContextEvent constructor: " + e);
		}
		useStreaming = true;
	}

	/**
	 * Returns the IStreamController, if set.
	 */
	public IStreamController getStreamController() {
		return this.streamController;
	}

	/**
	 * Writes the event out to IPC.
	 */
	public void writeToParcel(Parcel out, int flags) {
		if (DEBUG)
			Log.d(TAG, "Writing ContextEvent as Parcel");
		// Write out basic event data
		try {
			out.writeByte((byte) (useStreaming ? 1 : 0));
			out.writeString(this.responseId);
			out.writeParcelable(eventSource, flags);
			out.writeSerializable(super.getTimeStamp());
			out.writeInt(super.getExpireMills());
			out.writeString(contextType);
			// Write the total number of string-based formats we're holding
			out.writeInt(contextRepresentationStrings.size());
			if (useStreaming) {
				streamController.start();
				// Stream out each string-based format...
				for (final String s : contextRepresentationStringsBytes.keySet()) {
					if (DEBUG)
						Log.v(TAG, "Writing format: " + s);
					out.writeString(s);
					final byte[] bytes = contextRepresentationStringsBytes.get(s);
					if (DEBUG)
						Log.v(TAG, "Streaming format content with byte length: " + bytes.length);
					out.writeStrongBinder(new IDataInputStream.Stub() {
						private final ByteArrayInputStream in = new ByteArrayInputStream(bytes);

						public int read(byte[] buffer) throws RemoteException {
							try {
								if (streamController.outOfMemory()) {
									Log.e(TAG, "Closing stream and throwing RemoteException!");
									in.close();
									throw new Exception(
											"ContextEvent contained too much data. Try reducing query scope.");
								}
								return in.read(buffer);
							} catch (Exception e) {
								throw new RemoteException();
							}
						}
					});
				}
			} else {
				// Don't use streaming
				for (String format : contextRepresentationStrings.keySet()) {
					out.writeString(format);
					out.writeString(contextRepresentationStrings.get(format));
				}
			}
			// Stream out the IContextInfo, if the attachment is requested...
			out.writeByte((byte) (attachContextInfo ? 1 : 0));
			if (attachContextInfo) {
				if (useStreaming) {
					if (streamController.outOfMemory()) {
						throw new Exception("ContextEvent contained too much data. Try reducing query scope.");
					}
					out.writeStrongBinder(new IDataInputStream.Stub() {
						private final ByteArrayInputStream in = new ByteArrayInputStream(contextInfoBytes);

						public int read(byte[] buffer) throws RemoteException {
							try {
								if (streamController.outOfMemory()) {
									Log.e(TAG, "Closing stream and throwing RemoteException!");
									in.close();
									throw new Exception(
											"ContextEvent contained too much data. Try reducing query scope.");
								} else
									return in.read(buffer);
							} catch (Exception e) {
								throw new RemoteException();
							}
						}
					});
				} else {
					out.writeParcelable(contextInfo, flags);
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Error serializing ContextEvent: " + e);
		} finally {
			if (streamController != null)
				streamController.stop();
		}
	}

	/**
	 * A streaming-capable version of 'readParcelable'.
	 */
	private ContextEvent(Parcel in) throws Exception {
		try {
			this.useStreaming = in.readByte() == 1;
			this.responseId = in.readString();
			this.eventSource = in.readParcelable(getClass().getClassLoader());
			super.setTimeStamp((Date) in.readSerializable());
			this.setExpireMills(in.readInt());
			this.contextType = in.readString();
			// Convert the byte stream back into string-based context representations
			contextRepresentationStrings = new HashMap<String, String>();
			// Read the number of formats present
			int totalFormats = in.readInt();
			// Read in each format
			if (useStreaming) {
				for (int i = 0; i < totalFormats; i++) {
					String format = in.readString();
					if (DEBUG)
						Log.v(TAG, "Receiving streamed string-based format: " + format);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					IDataInputStream dataStream = IDataInputStream.Stub.asInterface(in.readStrongBinder());
					byte[] buffer = new byte[8192];
					int size;
					try {
						while ((size = dataStream.read(buffer)) != -1) {
							out.write(buffer, 0, size);
						}
					} catch (Exception e) {
						throw new Exception("Problem reading context representation strings: " + e);
					}
					if (DEBUG)
						Log.v(TAG, "Received " + format + ", which has a compressed size of: "
								+ out.toByteArray().length);
					contextRepresentationStrings.put(format, Utils.unZipString(out.toByteArray()));
				}
			} else {
				for (int i = 0; i < totalFormats; i++) {
					contextRepresentationStrings.put(in.readString(), in.readString());
				}
			}
			// Check if we've got an IContextInfo attachement
			this.attachContextInfo = in.readByte() == 1;
			if (attachContextInfo) {
				if (useStreaming) {
					// Convert the byte stream map back into a IContextInfo representation
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					IDataInputStream data = IDataInputStream.Stub.asInterface(in.readStrongBinder());
					byte[] buffer = new byte[8192];
					int read;
					try {
						while ((read = data.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						// Store the stream's raw bytes
						byte[] contextInfoBytes = out.toByteArray();
						// Read and return the IContextInfo object using the class' classloader
						try {
							Parcel p = Parcel.obtain();
							p.unmarshall(contextInfoBytes, 0, contextInfoBytes.length);
							/*
							 * Unmarshall the byte array using a Parcel, and make sure to set the Parcel's read position
							 * to 0, since the read position will be at the end of the object.
							 */
							p.setDataPosition(0);
							this.contextInfo = p.readParcelable(getClass().getClassLoader());
							p.recycle();
						} catch (Exception e) {
							throw new Exception("Could not extract IContextInfo... necessary classes are not available");
						}
					} catch (Exception e) {
						throw new Exception("Exception during readContextInfoStream: " + e);
					} finally {
						// Close the output stream
						try {
							out.close();
						} catch (IOException e) {
						}
					}
				} else {
					this.contextInfo = in.readParcelable(getClass().getClassLoader());
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * If 'attachContextInfo' is true, the embedded IContextInfo object will be serialized during the 'writeToParcel'
	 * method, which is used to send the object to a remote receiver (in another process) using Android IPC. If
	 * 'attachContextInfo' is false, the IContextInfo object will be not serialized during 'writeToParcel'. This is
	 * useful because not all remote clients will have the proper IContextInfo implementation classes on their
	 * class-path. By switching off attachments, a string-only version of the event is sent, which is guaranteed to
	 * succeed.
	 * 
	 * @param attachContextInfo
	 *            True if the attachment should be attached; false otherwise.
	 */
	public void attachContextInfo(boolean attachContextInfo) {
		this.attachContextInfo = attachContextInfo;
	}

	/**
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Returns the type of the context information represented by the event. This string must match one of the supported
	 * context information type strings described by the source ContextPlugin.
	 */
	public String getContextType() {
		return this.contextType;
	}

	/**
	 * Returns the ContextPluginInformation for the plug-in that generated the event
	 */
	public ContextPluginInformation getEventSource() {
		return this.eventSource;
	}

	/**
	 * Returns the event's IContextInfo (if present).
	 */
	public IContextInfo getIContextInfo() {
		return contextInfo;
	}

	/**
	 * Sets the event's IContextInfo.
	 */
	public void setIContextInfo(IContextInfo contextInfo) {
		this.contextInfo = contextInfo;
	}

	/**
	 * Gets the responseId (for unicast events).
	 */
	public String getResponseId() {
		return responseId;
	}

	/**
	 * Returns a string-based representation of the event's IContextInfo for the specified format string (e.g.
	 * "application/json") or null if the requested format is not supported.
	 */
	public String getStringRepresentation(String format) {
		if (contextRepresentationStrings.containsKey(format)) {
			return contextRepresentationStrings.get(format);
		} else
			return null;
	}

	/**
	 * Returns a Set of supported string-based context representation format types or null if no representation formats
	 * are supported. Examples formats could include MIME, Dublin Core, RDF, etc. See the plug-in documentation for
	 * supported representation types.
	 */
	public Set<String> getStringRepresentationFormats() {
		return this.contextRepresentationStrings.keySet();
	}

	/**
	 * Gets the target app id (for unicast events).
	 */
	public int getTargetAppId() {
		return targetAppId;
	}

	/**
	 * Returns true if this event has IContextInfo; false otherwise.
	 */
	public boolean hasIContextInfo() {
		return getIContextInfo() != null ? true : false;
	}

	/**
	 * Sets the ContextPluginInformation event source.
	 */
	public void setEventSource(ContextPluginInformation eventSource) {
		this.eventSource = eventSource;
	}

	/**
	 * Sets the responseId (for unicast events).
	 */
	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	/**
	 * Sets the target app id (for unicast events).
	 */
	public void setTargetAppId(int targetAppId) {
		this.targetAppId = targetAppId;
	}

	/**
	 * Returns true if the IContextData should be auto-encoded; false otherwise.
	 */
	public boolean autoWebEncode() {
		return autoWebEncode;
	}

	/**
	 * Returns the requested auto Web encoding format for the event.
	 */
	public String getWebEncodingFormat() {
		return this.webEncodingFormat;
	}

	/**
	 * Sets the event up for auto web encoding by Dynamix using JSON. The underlying IContextInfo object will be encoded
	 * using JavaBean conventions. Note that IContextInfo objects MUST adhere to JavaBean conventions for
	 * auto-web-encoding to succeed: http://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions
	 */
	public void setAutoWebEncode() {
		this.autoWebEncode = true;
		this.webEncodingFormat = PluginConstants.JSON_WEB_ENCODING;
	}

	/**
	 * Sets the event up for auto web encoding by Dynamix using the specified webEncodingFormat. Dynamix will NOT
	 * auto-web-encode the IContextData in this case. Rather, the IContextInfo's string-based representation format
	 * matching the specified webEncodingFormat will be used directly. If the IContextInfo doesn't provide the specified
	 * webEncodingFormat, no data will be web encoded. See IContextInfo.getStringRepresentation(String format);
	 */
	public void setManualWebEncode(String webEncodingFormat) {
		this.autoWebEncode = false;
		this.webEncodingFormat = webEncodingFormat;
	}

	/**
	 * Sets the event up so that no Web encoding will occur. In this case, the event cannot be used by Web clients.
	 */
	public void setNoWebEncoding() {
		this.autoWebEncode = false;
		this.webEncodingFormat = PluginConstants.NO_WEB_ENCODING;
	}
}