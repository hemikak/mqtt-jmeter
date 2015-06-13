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

package org.apache.jmeter.protocol.mqtt.control.gui;

import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.mqtt.sampler.PublisherSampler;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;

/**
 * @author Tuan Hiep
 */
public class MQTTPublisherGui extends AbstractSamplerGui implements
                                                         ChangeListener, ActionListener {

    private static final long serialVersionUID = 240L;
    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();

    private static final String[] QOS_TYPES_ITEMS = {Constants.MQTT_AT_MOST_ONCE, Constants.MQTT_AT_LEAST_ONCE, Constants.MQTT_EXACTLY_ONCE};


    private static final String[] CLIENT_TYPES_ITEMS = {Constants.MQTT_BLOCKING_CLIENT, Constants
            .MQTT_ASYNC_CLIENT};

    private static final String[] MESSAGE_INPUT_TYPE = {Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT, Constants.MQTT_MESSAGE_INPUT_TYPE_FILE};

    private final JLabeledTextField brokerUrlField = new JLabeledTextField(Constants.MQTT_PROVIDER_URL);
    private final JLabeledTextField clientId = new JLabeledTextField(Constants.MQTT_CLIENT_ID);
    private final JButton generateClientID = new JButton(Constants.MQTT_CLIENT_ID_GENERATOR);

    private final JLabeledTextField mqttDestination = new JLabeledTextField(Constants.MQTT_TOPIC);

    private final JCheckBox retained = new JCheckBox(Constants.MQTT_SEND_AS_RETAINED_MSG, false);

    private final JLabeledTextField mqttUser = new JLabeledTextField(Constants.MQTT_USERNAME);
    private final JLabeledTextField mqttPwd = new JLabeledPasswordField(Constants.MQTT_PASSWORD);
    private final JButton resetUserNameAndPassword = new JButton(Constants.MQTT_RESET_USERNAME_PASSWORD);

    private final JLabeledRadioI18N typeQoSValue = new JLabeledRadioI18N(Constants.MQTT_QOS, QOS_TYPES_ITEMS, Constants.MQTT_AT_MOST_ONCE);

    private final JLabeledRadioI18N typeClientValue = new JLabeledRadioI18N(Constants.MQTT_CLIENT_TYPES, CLIENT_TYPES_ITEMS,
                                                                            Constants.MQTT_BLOCKING_CLIENT);

    private final JLabeledRadioI18N messageInputValue = new JLabeledRadioI18N(Constants.MQTT_MESSAGE_INPUT_TYPE,
                                                                              MESSAGE_INPUT_TYPE,
                                                                              Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT);

    private final JLabel textArea = new JLabel(Constants.MQTT_TEXT_AREA);
    private final JSyntaxTextArea textMessage = new JSyntaxTextArea(10, 50);
    private final JTextScrollPane textPanel = new JTextScrollPane(textMessage);

    private final FilePanel fileChooser = new FilePanel(Constants.MQTT_FILE, "*");


    public MQTTPublisherGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return Constants.MQTT_PUBLISHER_TITLE;
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        PublisherSampler sampler = new PublisherSampler();
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
        PublisherSampler sampler = (PublisherSampler) s;
        this.configureTestElement(sampler);
        sampler.setBrokerUrl(brokerUrlField.getText());
        sampler.setClientId(clientId.getText());
        sampler.setTopicName(mqttDestination.getText());
        sampler.setMessageRetained(retained.isSelected());
        sampler.setUsername(mqttUser.getText());
        sampler.setPassword(mqttPwd.getText());
        sampler.setQOS(typeQoSValue.getText());
        sampler.setClientType(typeClientValue.getText());
        sampler.setMessageInputType(messageInputValue.getText());
        if (messageInputValue.getText().equals(Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT)) {
            sampler.setMessageValue(textMessage.getText());
        } else if (messageInputValue.getText().equals(Constants.MQTT_MESSAGE_INPUT_TYPE_FILE)) {
            sampler.setMessageValue(fileChooser.getFilename());
        }
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
        ControlPanel.add(retained);
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

        // Input type panels
        JPanel contentPanel = new VerticalPanel();
        messageInputValue.setLayout(new BoxLayout(messageInputValue, BoxLayout.X_AXIS));
        contentPanel.add(messageInputValue);

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(this.fileChooser, BorderLayout.CENTER);
        contentPanel.add(filePanel);

        // Text input panel
        JPanel messageContentPanel = new JPanel(new BorderLayout());
        messageContentPanel.add(this.textArea, BorderLayout.NORTH);
        messageContentPanel.add(this.textPanel, BorderLayout.CENTER);
        contentPanel.add(messageContentPanel);

        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Content"));
        mainPanel.add(contentPanel);


        // Setting default values and handlers
        generateClientID.setActionCommand(Constants.GENERATE_CLIENT_ID_COMMAND);
        resetUserNameAndPassword.setActionCommand(Constants.RESET_CREDENTIALS);
        generateClientID.addActionListener(this);
        resetUserNameAndPassword.addActionListener(this);
        messageInputValue.addChangeListener(this);
        brokerUrlField.setText(Constants.MQTT_URL_DEFAULT);

        this.textArea.setVisible(true);
        this.textPanel.setVisible(true);
        this.fileChooser.setVisible(false);

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
        PublisherSampler sampler = (PublisherSampler) el;
        brokerUrlField.setText(sampler.getBrokerUrl());
        clientId.setText(sampler.getClientId());
        mqttDestination.setText(sampler.getTopicName());
        retained.setSelected(sampler.isMessageRetained());
        mqttUser.setText(sampler.getUsername());
        mqttPwd.setText(sampler.getPassword());
        typeQoSValue.setText(sampler.getQOS());
        typeClientValue.setText(sampler.getClientType());
        messageInputValue.setText(sampler.getMessageInputType());

        if (sampler.getMessageInputType().equals(Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT)) {
            textMessage.setText(sampler.getMessageValue());
            this.textArea.setVisible(true);
            this.textPanel.setVisible(true);
            this.fileChooser.setVisible(false);
        } else if (sampler.getMessageInputType().equals(Constants.MQTT_MESSAGE_INPUT_TYPE_FILE)) {
            fileChooser.setFilename(sampler.getMessageValue());
            this.textArea.setVisible(false);
            this.textPanel.setVisible(false);
            this.fileChooser.setVisible(true);
        }
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
        panel.add(TPanel);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (Constants.GENERATE_CLIENT_ID_COMMAND.equals(e.getActionCommand())) {
                clientId.setText(Utils.UUIDGenerator());
            } else if (Constants.RESET_CREDENTIALS.equals(e.getActionCommand())) {
                mqttUser.setText(Constants.MQTT_USER_USERNAME);
                mqttPwd.setText(Constants.MQTT_USER_PASSWORD);
            }
        } catch (NoSuchAlgorithmException e1) {
            log.error(e1.toString());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT.equals(messageInputValue.getText())) {
            this.textArea.setVisible(true);
            this.textPanel.setVisible(true);
            this.fileChooser.setVisible(false);
        } else if (Constants.MQTT_MESSAGE_INPUT_TYPE_FILE.equals(messageInputValue.getText())) {
            this.textArea.setVisible(false);
            this.textPanel.setVisible(false);
            this.fileChooser.setVisible(true);
        }
    }
}
