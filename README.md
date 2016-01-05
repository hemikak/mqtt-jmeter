mqtt-jmeter
===========

# Introduction

This project is a JMeter plugin for MQTT(Message Queuing Telemetry Transport) protocol. The plugin uses Eclipse Paho MQTT client(0.4.0) for the subscriber and publisher. This is still an ongoing project.

# How to install MQTT plugin in JMeter

From the repository: https://github.com/hemikak/mqtt-jmeter
Get the source code, go to mqtt-jmeter folder and and use the command maven in terminal:

	mvn clean install

Once its done, go to the target folder and run the following command in cmd/terminal with the path to JMeter. Example :

	./installer.sh ~/Documents/wso2/mb/clients/apache-jmeter-2.13/

Or

    installer.bat C:\apache-jmeter-2.13

This would do the following tasks...
* Copies the **mqtt-jmeter.jar** from the **target** folder to **lib/ext** folder of JMeter.
* Updates the file messages.properties in the folder :/org/apache/jmeter/resources/
in **ApacheJMeter_core.jar** by new file messages.properties.

Regards,  
Hemika
    
