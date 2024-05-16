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
    private final CountDownLatch latch2;

    public MultiTranslationThread(IProducer producer, String topic, String message
            , CountDownLatch latch, CountDownLatch latch2) {
        this.latch = latch;
        this.producer = producer;
        this.topic = topic;
        this.message = message;
        this.latch2 = latch2;
    }

    @Override
    public void run() {

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n\t" + Thread.currentThread() + " producing for " + this.producer);

        for(int i=0;i<500000;i++) {
            this.producer.produce(this.topic, String.valueOf(i));
        }

        System.out.println("\n\t" + Thread.currentThread() + " finished...");
        latch2.countDown();
    }
}
