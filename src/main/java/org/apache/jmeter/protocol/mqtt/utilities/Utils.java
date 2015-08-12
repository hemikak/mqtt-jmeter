/**
 * Author : Hemika Yasinda Kodikara
 *
 * Copyright (c) 2015.
 */

package org.apache.jmeter.protocol.mqtt.utilities;

import org.apache.jorphan.io.TextFile;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for plugin
 */
public class Utils {

    /**
     * Creates a UUID. The UUID is modified to avoid "ClientId longer than 23 characters" for MQTT.
     *
     * @return A UUID as a string.
     * @throws NoSuchAlgorithmException
     */
    public static String UUIDGenerator() throws NoSuchAlgorithmException {
        String clientId = System.currentTimeMillis() + "." + System.getProperty("user.name");
        clientId = clientId.substring(0, 23);
        return clientId;
    }

    /**
     * The implementation uses TextFile to load the contents of the file and
     * returns a string.
     *
     * @param path path to the file to read in
     * @return the contents of the file
     */
    public static String getFileContent(String path) {
        TextFile tf = new TextFile(path);
        return tf.getText();
    }
}
