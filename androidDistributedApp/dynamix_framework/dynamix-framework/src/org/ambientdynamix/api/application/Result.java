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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base result for Dynamix Framework method calls.
 * 
 * @author Darren Carlson
 * 
 */
public class Result implements Parcelable {
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
		public Result createFromParcel(Parcel in) {
			return new Result(in);
		}

		public Result[] newArray(int size) {
			return new Result[size];
		}
	};
	// Private data
	private boolean success;
	private String message;
	private int errorCode;

	/**
	 * Default constructor that creates a successful Result with status "success = true"
	 */
	public Result() {
		this.success = true;
		this.message = "";
		this.errorCode = ErrorCodes.NO_ERROR;
	}

	/**
	 * Constructor that creates an unsuccessful Result with status "success = false".
	 * 
	 * @param errorMessage
	 *            The message associated with the error.
	 * @param errorCode
	 *            The status code associated with the error. @see org,ambientdynamix.api.application.ErrorCodes
	 */
	public Result(String errorMessage, int errorCode) {
		this.success = false;
		this.message = errorMessage;
		this.errorCode = errorCode;
	}

	/**
	 * Returns true if the Result of the operation was successful; false otherwise.
	 */
	public boolean wasSuccessful() {
		return success;
	}

	/**
	 * Returns the error message associated with unsuccessful operations.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the error code associated with unsuccessful operations.
	 * 
	 * @see org,ambientdynamix.api.application.ErrorCodes
	 */
	public int getErrorCode() {
		return errorCode;
	}

	protected Result(Parcel in) {
		this.success = in.readByte() == 1;
		this.message = in.readString();
		this.errorCode = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeByte((byte) (success ? 1 : 0));
		out.writeString(message);
		out.writeInt(errorCode);
	}
}
