package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IProducer;
import common.ProducerAndConsumerPair;
import eu.arrowhead.application.skeleton.consumer.MiddlewareSetup;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.junit.jupiter.api.BeforeAll;
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

class SystemTests {

    static MiddlewareSetup midSetup;

    static MqttCustomConsumer mqttConsumer;
    static KafkaCustomConsumer kafkaConsumer;
    static RabbitCustomConsumer rabbitConsumer;

    static MqttCustomProducer mqttProducer;
    static KafkaCustomProducer kafkaProducer;
    static RabbitCustomProducer rabbitProducer;

    static ConnectionDetails mqttConnection;
    static ConnectionDetails kafkaConnection;
    static ConnectionDetails rabbitConnection;

    MqttTopic topic;

    @BeforeAll
    static void setUp() {
        midSetup = new MiddlewareSetup(null);
        try {
            mqttConnection = midSetup.loadBroker("mqtt");
            kafkaConnection = midSetup.loadBroker("kafka");
            rabbitConnection = midSetup.loadBroker("rabbit");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<IProducer> producerList = new ArrayList<>();
        Map<String,String> settings = new HashMap<>();
        settings.put("qos","2");
        settings.put("consumerTopic","prefix/");

        mqttProducer = new MqttCustomProducer(mqttConnection,settings);
        kafkaProducer = new KafkaCustomProducer(kafkaConnection,settings);

        settings.put("queue","");
        rabbitProducer = new RabbitCustomProducer(rabbitConnection,settings);

        settings.put("topic","teste");
        settings.put("exchange","");
        settings.put("routing.key","teste");


        mqttConsumer = new MqttCustomConsumer(mqttConnection,producerList,settings);
        new Thread(mqttConsumer).start();

        kafkaConsumer = new KafkaCustomConsumer(kafkaConnection,producerList,settings);
        new Thread(kafkaConsumer).start();

        rabbitConsumer = new RabbitCustomConsumer(rabbitConnection,producerList,settings);
        new Thread(rabbitConsumer).start();
    }

    @ParameterizedTest
    @MethodSource("provideProducers")
    void messagesContentsArePreserved(ProducerAndConsumerPair pair) throws InterruptedException {
        setUp();
        pair.producer.produce("teste","TEST-DATA");
        Thread.sleep(Duration.ofSeconds(3).toMillis());
        assertEquals("TEST-DATA",pair.consumer.getLastMessage());
    }

    @ParameterizedTest
    @MethodSource("provideProducers")
    void producerUsesCorrectTopic(ProducerAndConsumerPair pair) throws InterruptedException {
        pair.producer.produce("prefix/notTeste","FAKE-DATA");
        Thread.sleep(Duration.ofSeconds(3).toMillis());
        assertNotEquals("FAKE-DATA",pair.consumer.getLastMessage());

        pair.producer.produce("prefix/teste","TEST-DATA");
        Thread.sleep(Duration.ofSeconds(3).toMillis());
        assertEquals("TEST-DATA",pair.consumer.getLastMessage());
    }

    @ParameterizedTest
    @MethodSource("provideProducers")
    void allMessagesAreRedirected(ProducerAndConsumerPair pair) throws InterruptedException {
        int initialMessages = pair.consumer.getNumberOfMessages();
        for (int i = 0; i < 1000; i++) {
            pair.producer.produce("teste", "TEST-DATA-" + i);
        }
        Thread.sleep(Duration.ofSeconds(6).toMillis());
        assertEquals(initialMessages + 1000,pair.consumer.getNumberOfMessages());
    }

    static Stream<ProducerAndConsumerPair> provideProducers() {
        return Stream.of(new ProducerAndConsumerPair(mqttProducer,mqttConsumer)
        , new ProducerAndConsumerPair(kafkaProducer,kafkaConsumer)
        , new ProducerAndConsumerPair(rabbitProducer,rabbitConsumer));
    }
}