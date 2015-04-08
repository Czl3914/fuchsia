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
 * Copyright (C) 2010-2012, openHAB.org <admin@openhab.org>
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

import java.util.HashMap;
import java.util.Map;

import org.ow2.chameleon.fuchsia.importer.zwave.internal.commandclass.ZWaveCommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Z-Wave device class. A Z-Wave device class groups devices with the same functionality 
 * together in a class.
 * 
 * TODO: Complete all device classes.
 *  
 * @author Jan-Willem Spuij
 * @since 1.3.0
 */
public class ZWaveDeviceClass {

	private static final Logger logger = LoggerFactory.getLogger(ZWaveDeviceClass.class);

	private Basic basicDeviceClass;
	private Generic genericDeviceClass;
	private Specific specificDeviceClass;
	
	/**
	 * Constructor. Creates a new instance of the Z-Wave device class.
	 * @param basicDeviceClass the basic device class of this node.
	 * @param genericDeviceClass the generic device class of this node.
	 * @param specificDeviceClass the specific device class of this node.
	 */
	public ZWaveDeviceClass(Basic basicDeviceClass, Generic genericDeviceClass, Specific specificDeviceClass){
		logger.trace("Constructing Zwave Device Class");
		
		this.basicDeviceClass = basicDeviceClass;
		this.genericDeviceClass = genericDeviceClass;
		this.specificDeviceClass = specificDeviceClass;
		
	}

	/**
	 * Returns the basic device class of the node.
	 * @return the basicDeviceClass
	 */
	public Basic getBasicDeviceClass() {
		return basicDeviceClass;
	}

	/**
	 * Set the basic device class of the node.
	 * @param basicDeviceClass the basicDeviceClass to set
	 */
	public void setBasicDeviceClass(Basic basicDeviceClass) {
		this.basicDeviceClass = basicDeviceClass;
	}

	/**
	 * Get the generic device class of the node.
	 * @return the genericDeviceClass
	 */
	public Generic getGenericDeviceClass() {
		return genericDeviceClass;
	}

	/**
	 * Set the generic device class of the node.
	 * @param genericDeviceClass the genericDeviceClass to set
	 */
	public void setGenericDeviceClass(Generic genericDeviceClass) {
		this.genericDeviceClass = genericDeviceClass;
	}
	
	/**
	 * Get the specific device class of the node.
	 * @return the specificDeviceClass
	 */
	public Specific getSpecificDeviceClass() {
		return specificDeviceClass;
	}	
	/**
	 * Set the specific device class of the node.
	 * @param specificDeviceClass the specificDeviceClass to set
	 * @exception IllegalArgumentException thrown when the specific device class does not match
	 * the generic device class.
	 */
	public void setSpecificDeviceClass(Specific specificDeviceClass) throws IllegalArgumentException {
		
		// The specific Device class does not match the generic device class.
		if (specificDeviceClass.genericDeviceClass != Generic.NOT_KNOWN && 
				specificDeviceClass.genericDeviceClass != this.genericDeviceClass)
			throw new IllegalArgumentException("specificDeviceClass");
		
		this.specificDeviceClass = specificDeviceClass;
	}
	
	
	/**
	 * Z-Wave basic Device Class enumeration. The Basic Device Class provides
	 * the device with a role in the Z-Wave network. 
	 * @author Brian Crosby
	 * @author Jan-Willem Spuij
	 * @since 1.3.0
	 */
	public enum Basic {
		NOT_KNOWN(0, "Not Known"), 
		CONTROLLER(1, "Controller"), 
		STATIC_CONTROLLER(2, "Static Controller"), 
		SLAVE(3, "Slave"), 
		ROUTING_SLAVE(4, "Routing Slave");

		/**
		 * A mapping between the integer code and its corresponding Basic device
		 * class to facilitate lookup by code.
		 */
		private static Map<Integer, Basic> codeToBasicMapping;

		private int key;
		private String label;

		private Basic(int key, String label) {
			this.key = key;
			this.label = label;
		}

		private static void initMapping() {
			codeToBasicMapping = new HashMap<Integer, Basic>();
			for (Basic s : values()) {
				codeToBasicMapping.put(s.key, s);
			}
		}

		/**
		 * Lookup function based on the basic device class code.
		 * Returns null if the code does not exist.
		 * @param i the code to lookup
		 * @return enumeration value of the basic device class.
		 */
		public static Basic getBasic(int i) {
			if (codeToBasicMapping == null) {
				initMapping();
			}
			
			return codeToBasicMapping.get(i);
		}

		/**
		 * @return the key
		 */
		public int getKey() {
			return key;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
	}
	
	
	/**
	 * Z-Wave Generic Device Class enumeration. The Generic Device Class
	 * describes functionality of a device in the Network. Generic Device Classes
	 * can have Command Classes that are mandatory or recommended for all devices
	 * that belong to this device class. Generic device class do not relate directly
	 * to Basic Device Classes. E.G. a BINARY_SWITCH can be a ROUTING_SLAVE or a SLAVE.
	 * @author Brian Crosby
	 * @author Jan-Willem Spuij
	 * @since 1.3.0
	 */
	public enum Generic {
		NOT_KNOWN(0, "Not Known"), 
		REMOTE_CONTROLLER(1, "Remote Controller"), 
		STATIC_CONTOLLER(2, "Static Controller"), 
		AV_CONTROL_POINT(3, "A/V Control Point"), 
		DISPLAY(4, "Display"), 
		THERMOSTAT(8, "Thermostat"), 
		WINDOW_COVERING(9, "Window Covering"), 
		REPEATER_SLAVE( 15, "Repeater Slave"), 
		BINARY_SWITCH(16, "Binary Switch"), 
		MULTILEVEL_SWITCH( 17, "Multi-Level Switch"), 
		REMOTE_SWITCH(18, "Remote Switch"), 
		TOGGLE_SWITCH( 19, "Toggle Switch"), 
		Z_IP_GATEWAY(20, "Z/IP Gateway"), 
		Z_IP_NODE( 21, "Z/IP Node"), 
		VENTILATION(22, "Ventilation"), 
		BINARY_SENSOR( 32, "Binary Sensor"), 
		MULTILEVEL_SENSOR(33, "Multi-Level Sensor"), 
		PULSE_METER(48, "Pulse Meter"), 
		METER( 49, "Meter"), 
		ENTRY_CONTROL(64, "Entry Control"), 
		SEMI_INTEROPERABLE( 80, "Semi-Interoperable"),
		ALARM_SENSOR(161, "Alarm Sensor"),
		NON_INTEROPERABLE(255, "Non-Interoperable");

		/**
		 * A mapping between the integer code and its corresponding Generic
		 * Device class to facilitate lookup by code.
		 */
		private static Map<Integer, Generic> codeToGenericMapping;

		private int key;
		private String label;

		private Generic(int key, String label) {
			this.key = key;
			this.label = label;
		}

		private static void initMapping() {
			codeToGenericMapping = new HashMap<Integer, Generic>();
			for (Generic s : values()) {
				codeToGenericMapping.put(s.key, s);
			}
		}

		/**
		 * Lookup function based on the generic device class code.
		 * Returns null if the code does not exist.
		 * @param i the code to lookup
		 * @return enumeration value of the generic device class.
		 */
		public static Generic getGeneric(int i) {
			if (codeToGenericMapping == null) {
				initMapping();
			}
			
			return codeToGenericMapping.get(i);
		}

		/**
		 * @return the key
		 */
		public int getKey() {
			return key;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * Get the mandatory command classes for this device class.
		 * @return the mandatory command classes.
		 */
		public ZWaveCommandClass.CommandClass[] getMandatoryCommandClasses() {
			switch (this) {
				case NOT_KNOWN:
					return new ZWaveCommandClass.CommandClass[0];
				case REMOTE_CONTROLLER:
				case STATIC_CONTOLLER:
				case REPEATER_SLAVE:
				case TOGGLE_SWITCH:
				case REMOTE_SWITCH:
				case WINDOW_COVERING:
				case THERMOSTAT:
				case AV_CONTROL_POINT:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC };
				case BINARY_SWITCH:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.SWITCH_BINARY };
				case MULTILEVEL_SWITCH:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.SWITCH_MULTILEVEL };
				case BINARY_SENSOR:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.SENSOR_BINARY };
				case MULTILEVEL_SENSOR:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.SENSOR_MULTILEVEL };
				case PULSE_METER:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.METER_PULSE };
				case ENTRY_CONTROL:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.LOCK };
				case ALARM_SENSOR:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.ALARM };
				case SEMI_INTEROPERABLE:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.NO_OPERATION, ZWaveCommandClass.CommandClass.BASIC, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.VERSION, ZWaveCommandClass.CommandClass.PROPRIETARY };
				default:
					return new ZWaveCommandClass.CommandClass[0];
			}
		}
	}
	
	
	/**
	 * Z-Wave Specific Device Class enumeration. Specific Device Classes are 
	 * a more detailed definition of a device, and are based on a
	 * Generic Device Class. The Specific Device Class inherits all the 
	 * mandatory commands from the Generic Device Class. In addition to 
	 * these commands, more mandatory or recommended Command Classes can 
	 * be specified for a Specific Device Class. 
	 * @author Brian Crosby
	 * @author Jan-Willem Spuij
	 * @since 1.3.0
	 */
	public enum Specific {
		NOT_USED(0, Generic.NOT_KNOWN, "Not Known"),
		PORTABLE_REMOTE_CONTROLLER(1, Generic.REMOTE_CONTROLLER, "Portable Remote Controller"),
		PORTABLE_SCENE_CONTROLLER(2, Generic.REMOTE_CONTROLLER, "Portable Scene Controller"),
		PC_CONTROLLER(1, Generic.STATIC_CONTOLLER, "PC Controller"),
		SCENE_CONTROLLER(2, Generic.STATIC_CONTOLLER, "Scene Controller"),
		BASIC_REPEATER_SLAVE(1, Generic.REPEATER_SLAVE, "Basic Repeater Slave"),
		POWER_SWITCH_BINARY(1, Generic.BINARY_SWITCH, "Binary Power Switch"),
		SCENE_SWITCH_BINARY(2, Generic.BINARY_SWITCH, "Binary Scene Switch"),
		POWER_SWITCH_MULTILEVEL(1, Generic.MULTILEVEL_SWITCH, "Multilevel Power Switch"),
		SCENE_SWITCH_MULTILEVEL(2, Generic.MULTILEVEL_SWITCH, "Multilevel Scene Switch"), // or is this 4? open-zwave says 4, documents say 2....
		MOTOR_MULTIPOSITION(3, Generic.MULTILEVEL_SWITCH, "Multiposition Motor"),
		MOTOR_CONTROL_CLASS_A(5, Generic.MULTILEVEL_SWITCH, "Motor Control Class A"),
		MOTOR_CONTROL_CLASS_B(6, Generic.MULTILEVEL_SWITCH, "Motor Control Class B"),
		MOTOR_CONTROL_CLASS_C(7, Generic.MULTILEVEL_SWITCH, "Motor Control Class C"),
		SWITCH_TOGGLE_BINARY(1, Generic.TOGGLE_SWITCH, "Binary Toggle Switch"),
		SWITCH_TOGGLE_MULTILEVEL(2, Generic.TOGGLE_SWITCH, "Multilevel Toggle Switch"),
		SWITCH_REMOTE_BINARY(1, Generic.REMOTE_SWITCH, "Binary Remote Switch"),
		SWITCH_REMOTE_MULTILEVEL(2, Generic.REMOTE_SWITCH, "Multilevel Remote Switch"), 
		SWITCH_REMOTE_TOGGLE_BINARY(3, Generic.REMOTE_SWITCH, "Binary Toggle Remote Switch"),
		SWITCH_REMOTE_TOGGLE_MULTILEVEL(4, Generic.REMOTE_SWITCH, "Multilevel Toggle Remote Switch"),
		ROUTING_SENSOR_BINARY(1, Generic.BINARY_SENSOR, "Routing Binary Sensor"),
		ROUTING_SENSOR_MULTILEVEL(1, Generic.MULTILEVEL_SENSOR, "Routing Multilevel Sensor"),
		DOOR_LOCK(1, Generic.ENTRY_CONTROL, "Door Lock"),
		ENERGY_PRODUCTION(1, Generic.SEMI_INTEROPERABLE, "Energy Production"),
		SIMPLE_WINDOW_COVERING(1, Generic.WINDOW_COVERING, "Simple Window Covering Control"),
		THERMOSTAT_HEATING(1, Generic.THERMOSTAT, "Heating Thermostat"),
		THERMOSTAT_GENERAL(2, Generic.THERMOSTAT, "General Thermostat"),
		SETBACK_SCHEDULE_THERMOSTAT(3, Generic.THERMOSTAT, "Setback Schedule Thermostat"),
		SATELLITE_RECEIVER(1, Generic.AV_CONTROL_POINT, "Satellite Receiver"),
		ALARM_SENSOR_ROUTING_BASIC(1, Generic.ALARM_SENSOR, "Basic Routing Alarm Sensor"),
		ALARM_SENSOR_ROUTING(2, Generic.ALARM_SENSOR, "Routing Alarm Sensor"),
		ALARM_SENSOR_ZENSOR_BASIC(3, Generic.ALARM_SENSOR, "Basic Zensor Alarm Sensor"),
		ALARM_SENSOR_ZENSOR(4, Generic.ALARM_SENSOR, "Zensor Alarm Sensor"),
		ALARM_SENSOR_ZENSOR_ADVANCED(5, Generic.ALARM_SENSOR, "Advanced Zensor Alarm Sensor"),
		SMOKE_SENSOR_ROUTING_BASIC(6, Generic.ALARM_SENSOR, "Basic Routing Smoke Sensor"),
		SMOKE_SENSOR_ROUTING(7, Generic.ALARM_SENSOR, "Routing Smoke Sensor"),
		SMOKE_SENSOR_ZENSOR_BASIC(8, Generic.ALARM_SENSOR, "Basic Zensor Smoke Sensor"),
		SMOKE_SENSOR_ZENSOR(9, Generic.ALARM_SENSOR, "Zensor Smoke Sensor"),
		SMOKE_SENSOR_ZENSOR_ADVANCED(10, Generic.ALARM_SENSOR, "Advanced Zensor Smoke Sensor");
		
		/**
	     * A mapping between the integer code and its corresponding Generic Device class to facilitate lookup by code.
	     */
	    private static Map<Generic, Map<Integer, Specific>> codeToSpecificMapping;
		
		private int key;
		private Generic genericDeviceClass;
		private String label;
		
		private Specific (int key, Generic genericDeviceClass, String label) {
			this.key = key;
			this.label = label;
			this.genericDeviceClass = genericDeviceClass;
		}
		
	    private static void initMapping() {
	    	codeToSpecificMapping = new HashMap<Generic, Map<Integer, Specific>>();
	        for (Specific s : values()) {
	        	if (!codeToSpecificMapping.containsKey(s.genericDeviceClass))
	        		codeToSpecificMapping.put(s.genericDeviceClass, new HashMap<Integer, Specific>());
	        	codeToSpecificMapping.get(s.genericDeviceClass).put(s.key, s);
	        }
	    }

	    /**
	     * Lookup function based on the generic device class and the specific device class code.
		 * Returns null if the code does not exist.
	     * @param genericDeviceClass the generic device class
	     * @param i the specific device class code
	     * @return the Specific enumeration
	     */
		public static Specific getSpecific(Generic genericDeviceClass, int i) {
	        if (codeToSpecificMapping == null) {
	            initMapping();
	        }
	        // special case for SPECIFIC_TYPE_NOT_USED. It's valid for all
	        // generic classes (and bound to NOT_KNOWN).
	        if (i == 0)
	        	return codeToSpecificMapping.get(Generic.NOT_KNOWN).get(i);
	        
	        if (!codeToSpecificMapping.containsKey(genericDeviceClass))
				return null;

	        return codeToSpecificMapping.get(genericDeviceClass).get(i);
		}

		/**
		 * @return the key
		 */
		public int getKey() {
			return key;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * Get the mandatory command classes for this device class.
		 * @return the mandatory command classes.
		 */
		public ZWaveCommandClass.CommandClass[] getMandatoryCommandClasses() {
			switch (this) {
				case NOT_USED:
				case PORTABLE_REMOTE_CONTROLLER:
				case PC_CONTROLLER:
				case BASIC_REPEATER_SLAVE:
				case SWITCH_REMOTE_BINARY:
				case SWITCH_REMOTE_MULTILEVEL: 
				case SWITCH_REMOTE_TOGGLE_BINARY:
				case SWITCH_REMOTE_TOGGLE_MULTILEVEL:
				case ROUTING_SENSOR_BINARY: // In the documentation Binary Sensor command class is specified, but this is already on the generic class...
				case ROUTING_SENSOR_MULTILEVEL: // In the documentation Multilevel Sensor command class is specified, but this is already on the generic class...
				case DOOR_LOCK:
				case THERMOSTAT_HEATING:
					return new ZWaveCommandClass.CommandClass[0];
				case PORTABLE_SCENE_CONTROLLER:
				case SCENE_CONTROLLER:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.ASSOCIATION, ZWaveCommandClass.CommandClass.SCENE_CONTROLLER_CONF, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC };
				case POWER_SWITCH_BINARY:
				case POWER_SWITCH_MULTILEVEL:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.SWITCH_ALL };
				case SCENE_SWITCH_BINARY:
				case SCENE_SWITCH_MULTILEVEL:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.SCENE_ACTIVATION, ZWaveCommandClass.CommandClass.SCENE_ACTUATOR_CONF, ZWaveCommandClass.CommandClass.SWITCH_ALL, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC };
				case MOTOR_MULTIPOSITION:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.VERSION, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC };
				case MOTOR_CONTROL_CLASS_A:
				case MOTOR_CONTROL_CLASS_B:
				case MOTOR_CONTROL_CLASS_C:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.SWITCH_BINARY, ZWaveCommandClass.CommandClass.VERSION, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC };
				case SWITCH_TOGGLE_BINARY: 
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.SWITCH_TOGGLE_BINARY };
				case SWITCH_TOGGLE_MULTILEVEL: 
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.SWITCH_TOGGLE_MULTILEVEL };
				case ENERGY_PRODUCTION:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.ENERGY_PRODUCTION };
				case SIMPLE_WINDOW_COVERING:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.BASIC_WINDOW_COVERING };
				case THERMOSTAT_GENERAL:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.THERMOSTAT_MODE, ZWaveCommandClass.CommandClass.THERMOSTAT_SETPOINT };
				case SETBACK_SCHEDULE_THERMOSTAT:
					// TODO: Battery and Wake Up command classes are mandatory for battery operated setback schedule thermostats.
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.CLIMATE_CONTROL_SCHEDULE, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.MULTI_CMD, ZWaveCommandClass.CommandClass.VERSION };
				case SATELLITE_RECEIVER:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.SIMPLE_AV_CONTROL, ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.VERSION };
				case ALARM_SENSOR_ROUTING_BASIC:
				case SMOKE_SENSOR_ROUTING_BASIC:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.ASSOCIATION, ZWaveCommandClass.CommandClass.VERSION };
				case ALARM_SENSOR_ROUTING:
				case ALARM_SENSOR_ZENSOR_ADVANCED:
				case SMOKE_SENSOR_ROUTING:
				case SMOKE_SENSOR_ZENSOR_ADVANCED:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.BATTERY, ZWaveCommandClass.CommandClass.ASSOCIATION, ZWaveCommandClass.CommandClass.VERSION };
				case ALARM_SENSOR_ZENSOR_BASIC:
				case SMOKE_SENSOR_ZENSOR_BASIC:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.VERSION };
				case ALARM_SENSOR_ZENSOR:
				case SMOKE_SENSOR_ZENSOR:
					return new ZWaveCommandClass.CommandClass[] { ZWaveCommandClass.CommandClass.MANUFACTURER_SPECIFIC, ZWaveCommandClass.CommandClass.BATTERY, ZWaveCommandClass.CommandClass.VERSION };
					
				default:
					return new ZWaveCommandClass.CommandClass[0];
			}
		}
	}
	
	
}
