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
import org.apache.jmeter.protocol.mqtt.paho.clients.AsyncClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BaseClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BlockingClient;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.io.TextFile;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;


public class MqttPublisher extends AbstractJavaSamplerClient implements
        Serializable, Closeable {

    private static BaseClient client;
    private static int qos = 0;
    private static String topicName = "";
    private static String publishMessage = "";
    private static boolean retained;
    private static AtomicInteger publishedMessageCount = new AtomicInteger(0);

    private static final long serialVersionUID = 1L;

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("BROKER_URL", "tcp://localhost:1883");

        try {
            defaultParameters.addArgument("CLIENT_ID", Utils.UUIDGenerator());
        } catch (NoSuchAlgorithmException e) {
            getLogger().error(e.toString());
        }

        defaultParameters.addArgument("TOPIC_NAME", "Sample.MQTT.Topic");
        defaultParameters.addArgument("MESSAGE_RETAINED", "false");
        defaultParameters.addArgument("USERNAME", "admin");
        defaultParameters.addArgument("PASSWORD", "admin");
        defaultParameters.addArgument("QOS", "AT_MOST_ONCE");
        defaultParameters.addArgument("CLIENT_TYPE", "false");
        return defaultParameters;
    }

    public void setupTest(JavaSamplerContext context) {
        String brokerURL = context.getParameter("BROKER_URL");
        String clientId = context.getParameter("CLIENT_ID");
        topicName = context.getParameter("TOPIC_NAME");
        retained = Boolean.parseBoolean(context.getParameter("CLEAN_SESSION"));
        String username = context.getParameter("USERNAME");
        String password = context.getParameter("PASSWORD");
        String client_type = context.getParameter("CLIENT_TYPE");
        String messageInputType = context.getParameter("MESSAGE_INPUT_TYPE");

        // Quality
        if (Constants.MQTT_AT_MOST_ONCE.equals(context.getParameter("QOS"))) {
            qos = 0;
        } else if (Constants.MQTT_AT_LEAST_ONCE.equals(context.getParameter("QOS"))) {
            qos = 1;
        } else if (Constants.MQTT_EXACTLY_ONCE.equals(context.getParameter("QOS"))) {
            qos = 2;
        }

        if (Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT.equals(messageInputType)) {
            publishMessage = context.getParameter("MESSAGE_VALUE");
        } else if (Constants.MQTT_MESSAGE_INPUT_TYPE_FILE.equals(messageInputType)) {
            publishMessage = getFileContent(context.getParameter("MESSAGE_VALUE"));
        }

        setupTest(brokerURL, clientId, username, password, client_type);
    }

    public void setupTest(String brokerURL, String clientId, String userName, String password, String clientType) {
        try {
            if (Constants.MQTT_BLOCKING_CLIENT.equals(clientType)) {
                client = new BlockingClient(brokerURL, clientId, false, userName, password);
            } else if (Constants.MQTT_ASYNC_CLIENT.equals(clientType)) {
                client = new AsyncClient(brokerURL, clientId, false, userName, password);
            }
        } catch (MqttException e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    public SampleResult runTest(JavaSamplerContext context) {
        if (!client.isConnected()) {
            setupTest(context);
        }
        SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            client.publish(topicName, qos, publishMessage.getBytes(), retained);
            result.setSuccessful(true);
            result.sampleEnd(); // stop stopwatch
            result.setResponseMessage("Sent " + publishedMessageCount.get() + " messages total");
            result.setResponseCode("OK");
            publishedMessageCount.incrementAndGet();
            return result;
        } catch (MqttException e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.toString());
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), null);
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("FAILED");
            return result;
        }

    }

    @Override
    public void close() throws IOException {
        try {
            client.disconnect();
        } catch (MqttException e) {
            getLogger().error("Error when closing subscriber", e);
        }
    }

    /**
     * The implementation uses TextFile to load the contents of the file and
     * returns a string.
     *
     * @param path path to the file to read in
     * @return the contents of the file
     */
    public String getFileContent(String path) {
        TextFile tf = new TextFile(path);
        return tf.getText();
    }
}