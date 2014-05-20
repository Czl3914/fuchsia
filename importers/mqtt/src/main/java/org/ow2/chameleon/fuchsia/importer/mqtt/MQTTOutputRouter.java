package org.ow2.chameleon.fuchsia.importer.mqtt;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer MQTT
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.rabbitmq.client.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.Hashtable;

@Component
@Provides
public class MQTTOutputRouter implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTOutputRouter.class);

    @Requires
    EventAdmin eventAdmin;

    private Connection connection;

    private Channel channel;

    private boolean isMonitoringArrivals;

    private Thread eventArrivalMonitor;

    @Property(name = "mqtt.server.host", value = ConnectionFactory.DEFAULT_HOST)
    private String serverHost;

    @Property(name = "mqtt.server.port", value = ConnectionFactory.DEFAULT_AMQP_PORT + "")
    private int serverPort;

    @Property(name = "mqtt.queue")
    String queue;

    @Validate
    public void start() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection(new Address[]{new Address(serverHost, serverPort)});
        channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, false, null);

        eventArrivalMonitor = new Thread(this);
        eventArrivalMonitor.start();

        isMonitoringArrivals = true;

    }

    @Invalidate
    public void stop() {
        isMonitoringArrivals = false;
    }

    public void run() {

        try {

            LOG.info("Monitoring AMPQ Queue named '{}'", queue);

            QueueingConsumer consumer = new QueueingConsumer(channel);

            channel.basicConsume(queue, true, consumer);

            while (isMonitoringArrivals) {

                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                Charset messageCharset = Charset.forName(delivery.getProperties().getContentEncoding());

                String message = new String(delivery.getBody(), messageCharset);

                LOG.info("AMQP: message '{}' received,", message);

                LOG.info("Forwarding ..");

                Dictionary metatable = new Hashtable();
                metatable.put("content", message);

                eventAdmin.sendEvent(getEventAdminMessage(metatable));

                LOG.info("EventAdmin: message '{}' queue '{}' sent", message, queue);

            }

        } catch (Exception e) {

            LOG.error("Failed to monitor AMPQ Queue named '{}'", queue, e);

        }

    }

    private Event getEventAdminMessage(Dictionary properties) {

        Event eventAdminMessage = new Event(queue, properties);

        return eventAdminMessage;

    }

    public boolean validate(ImportDeclaration declaration) {
        return false;
    }

}
