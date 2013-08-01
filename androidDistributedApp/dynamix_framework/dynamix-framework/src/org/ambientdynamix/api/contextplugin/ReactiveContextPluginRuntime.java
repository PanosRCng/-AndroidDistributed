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
package org.ambientdynamix.api.contextplugin;

import java.util.UUID;

import org.ambientdynamix.api.contextplugin.security.SecuredContextInfo;

import android.os.Bundle;

/**
 * Base class for reactive ContextPluginRuntime implementations, which perform single context sensing or acting actions
 * in response to a specific application's requests to do so, (optionally) sending the resultant IContextInfo only to
 * the application that made the request. Note that requesting applications must hold appropriate context support and
 * security credentials to interact with a ReactiveContextPluginRuntime.
 * <p>
 * Note: Each ContextPluginRuntime operate in conjunction with an associated ContextPlugin, which provides meta-data
 * describing the plugin's name, description, version, supported fidelity levels, etc. There are various types of C
 * ontextPluginRuntimes, each with different runtime behavior. Currently, the available types are
 * AutoContextPluginRuntime, ReactiveContextPluginRuntime and AutoReactiveContextPluginRuntime. Please see the
 * documentation accompanying these classes for details.
 * 
 * @see ContextPlugin
 * @see ContextPluginRuntime
 * @see AutoContextPluginRuntime
 * @see InteractiveContextPluginRuntime
 * @see AutoReactiveContextPluginRuntime
 * @author Darren Carlson
 */
public abstract class ReactiveContextPluginRuntime extends UnicastEventPluginRuntimeBase {
	/**
	 * Performs a single context interaction for the specified requestId, using the specified scan configuration.
	 * Results for this method should be delivered using the standard 'sendContextEvent' methods, providing the
	 * originating requestId as the responseId. This method may be called simultaneously by multiple threads, so care
	 * should be taken to always handle responses using the originating requestId of a given request thread.
	 * Importantly, if 'stop' or 'destroy' are called, the runtime MUST terminate all request processing immediately
	 * (and outstanding requests) according to the semantics defined in ContextPluginRuntime.
	 * <p>
	 * Note: Implementations should endeavor to conserve local resources and battery power according to the current
	 * PowerScheme (which may change over time).
	 * <p>
	 * Note: Implementations must queue multiple incoming context requests, handling them as first-come-first-serve.
	 * <p>
	 * Note: PowerScheme.MANUAL has no effect on IReactiveContextPluginRuntime, since each context scan is inherently
	 * manual.
	 * <p>
	 * Note: This method will be called on a dedicated thread, so the method may block (if needed).
	 * 
	 * @param requestId
	 *            The request identifier to be included in the results
	 * @param errorMessage
	 *            The type of context information to model for this request
	 * @param requestConfig
	 *            An optional configuration Bundle that can be used to control the context sensing or acting process.
	 *            The format of the Bundle is plug-in dependent.
	 * @see SecuredContextInfo
	 */
	public abstract void handleConfiguredContextRequest(UUID requestId, String contextInfoType, Bundle requestConfig);

	/**
	 * Performs a single context interaction for the specified requestId. Results for this method should be delivered
	 * using the standard 'sendContextEvent' methods, providing the originating requestId as the responseId. This method
	 * may be called simultaneously by multiple threads, so care should be taken to always handle responses using the
	 * originating requestId of a given request thread. Importantly, if 'stop' or 'destroy' are called, the runtime MUST
	 * terminate all request processing immediately (and outstanding requests) according to the semantics defined in
	 * ContextPluginRuntime.
	 * <p>
	 * Note: Implementations should endeavor to conserve local resources and battery power according to the current
	 * PowerScheme (which may change over time).
	 * <p>
	 * Note: Implementations must queue multiple incoming context requests, handling them as first-come-first-serve.
	 * <p>
	 * Note: PowerScheme.MANUAL has no effect on IReactiveContextPluginRuntime, since each context scan is inherently
	 * manual.
	 * <p>
	 * Note: This method will be called on a dedicated thread, so the method may block (if needed).
	 * 
	 * @param requestId
	 *            The request identifier to be included in the results
	 * @param errorMessage
	 *            The type of context information to model for this request
	 * @param scanConfig
	 *            An optional configuration Bundle that can be used to control the context acquisition process. The
	 *            format of the Bundle is plug-in dependent.
	 * @see SecuredContextInfo
	 */
	public abstract void handleContextRequest(UUID requestId, String contextInfoType);
}
