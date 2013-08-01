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


/**
 * Context scan error codes, which are returned in the 'onContextScanFailed' method of the IDynamixListener interface.
 * 
 * @see IDynamixListener
 * @author Darren Carlson
 */
public class ErrorCodes {
	public static final int NO_ERROR = 0;
	public static final int NOT_AUTHORIZED = 1;
	public static final int SCAN_CONFIGURATION_NOT_SUPPORTED = 2;
	public static final int MISSING_SCAN_CONFIGURATION = 3;
	public static final int CONFIGURATION_ERROR = 4;
	public static final int CONTEXT_TYPE_NOT_SUPPORTED = 5;
	public static final int TOO_MUCH_DATA_REQUESTED = 6;
	public static final int INTERNAL_PLUG_IN_ERROR = 7;
	public static final int DYNAMIX_FRAMEWORK_ERROR = 8;
	public static final int NO_CONTEXT_SUPPORT = 9;
	public static final int SESSION_NOT_FOUND = 10;
	public static final int PLUG_IN_NOT_CONFIGURED = 11;
	public static final int PLUG_IN_TYPE_MISMATCH = 12;
	public static final int PLUG_IN_NOT_FOUND = 13;
	public static final int APPLICATION_EXCEPTION = 14;
	public static final int CONTEXT_SCAN_FAILED = 15;
	public static final int RESOURCE_BUSY = 16;
	public static final int MISSING_PARAMETERS = 17;
	public static final int NOT_READY = 18;
	public static final int NOT_SUPPORTED = 19;
	public static final int PLUG_IN_DISABLED = 20;

	// Singleton constructor
	private ErrorCodes() {
	}
}