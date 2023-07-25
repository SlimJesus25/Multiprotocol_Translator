package eu.arrowhead.application.skeleton.consumer;

import common.ConnectionDetails;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class MqttCustomProducerPerformanceTest {

    private MqttCustomProducer producer;

    private final float numberOfMessages = 100000;

    @BeforeEach
    void setUp() {
        Map<String,String> settings = new HashMap<>();
        settings.put("topic","teste");
        settings.put("client.id","tehuhst");
        settings.put("qos","0");

        producer = new MqttCustomProducer(new ConnectionDetails("192.168.0.104",31671),settings);
    }

    @Test
    void timer() {
        float timeS = System.nanoTime();
        produce();
        float endTime = System.nanoTime();
        float totalTime = ((endTime - timeS) / 1000000000);
        System.out.println("Total time = " + totalTime);
        System.out.println("Average time per mqtt produce = " + (totalTime) / numberOfMessages);
    }

    void produce() {
        for (int i = 0; i < (int) numberOfMessages; i++) {
            producer.produce("banana",String.valueOf(i));
        }
    }
}