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
 * Result class that provides an identifier.
 * 
 * @author Darren Carlson
 * 
 */
public class IdResult extends Result implements Parcelable {
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<IdResult> CREATOR = new Parcelable.Creator<IdResult>() {
		public IdResult createFromParcel(Parcel in) {
			return new IdResult(in);
		}

		public IdResult[] newArray(int size) {
			return new IdResult[size];
		}
	};
	// Private data
	private String id = "";

	/**
	 * Creates a successful result using the incoming id.
	 * 
	 * @param plugInfo
	 *            The id associated with this Result.
	 */
	public IdResult(String id) {
		super();
		this.id = id;
	}

	/**
	 * Constructor that creates a Result with status "success = false".
	 * 
	 * @param errorMessage
	 *            The message associated with the error.
	 * @param errorCode
	 *            The status code associated with the error. @see org,ambientdynamix.api.application.ErrorCodes
	 */
	public IdResult(String errorMessage, int errorCode) {
		super(errorMessage, errorCode);
	}

	/**
	 * Returns the identifier associated with this Result.
	 */
	public String getId() {
		return id;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(id);
	}

	private IdResult(Parcel in) {
		super(in);
		id = in.readString();
	}
}
