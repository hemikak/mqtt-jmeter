/**
 * Author : Hemika Yasinda Kodikara
 *
 * Copyright (c) 2015.
 */

package org.apache.jmeter.protocol.mqtt.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.protocol.mqtt.client.MqttPublisher;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.util.Date;

public class PublisherSampler extends AbstractSampler implements ThreadListener, TestStateListener {

    private static final long serialVersionUID = 233L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    private JavaSamplerContext context = null;
    public transient MqttPublisher publisher = null;
    private static final String BROKER_URL = "mqtt.broker.url";
    private static final String CLIENT_ID = "mqtt.client.id";
    private static final String TOPIC_NAME = "mqtt.topic.name";
    private static final String RETAINED = "mqtt.message.retained";
    private static final String CLEAN_SESSION = "mqtt.clean.session";
    private static final String KEEP_ALIVE = "mqtt.keep.alive";
    private static final String USERNAME = "mqtt.auth.username";
    private static final String PASSWORD = "mqtt.auth.password";
    private static final String QOS = "mqtt.qos";
    private static final String CLIENT_TYPE = "mqtt.client.type";
    private static final String MESSAGE_INPUT_TYPE = "mqtt.message.input.type";
    private static final String MESSAGE_VALUE = "mqtt.message.input.value";

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

    public boolean isMessageRetained() {
        return getPropertyAsBoolean(RETAINED);
    }

    public boolean isCleanSession() {
        return getPropertyAsBoolean(CLEAN_SESSION);
    }

    public String getKeepAlive() {
        return getPropertyAsString(KEEP_ALIVE);
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

    public String getMessageInputType() {
        return getPropertyAsString(MESSAGE_INPUT_TYPE);
    }

    public String getMessageValue() {
        return getPropertyAsString(MESSAGE_VALUE);
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

    public void setMessageRetained(boolean isCleanSession) {
        setProperty(RETAINED, isCleanSession);
    }

    public void setCleanSession(boolean isCleanSession) {
        setProperty(CLEAN_SESSION, isCleanSession);
    }

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

    public void setMessageInputType(String messageInputType) {
        setProperty(MESSAGE_INPUT_TYPE, messageInputType.trim());
    }

    public void setMessageValue(String messageValue) {
        setProperty(MESSAGE_VALUE, messageValue.trim());
    }

    public PublisherSampler() {
    }

    private void logThreadStart() {
        if (log.isDebugEnabled()) {
            log.debug("Thread started " + new Date());
            log.debug("MQTT PublishSampler: ["
                      + Thread.currentThread().getName() + "], hashCode=["
                      + hashCode() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadStarted() {
        logThreadStart();

        if (publisher == null) {
            try {
                publisher = new MqttPublisher();
            } catch (Exception e) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        Arguments parameters = new Arguments();
        parameters.addArgument("BROKER_URL", getBrokerUrl());
        parameters.addArgument("CLIENT_ID", getClientId());
        parameters.addArgument("TOPIC_NAME", getTopicName());
        parameters.addArgument("MESSAGE_RETAINED", Boolean.toString(isMessageRetained()));
        parameters.addArgument("CLEAN_SESSION", Boolean.toString(isCleanSession()));
        parameters.addArgument("KEEP_ALIVE", getKeepAlive());
        parameters.addArgument("USERNAME", getUsername());
        parameters.addArgument("PASSWORD", getPassword());
        parameters.addArgument("QOS", getQOS());
        parameters.addArgument("CLIENT_TYPE", getClientType());
        parameters.addArgument("MESSAGE_INPUT_TYPE", getMessageInputType());
        parameters.addArgument("MESSAGE_VALUE", getMessageValue());

        this.context = new JavaSamplerContext(parameters);
        this.publisher.setNameLabel(getName());
        this.publisher.setupTest(this.context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        log.debug("Thread ended " + new Date());
        if (publisher != null) {
            try {
                publisher.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        log.debug("Thread ended " + new Date());
        if (publisher != null) {
            try {
                publisher.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.warn(e.getLocalizedMessage(), e);
            }
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String arg0) {
        testStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry entry) {
        return this.publisher.runTest(context);
    }
}
