mqtt-jmeter
===========

# Introduction

This project is a JMeter plugin for MQTT(Message Queuing Telemetry Transport) protocol. The plugin uses Eclipse Paho MQTT client(1.0.2) for the subscriber and publisher.

# How to install MQTT plugin in JMeter

From the repository: https://github.com/hemikak/mqtt-jmeter
Get the source code, go to mqtt-jmeter folder and and use the command maven in terminal:

	mvn clean install

Once its done, go to the target folder and run the following command in terminal with the path to JMeter. Ex :

	./installer.sh ~/Documents/wso2/mb/clients/apache-jmeter-2.13/

This would do the following tasks...
* Copies the **mqtt-jmeter.jar** from the **target** folder to **lib/ext** folder of JMeter.
* Updates the file messages.properties in the folder :/org/apache/jmeter/resources/
in **ApacheJMeter_core.jar** by new file messages.properties.

Regards,  
Hemika

# Pull request sbeaulie
Modified the code to enable a connection to the AWS IoT MQTT broker
-Update dependency to <eclipse.paho.mqtt.version>1.0.2</eclipse.paho.mqtt.version>
-Allow for blank username and password in publisher and subscriber
-Added clean session checkbox in publisher gui instead of hard coded false

Note: AWS IoT needs you to use qos=1, isRetained = false, Clean session = true, username and password blank
You also need to load the certificates and keys in the JVM, scope is outside of this document

other minor changes:
-Fix a NPE with the Util UUID generator substring method
-set the Sampler label name in the response sampler for nicer display in View Result Tree Listeners
