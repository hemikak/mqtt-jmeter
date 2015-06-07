/**
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License. 

 Copyright 2014 University Joseph Fourier, LIG Laboratory, ERODS Team

 */

package org.apache.jmeter.protocol.mqtt.client;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.protocol.mqtt.control.gui.MQTTSubscriberGui;
import org.apache.jmeter.protocol.mqtt.data.objects.Message;
import org.apache.jmeter.protocol.mqtt.paho.clients.BaseClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BlockingClient;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.Serializable;

public class MqttSubscriber extends AbstractJavaSamplerClient implements
        Serializable {

    private static BaseClient client;
    private static final long serialVersionUID = 1L;
    private boolean interrupted;
    private String lineSeparator = System.getProperty("line.separator");

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("HOST", "tcp://localhost:1883");
        defaultParameters.addArgument("CLIENT_ID",
                "${__time(YMDHMS)}${__threadNum}");
        defaultParameters.addArgument("TOPIC", "TEST.MQTT");
        defaultParameters.addArgument("AGGREGATE", "100");
        defaultParameters.addArgument("DURABLE", "false");
        return defaultParameters;
    }

    public void setupTest(JavaSamplerContext context) {
        String host = context.getParameter("HOST");
        String clientId = context.getParameter("CLIENT_ID");
        String topic = context.getParameter("TOPIC");
        setupTest(host, clientId, topic, context.getParameter("USER"), context.getParameter("PASSWORD"),
                Boolean.parseBoolean(context.getParameter("DURABLE")),
                context.getParameter("QOS"));

    }

    private void setupTest(String brokerURL, String clientId, String topic,
                           String userName, String password, boolean cleanSession,
                           String quality) {
        try {
            // Quality
            int qos = 0;
            if (MQTTSubscriberGui.EXACTLY_ONCE.equals(quality)) {
                qos = 2;
            } else if (MQTTSubscriberGui.AT_LEAST_ONCE.equals(quality)) {
                qos = 1;
            } else if (MQTTSubscriberGui.AT_MOST_ONCE.equals(quality)) {
                qos = 0;
            }

            client = new BlockingClient(brokerURL, clientId, cleanSession, userName, password);
            client.subscribe(topic, qos);
        } catch (MqttSecurityException e) {
            getLogger().error("Security related error occurred", e);
        } catch (MqttException e) {
            getLogger().error("Non-security related error occurred", e);
        }
    }

    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.sampleStart();

        Message receivedMessage;
        while (!interrupted && null != client.getReceivedMessages() && null != client.getReceivedMessageCounter()) {
            receivedMessage = client.getReceivedMessages().poll();
            client.getReceivedMessageCounter().incrementAndGet();
            if (receivedMessage != null) {
                result.sampleEnd();
                result.setSuccessful(true);
                result.setResponseMessage("Received " + client.getReceivedMessageCounter().get() + " messages." +
                                          lineSeparator + "Current message QOS : " + receivedMessage.getQos() +
                                          lineSeparator + "Is current message a duplicate : " + receivedMessage.isDup()
                                          + lineSeparator + "Received timestamp of current message : " +
                                          receivedMessage.getCurrentTimestamp() + lineSeparator + "Is current message" +
                                          " a retained message : " + receivedMessage.isRetained());
                result.setBytes(receivedMessage.getPayload().length);
                result.setResponseData(receivedMessage.getPayload());
                result.setResponseCodeOK();
                return result;
            }
        }

        result.setSuccessful(false);
        result.setResponseMessage("Error occurred while receiving messages. Received " + client
                .getReceivedMessageCounter().get() + " valid messages.");
        result.sampleEnd();
        result.setResponseCode("FAILED");
        return result;

    }

    public void close(boolean interrupted) {
        try {
            this.interrupted = interrupted;
            if (null != client && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            getLogger().error("Error when closing subscriber", e);
        }
    }
}
