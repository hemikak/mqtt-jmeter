package org.apache.jmeter.protocol.mqtt.paho.clients;

import org.apache.jmeter.protocol.mqtt.data.objects.Message;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public abstract class BaseClient implements MqttCallback {
    protected ConcurrentLinkedQueue<Message> mqttMessageStorage = null;
    protected AtomicLong receivedMessageCounter = null;

    public abstract void publish(String topicName, int qos, byte[] payload, boolean isRetained) throws MqttException;
    public abstract void subscribe(String topicName, int qos) throws MqttException;
    public abstract void disconnect() throws MqttException;
    public abstract  boolean isConnected();

    public ConcurrentLinkedQueue<Message> getReceivedMessages(){
        return mqttMessageStorage;
    }
    public AtomicLong getReceivedMessageCounter(){
        return receivedMessageCounter;
    }
}
