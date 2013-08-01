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
package org.ambientdynamix.security;

import java.sql.Time;

/**
 * Experimental TimeConstraint, which models a time-span of context validity. Currently not used.
 * 
 * @author Darren Carlson
 */
public class TimeConstraint {
	private Time _validFrom;
	private Time _validUntil;

	private TimeConstraint(Time validFrom, Time validUntil) {
		_validFrom = validFrom;
		_validUntil = validUntil;
	}

	public boolean validNow() {
		Time t = new Time(System.currentTimeMillis());
		if (t.before(_validFrom) || t.after(_validUntil))
			return false;
		else
			return true;
	}
}
