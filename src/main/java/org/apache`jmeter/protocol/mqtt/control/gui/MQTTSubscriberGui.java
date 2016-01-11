/**
 * Author : Hemika Yasinda Kodikara
 *
 * Copyright (c) 2015.
 */

package org.apache.jmeter.protocol.mqtt.control.gui;

import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.mqtt.sampler.SubscriberSampler;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the GUI for mqtt Subscriber <br>
 */
public class MQTTSubscriberGui extends AbstractSamplerGui implements ActionListener {

    private static final long serialVersionUID = 240L;
    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();

    private static final String[] QOS_TYPES_ITEMS = {Constants.MQTT_AT_MOST_ONCE, Constants.MQTT_AT_LEAST_ONCE, Constants.MQTT_EXACTLY_ONCE};
    private static final String[] CLIENT_TYPES_ITEMS = {Constants.MQTT_BLOCKING_CLIENT, Constants.MQTT_ASYNC_CLIENT};

    private final JLabeledTextField brokerUrlField = new JLabeledTextField(Constants.MQTT_PROVIDER_URL);
    private final JLabeledTextField clientId = new JLabeledTextField(Constants.MQTT_CLIENT_ID);
    private final JButton generateClientID = new JButton(Constants.MQTT_CLIENT_ID_GENERATOR);

    private final JLabeledTextField mqttDestination = new JLabeledTextField(Constants.MQTT_TOPIC);

    private final JCheckBox cleanSession = new JCheckBox(Constants.MQTT_CLEAN_SESSION, false);

    private final JLabeledTextField mqttUser = new JLabeledTextField(Constants.MQTT_USERNAME);
    private final JLabeledTextField mqttPwd = new JLabeledPasswordField(Constants.MQTT_PASSWORD);
    private final JButton resetUserNameAndPassword = new JButton(Constants.MQTT_RESET_USERNAME_PASSWORD);

    private final JLabeledRadioI18N typeQoSValue = new JLabeledRadioI18N(Constants.MQTT_QOS, QOS_TYPES_ITEMS, Constants.MQTT_AT_MOST_ONCE);
    private final JLabeledRadioI18N typeClientValue = new JLabeledRadioI18N(Constants.MQTT_CLIENT_TYPES, CLIENT_TYPES_ITEMS,
                                                                            Constants.MQTT_BLOCKING_CLIENT);

    public MQTTSubscriberGui() {
        init();
    }


    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return Constants.MQTT_SUBSCRIBER_TITLE;
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        SubscriberSampler sampler = new SubscriberSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement s) {
        SubscriberSampler sampler = (SubscriberSampler) s;
        this.configureTestElement(sampler);
        sampler.setBrokerUrl(brokerUrlField.getText());
        sampler.setClientId(clientId.getText());
        sampler.setTopicName(mqttDestination.getText());
        sampler.setCleanSession(cleanSession.isSelected());
        sampler.setUsername(mqttUser.getText());
        sampler.setPassword(mqttPwd.getText());
        sampler.setQOS(typeQoSValue.getText());
        sampler.setClientType(typeClientValue.getText());

    }

    private void init() {
        brokerUrlField.setText(Constants.MQTT_URL_DEFAULT);
                               setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        JPanel mainPanel = new VerticalPanel();
        add(mainPanel, BorderLayout.CENTER);
        JPanel DPanel = new JPanel();
        DPanel.setLayout(new BoxLayout(DPanel, BoxLayout.X_AXIS));
        DPanel.add(brokerUrlField);
        DPanel.add(clientId);
        DPanel.add(generateClientID);
        JPanel ControlPanel = new VerticalPanel();
        ControlPanel.add(DPanel);
        ControlPanel.add(createDestinationPane());
        ControlPanel.add(cleanSession);
        ControlPanel.add(createAuthPane());
        ControlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray),
                "Connection Info"));
        mainPanel.add(ControlPanel);
        JPanel TPanel = new VerticalPanel();
        TPanel.setLayout(new BoxLayout(TPanel, BoxLayout.X_AXIS));
        typeQoSValue.setLayout(new BoxLayout(typeQoSValue, BoxLayout.X_AXIS));
        typeClientValue.setLayout(new BoxLayout(typeClientValue, BoxLayout.X_AXIS));
        TPanel.add(typeQoSValue);
        TPanel.add(typeClientValue);
        TPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Option"));
        mainPanel.add(TPanel);

        generateClientID.setActionCommand(Constants.GENERATE_CLIENT_ID_COMMAND);
        resetUserNameAndPassword.setActionCommand(Constants.RESET_CREDENTIALS);
        generateClientID.addActionListener(this);
        resetUserNameAndPassword.addActionListener(this);
        brokerUrlField.setText(Constants.MQTT_URL_DEFAULT);

    }

    /**
     * @return JPanel Panel with checkbox to choose  user and password
     */
    private Component createAuthPane() {
        mqttUser.setText(Constants.MQTT_USER_USERNAME);
        mqttPwd.setText(Constants.MQTT_USER_PASSWORD);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(mqttUser);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(mqttPwd);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(resetUserNameAndPassword);
        return panel;
    }

    /**
     * the implementation loads the URL and the soap action for the request.
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        SubscriberSampler sampler = (SubscriberSampler) el;
        brokerUrlField.setText(sampler.getBrokerUrl());
        clientId.setText(sampler.getClientId());
        mqttDestination.setText(sampler.getTopicName());
        cleanSession.setSelected(sampler.isCleanSession());
        mqttUser.setText(sampler.getUsername());
        mqttPwd.setText(sampler.getPassword());
        typeQoSValue.setText(sampler.getQOS());
        typeClientValue.setText(sampler.getClientType());
    }

    @Override
    public void clearGui() {
        super.clearGui();
    }

    private JPanel createDestinationPane() {
        JPanel panel = new VerticalPanel(); //new BorderLayout(3, 0)
        this.mqttDestination.setLayout((new BoxLayout(mqttDestination, BoxLayout.X_AXIS)));
        panel.add(mqttDestination);
        JPanel TPanel = new JPanel();
        TPanel.setLayout(new BoxLayout(TPanel, BoxLayout.X_AXIS));
        TPanel.add(Box.createHorizontalStrut(100));
        panel.add(TPanel);        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.GENERATE_CLIENT_ID_COMMAND.equals(e.getActionCommand())) {
            clientId.setText(Utils.UUIDGenerator());
        } else if(Constants.RESET_CREDENTIALS.equals(e.getActionCommand())){
            mqttUser.setText(Constants.MQTT_USER_USERNAME);
            mqttPwd.setText(Constants.MQTT_USER_PASSWORD);
        }
    }
}