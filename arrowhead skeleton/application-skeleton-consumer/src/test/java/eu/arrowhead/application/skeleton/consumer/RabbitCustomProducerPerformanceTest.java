package eu.arrowhead.application.skeleton.consumer;

import common.ConnectionDetails;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;

import java.util.HashMap;
import java.util.Map;

class RabbitCustomProducerPerformanceTest {

    private RabbitCustomProducer producer;

    private float numberOfMessages = 100000f;

    @org.junit.jupiter.api.Test
    void timer() {
        Map<String,String> settings = new HashMap<>();
        settings.put("queue","mesa");
        settings.put("client.id","client03");
        settings.put("qos","0");
        producer = new RabbitCustomProducer(new ConnectionDetails("192.168.0.104",5672),settings);

        float timeS = System.nanoTime();
        produce();
        float endTime = System.nanoTime();
        float totalTime = ((endTime - timeS) / 1000000000);
        System.out.println("Total time = " + totalTime);
        System.out.println("Average time per rabbit produce = " + (totalTime) / numberOfMessages);
    }


    void produce() {
        for (int i = 0; i < (int) numberOfMessages; i++) {
            producer.produce("banana",String.valueOf(i));
        }
    }
}