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
import org.apache.jmeter.protocol.mqtt.sampler.SubscriberSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class MqttSubscriber extends AbstractJavaSamplerClient implements
        Serializable {
    private static ConcurrentLinkedQueue<byte[]> mqttMessageStorage = new ConcurrentLinkedQueue<byte[]>();
    private static AtomicLong receivedMessageCount;
    private static MqttAsyncClient client;
    private String topic;
    private static final long serialVersionUID = 1L;
    private boolean interrupted;

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

    private void setupTest(String host, String clientId, String topic,
                           String user, String password, boolean cleanSession,
                           String quality) {
        try {
            // Quality
            int qos = 0;
            if (MQTTSubscriberGui.EXACTLY_ONCE.equals(quality)) {
                qos = 0;
            } else if (MQTTSubscriberGui.AT_LEAST_ONCE.equals(quality)) {
                qos = 1;
            } else if (MQTTSubscriberGui.AT_MOST_ONCE.equals(quality)) {
                qos = 2;
            }

            this.topic = topic;

            String generateClientId = MqttAsyncClient.generateClientId();
            if(generateClientId.length() > 20){
                generateClientId = generateClientId.substring(0, 20);
            }

            client = new MqttAsyncClient(host, generateClientId, new MemoryPersistence());
            JMeterIMqttSubscriberActionListener actionListener = new JMeterIMqttSubscriberActionListener(topic, qos, client);

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(user);
            connectOptions.setPassword(password.toCharArray());
            connectOptions.setCleanSession(cleanSession);

            client.connect(connectOptions,  null, actionListener);

            receivedMessageCount = new AtomicLong(0);

            JMeterMqttSubscriberCallback callback = new JMeterMqttSubscriberCallback();
            client.setCallback(callback);
        } catch (MqttSecurityException e) {
            getLogger().error("Security related error occurred", e);
        } catch (MqttException e) {
            getLogger().error("Non-security related error occurred", e);
        }
    }

    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.sampleStart();

        byte[] messagePayload = null;
        while(!interrupted && null != mqttMessageStorage && null != receivedMessageCount){
            messagePayload = mqttMessageStorage.poll();
            receivedMessageCount.incrementAndGet();
            if(messagePayload != null){
                break;
            }
        }

        result.setSuccessful(true);
        result.setResponseMessage("Received " + receivedMessageCount.get() + " messages(may be incorrect)");

        result.setResponseData(messagePayload);
        result.sampleEnd(); // stop stopwatch
        result.setResponseCode("OK");
        return result;

//        try {
//            while (true) {
//                if(client.isConnected() && null != mqttMessageStorage && null != receivedMessageCount){
//                    MqttMessage message = mqttMessageStorage.poll();
//                    if (null != message) {
//                        if (null != context.getParameter("TIMEOUT") && !context.getParameter("TIMEOUT").equals("")) {
//                            Thread.sleep(Long.parseLong(context.getParameter("TIMEOUT")));
//                        }
//                        result.setSuccessful(true);
//                        result.setResponseMessage("Received " + receivedMessageCount.get() + " messages(may be incorrect)");
//
//                        result.setResponseData(message.getPayload());
//                        result.sampleEnd(); // stop stopwatch
//                        result.setResponseCode("OK");
//                        return result;
//                    }
//                }else if(interrupted){
//                    result.setSuccessful(false);
//                    result.setResponseMessage("Client is being stopped");
//                    result.sampleEnd(); // stop stopwatch
//                    result.setResponseCode("OK");
//                    return result;
//                }
//            }
//        } catch (InterruptedException e) {
//            result.sampleEnd(); // stop stopwatch
//            result.setSuccessful(false);
//            result.setResponseMessage("Exception: " + e);
//            // get stack trace as a String to return as document data
//            java.io.StringWriter stringWriter = new java.io.StringWriter();
//            e.printStackTrace(new java.io.PrintWriter(stringWriter));
//            result.setResponseData(stringWriter.toString(), null);
//            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
//            result.setResponseCode("FAILED");
//            return result;
//        }
    }

    public void close(boolean interrupted) {
        try {
            this.interrupted = interrupted;
            if (null != client && client.isConnected()) {
                client.unsubscribe(this.topic);
                client.disconnect();
                getLogger().info("Client disconnected");
                client.close();
                getLogger().info("Client closed");
            }
        } catch (MqttException e) {
            getLogger().error("Error when closing subscriber", e);
        }
    }

    private class JMeterIMqttSubscriberActionListener implements IMqttActionListener{

        private String topic;
        private int qos;
        private MqttAsyncClient asyncClient;

        public JMeterIMqttSubscriberActionListener(String topic, int qos, MqttAsyncClient asyncClient) {
            this.topic = topic;
            this.qos = qos;
            this.asyncClient = asyncClient;
        }

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            try {
                asyncClient.subscribe(topic, qos, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        getLogger().info("Successfully subscribed");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        getLogger().info("Subscribing failed");
                    }
                });

            } catch (MqttException e) {
                getLogger().error("Unable to subscribe", e);
            }
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            getLogger().error("Unable to subscribe", throwable);
        }
    }

    private class JMeterMqttSubscriberCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable throwable) {
            getLogger().error("Connection lost on callback", throwable);
        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            if (null != mqttMessage) {
                mqttMessageStorage.add(mqttMessage.getPayload());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        }
    }
}
