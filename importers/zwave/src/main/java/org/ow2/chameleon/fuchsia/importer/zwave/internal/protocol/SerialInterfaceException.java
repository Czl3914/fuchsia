/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
 * %%
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
 * #L%
 */
/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol;

/**
 * Exceptions thrown from the serial interface.
 * 
 * @author Jan-Willem Spuij
 * @since 1.3.0
 */
public class SerialInterfaceException extends Exception {

	private static final long serialVersionUID = 8852643957484264124L;

	/**
	 * Constructor. Creates a new instance of SerialInterfaceException.
	 */
	public SerialInterfaceException() {
	}

	/**
	 * Constructor. Creates a new instance of SerialInterfaceException.
	 * @param message the detail message.
	 */
	public SerialInterfaceException(String message) {
		super(message);
	}

	/**
	 * Constructor. Creates a new instance of SerialInterfaceException.
	 * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public SerialInterfaceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor. Creates a new instance of SerialInterfaceException.
	 * @param message the detail message.
	 * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public SerialInterfaceException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
