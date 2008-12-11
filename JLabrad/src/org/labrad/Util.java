/*
 * Copyright 2008 Matthew Neeley
 * 
 * This file is part of JLabrad.
 *
 * JLabrad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JLabrad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JLabrad.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.labrad;

import java.util.Map;

public class Util {
	/**
     * Converts a byte array into a hex string.
     * @param bytes
     * @return
     */
    public static String dumpBytes(byte[] bytes) {
        int counter = 0;
        String dump = "";
        for (byte b : bytes) {
            int high = (b & 0xF0) >> 4;
            int low = (b & 0x0F);
            dump += "0123456789ABCDEF".substring(high, high + 1)
                    + "0123456789ABCDEF".substring(low, low + 1);
            counter++;
            if (counter == 4) {
                dump += " ";
                counter = 0;
            }
        }
        return dump;
    }
    
    /**
	 * Get an environment variable, or fall back on the given default if not found.
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static String getEnv(String key, String defaultVal) {
		Map<String, String> env = System.getenv();
		String value = defaultVal;
		for (String envKey : env.keySet()) {
			if (key.equalsIgnoreCase(envKey)) {
				value = env.get(envKey);
				break;
			}
		}
		return value;
	}

    /**
     * Get an integer value from the environment, returning a default if
     * the environment variable is not found or cannot be converted to an int.
     * @param key
     * @param defaultVal
     * @return
     */
    public static int getEnvInt(String key, int defaultVal) {
        String defaultStr = String.valueOf(defaultVal);
        String envStr = getEnv(key, defaultStr);
        int value = defaultVal;
        try {
            value = Integer.valueOf(envStr);
        } catch (NumberFormatException e) {}
        return value;
    }
}
