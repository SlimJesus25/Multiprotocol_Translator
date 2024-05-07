package eu.arrowhead.application.skeleton.consumer.classes;

import common.IProducer;

import java.util.concurrent.CountDownLatch;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class MultiTranslationThread implements Runnable{

    private final IProducer producer;
    private final String topic;
    private final String message;
    private final CountDownLatch latch;

    public MultiTranslationThread(IProducer producer, String topic, String message, CountDownLatch latch) {
        this.latch = latch;
        this.producer = producer;
        this.topic = topic;
        this.message = message;
    }

    @Override
    public void run() {

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(int i=0;i<100000;i++)
            this.producer.produce(this.topic, this.message);
    }
}
