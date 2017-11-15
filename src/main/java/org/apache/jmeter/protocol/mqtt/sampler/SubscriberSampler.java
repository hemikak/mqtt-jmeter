/*
 * Copyright 2016 Hemika Yasinda Kodikara
 *
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
 */

package org.apache.jmeter.protocol.mqtt.sampler;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.mqtt.client.ClientPool;
import org.apache.jmeter.protocol.mqtt.data.objects.Message;
import org.apache.jmeter.protocol.mqtt.paho.clients.AsyncClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BaseClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BlockingClient;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Date;

/**
 * This is MQTT Subscriber sample class. The implementation includes subscriber for MQTT messages with the sample
 * processing.
 */
public class SubscriberSampler extends AbstractSampler implements Interruptible, ThreadListener, TestStateListener {

    private transient BaseClient client;
    private static final long serialVersionUID = 240L;
    private static final String lineSeparator = System.getProperty("line.separator");
    private MqttException exceptionOccurred = null;

    private static final String nameLabel = "MQTT Subscriber";
    private static final Logger log = LoggingManager.getLoggerForClass();
    private transient volatile boolean interrupted = false;

    private static final String BROKER_URL = "mqtt.broker.url";
    private static final String CLIENT_ID = "mqtt.client.id";
    private static final String TOPIC_NAME = "mqtt.topic.name";
    private static final String CLEAN_SESSION = "mqtt.clean.session";
    private static final String NEW_THREAD = "mqtt.new.thread";
    private static final String THREAD_TIMEOUT = "mqtt.thread.timeout";
    private static final String KEEP_ALIVE = "mqtt.keep.alive";
    private static final String USERNAME = "mqtt.auth.username";
    private static final String PASSWORD = "mqtt.auth.password.subscriber";
    private static final String QOS = "mqtt.qos";
    private static final String CLIENT_TYPE = "mqtt.client.type";

    // Getters
    public String getBrokerUrl() {
        return getPropertyAsString(BROKER_URL);
    }

    public String getClientId() {
        return getPropertyAsString(CLIENT_ID);
    }

    public String getTopicName() {
        return getPropertyAsString(TOPIC_NAME);
    }

    public boolean isCleanSession() {
        return getPropertyAsBoolean(CLEAN_SESSION);
    }

    public boolean isNewThread() {
        return getPropertyAsBoolean(NEW_THREAD);
    }

    public String getThreadTimeout() {return getPropertyAsString(THREAD_TIMEOUT);}

    public int getKeepAlive() {
        return getPropertyAsInt(KEEP_ALIVE);
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    public String getQOS() {
        return getPropertyAsString(QOS);
    }

    public String getClientType() {
        return getPropertyAsString(CLIENT_TYPE);
    }

    public String getNameLabel() {
        return nameLabel;
    }

    // Setters
    public void setBrokerUrl(String brokerURL) {
        setProperty(BROKER_URL, brokerURL.trim());
    }

    public void setClientId(String clientID) {
        setProperty(CLIENT_ID, clientID.trim());
    }

    public void setTopicName(String topicName) {
        setProperty(TOPIC_NAME, topicName.trim());
    }

    public void setCleanSession(boolean isCleanSession) {
        setProperty(CLEAN_SESSION, isCleanSession);
    }

    public void setNewThread(boolean isNewThread){
        setProperty(NEW_THREAD, isNewThread);
    }

    public void setThreadTimeOut(String threadTimeOut) {setProperty(THREAD_TIMEOUT, threadTimeOut);}

    public void setKeepAlive(String keepAlive) {setProperty(KEEP_ALIVE, keepAlive);}

    public void setUsername(String username) {
        setProperty(USERNAME, username.trim());
    }

    public void setPassword(String password) {
        setProperty(PASSWORD, password.trim());
    }

    public void setQOS(String qos) {
        setProperty(QOS, qos.trim());
    }

    public void setClientType(String clientType) {
        setProperty(CLIENT_TYPE, clientType.trim());
    }

    public SubscriberSampler() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean interrupt() {
        boolean oldValue = interrupted;
        interrupted = true;   // so we break the loops in SampleWithListener and SampleWithReceive

        log.debug("Thread ended " + new Date());
        try {
            ClientPool.clearClient();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage(), e);
        }

        return !oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        log.debug("Thread ended " + new Date());
        try {
            ClientPool.clearClient();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String arg0) {
        testEnded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        if (log.isDebugEnabled()) {
            log.debug("Thread ended " + new Date());
            log.debug("MQTT SubscriberSampler: ["
                      + Thread.currentThread().getName() + "], hashCode=["
                      + hashCode() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String arg0) {
        testStarted();
    }

    private void logThreadStart() {
        if (log.isDebugEnabled()) {
            log.debug("Thread started " + new Date());
            log.debug("MQTTSampler: [" + Thread.currentThread().getName()
                      + "], hashCode=[" + hashCode() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadStarted() {
        interrupted = false;
        logThreadStart();
        if (client == null) {
            try {
                if (!validate()) {
                    interrupt();
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        log.debug("Thread ended " + new Date());
        try {
            ClientPool.clearClient();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Initializes the MQTT client for subscriber.
     */
    private void initClient() {
        String brokerURL = getBrokerUrl();
        String clientId = getClientId();
        String topicName = getTopicName();
        boolean isCleanSession = isCleanSession();
        int keepAlive = getKeepAlive();
        String userName = getUsername();
        String password = getPassword();
        String clientType = getClientType();

        // Generating client ID if empty
        if (StringUtils.isEmpty(clientId)) {
            clientId = Utils.UUIDGenerator();
        }

        exceptionOccurred = null;

        try {
            if (Constants.MQTT_BLOCKING_CLIENT.equals(clientType)) {
                client = new BlockingClient(brokerURL, clientId, isCleanSession, userName, password, keepAlive);
            } else if (Constants.MQTT_ASYNC_CLIENT.equals(clientType)) {
                client = new AsyncClient(brokerURL, clientId, isCleanSession, userName, password, keepAlive);
            }

            if (client != null) {
                ClientPool.addClient(client);
           }


        } catch (MqttException e) {
            exceptionOccurred = e;
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry entry) {
        final SampleResult result = new SampleResult();
        result.setSampleLabel(getNameLabel());
        result.sampleStart();

        initClient();
        if (client != null) {
            try {
                client.subscribe(getTopicName(), getIntQos(getQOS()));
            } catch (MqttException e) {
                log.error("Error while subscribed to  topic", e);
                exceptionOccurred = e;
            }

            if (null != exceptionOccurred) {
                result.setSuccessful(false);
                result.setResponseMessage("Client is not connected." + lineSeparator + exceptionOccurred.toString());
                result.setResponseData(exceptionOccurred.toString().getBytes());
                result.sampleEnd();
                result.setResponseCode("FAILED");
                return result;
            }

            if (isNewThread()) {
                long timeout;
                try {
                    timeout = Integer.valueOf(getThreadTimeout());
                } catch (NumberFormatException e) {
                    timeout = 0;
                }
                if (timeout > 0) {
                    final long finalTimeout = timeout;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            reciveMessages(result, true, finalTimeout);
                        }
                    }).start();

                    result.setSuccessful(true);
                    result.setResponseMessage("Subscriber started on another thread check log for actual value.");
                    result.sampleEnd();
                    result.setResponseCodeOK();
                    return result;
                } else {

                    return reciveMessages(result, false, 0);
                }
            }
        }

        result.setSuccessful(false);
        result.setResponseMessage("Client has been stopped or an error occurred while receiving messages. Received "
                + " valid messages.");
        result.sampleEnd();
        result.setResponseCode("FAILED");
        return result;
    }

    private boolean timeoutDidNotPass(long start, long timeout, boolean checkTimeOut) {
        if (checkTimeOut) {
            long currTime = System.currentTimeMillis();
            if (currTime - start > timeout) {
               return false;
            }
        }
        return true;
    }

    private SampleResult reciveMessages(SampleResult result, boolean onAnotherThread, long timeout) {
        Message receivedMessage;
        long startTime = System.currentTimeMillis();
        long timeoutMili = timeout * 1000;
        boolean checkTimeOut = timeout > 0;
        while (timeoutDidNotPass(startTime, timeoutMili, checkTimeOut) && !interrupted && null != client.getReceivedMessages() && null != client.getReceivedMessageCounter()) {
            receivedMessage = client.getReceivedMessages().poll();
            if (receivedMessage != null) {
                if (onAnotherThread){
                    log.info(lineSeparator + "Received " + client.getReceivedMessageCounter().get() + " " +
                            "messages." +
                            lineSeparator + "Current message QOS : " + receivedMessage.getQos() +
                            lineSeparator + "Is current message a duplicate : " + receivedMessage.isDup()
                            + lineSeparator + "Received timestamp of current message : " +
                            receivedMessage.getCurrentTimestamp() + lineSeparator + "Is current message" +
                            " a retained message : " + receivedMessage.isRetained());
                }
                else {
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
        }
        if (onAnotherThread) {
            if (!timeoutDidNotPass(startTime, timeoutMili, checkTimeOut) && client != null) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } else {
                log.error("Client has been stopped or an error occurred while receiving messages. Received valid messages.");
            }
        }
        else {
            result.setSuccessful(false);
            result.setResponseMessage("Client has been stopped or an error occurred while receiving messages. Received "
                    + " valid messages.");
            result.sampleEnd();
            result.setResponseCode("FAILED");
        }
        return result;
    }

    /**
     * Validates parameters
     *
     * @return true if valid parameters, else false
     */
    private boolean validate() {
        if (StringUtils.isBlank(getBrokerUrl())) {
            log.error("The broker url cannot be empty");
            return false;
        }
        if (StringUtils.isBlank(getTopicName())) {
            log.error("The topic name(destination) cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * get quality of service value
     */
    private int getIntQos(String stringQos) {
        int qos = 0;
        if (Constants.MQTT_AT_MOST_ONCE.equals(stringQos)) {
            qos = 0;
        } else if (Constants.MQTT_AT_LEAST_ONCE.equals(stringQos)) {
            qos = 1;
        } else if (Constants.MQTT_EXACTLY_ONCE.equals(stringQos)) {
            qos = 2;
        }
        return qos;
    }
}

