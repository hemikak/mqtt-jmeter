package org.apache.jmeter.protocol.mqtt.paho.clients;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 */
public interface BaseClient extends MqttCallback {
    public void publish(String topicName, int qos, byte[] payload, boolean isRetained) throws MqttException;
    public void subscribe(String topicName, int qos) throws MqttException;
    public void disconnect() throws MqttException;
    public boolean isConnected();
}
