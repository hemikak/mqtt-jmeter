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
import org.apache.jmeter.protocol.mqtt.data.objects.Message;
import org.apache.jmeter.protocol.mqtt.paho.clients.AsyncClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BaseClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BlockingClient;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.Serializable;

public class MqttSubscriber extends AbstractJavaSamplerClient implements Serializable {

    private BaseClient client;
    private static final long serialVersionUID = 1L;
    private static final String lineSeparator = System.getProperty("line.separator");
    private boolean interrupted;
    private MqttException exceptionOccurred = null;
    private String nameLabel = "Subscriber";

    /**
     * {@inheritDoc}
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("BROKER_URL", "tcp://localhost:1883");
        defaultParameters.addArgument("CLIENT_ID", Utils.UUIDGenerator());
        defaultParameters.addArgument("TOPIC_NAME", "Sample.MQTT.Topic");
        defaultParameters.addArgument("CLEAN_SESSION", "false");
        defaultParameters.addArgument("KEEP_ALIVE", "0");
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
        String broker_url = context.getParameter("BROKER_URL");
        String clientId = context.getParameter("CLIENT_ID");
        String topicName = context.getParameter("TOPIC_NAME");
        boolean isCleanSession = Boolean.parseBoolean(context.getParameter("CLEAN_SESSION"));
        int keepAlive = Integer.parseInt(context.getParameter("KEEP_ALIVE"));
        String username = context.getParameter("USERNAME");
        String password = context.getParameter("PASSWORD");
        String client_type = context.getParameter("CLIENT_TYPE");

        // Generating client ID if empty
        if (StringUtils.isEmpty(clientId)) {
            clientId = Utils.UUIDGenerator();
        }

        // Quality
        int qos = 0;
        if (Constants.MQTT_AT_MOST_ONCE.equals(context.getParameter("QOS"))) {
            qos = 0;
        } else if (Constants.MQTT_AT_LEAST_ONCE.equals(context.getParameter("QOS"))) {
            qos = 1;
        } else if (Constants.MQTT_EXACTLY_ONCE.equals(context.getParameter("QOS"))) {
            qos = 2;
        }

        setupTest(broker_url, clientId, topicName, username, password,
                isCleanSession,
                qos, client_type, keepAlive);

    }

    /**
     * Starts up a new MQTT client.
     *
     * @param brokerURL    The broker url for the client to connect.
     * @param clientId     The client ID.
     * @param topic        The topic name to subscribe.
     * @param userName     The username of the user.
     * @param password     The password of the user.
     * @param cleanSession Use a clean session subscriber.
     * @param qos          The quality of service value.
     * @param client_type  The client to be either blocking or async.
     */
    private void setupTest(String brokerURL, String clientId, String topic,
                           String userName, String password, boolean cleanSession,
                           int qos, String client_type, int keepAlive) {
        try {
            exceptionOccurred = null;

            if (Constants.MQTT_BLOCKING_CLIENT.equals(client_type)) {
                client = new BlockingClient(brokerURL, clientId, cleanSession, userName, password, keepAlive);
            } else if (Constants.MQTT_ASYNC_CLIENT.equals(client_type)) {
                client = new AsyncClient(brokerURL, clientId, cleanSession, userName, password, keepAlive);
            }
            client.subscribe(topic, qos);
        } catch (MqttSecurityException e) {
            getLogger().error("Security related error occurred", e);
            exceptionOccurred = e;
        } catch (MqttException e) {
            getLogger().error("Non-security related error occurred", e);
            exceptionOccurred = e;
        }
//        catch (NoSuchAlgorithmException e) {
//            getLogger().error("Unable to generate client ID", e);
//            exceptionOccurred = new MqttException(e);
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getNameLabel());
        result.sampleStart();

        if (null != exceptionOccurred) {
            result.setSuccessful(false);
            result.setResponseMessage("Client is not connected." + lineSeparator + exceptionOccurred.toString());
            result.setResponseData(exceptionOccurred.toString().getBytes());
            result.sampleEnd();
            result.setResponseCode("FAILED");
            return result;
        }

        Message receivedMessage;
        while (!interrupted && null != client.getReceivedMessages() && null != client.getReceivedMessageCounter()) {
            receivedMessage = client.getReceivedMessages().poll();
            if (receivedMessage != null) {
                client.getReceivedMessageCounter().incrementAndGet();
                result.sampleEnd();
                result.setSuccessful(true);
                result.setResponseMessage(lineSeparator + "Received " + client.getReceivedMessageCounter().get() + " " +
                                          "messages." +
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
        result.setResponseMessage("Client has been stopped or an error occurred while receiving messages. Received "  + " valid messages.");
        result.sampleEnd();
        result.setResponseCode("FAILED");
        return result;

    }

    /**
     * Disconnected the client.
     *
     * @param interrupted Whether the thread was interrupted.
     */
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

	public String getNameLabel() {
		return nameLabel;
	}

	public void setNameLabel(String nameLabel) {
		this.nameLabel = nameLabel;
	}
}
