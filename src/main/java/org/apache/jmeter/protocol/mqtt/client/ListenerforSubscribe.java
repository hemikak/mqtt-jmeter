package org.apache.jmeter.protocol.mqtt.client;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

public class ListenerforSubscribe implements Listener {

    public static AtomicLong count = new AtomicLong(0);
    public static BlockingQueue<String> queue;

    @Override
    public void onConnected() {
        System.out.println("Subscriber is listening");
        queue = new LinkedBlockingQueue<String>();

    }

    @Override
    public void onDisconnected() {
        System.out.println("Subscriber disabled listening");
        queue = null;
    }

    @Override
    public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
        if (null != queue) {
            String message = new String(body.getData());
            try {
                System.out.println("Received : " + message);
                queue.put(message);
                count.getAndIncrement();
                ack.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Throwable value) {
        System.out.println("Subscriber couldn't set up listener");
        System.out.println(value);
    }

}
