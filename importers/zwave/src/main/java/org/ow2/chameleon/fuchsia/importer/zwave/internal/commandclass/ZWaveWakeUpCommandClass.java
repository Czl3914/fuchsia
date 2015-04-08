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
package org.ow2.chameleon.fuchsia.importer.zwave.internal.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.SerialMessage;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveController;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveEndpoint;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveEvent;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveEventListener;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveNode;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveNode.NodeStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wake Up Command Class. Enables a node to notify another device that it woke up and is ready
 * to receive commands.
 * 
 * @author Jan-Willem Spuij
 * @since 1.3.0
 */
public class ZWaveWakeUpCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization, ZWaveEventListener {

	private static final Logger logger = LoggerFactory.getLogger(ZWaveWakeUpCommandClass.class);

	private static final int WAKE_UP_INTERVAL_SET = 0x04;
	private static final int WAKE_UP_INTERVAL_GET = 0x05;
	private static final int WAKE_UP_INTERVAL_REPORT = 0x06;
	private static final int WAKE_UP_NOTIFICATION = 0x07;
	private static final int WAKE_UP_NO_MORE_INFORMATION = 0x08;
	private static final int WAKE_UP_INTERVAL_CAPABILITIES_GET = 0x09;
	private static final int WAKE_UP_INTERVAL_CAPABILITIES_REPORT = 0x0A;

	private static final int MAX_BUFFFER_SIZE = 128;

	private final ArrayBlockingQueue<SerialMessage> wakeUpQueue = new ArrayBlockingQueue<SerialMessage>(MAX_BUFFFER_SIZE, true);
	
	private int interval = 0;
	
	private int minInterval = 0;
	private int maxInterval = 0;
	private int defaultInterval = 0;
	private int intervalStep = 0;
	
	private volatile boolean isAwake = false;
	
	private boolean initializationComplete = false;
	
	/**
	 * Creates a new instance of the ZWaveWakeUpCommandClass class.
	 * @param node the node this command class belongs to
	 * @param controller the controller to use
	 * @param endpoint the endpoint this Command class belongs to
	 */
	public ZWaveWakeUpCommandClass(ZWaveNode node,
			ZWaveController controller, ZWaveEndpoint endpoint) {
		super(node, controller, endpoint);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public CommandClass getCommandClass() {
		return CommandClass.WAKE_UP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxVersion() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleApplicationCommandRequest(SerialMessage serialMessage,
			int offset, int endpoint) {
		logger.trace("Handle Message Wake Up Request");
		logger.debug(String.format("Received Wake Up Request for Node ID = %d", this.getNode().getNodeId()));
		int command = serialMessage.getMessagePayloadByte(offset);

		switch (command) {
			case WAKE_UP_INTERVAL_SET:
			case WAKE_UP_INTERVAL_GET:
			case WAKE_UP_INTERVAL_CAPABILITIES_GET:
			case WAKE_UP_NO_MORE_INFORMATION:
				logger.warn(String.format("Command 0x%02X not implemented.", command));
				return;
			case WAKE_UP_INTERVAL_REPORT:
				logger.trace("Process Wake Up Interval");
				
				// according to open-zwave: it seems that some devices send incorrect interval report messages. Don't know if they are spurious.
				// if not we should advance the node stage.
                if(serialMessage.getMessagePayload().length < offset + 4) {
                		logger.error("Unusual response: WAKE_UP_INTERVAL_REPORT with length = {}. Ignored.", serialMessage.getMessagePayload().length);
                		return;
                }
                
                int targetNodeId = serialMessage.getMessagePayloadByte(offset +4);
                int receivedInterval = ((serialMessage.getMessagePayloadByte(offset + 1)) << 16) | ((serialMessage.getMessagePayloadByte(offset + 2)) << 8) | (serialMessage.getMessagePayloadByte(offset + 3));
				logger.debug(String.format("Wake up interval report for nodeId = %d, value = %d seconds, targetNodeId = %d", this.getNode().getNodeId(), receivedInterval, targetNodeId));
                
				if (targetNodeId != this.getController().getOwnNodeId())
					return;
				
				this.interval = receivedInterval;
				logger.debug("Wake up interval set for node {}", this.getNode().getNodeId());
				
				this.initializationComplete = true;
				this.getNode().advanceNodeStage();
				break;
			case WAKE_UP_INTERVAL_CAPABILITIES_REPORT:
				logger.trace("Process Wake Up Interval Capabilities");
				
                this.minInterval = ((serialMessage.getMessagePayloadByte(offset + 1)) << 16) | ((serialMessage.getMessagePayloadByte(offset + 2)) << 8) | (serialMessage.getMessagePayloadByte(offset + 3));
                this.maxInterval = ((serialMessage.getMessagePayloadByte(offset + 4)) << 16) | ((serialMessage.getMessagePayloadByte(offset + 5)) << 8) | (serialMessage.getMessagePayloadByte(offset + 6));
                this.defaultInterval = ((serialMessage.getMessagePayloadByte(offset + 7)) << 16) | ((serialMessage.getMessagePayloadByte(offset + 8)) << 8) | (serialMessage.getMessagePayloadByte(offset + 9));
                this.intervalStep = ((serialMessage.getMessagePayloadByte(offset + 10)) << 16) | ((serialMessage.getMessagePayloadByte(offset + 11)) << 8) | (serialMessage.getMessagePayloadByte(offset + 12));
				
				logger.debug(String.format("Wake up interval capabilities report for nodeId = %d", this.getNode().getNodeId()));
				logger.debug(String.format("Minimum interval = %d", this.minInterval));
				logger.debug(String.format("Maximum interval = %d", this.maxInterval));
				logger.debug(String.format("Default interval = %d", this.defaultInterval));
				logger.debug(String.format("Interval step = %d", this.intervalStep));
                
				this.initializationComplete = true;
				this.getNode().advanceNodeStage();
				break;
			case WAKE_UP_NOTIFICATION:
				logger.trace("Process Wake Up Notification");
				
				logger.debug("Node {} is awake", this.getNode().getNodeId());
				this.setAwake(true);
				serialMessage.setTransActionCanceled(true);

				// if this node has not gone through it's query stages yet, finish it.
				if (this.getNode().getNodeStage() != NodeStage.NODEBUILDINFO_DONE && !this.initializationComplete) {
					logger.info("Got Wake Up Notification from node {}, continuing initialization.", this.getNode().getNodeId());
					this.getNode().setNodeStage(NodeStage.NODEBUILDINFO_WAKEUP);
					this.getNode().advanceNodeStage();
					return;
				}

				logger.debug("Sending {} messages from the wake-up queue of node {}", this.wakeUpQueue.size(), this.getNode().getNodeId());

				// reset node stage, apparently we woke up again.
				this.getNode().setNodeStage(NodeStage.NODEBUILDINFO_DONE);

				// handle all messages in the wake-up queue for this node.
				while (!this.wakeUpQueue.isEmpty()) {
					serialMessage = this.wakeUpQueue.poll();
					this.getController().sendData(serialMessage);
				}
				
				// no more information. Go back to sleep.
				logger.trace("No more messages, go back to sleep node {}", this.getNode().getNodeId());
				this.getController().sendData(this.getNoMoreInformationMessage());
				break;
			default:
				logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", 
					command, 
					this.getCommandClass().getLabel(),
					this.getCommandClass().getKey()));
		}
	}
	
	/**
	 * Gets a SerialMessage with the WAKE_UP_NO_MORE_INFORMATION command.
	 * @return the serial message
	 */
	public SerialMessage getNoMoreInformationMessage() {
		logger.debug("Creating new message for application command WAKE_UP_NO_MORE_INFORMATION for node {}", this.getNode().getNodeId());
		SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessagePriority.Low);
    	byte[] newPayload = { 	(byte) this.getNode().getNodeId(), 
    							2, 
								(byte) getCommandClass().getKey(), 
								(byte) WAKE_UP_NO_MORE_INFORMATION };
    	result.setMessagePayload(newPayload);

    	return result;
	}
	
	/**
	 * Puts a message in the wake-up queue of this node to send the message on wake-up.
	 * @param serialMessage the message to put in the wake-up queue.
	 */
	public void putInWakeUpQueue(SerialMessage serialMessage) {
		if (this.wakeUpQueue.contains(serialMessage)) {
			logger.debug("Message already on the wake-up queue for node {}. Discarding.", this.getNode().getNodeId());
			return;
		}
			
		logger.debug("Putting message in wakeup queue for node {}.", this.getNode().getNodeId());
		this.wakeUpQueue.add(serialMessage);
	}
	
	/**
	 * Gets a SerialMessage with the WAKE UP INTERVAL GET command 
	 * @return the serial message
	 */
	public SerialMessage getIntervalMessage() {
		logger.debug("Creating new message for application command WAKE_UP_INTERVAL_GET for node {}", this.getNode().getNodeId());
		SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.ApplicationCommandHandler, SerialMessage.SerialMessagePriority.Get);
    	byte[] newPayload = { 	(byte) this.getNode().getNodeId(), 
    							2, 
								(byte) getCommandClass().getKey(), 
								(byte) WAKE_UP_INTERVAL_GET };
    	result.setMessagePayload(newPayload);
    	return result;		
	}
	
	/**
	 * Gets a SerialMessage with the WAKE UP INTERVAL CAPABILITIES GET command 
	 * @return the serial message
	 */
	public SerialMessage getIntervalCapabilitiesMessage() {
		logger.debug("Creating new message for application command WAKE_UP_INTERVAL_CAPABILITIES_GET for node {}", this.getNode().getNodeId());
		SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.ApplicationCommandHandler, SerialMessage.SerialMessagePriority.Get);
    	byte[] newPayload = { 	(byte) this.getNode().getNodeId(), 
    							2, 
								(byte) getCommandClass().getKey(), 
								(byte) WAKE_UP_INTERVAL_CAPABILITIES_GET };
    	result.setMessagePayload(newPayload);
    	return result;		
	}
	
	/**
	 * Returns the interval at which the device wakes up every time.
	 * @return the interval in seconds.
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Returns the minimum wake up interval that can be set to the device.
	 * @return the minInterval in seconds.
	 */
	public int getMinInterval() {
		return minInterval;
	}

	/**
	 * Returns the maximum wake up interval that can be set to the device.
	 * @return the maxInterval in seconds.
	 */
	public int getMaxInterval() {
		return maxInterval;
	}

	/**
	 * Returns the factory default wake up interval for the device.
	 * @return the defaultInterval in seconds.
	 */
	public int getDefaultInterval() {
		return defaultInterval;
	}

	/**
	 * Returns the minimum step that must be taken into account when setting the interval for the device.
	 * @return the intervalStep in seconds.
	 */
	public int getIntervalStep() {
		return intervalStep;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<SerialMessage> initialize() {
		ArrayList<SerialMessage> result = new ArrayList<SerialMessage>(2);
		result.add(this.getIntervalMessage()); // get wake up interval.
		if (this.getVersion() > 1)
			result.add(this.getIntervalCapabilitiesMessage()); // get default values for wake up interval.
		return result;
	}

	/**
	 * Event handler for incoming Z-Wave events. We monitor Z-Wave events for completed
	 * transactions. Once a transaction is completed for the WAKE_UP_NO_MORE_INFORMATION
	 * event, we set the node state to asleep.
	 * {@inheritDoc}
	 */
	@Override
	public void ZWaveIncomingEvent(ZWaveEvent event) {
		if (event.getEventType() != ZWaveEvent.ZWaveEventType.TRANSACTION_COMPLETED_EVENT)
			return;
		
		SerialMessage serialMessage = (SerialMessage)event.getEventValue();
		
		if (serialMessage.getMessageClass() != SerialMessage.SerialMessageClass.SendData && serialMessage.getMessageType() != SerialMessage.SerialMessageType.Request)
			return;
		
		byte[] payload = serialMessage.getMessagePayload();
		
		if (payload.length < 4)
			return;
		if ((payload[0] & 0xFF) != this.getNode().getNodeId())
			return;
		if ((payload[2] & 0xFF) != this.getCommandClass().getKey())
			return;
		if ((payload[3] & 0xFF) != WAKE_UP_NO_MORE_INFORMATION)
			return;
		
		logger.debug("Node {} went to sleep", this.getNode().getNodeId());
		this.setAwake(false);
	}

	/**
	 * Returns whether the node is awake.
	 * @return the isAwake
	 */
	public boolean isAwake() {
		return isAwake;
	}

	/**
	 * Sets whether the node is awake.
	 * @param isAwake the isAwake to set
	 */
	public void setAwake(boolean isAwake) {
		this.isAwake = isAwake;
	}

}
