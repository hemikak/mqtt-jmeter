/**
 * Author : Hemika Yasinda Kodikara
 *
 * Copyright (c) 2015.
 */

package org.apache.jmeter.protocol.mqtt.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.protocol.mqtt.paho.clients.AsyncClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BaseClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BlockingClient;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;


public class MqttPublisher extends AbstractJavaSamplerClient implements Serializable, Closeable {

    private BaseClient client;
    private int qos = 0;
    private String topicName = StringUtils.EMPTY;
    private String publishMessage = StringUtils.EMPTY;
    private boolean retained;
    private AtomicInteger publishedMessageCount = new AtomicInteger(0);
    private static final String lineSeparator = System.getProperty("line.separator");
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupTest(JavaSamplerContext context) {
        String brokerURL = context.getParameter("BROKER_URL");
        String clientId = context.getParameter("CLIENT_ID");
        topicName = context.getParameter("TOPIC_NAME");
        retained = Boolean.parseBoolean(context.getParameter("MESSAGE_RETAINED"));
        String username = context.getParameter("USERNAME");
        String password = context.getParameter("PASSWORD");
        String client_type = context.getParameter("CLIENT_TYPE");
        String messageInputType = context.getParameter("MESSAGE_INPUT_TYPE");

        // Generating client ID if empty
        if (StringUtils.isEmpty(clientId)){
            clientId  = System.nanoTime() + "." + System.getProperty("user.name");
            if (clientId.length() > 23) {
                clientId = clientId.substring(0, 23);
            }
        }

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
            publishMessage = Utils.getFileContent(context.getParameter("MESSAGE_VALUE"));
        }

        setupTest(brokerURL, clientId, username, password, client_type);
    }

    /**
     * Starts up a new MQTT client.
     *
     * @param brokerURL  The broker url for the client to connect.
     * @param clientId   The client ID.
     * @param userName   The username of the user.
     * @param password   The password of the user.
     * @param clientType The client to be either blocking or async.
     */
    public void setupTest(String brokerURL, String clientId, String userName, String password,
                          String clientType) {
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

    /**
     * {@inheritDoc}
     */
    @Override
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
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), null);
            result.setResponseMessage("Unable publish messages." + lineSeparator + "Exception: " + e.toString());
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("FAILED");
            return result;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        try {
            client.disconnect();
        } catch (MqttException e) {
            getLogger().error("Error when closing subscriber", e);
        }
    }
}