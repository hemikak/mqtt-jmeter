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

package org.apache.jmeter.protocol.mqtt.sampler;

import java.util.Date;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.protocol.mqtt.client.MqttSubscriber;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class SubscriberSampler extends AbstractSampler implements
        Interruptible, ThreadListener, TestStateListener {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private transient volatile boolean interrupted = false;
    private static final long serialVersionUID = 240L;

    private JavaSamplerContext context = null;
    public transient MqttSubscriber subscriber = null;
    private static final String BROKER_URL = "mqtt.broker.url";
    private static final String CLIENT_ID = "mqtt.client.id";
    private static final String TOPIC_NAME = "mqtt.topic.name";
    private static final String CLEAN_SESSION = "mqtt.clean.session";
    private static final String USERNAME = "mqtt.auth.username";
    private static final String PASSWORD = "mqtt.auth.password";
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

    @Override
    public boolean interrupt() {
        boolean oldValue = interrupted;
        interrupted = true;   // so we break the loops in SampleWithListener and SampleWithReceive

        log.debug("Thread ended " + new Date());
        if (this.subscriber != null) {
            try {
                this.subscriber.close(interrupted);

            } catch (Exception e) {
                e.printStackTrace();
                log.warn(e.getLocalizedMessage(), e);
            }

        }


        return !oldValue;
    }

    @Override
    public void testEnded() {
        log.debug("Thread ended " + new Date());
        if (this.subscriber != null) {
            try {
                this.subscriber.close(interrupted);

            } catch (Exception e) {
                e.printStackTrace();
                log.warn(e.getLocalizedMessage(), e);
            }

        }
    }

    @Override
    public void testEnded(String arg0) {
        testEnded();
    }

    @Override
    public void testStarted() {
    }

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

    @Override
    public void threadStarted() {
        interrupted = false;
        logThreadStart();
        if (subscriber == null) {
            try {
                subscriber = new MqttSubscriber();
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }

        Arguments parameters = new Arguments();
        parameters.addArgument("BROKER_URL", getBrokerUrl());
        parameters.addArgument("CLIENT_ID", getClientId());
        parameters.addArgument("TOPIC_NAME", getTopicName());
        parameters.addArgument("CLEAN_SESSION", Boolean.toString(isCleanSession()));
        parameters.addArgument("USERNAME", getUsername());
        parameters.addArgument("PASSWORD", getPassword());
        parameters.addArgument("QOS", getQOS());
        parameters.addArgument("CLIENT_TYPE", getClientType());

        if (validate()) {
            context = new JavaSamplerContext(parameters);
            subscriber.setupTest(context);
        } else {
            interrupt();
        }
    }

    @Override
    public void threadFinished() {
        log.debug("Thread ended " + new Date());
        if (this.subscriber != null) {
            try {
                this.subscriber.close(interrupted);

            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage(), e);
            }

        }
    }

    @Override
    public SampleResult sample(Entry entry) {
        return this.subscriber.runTest(context);
    }

    private boolean validate() {
        if (getBrokerUrl().equals("")) {
            log.error("The broker url cannot be empty");
            return false;
        }
        if (getClientId().equals("")) {
            log.error("The client ID cannot be empty");
            return false;
        }
        if (getTopicName().equals("")) {
            log.error("The topic name(destination) cannot be empty");
            return false;
        }
        if (getUsername().equals("")) {
            log.error("The username cannot be empty");
            return false;
        }
        if (getPassword().equals("")) {
            log.error("The password cannot be empty");
            return false;
        }

        return true;
    }

}
