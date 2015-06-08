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
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
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
    private static final String GENERATE_CLIENT_ID_COMMAND = "generate_client_id";
    private static final String RESET_CREDENTIALS = "reset_credentials";

    public static final String AT_MOST_ONCE = "mqtt_at_most_once";
    public static final String EXACTLY_ONCE = "mqtt_extactly_once";
    public static final String AT_LEAST_ONCE = "mqtt_at_least_once";
    private static final String[] QOS_TYPES_ITEMS = {AT_MOST_ONCE, AT_LEAST_ONCE, EXACTLY_ONCE};

    public static final String BLOCKING_CLIENT = "mqtt_blocking_client";
    public static final String ASYNC_CLIENT = "mqtt_async_client";
    private static final String[] CLIENT_TYPES_ITEMS = {BLOCKING_CLIENT, ASYNC_CLIENT};

    public static final String FILE_INPUT = "mqtt_message_input_type_file";
    public static final String TEXT_INPUT = "mqtt_message_input_type_text";
    private static final String[] MESSAGE_INPUT_TYPE = {TEXT_INPUT, FILE_INPUT};

    private final JLabeledTextField brokerUrlField = new JLabeledTextField(JMeterUtils.getResString
            ("mqtt_provider_url"));
    private final JLabeledTextField clientId = new JLabeledTextField(JMeterUtils.getResString("mqtt_client_id"));
    private final JButton generateClientID = new JButton(JMeterUtils.getResString("mqtt_client_id_generator"));

    private final JLabeledTextField mqttDestination = new JLabeledTextField(JMeterUtils.getResString("mqtt_topic"));

    private final JCheckBox retained = new JCheckBox(JMeterUtils.getResString("mqtt_send_as_retained_msg"), false);

    private final JLabeledTextField mqttUser = new JLabeledTextField(JMeterUtils.getResString("mqtt_user"));
    private final JLabeledTextField mqttPwd = new JLabeledPasswordField(JMeterUtils.getResString("mqtt_pwd"));
    private final JButton resetUserNameAndPassword = new JButton(JMeterUtils.getResString
            ("mqtt_reset_username_password"));

    private final JLabeledRadioI18N typeQoSValue = new JLabeledRadioI18N("mqtt_qos", QOS_TYPES_ITEMS, AT_MOST_ONCE);
    private final JLabeledRadioI18N typeClientValue = new JLabeledRadioI18N("mqtt_client_types", CLIENT_TYPES_ITEMS,
            BLOCKING_CLIENT);

    private final JLabeledRadioI18N messageInputValue = new JLabeledRadioI18N("mqtt_message_input_type",
            MESSAGE_INPUT_TYPE,
            TEXT_INPUT);
    private final JLabel textArea = new JLabel(JMeterUtils.getResString("mqtt_text_area"));
    private final JSyntaxTextArea textMessage = new JSyntaxTextArea(10, 50);
    private final JTextScrollPane textPanel = new JTextScrollPane(textMessage);

    private final FilePanel fileChooser = new FilePanel(JMeterUtils.getResString("mqtt_file"), "*");


    public MQTTPublisherGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "mqtt_publisher";
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
        if (messageInputValue.getText().equals(TEXT_INPUT)) {
            sampler.setMessageValue(textMessage.getText());
        } else if (messageInputValue.getText().equals(FILE_INPUT)) {
            sampler.setMessageValue(fileChooser.getFilename());
        }
    }

    private void init() {
        brokerUrlField.setText(JMeterUtils.getResString("mqtt_url_default"));
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
        generateClientID.setActionCommand(GENERATE_CLIENT_ID_COMMAND);
        resetUserNameAndPassword.setActionCommand(RESET_CREDENTIALS);
        generateClientID.addActionListener(this);
        resetUserNameAndPassword.addActionListener(this);
        messageInputValue.addChangeListener(this);
        brokerUrlField.setText(JMeterUtils.getResString("mqtt_url_default"));

        this.textArea.setVisible(true);
        this.textPanel.setVisible(true);
        this.fileChooser.setVisible(false);

    }

    /**
     * @return JPanel Panel with checkbox to choose  user and password
     */
    private Component createAuthPane() {
        mqttUser.setText(JMeterUtils.getResString("mqtt_user_username"));
        mqttPwd.setText(JMeterUtils.getResString("mqtt_user_password"));
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

        if (sampler.getMessageInputType().equals(TEXT_INPUT)) {
            textMessage.setText(sampler.getMessageValue());
            this.textArea.setVisible(true);
            this.textPanel.setVisible(true);
            this.fileChooser.setVisible(false);
        } else if (sampler.getMessageInputType().equals(FILE_INPUT)) {
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
            if (GENERATE_CLIENT_ID_COMMAND.equals(e.getActionCommand())) {
                clientId.setText(Utils.UUIDGenerator());
            } else if (RESET_CREDENTIALS.equals(e.getActionCommand())) {
                mqttUser.setText(JMeterUtils.getResString("mqtt_user_username"));
                mqttPwd.setText(JMeterUtils.getResString("mqtt_user_password"));
            }
        } catch (NoSuchAlgorithmException e1) {
            log.error(e1.toString());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (TEXT_INPUT.equals(messageInputValue.getText())) {
            this.textArea.setVisible(true);
            this.textPanel.setVisible(true);
            this.fileChooser.setVisible(false);
        } else if (FILE_INPUT.equals(messageInputValue.getText())) {
            this.textArea.setVisible(false);
            this.textPanel.setVisible(false);
            this.fileChooser.setVisible(true);
        }
    }
}
 