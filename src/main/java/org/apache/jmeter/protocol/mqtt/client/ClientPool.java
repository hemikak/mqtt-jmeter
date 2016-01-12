/**
 * Author : Hemika Yasinda Kodikara
 *
 * Copyright (c) 2016.
 */

package org.apache.jmeter.protocol.mqtt.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ClientPool holds the client instances in an ArrayList. The main purpose of
 * this is to make it easier to clean up all the instances at the end of a test.
 * If we didn't do this, threads might become zombie.
 * <p/>
 * N.B. This class needs to be fully synchronized as it is called from sample threads
 * and the thread that runs testEnded() methods.
 */
public class ClientPool {

    private static final ArrayList<Closeable> clients = new ArrayList<Closeable>();

    /**
     * Add a ReceiveClient to the ClientPool. This is so that we can make sure
     * to close all clients and make sure all threads are destroyed.
     *
     * @param client the ReceiveClient to add
     */
    public static synchronized void addClient(Closeable client) {
        clients.add(client);
    }

    /**
     * Clear all the clients created by either Publish or Subscribe sampler. We
     * need to do this to make sure all the threads created during the test are
     * destroyed and cleaned up.
     */
    public static synchronized void clearClient() throws IOException {
        for (Closeable client : clients) {
            client.close();
        }
        clients.clear();
    }
}
