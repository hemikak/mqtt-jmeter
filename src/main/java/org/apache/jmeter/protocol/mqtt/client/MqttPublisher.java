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
import org.apache.jmeter.protocol.mqtt.control.gui.MQTTPublisherGui;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;


public class MqttPublisher extends AbstractJavaSamplerClient implements
        Serializable, Closeable {

    private static MqttAsyncClient client;
    private static int qos = 0;
    private static String topic = "";
    private static String publishMessage = "";
    private static boolean retained;
    private static AtomicInteger publishedMessageCount = new AtomicInteger(0);

    private static final long serialVersionUID = 1L;

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("HOST", "tcp://localhost:1883");
        defaultParameters.addArgument("CLIENT_ID", "Hiep");
        defaultParameters.addArgument("TOPIC", "TEST.MQTT");
        defaultParameters.addArgument("AGGREGATE", "1");
        defaultParameters.addArgument("MESSAGE", "This is my test message");
        return defaultParameters;
    }

    public void setupTest(JavaSamplerContext context) {
        String host = context.getParameter("HOST");
        String clientId = context.getParameter("CLIENT_ID");

        // Quality
        if (MQTTPublisherGui.EXACTLY_ONCE.equals(context.getParameter("QOS"))) {
            qos = 0;
        } else if (MQTTPublisherGui.AT_LEAST_ONCE.equals(context.getParameter("QOS"))) {
            qos = 1;
        } else if (MQTTPublisherGui.AT_MOST_ONCE.equals(context.getParameter("QOS"))) {
            qos = 2;
        }
        topic = context.getParameter("TOPIC");
        publishMessage = context.getParameter("MESSAGE");
        retained = Boolean.parseBoolean(context.getParameter("RETAINED"));

        setupTest(host, clientId, context.getParameter("USER"), context.getParameter("PASSWORD"));
    }

    public void setupTest(String host, String clientId, String user, String password) {
        try {

            client = new MqttAsyncClient(host, clientId);
            JMeterIMqttPublisherActionListener actionListener = new JMeterIMqttPublisherActionListener();

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(user);
            connectOptions.setPassword(password.toCharArray());

            client.connect(connectOptions, null, actionListener);

        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch
        try {

            while(null != client && client.isConnected()){
                // Create and configure message
                MqttMessage message = new MqttMessage(publishMessage.getBytes());
                message.setQos(qos);
                message.setRetained(retained);

                client.publish(topic, message);

                result.sampleEnd(); // stop stopwatch
                result.setSuccessful(true);
                result.setResponseMessage("Sent " + publishedMessageCount.get() + " messages total");
                result.setResponseCode("OK");
                publishedMessageCount.incrementAndGet();
            }

        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.toString());
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), null);
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("FAILED");
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            if (null != client && client.isConnected()) {
                client.disconnect();
                client.close();
            }
        } catch (MqttException e) {
            getLogger().error("Error when closing subscriber", e);
        }
    }

    private class JMeterIMqttPublisherActionListener implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            getLogger().info("Publisher successfully connected");
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            getLogger().error("Unable to publish", throwable);
        }
    }
}