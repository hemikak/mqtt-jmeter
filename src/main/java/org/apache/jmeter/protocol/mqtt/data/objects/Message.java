/**
 * Author : Hemika Yasinda Kodikara
 *
 * Copyright (c) 2015.
 */

package org.apache.jmeter.protocol.mqtt.data.objects;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Message object to hold MQTT message content.
 */
public class Message {
    private byte[] payload;
    private int qos = 0;
    private boolean retained = false;
    private boolean dup = false;
    private long currentTimestamp;

    public Message(byte[] payload, int qos, boolean retained, boolean dup, long currentTimestamp) {
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.dup = dup;
        this.currentTimestamp = currentTimestamp;
    }

    public Message(MqttMessage mqttMessage) {
        this.payload = mqttMessage.getPayload();
        this.qos = mqttMessage.getQos();
        this.retained = mqttMessage.isRetained();
        this.dup = mqttMessage.isDuplicate();
        this.currentTimestamp = System.currentTimeMillis();
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetained() {
        return retained;
    }

    public boolean isDup() {
        return dup;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }
}
