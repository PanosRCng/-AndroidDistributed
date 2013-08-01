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
package org.ambientdynamix.api.contextplugin.security;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Secured wrapper for the Android SpeechRecognizer. Needed since the SpeechRecognizer can only run on the app's main
 * thread, and Dynamix context plug-ins are each running on their own threads. This class provides multi-threaded event
 * dispatching for SpeechRecognizer events, plus managed exception handling.
 * 
 * @author Darren Carlson
 * 
 */
public class SecuredSpeechRecognizer{
/*	

		handler.post(new Runnable() {
			public void run() {
				try {
					if (SpeechRecognizer.isRecognitionAvailable(c)) {
						sr = SpeechRecognizer.createSpeechRecognizer(c);

						sr.setRecognitionListener(SecuredSpeechRecognizer.this);
					}
				} catch (Exception e) {
					Log.w(TAG, "Exception creating SpeechRecognizer: " + e.toString());
				} finally {
					init = true;
				}
			}
		});
		// Wait for the SpeechRecognizer to be initialized
		while (!init) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		// If we failed to create a SpeechRecognizer, throw an exception
		if (sr == null)
			throw new Exception("Could not create SpeechRecognizer");
	}


	public void setRecognitionListener(RecognitionListener listener) {
		this.listener = listener;
	}


	public void cancel() {
		handler.post(new Runnable() {
			public void run() {
				try {
					sr.cancel();
				} catch (Exception e) {
					Log.w(TAG, "Exception in cancel: " + e.toString());
				}
			}
		});
	}


	public void destroy() {
		handler.post(new Runnable() {
			public void run() {
				try {
					sr.destroy();
				} catch (Exception e) {
					Log.w(TAG, "Exception in destroy: " + e.toString());
				}
			}
		});
	}


	public void startListening(final Intent recognizerIntent) {
		handler.post(new Runnable() {
			public void run() {
				try {
					sr.startListening(recognizerIntent);
				} catch (Exception e) {
					Log.w(TAG, "Exception in startListening: " + e.toString());
				}
			}
		});
	}

	public void stopListening() {
		handler.post(new Runnable() {
			public void run() {
				try {
					sr.stopListening();
				} catch (Exception e) {
					Log.w(TAG, "Exception in stopListening: " + e.toString());
				}
			}
		});
	}

	@Override
	public void onBeginningOfSpeech() {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onBeginningOfSpeech();
			}
		});
	}

	@Override
	public void onBufferReceived(final byte[] buffer) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onBufferReceived(buffer);
			}
		});
	}

	@Override
	public void onEndOfSpeech() {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onEndOfSpeech();
			}
		});
	}

	@Override
	public void onError(final int error) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onError(error);
			}
		});
	}

	@Override
	public void onEvent(final int eventType, final Bundle params) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onEvent(eventType, params);
			}
		});
	}

	@Override
	public void onPartialResults(final Bundle partialResults) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onPartialResults(partialResults);
			}
		});
	}

	@Override
	public void onReadyForSpeech(final Bundle params) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onReadyForSpeech(params);
			}
		});
	}

	@Override
	public void onResults(final Bundle results) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onResults(results);
			}
		});
	}

	@Override
	public void onRmsChanged(final float rmsdB) {
		dispatch(new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onRmsChanged(rmsdB);
			}
		});
	}

	private void dispatch(final Runnable r) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					r.run();
				} catch (Exception e) {
					Log.w(TAG, "Exception during call: " + e);
				}
			}
		});
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.w(TAG, "Uncaught exception during call: " + ex);
			}
		});
		t.setDaemon(true);
		t.start();
	}
*/
}
