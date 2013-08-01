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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Utility class for 'expirable' events.
 * 
 * @author Darren Carlson
 */
public class Expirable implements Parcelable, Serializable {
	private static final long serialVersionUID = -7685812328512103284L;
	/**
	 * Static Parcelable.Creator required to reconstruct a the object from an incoming Parcel.
	 */
	public static final Parcelable.Creator<Expirable> CREATOR = new Parcelable.Creator<Expirable>() {
		public Expirable createFromParcel(Parcel in) {
			return new Expirable(in);
		}

		public Expirable[] newArray(int size) {
			return new Expirable[size];
		}
	};
	// Private data
	private Date timeStamp;
	private Date expireTime;
	private int expireMills = 0;

	/**
	 * Creates a default Expirable that does not expire, with the the timestamp set to the current system time.
	 */
	public Expirable() {
		this.timeStamp = new Date();
		this.expireTime = calcExpireTime();
	}

	/**
	 * Creates an Expirable.
	 * 
	 * @param timeStamp
	 *            The time the expirable was created
	 * @param expireMills
	 *            The length of time until expiration (in milliseconds)
	 */
	public Expirable(Date timeStamp, int expireMills) {
		this.timeStamp = timeStamp;
		this.expireMills = expireMills;
		this.expireTime = calcExpireTime();
	}

	/**
	 * Creates an Expirable the the timestamp set to the current system time.
	 * 
	 * @param expireMills
	 *            The length of time until expiration (in milliseconds)
	 */
	public Expirable(int expireMills) {
		this.timeStamp = new Date();
		this.expireMills = expireMills;
		this.expireTime = calcExpireTime();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Checks if this ContextEvent expires Returns true is it expires; false otherwise
	 */
	public boolean expires() {
		return (expireMills > 0) ? true : false;
	}

	/**
	 * @return How long this event is valid (in milliseconds)
	 */
	public int getExpireMills() {
		return this.expireMills;
	}

	/**
	 * Returns the expiration time of this event, which is calculated by adding the specified expiration milliseconds to
	 * the event's time-stamp. If the event does not expire, this event Returns a n expiration date 1000 years in the
	 * future, for convenience during Date comparisons, etc.
	 */
	public Date getExpireTime() {
		return expireTime;
	}

	/**
	 * Returns the time this event was generated
	 */
	public Date getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Sets how long this event is valid (in milliseconds)
	 */
	public void setExpireMills(int expireMills) {
		this.expireMills = expireMills;
	}

	/**
	 * Sets the time-stamp to the incoming Date
	 */
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.timeStamp.getTime());
		dest.writeInt(this.expireMills);
	}

	/*
	 * Private constructor (required for the static Parcelable.Creator method)
	 */
	private Expirable(Parcel in) {
		this.timeStamp = new Date(in.readLong());
		this.expireMills = in.readInt();
		this.expireTime = calcExpireTime();
	}

	private Date calcExpireTime() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(timeStamp);
		if (expires())
			cal.add(Calendar.MILLISECOND, expireMills);
		else
			cal.add(Calendar.YEAR, 1000);
		return cal.getTime();
	}
}