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
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

/**
 * Utilities for the Dynamix Application API.
 * 
 * @author Darren Carlson
 */
public class Utils {
	private final static String TAG = Utils.class.getSimpleName();

	// Singleton constructor
	private Utils() {
	}
	


	/**
	 * Creates a delimited string based from the incoming Iterable.
	 * @param s The String.
	 * @param delimiter The delimiter.
	 * @return The delimited string.
	 */
	public static String join(Iterable<? extends CharSequence> s, String delimiter) {
	    Iterator<? extends CharSequence> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
	    return buffer.toString();
	}

	/**
	 * Unzips the incoming raw bytes (in-memory). Returns the uncompressed bytes.
	 */
	public static byte[] unzipRaw(byte[] gzipBuff) throws Exception {
		Log.v(TAG, "Unzipping raw buffer compressed size:: " + gzipBuff.length);
		int size = 0;
		ByteArrayInputStream memstream = new ByteArrayInputStream(gzipBuff);
		GZIPInputStream gzin = new GZIPInputStream(memstream);
		final int buffSize = 8192;
		byte[] tempBuffer = new byte[buffSize];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer;
		try {
			while ((size = gzin.read(tempBuffer, 0, buffSize)) != -1) {
				baos.write(tempBuffer, 0, size);
			}
			buffer = baos.toByteArray();
			baos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
		Log.v(TAG, "Unzipped buffer decompressed size: " + buffer.length);
		return buffer;
	}

	/**
	 * Unzips the incoming buffer (in-memory) using UTF-8 byte encoding. Returns the decoded String.
	 * 
	 * @throws IOException
	 */
	public static String unZipString(byte[] gzipBuff) throws Exception {
		Log.v(TAG, "Unzipping compressed text byte array with size: " + gzipBuff.length);
		int size = 0;
		ByteArrayInputStream memstream = new ByteArrayInputStream(gzipBuff);
		GZIPInputStream gzin = new GZIPInputStream(memstream);
		final int buffSize = 8192;
		byte[] tempBuffer = new byte[buffSize];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String s;
		try {
			while ((size = gzin.read(tempBuffer, 0, buffSize)) != -1) {
				baos.write(tempBuffer, 0, size);
			}
			byte[] buffer = baos.toByteArray();
			baos.close();
			s = new String(buffer, "UTF-8");
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
		Log.v(TAG, "Unzipping text has size: " + s.getBytes().length);
		return s;
	}

	/**
	 * Zips the incoming bytes (in-memory) without any encoding (raw) using GZip and Deflater.BEST_SPEED.
	 */
	public static byte[] zipRaw(byte[] gzipBuff) throws Exception {
		return zipRaw(gzipBuff, Deflater.BEST_SPEED);
	}

	/**
	 * Zips the incoming bytes (in-memory) without any encoding (raw) using GZip. Returns the compressed bytes. The
	 * compression level must be a value between 0 and 9 (e.g., "Deflater.BEST_SPEED")
	 */
	public static byte[] zipRaw(byte[] gzipBuff, final int compressionLevel) throws Exception {
		Log.v(TAG, "GZipping bytes of size: " + gzipBuff.length);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] bArray = null;
		GZIPOutputStream gz = null;
		try {
			gz = new GZIPOutputStream(bos) {
				{
					def.setLevel(compressionLevel);
				}
			};
			gz.write(gzipBuff, 0, gzipBuff.length);
			gz.finish();
			gz.close();
			bos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				gz.close();
			} catch (Exception e) {
			}
			try {
				bos.close();
			} catch (Exception e) {
			}
		}
		bArray = bos.toByteArray();
		Log.v(TAG, "Final GZipped size: " + bArray.length);
		return bArray;
	}

	/**
	 * GZips the incoming String (in-memory) using UTF-8 byte encoding and Deflater.BEST_SPEED.
	 */
	public static byte[] zipString(String text) throws Exception {
		return zipString(text, Deflater.BEST_SPEED);
	}

	/**
	 * GZips the incoming String (in-memory) using UTF-8 byte encoding. Returns the resulting compressed bytes. The
	 * compression level must be a value between 0 and 9 (e.g., "Deflater.BEST_SPEED")
	 */
	public static byte[] zipString(String text, final int compressionLevel) throws Exception {
		Log.v(TAG, "GZipping text of size: " + text.getBytes().length);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] bArray = null;
		GZIPOutputStream gz = null;
		try {
			byte b[] = text.getBytes("UTF-8");
			gz = new GZIPOutputStream(bos) {
				{
					def.setLevel(compressionLevel);
				}
			};
			gz.write(b, 0, b.length);
			gz.finish();
			gz.close();
			bos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				gz.close();
			} catch (IOException e) {
			}
			try {
				bos.close();
			} catch (IOException e) {
			}
		}
		bArray = bos.toByteArray();
		Log.v(TAG, "Final GZipped size: " + bArray.length);
		return bArray;
	}
}
