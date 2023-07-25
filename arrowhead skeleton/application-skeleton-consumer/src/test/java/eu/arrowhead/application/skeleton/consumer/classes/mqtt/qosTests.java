package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IProducer;
import common.ProducerAndConsumerPair;
import eu.arrowhead.application.skeleton.consumer.MiddlewareSetup;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitConsumerSettings;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class qosTests {

    static MqttCustomConsumer exactlyOnceMqtt;
    static KafkaCustomConsumer exactlyOnceKafka;
    static RabbitCustomConsumer exactlyOnceRabbit;

    static MqttCustomConsumer atMostOnceMqtt;
    static KafkaCustomConsumer atMostOnceKafka;
    static RabbitCustomConsumer atMostOnceRabbit;

    static MqttCustomProducer mqttProducer;
    static KafkaCustomProducer kafkaProducer;
    static RabbitCustomProducer rabbitProducer;

    @BeforeAll
    static void setUp() {
        ConnectionDetails mqttConnection;
        ConnectionDetails kafkaConnection;
        ConnectionDetails rabbitConnection;

        MiddlewareSetup midSetup = new MiddlewareSetup(null);
        try {
            mqttConnection = midSetup.loadBroker("mqtt");
            kafkaConnection = midSetup.loadBroker("kafka");
            rabbitConnection = midSetup.loadBroker("rabbit");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        List<IProducer> producerList = new ArrayList<>();
        Map<String, String> settings = new HashMap<>();
        settings.put("qos", "2");
        settings.put("topic", "teste");
        settings.put("exchange","");
        settings.put("routing.key","teste");

        exactlyOnceKafka = new KafkaCustomConsumer(kafkaConnection,producerList,settings);
        exactlyOnceMqtt = new MqttCustomConsumer(mqttConnection,producerList,settings);
        exactlyOnceRabbit = new RabbitCustomConsumer(rabbitConnection,producerList,settings);

        new Thread(exactlyOnceKafka).start();
        new Thread(exactlyOnceMqtt).start();
        new Thread(exactlyOnceRabbit).start();

        settings = new HashMap<>();

        settings.put("identifier.gen","messageBody");
        settings.put("topic","test");
        settings.put("exchange","");
        settings.put("routing.key","test");


        atMostOnceKafka = new KafkaCustomConsumer(kafkaConnection,producerList,settings);
        atMostOnceMqtt = new MqttCustomConsumer(mqttConnection,producerList,settings);
        atMostOnceRabbit = new RabbitCustomConsumer(rabbitConnection,producerList,settings);

        new Thread(atMostOnceKafka).start();
        new Thread(atMostOnceMqtt).start();
        new Thread(atMostOnceRabbit).start();


        settings.put("qos","2");
        settings.put("topic","");

        mqttProducer = new MqttCustomProducer(mqttConnection,settings);
        kafkaProducer = new KafkaCustomProducer(kafkaConnection,settings);
        rabbitProducer = new RabbitCustomProducer(rabbitConnection,settings);
    }

    @ParameterizedTest
    @MethodSource("provideAtMostOnce")
    void verifyAtMostOnce(ProducerAndConsumerPair pair) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            pair.producer.produce("test","TEST_DATA");
        }
        Thread.sleep(Duration.ofSeconds(4).toMillis());
        assertEquals(10,pair.consumer.getNumberOfMessages());
    }

    @ParameterizedTest
    @MethodSource("provideExactlyOnce")
    void verifyExactlyOnce(ProducerAndConsumerPair pair) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            pair.producer.produce("teste","TEST-DATA");
        }
        Thread.sleep(Duration.ofSeconds(4).toMillis());
        assertEquals(1,pair.consumer.getNumberOfMessages());
    }

    @ParameterizedTest
    @MethodSource("provideExactlyOnce")
    void verifyExactlyOnce2(ProducerAndConsumerPair pair) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            pair.producer.produce("teste",String.valueOf(i));
        }
        Thread.sleep(Duration.ofSeconds(4).toMillis());
        assertEquals(10,pair.consumer.getNumberOfMessages());
    }

    static Stream<ProducerAndConsumerPair> provideExactlyOnce() {
        return Stream.of(new ProducerAndConsumerPair(kafkaProducer,exactlyOnceKafka)
                , new ProducerAndConsumerPair(rabbitProducer,exactlyOnceRabbit));
    }

    static Stream<ProducerAndConsumerPair> provideAtMostOnce() {
        return Stream.of(new ProducerAndConsumerPair(kafkaProducer,atMostOnceKafka)
                , new ProducerAndConsumerPair(rabbitProducer,atMostOnceRabbit));
    }
}
