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

import java.io.Serializable;

/**
 * The PowerScheme class provides a generic mechanism for specifying the power usage profile of a given
 * ContextPluginRuntime. Broadly, a PowerScheme specifies how a ContextPluginRuntime should attempt to optimize
 * its power consumption in relation to its acquisition performance and modeling precision. Concrete
 * ContextPluginRuntime implementations are free to realize such optimizations as needed for a given context modeling
 * domain; however, the guidelines specified by each static PowerScheme type should be followed as closely as
 * possible.
 * <p>
 * Note that PowerSchemes may be changed during runtime and ContextPluginRuntimes must be capable of dynamically
 * adjusting their power consumption to match new PowerScheme values.
 * 
 * @author Darren Carlson
 */
public final class PowerScheme implements Serializable {
	private static final long serialVersionUID = 3871885926514568085L;
	/**
	 * Specifies that context modeling should occur manually, as specified by the user. In MANUAL mode,
	 * ContextPluginRuntimes should perform a single context scan and then stop according to the description in the
	 * ContextPluginRuntime class.
	 * 
	 * @see ContextPluginRuntime
	 */
	public static final PowerScheme MANUAL = new PowerScheme(1, "Manual");
	/**
	 * Specifies that context modeling should attempt to conserve power as much as possible, potentially at the expense
	 * of reduced performance or precision.
	 */
	public static final PowerScheme POWER_SAVER = new PowerScheme(2, "Power Saver");
	/**
	 * Specifies that context modeling should attempt to balance performance and power consumption.
	 */
	public static final PowerScheme BALANCED = new PowerScheme(3, "Balanced");
	/**
	 * Specifies that context modeling should attempt to provide maximum performance, even at the expense of increased
	 * power consumption.
	 */
	public static final PowerScheme HIGH_PERFORMANCE = new PowerScheme(4, "High Performance");
	/**
	 * An array of static PowerSchemes arranged in order from manual to high performance (manual is index 0).
	 */
	public static final PowerScheme[] PowerSchemes = new PowerScheme[] { MANUAL, POWER_SAVER, BALANCED,
			HIGH_PERFORMANCE };
	// Private variables
	private int id;
	private String name;

	// Private constructor to ensure singleton
	private PowerScheme(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns the PowerScheme associated with the incoming id value, or null if the id is not found.
	 * 
	 * @param id
	 * @return PowerScheme or NULL
	 */
	public static PowerScheme getPowerSchemeForID(int id) {
		for (PowerScheme s : PowerSchemes) {
			if (s.id == id)
				return s;
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PowerScheme) {
			PowerScheme candidate = (PowerScheme) o;
			return candidate.id == this.id ? true : false;
		}
		return false;
	}

	/**
	 * Return the identifier of this PowerScheme
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name of this PowerScheme
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + this.name.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
