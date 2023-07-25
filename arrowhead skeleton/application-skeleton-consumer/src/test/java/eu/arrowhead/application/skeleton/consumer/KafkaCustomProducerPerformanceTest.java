package eu.arrowhead.application.skeleton.consumer;

import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KafkaCustomProducerPerformanceTest {

    private KafkaCustomProducer producer;

    private KafkaCustomConsumer consumer;

    private MqttCustomProducer mqttProducer;

    private MqttCustomConsumer mqttConsumer;

    private final float numberOfMessages = 100000;

    @BeforeEach
    void setUp() {
        Map<String,String> settings = new HashMap<>();
        settings.put("topic","test");
        settings.put("client.id","teste");
        settings.put("qos","0");

        List<IProducer> producerList = new ArrayList<>();
        List<IProducer> producerList2 = new ArrayList<>();

        producer = new KafkaCustomProducer(new ConnectionDetails("localhost",29092),settings);
        producerList.add(producer);
        mqttProducer = new MqttCustomProducer(new ConnectionDetails("localhost",1883),settings);
        producerList2.add(mqttProducer);

        consumer = new KafkaCustomConsumer(new ConnectionDetails("localhost",29092),producerList2,settings);
        mqttConsumer = new MqttCustomConsumer(new ConnectionDetails("localhost",1883),producerList,settings);
    }

    @Test
    void aaa() {
        for (int i = 0; i < 100000; i++) {
            producer.produce("test",String.valueOf(i));
        }
    }


}