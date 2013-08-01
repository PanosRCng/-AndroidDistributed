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
package org.ambientdynamix.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.ambientdynamix.api.application.ErrorCodes;
import org.ambientdynamix.data.DynamixPreferences;
import org.ambientdynamix.security.TrustedCert;
import org.ambientdynamix.util.Utils;
import org.ambientdynamix.web.NanoHTTPD;
import org.ambientdynamix.web.RESTHandler;
import org.ambientdynamix.web.WebListenerManager;
import org.ambientdynamix.web.WebUtils;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.RemoteException;
import android.util.Log;

/**
 * Local web server implementation for handling Dynamix web client requests.
 * 
 * @author Darren Carlson
 * 
 */
public class WebConnector extends NanoHTTPD {
	// Private data
	private static WebConnector server;
	private static Map<String, WebListenerManager<String>> listeners = new HashMap<String, WebListenerManager<String>>();
	static final String TAG = WebConnector.class.getSimpleName();
	private static RESTHandler restHandler;
	private static WebFacadeBinder facade;
	private static List<TrustedCert> authorizedCerts = new ArrayList<TrustedCert>();

	/**
	 * Singleton constructor.
	 */
	private WebConnector(int port, File wwwroot) throws IOException {
		super(port, wwwroot);
	}

	/**
	 * Starts the WebConnector and related services.
	 * 
	 * @param port
	 *            The port the server should use.
	 * @param checkPeriodMills
	 *            The period (in milliseconds) for determining web client timeouts.
	 * @param timeoutMills
	 *            The time (in milliseconds) that a web client must interact with the WebConnector before the web client
	 *            times out.
	 * @throws IOException
	 */
	protected synchronized static void startServer(WebFacadeBinder facade, int port, int checkPeriodMills,
			int timeoutMills) throws IOException {
		if (server == null) {
			WebConnector.facade = facade;
			server = new WebConnector(port, null);
			ListenerMonitor.start(checkPeriodMills, timeoutMills);
			restHandler = new RESTHandler(facade);
		}
	}

	/**
	 * Starts the WebConnector and related services.
	 * 
	 * @param port
	 *            The port the server should use.
	 * @param checkPeriodMills
	 *            The period (in milliseconds) for determining web client timeouts.
	 * @param timeoutMills
	 *            The time (in milliseconds) that a web client must interact with the WebConnector before the web client
	 *            times out.
	 * @param authCerts
	 *            A list of authorized X509Certificates for validating web calls.
	 * @throws IOException
	 */
	protected synchronized static void startServer(WebFacadeBinder facade, int port, int checkPeriodMills,
			int timeoutMills, List<TrustedCert> authCerts) throws IOException {
		if (server == null) {
			WebConnector.facade = facade;
			server = new WebConnector(port, null);
			ListenerMonitor.start(checkPeriodMills, timeoutMills);
			restHandler = new RESTHandler(facade);
			setAuthorizedCerts(authCerts);
		}
	}

	/**
	 * Stops the WebConnector and related services. Clears all authorized certificates.
	 */
	protected synchronized static void stopServer() {
		if (server != null) {
			server.stop();
			server = null;
			ListenerMonitor.stop();
			restHandler = null;
			// TODO Should we close sessions for connected listeners here?
			synchronized (listeners) {
				for (WebListenerManager<String> m : listeners.values()) {
					try {
						facade.removeDynamixListener(m.getListener());
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
				listeners.clear();
			}
			clearAuthorizedCerts();
		}
	}

	/**
	 * Returns true if the WebConnector is started; false otherwise.
	 */
	protected synchronized static boolean isStarted() {
		return server != null;
	}

	/**
	 * Sets the time period (in milliseconds) between checks for web client timeouts.
	 */
	protected static void setWebClientTimeoutCheckPeriod(int checkPeriodMills) {
		ListenerMonitor.setCheckPeriod(checkPeriodMills);
	}

	/**
	 * Sets the web client timeout duration (in milliseconds).
	 */
	protected static void setWebClientTimeoutMills(int timeoutMills) {
		ListenerMonitor.setTimeoutMills(timeoutMills);
	}

	/**
	 * Pauses timeout checking for web clients.
	 */
	protected static synchronized void pauseTimeoutChecking() {
		if (server != null) {
			ListenerMonitor.pause();
		} else
			Log.v(TAG, "Not started... ignoring pause request");
	}

	/**
	 * Resumes timeout checking for web clients.
	 */
	protected static synchronized void resumeTimeoutChecking() {
		if (server != null) {
			ListenerMonitor.resume();
		} else
			Log.v(TAG, "Not started... ignoring resume request");
	}

	/**
	 * Returns the list of authorized certificates, which are used to validate web calls.
	 */
	protected static List<TrustedCert> getAuthorizedCerts() {
		return authorizedCerts;
	}

	/**
	 * Adds an authorized certificate, which is used to validate web calls.
	 */
	protected static void addAuthorizedCert(TrustedCert authorizedCert) {
		synchronized (authorizedCerts) {
			if (!authorizedCerts.contains(authorizedCert))
				authorizedCerts.add(authorizedCert);
		}
	}

	/**
	 * Adds a list of authorized certificates, which are used to validate web calls.
	 */
	protected static void setAuthorizedCerts(List<TrustedCert> authCerts) {
		if (authCerts != null)
			synchronized (authorizedCerts) {
				for (TrustedCert cert : authCerts) {
					if (!authorizedCerts.contains(cert))
						authorizedCerts.add(cert);
				}
			}
	}

	/**
	 * Removes an authorized certificate, which is used to validate web calls.
	 */
	protected static void removeAuthorizedCert(TrustedCert authorizedCert) {
		synchronized (authorizedCerts) {
			boolean success = authorizedCerts.remove(authorizedCert);
			Log.i(TAG, "Removing cert " + authorizedCert.getAlias() + " result " + success);
		}
	}

	/**
	 * Clears the list of authorized certificates, which are used to validate web calls.
	 */
	protected static void clearAuthorizedCerts() {
		synchronized (authorizedCerts) {
			authorizedCerts.clear();
		}
	}

	/**
	 * Adds the String command to the WebListenerManager identified by the token.
	 * 
	 * @param token
	 *            The listener's token.
	 * @param command
	 *            The command to send.
	 */
	protected void sendEvent(String token, String command) {
		// Log.i(TAG, "Queue command for " + token);
		synchronized (listeners) {
			WebListenerManager<String> m = listeners.get(token);
			if (m != null)
				m.add(URLEncoder.encode(command));
		}
	}

	/**
	 * Handles web server processing for commands sent by Dynanix web clients. This method is called by NanoHTTPD for
	 * each client call. Each call to this method runs on its own thread, so it's ok to block.
	 */
	protected Response serve(Socket socket, String uri, String method, Properties header, Properties parms,
			Properties files) {
		// Create and configure the response
		Response r = new Response();
		r.header.setProperty("Access-Control-Allow-Origin", "*");
		r.header.setProperty("Cache-Control", "no-cache");
		r.header.setProperty("Pragma", "no-cache");
		r.header.setProperty("Expires", "0");
		r.mimeType = MIME_PLAINTEXT;
		/*
		 * Handle security for requests. Requests are only allowed from the loopback address and verified apps, which
		 * are identified using their X509 certs.
		 */
		// Verify that the request is local
		if (socket.getInetAddress().isLoopbackAddress()) {
			// Get requesting app information using the socket
			RunningAppProcessInfo app = WebUtils.getAppProcessForSocket(socket);
			if (app != null) {
				if (DynamixPreferences.collectCerts(DynamixService.getAndroidContext()))
					Log.i(TAG, "Web call from: " + app.processName + " with UID " + app.uid);
				// Log.i(TAG, "Total Certs is: " + authorizedCerts.size());
				/*
				 * Verify that the requesting app has a valid certificate. According to the Android docs, multiple
				 * versions of an app (e.g., upgrades) should be signed by the same cert. There will typically be one
				 * packageName for an app. See http://developer.android.com/tools/publishing/app-signing.html
				 */
				for (String packageName : app.pkgList) {
					// Get the cert for the app's packageName
					X509Certificate cert = WebUtils.getCertForApp(packageName);
					try {
						// Ensure it's valid for this time period
						cert.checkValidity(new Date());
						// Check against the list of authorized certs
						for (TrustedCert authorized : authorizedCerts) {
							// Try to verify the app's cert
							try {
								authorized.getCert().verify(cert.getPublicKey());
								// Complete serve request
								return doAuthorizedServe(r, uri, method, header, parms, files);
							} catch (InvalidKeyException e) {
								
							} catch (CertificateException e) {
								
							} catch (NoSuchAlgorithmException e) {
								
							} catch (NoSuchProviderException e) {
								
							} catch (SignatureException e) {
								
							}
							// Log.i(TAG, cert.getSerialNumber() + " is VALID using " + authorized.getSerialNumber()
							// + " from " + authorized.getSubjectDN());
							
							
						}
						// If we reach this point, no authorized cert could be found for the app
						Log.w(TAG, "No certificate found for " + app);
						/*
						 * TODO: Set to true to store incoming cert to file - bypasses security. Used to obtain certs
						 * from authorized apps. Remove this for production code.
						 */
						if (DynamixPreferences.collectCerts(DynamixService.getAndroidContext())) {
							try {
								Log.w(TAG, "Auto-authorizing cert for " + app);
								DynamixService.storeAuthorizedCert(packageName, cert);
							} catch (Exception e) {
								Log.w(TAG, e);
							}
							// File certFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
							// "/"
							// + packageName + ".crt");
							// WebUtils.exportCertificate(cert, certFile, true);
							// return doAuthorizedServe(r, uri, method, header, parms, files);
						}
						r.status = NanoHTTPD.HTTP_FORBIDDEN;
						r.setText(ErrorCodes.NOT_AUTHORIZED + ",No certificate found");
					} catch (CertificateExpiredException e1) {
						Log.w(TAG, "Cert expired for " + app);
						r.status = NanoHTTPD.HTTP_FORBIDDEN;
						r.setText(ErrorCodes.NOT_AUTHORIZED + ",Expired certificate");
						return r;
					} catch (CertificateNotYetValidException e1) {
						Log.w(TAG, "Cert not yet valid for " + app);
						r.status = NanoHTTPD.HTTP_FORBIDDEN;
						r.setText(ErrorCodes.NOT_AUTHORIZED + ",Certificate not yet valid");
						return r;
					}
				}
			} else {
				Log.w(TAG, "App not found");
				r.status = NanoHTTPD.HTTP_FORBIDDEN;
				r.setText(ErrorCodes.NOT_AUTHORIZED + ",App not found");
			}
		} else {
			Log.w(TAG, "Non-localhost request");
			r.status = NanoHTTPD.HTTP_FORBIDDEN;
			r.setText(ErrorCodes.NOT_AUTHORIZED + ",Non-localhost request");
		}
		return r;
	}

	/*
	 * Performs Dynamix handling for requests that have been authorized by the 'serve' method.
	 */
	private Response doAuthorizedServe(Response r, String uri, String method, Properties header, Properties parms,
			Properties files) {
		// Access basic request data
		String origin = header.getProperty("origin");
		// Validate the origin
		if (origin.equalsIgnoreCase("null") || (origin != null && origin.length() > 0)) {
			// Check if we're http (if we are, there is no referer)
			boolean https = origin.toLowerCase().startsWith("https");
			String referer = null;
			if (!https)
				referer = header.getProperty("referer");
			if (DynamixPreferences.collectCerts(DynamixService.getAndroidContext()))
				Log.i(TAG, "Request origin " + origin + " and referrer " + referer);
			// Validate referer, if necessary
			if (https || referer != null && referer.length() > 0) {
				// Make sure the origin is contained within the referer (if not https)
				if (https || referer.toLowerCase().contains(origin.toLowerCase())) {
					// Try to create the web client's id
					String id = null;
					try {
						if (https)
							id = Utils.getIdentificationUrl(origin, true);
						else
							id = Utils.getIdentificationUrl(referer, false);
					} catch (MalformedURLException e1) {
						Log.w(TAG, "Error getting id for " + referer + " | " + e1);
						r.status = HTTP_INTERNALERROR;
						r.mimeType = MIME_PLAINTEXT;
						r.setText(Integer.toString(ErrorCodes.DYNAMIX_FRAMEWORK_ERROR) + "Error getting id for "
								+ referer + " | " + e1);
						// We return here to stop further processing
						return r;
					}
					// Handle DYNAMIX_BIND
					if (uri.equalsIgnoreCase(RESTHandler.DYNAMIX_BIND)) {
						// Create new token for caller
						String token = UUID.randomUUID().toString();
						synchronized (listeners) {
							// Add listener, if necessary
							if (!listeners.containsKey(token)) {
								addListener(token, id);
							} else {
								Log.d(TAG, "Already registered listener with token: " + token);
							}
						}
						// Return the new listener token
						r.status = NanoHTTPD.HTTP_OK;
						r.setText(token);
					}
					// Handle DYNAMIX_UNBIND
					else if (uri.equalsIgnoreCase(RESTHandler.DYNAMIX_UNBIND)) {
						// Check if the incoming token is a known listener
						String token = parms.getProperty("token");
						Log.d(TAG, "Processing unbind for: " + token);
						// Access the WebListenerManager
						WebListenerManager wlMgr = listeners.get(token);
						if (wlMgr != null) {
							// Ping the manager to keep it alive for the moment
							wlMgr.ping();
							// Remove listener, if possible
							try {
								wlMgr.getListener().onDynamixUnbind();
								facade.removeDynamixListener(wlMgr.getListener());
							} catch (RemoteException e) {
								Log.w(TAG, "DYNAMIX_UNBIND exception: " + e.toString());
							}
							// Set the manager to dead so it will be removed
							wlMgr.setDead(true);
							// Return success
							r.status = NanoHTTPD.HTTP_OK;
						} else {
							Log.d(TAG, "Token invalid: " + token);
							// Return failure
							r.status = NanoHTTPD.HTTP_FORBIDDEN;
						}
					}
					// Handle IS_DYNAMIX_TOKEN_VALID
					else if (uri.equalsIgnoreCase(RESTHandler.IS_DYNAMIX_TOKEN_VALID)) {
						// Check if the incoming token is a known listener
						String token = parms.getProperty("token");
						Log.d(TAG, "Checking token: " + token);
						synchronized (listeners) {
							// Access the WebListenerManager
							WebListenerManager wlMgr = listeners.get(token);
							if (wlMgr != null) {
								Log.d(TAG, "Token valid: " + token);
								// Ping the manager to keep it alive
								wlMgr.ping();
								// Return success
								r.status = NanoHTTPD.HTTP_OK;
								r.setText("true");
							} else {
								Log.d(TAG, "Token invalid: " + token);
								// Return failure
								r.status = NanoHTTPD.HTTP_FORBIDDEN;
							}
						}
					}
					// Handle IS_DYNAMIX_SESSION_OPEN
					else if (uri.equalsIgnoreCase(RESTHandler.IS_DYNAMIX_SESSION_OPEN)) {
						// Check if the incoming token is a known listener
						String token = parms.getProperty("token");
						Log.d(TAG, "Checking if listener is bound to token: " + token);
						synchronized (listeners) {
							// Access the WebListenerManager
							WebListenerManager wlMgr = listeners.get(token);
							if (wlMgr != null) {
								// Ping the manager to keep it alive
								wlMgr.ping();
								// Access session open state
								if (wlMgr.getListener().isSessionOpen())
									r.setText("true");
								else
									r.setText("false");
								r.status = NanoHTTPD.HTTP_OK;
							} else {
								Log.d(TAG, "Token invalid: " + token);
								// Return failure
								r.status = NanoHTTPD.HTTP_FORBIDDEN;
							}
						}
					}
					// Otherwise, handle Dynamix REST API call
					else {
						// Validate incoming token
						String token = parms.getProperty("token");
						try {
							// Make sure the incoming token is a valid UUID
							UUID test = UUID.fromString(token);
						} catch (Exception e1) {
							// Error: Invalid Token Format
							Log.w(TAG, "Invalid Token Format: " + token);
							r.status = HTTP_BADREQUEST;
							r.mimeType = MIME_PLAINTEXT;
							r.setText(Integer.toString(ErrorCodes.MISSING_PARAMETERS) + ",Invalid Token: " + token);
						}
						// Verify that the incoming token is authorized
						if (listeners.containsKey(token)) {
							// Access the WebListenerManager for the request
							WebListenerManager<String> wlMgr = null;
							synchronized (listeners) {
								// Access the WebListenerManager
								wlMgr = listeners.get(token);
							}
							// Ping the WebListenerManager
							wlMgr.ping();
							// Use the RESTHandler to process the request
							try {
								restHandler.processRequest(r, wlMgr, uri, method, header, parms);
							} catch (Exception e) {
								// Error: Dynamix could not handle the request
								Log.w(TAG, "REST Processor Error: " + e.toString());
								r.status = HTTP_INTERNALERROR;
								r.mimeType = MIME_PLAINTEXT;
								r.setText(Integer.toString(ErrorCodes.DYNAMIX_FRAMEWORK_ERROR)
										+ ",REST Processor Error: " + e.toString());
							}
						} else {
							// Error: Token Not Authorized
							Log.w(TAG, "Token Not Authorized: " + token);
							r.status = HTTP_FORBIDDEN;
							r.mimeType = MIME_PLAINTEXT;
							r.setText(Integer.toString(ErrorCodes.NOT_AUTHORIZED) + ",Token Not Authorized: " + token);
						}
					}
				} else {
					// Error: Referer not part of Origin
					Log.w(TAG, "Referer not part of Origin");
					r.status = HTTP_BADREQUEST;
					r.mimeType = MIME_PLAINTEXT;
					r.setText(Integer.toString(ErrorCodes.MISSING_PARAMETERS) + ",Referer not part of Origin");
				}
			} else {
				// Error: Referer Required
				Log.w(TAG, "Referer Required");
				r.status = HTTP_BADREQUEST;
				r.mimeType = MIME_PLAINTEXT;
				r.setText(Integer.toString(ErrorCodes.MISSING_PARAMETERS) + ",Referer Required");
			}
		} else {
			// Error: Origin Required
			Log.w(TAG, "Origin Required");
			r.status = HTTP_BADREQUEST;
			r.mimeType = MIME_PLAINTEXT;
			r.setText(Integer.toString(ErrorCodes.MISSING_PARAMETERS) + ",Origin Required");
		}
		// Return the response to NanoHTTPD, which sends the response to the requesting client.
		return r;
	}

	/**
	 * Adds the listener, which is identified by both a security token and the web app's url.
	 * 
	 * @param token
	 *            The token of the listener.
	 * @param webAppUrl
	 *            The web app's url, which must conform to the Dynamix web app naming conventions.
	 */
	protected void addListener(String token, String webAppUrl) {
		synchronized (listeners) {
			if (!listeners.keySet().contains(token)) {
				WebListenerManager<String> q = new WebListenerManager<String>(new WebListener(webAppUrl, token, this));
				listeners.put(token, q);
				Log.i(TAG, "Added WebListener: " + webAppUrl + " with token " + token);
			} else
				Log.w(TAG, "WebListener already added: " + webAppUrl + " with token " + token);
		}
	}

	/**
	 * Returns true if the listener's token is registered; false otherwise.
	 */
	protected boolean containsListener(String token) {
		synchronized (listeners) {
			return listeners.containsKey(token);
		}
	}

	/**
	 * Returns the listener manager associated with the token.
	 */
	protected WebListenerManager getListener(String token) {
		synchronized (listeners) {
			return listeners.get(token);
		}
	}

	/**
	 * Monitor class that removes dead listeners as needed.
	 * 
	 * @author Darren Carlson
	 * 
	 */
	private static class ListenerMonitor {
		private static int checkPeriod = 5000;
		private static int timeoutMills = 15000;
		private static boolean done = true;
		private static boolean paused = false;
		private static Thread t = null;

		/**
		 * Stops the ListenerMonitor
		 */
		public synchronized static void stop() {
			done = true;
			paused = false;
		}

		/**
		 * Sets the check period (in milliseconds).
		 */
		public synchronized static void setCheckPeriod(int checkPeriodMills) {
			if (checkPeriod > 0)
				ListenerMonitor.checkPeriod = checkPeriodMills;
			else
				ListenerMonitor.checkPeriod = 5000;
		}

		/**
		 * Pauses timeout checking for web clients.
		 */
		public synchronized static void pause() {
			if (!done) {
				if (!paused) {
					Log.d(TAG, "Pausing timeout checking for web clients");
					paused = true;
				} else
					Log.w(TAG, "Already paused");
			} else
				Log.w(TAG, "Not started");
		}

		/**
		 * Resumes timeout checking for web clients.
		 */
		public synchronized static void resume() {
			if (paused) {
				// Ping all the listeners, since we've been paused
				synchronized (listeners) {
					for (WebListenerManager<String> wm : listeners.values())
						wm.ping();
				}
				Log.d(TAG, "Resuming timeout checking for web clients");
				paused = false;
			} else
				Log.w(TAG, "Not paused");
		}

		/**
		 * Sets the timeout period (in milliseconds).
		 * 
		 * @param timeoutMills
		 */
		public synchronized static void setTimeoutMills(int timeoutMills) {
			if (timeoutMills > 0)
				ListenerMonitor.timeoutMills = timeoutMills;
			else
				ListenerMonitor.timeoutMills = 15000;
		}

		/**
		 * Starts monitoring web clients for timeouts.
		 * 
		 * @param checkPeriod
		 *            The check period in milliseconds.
		 * @param timeoutMills
		 *            The timeout duration in milliseconds.
		 */
		public synchronized static void start(int checkPeriod, int timeoutMills) {
			setCheckPeriod(checkPeriod);
			setTimeoutMills(timeoutMills);
			if (done) {
				done = false;
				t = new Thread(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "ListenerMonitor started");
						while (!done) {
							try {
								// Sleep for the check period
								Thread.sleep(ListenerMonitor.checkPeriod);
							} catch (InterruptedException e) {
							}
							if (!paused) {
								// Create an ArrayList of tokens to remove
								List<String> remove = new ArrayList<String>();
								// Remember the current time
								Date now = new Date();
								synchronized (listeners) {
									// First, check for any existing dead listeners
									for (String listener : listeners.keySet()) {
										WebListenerManager<String> m = listeners.get(listener);
										if (m.isDead())
											remove.add(listener);
									}
									// Next, remove the dead listeners
									for (String listener : remove) {
										Log.d(TAG, "Removing dead listener: " + listener);
										WebListenerManager<String> m = listeners.remove(listener);
									}
									// Finally, check for any listener timeouts
									for (String listener : listeners.keySet()) {
										WebListenerManager<String> m = listeners.get(listener);
										if ((now.getTime() - m.getLastAccess().getTime()) > ListenerMonitor.timeoutMills) {
											// Set the listener to dead
											m.setDead(true);
											/*
											 * Remove the listener's Dynamix session. Note that the listener is retained
											 * in the WebConnector until the next checkPeriod, so that the
											 * onDynamixListenerRemoved event can be fired on the web client.
											 */
											try {
												m.getListener().onDynamixUnbind();
												facade.removeDynamixListener(m.getListener());
											} catch (RemoteException e) {
												Log.e(TAG, e.toString());
											}
										}
									}
								}
							}
						}
						paused = false;
						Log.d(TAG, "ListenerMonitor stopped");
					}
				});
				t.setDaemon(true);
				t.start();
			} else
				Log.w(TAG, "ListenerMonitor is already running!");
		}
	}
}
