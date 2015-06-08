package org.apache.jmeter.protocol.mqtt.utilities;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 *
 */
public class Utils {
    public static String UUIDGenerator() throws NoSuchAlgorithmException {
        SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
        HashIDGenerator hashIDGenerator = new HashIDGenerator("jmeter-mqtt", 5);
        return hashIDGenerator.encrypt(prng.nextLong());
    }
}
