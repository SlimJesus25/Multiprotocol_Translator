package eu.arrowhead.application.skeleton.consumer.classes.dds;

import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.MiddlewareSetup;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;
import eu.arrowhead.application.skeleton.consumer.exceptions.InvalidQoSException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDSCustomTest {

    static DDSCustomConsumer ddsInternalConsumer;
    static DDSCustomProducer ddsInternalProducer;

    static KafkaCustomConsumer kafkaExternalConsumer;
    static KafkaCustomProducer kafkaExternalProducer;
    static KafkaCustomProducer kafkaInternalProducer;
    static KafkaCustomConsumer kafkaInternalConsumer;

    static MqttCustomConsumer mqttExternalConsumer;
    static MqttCustomProducer mqttExternalProducer;
    static MqttCustomConsumer mqttInternalConsumer;
    static MqttCustomProducer mqttInternalProducer;

    static RabbitCustomConsumer rabbitExternalConsumer;
    static RabbitCustomProducer rabbitExternalProducer;
    static RabbitCustomConsumer rabbitInternalConsumer;
    static RabbitCustomProducer rabbitInternalProducer;

    static Map<String, String> settings = new HashMap<>();
    static MiddlewareSetup midSetup;
    static List<IProducer> producerList;
    static ConnectionDetails ddsConnection;

    @BeforeAll
    static void setUp() {

        midSetup = new MiddlewareSetup(null);

        try {
            ddsConnection = midSetup.loadBroker("dds");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("identifier.gen","messageBody");
        settings.put("topic","ABC");
        settings.put("qos", "2");
        settings.put("exchange","");
        settings.put("routing.key","test");
    }

    @Test
    public void translationKafkaToDDSQoSAtMostOnce(){
        ConnectionDetails kafkaConnection;

        try {
            kafkaConnection = midSetup.loadBroker("kafka");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","0");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        kafkaExternalProducer = new KafkaCustomProducer(kafkaConnection, settings);
        kafkaInternalConsumer = new KafkaCustomConsumer(kafkaConnection, producerList, settings);

        new Thread(kafkaInternalConsumer).start();

        kafkaExternalProducer.produce("ABC", "test123");
    }

    @Test
    public void translationKafkaToDDSQoSAtLeastOnce(){
        ConnectionDetails kafkaConnection;

        try {
            kafkaConnection = midSetup.loadBroker("kafka");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","1");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        kafkaExternalProducer = new KafkaCustomProducer(kafkaConnection, settings);
        kafkaInternalConsumer = new KafkaCustomConsumer(kafkaConnection, producerList, settings);

        new Thread(kafkaInternalConsumer).start();

        kafkaExternalProducer.produce("ABC", "test123");
    }

    @Test
    public void translationKafkaToDDSQoSExactlyOnce(){
        ConnectionDetails kafkaConnection;

        try {
            kafkaConnection = midSetup.loadBroker("kafka");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","2");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        kafkaExternalProducer = new KafkaCustomProducer(kafkaConnection, settings);
        kafkaInternalConsumer = new KafkaCustomConsumer(kafkaConnection, producerList, settings);

        new Thread(kafkaInternalConsumer).start();

        kafkaExternalProducer.produce("ABC", "test123");
    }

    /* --------------------------------- */
    /* ------------ MQTT --------------- */
    /* --------------------------------- */

    @Test
    public void translationMQTTToDDSQoSAtMostOnce(){
        ConnectionDetails mqttConnection;

        try {
            mqttConnection = midSetup.loadBroker("mqtt");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","0");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        mqttExternalProducer = new MqttCustomProducer(mqttConnection, settings);
        mqttInternalConsumer = new MqttCustomConsumer(mqttConnection, producerList, settings);

        new Thread(mqttInternalConsumer).start();

        mqttExternalProducer.produce("ABC", "test123");
    }

    @Test
    public void translationMQTTToDDSQoSAtLeastOnce(){
        ConnectionDetails mqttConnection;

        try {
            mqttConnection = midSetup.loadBroker("mqtt");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","1");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        mqttExternalProducer = new MqttCustomProducer(mqttConnection, settings);
        mqttInternalConsumer = new MqttCustomConsumer(mqttConnection, producerList, settings);

        new Thread(mqttInternalConsumer).start();

        mqttExternalProducer.produce("ABC", "test123");
    }

    @Test
    public void translationMQTTToDDSQoSExactlyOnce(){
        ConnectionDetails mqttConnection;

        try {
            mqttConnection = midSetup.loadBroker("mqtt");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","2");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        mqttExternalProducer = new MqttCustomProducer(mqttConnection, settings);
        mqttInternalConsumer = new MqttCustomConsumer(mqttConnection, producerList, settings);

        new Thread(mqttInternalConsumer).start();

        mqttExternalProducer.produce("ABC", "test123");
    }

    /* --------------------------------- */
    /* ----------- RabbitMQ ------------ */
    /* --------------------------------- */

    @Test
    public void translationRabbitToDDSQoSAtMostOnce(){
        ConnectionDetails rabbitConnection;

        try {
            rabbitConnection = midSetup.loadBroker("rabbit");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","0");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        rabbitExternalProducer = new RabbitCustomProducer(rabbitConnection, settings);
        rabbitInternalConsumer = new RabbitCustomConsumer(rabbitConnection, producerList, settings);

        new Thread(rabbitInternalConsumer).start();

        rabbitExternalProducer.produce("ABC", "test123");
    }

    @Test
    public void translationRabbitToDDSQoSAtLeastOnce(){
        ConnectionDetails rabbitConnection;

        try {
            rabbitConnection = midSetup.loadBroker("rabbit");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","1");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        rabbitExternalProducer = new RabbitCustomProducer(rabbitConnection, settings);
        rabbitInternalConsumer = new RabbitCustomConsumer(rabbitConnection, producerList, settings);

        new Thread(rabbitInternalConsumer).start();

        rabbitExternalProducer.produce("ABC", "test123");
    }

    @Test
    public void translationRabbitToDDSQoSExactlyOnce(){
        ConnectionDetails rabbitConnection;

        try {
            rabbitConnection = midSetup.loadBroker("rabbit");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","2");

        ddsInternalProducer = new DDSCustomProducer(ddsConnection, settings);
        producerList = new ArrayList<>();
        producerList.add(ddsInternalProducer);

        rabbitExternalProducer = new RabbitCustomProducer(rabbitConnection, settings);
        rabbitInternalConsumer = new RabbitCustomConsumer(rabbitConnection, producerList, settings);

        new Thread(rabbitInternalConsumer).start();

        rabbitExternalProducer.produce("ABC", "test123");
    }

    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    ////////////////////////////////////////////////

    @Test
    public void translationDDSToRabbitAtMostOnce(){

        ConnectionDetails rabbitConnection;

        try {
            rabbitConnection = midSetup.loadBroker("rabbit");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        settings.put("qos","0");

        rabbitInternalProducer = new RabbitCustomProducer(rabbitConnection, settings);

        List<IProducer> producerList = new ArrayList<>();
        producerList.add(rabbitInternalProducer);

        ddsInternalConsumer = new DDSCustomConsumer(ddsConnection, producerList, settings);
        new Thread(ddsInternalConsumer).start();

    }

    @Test
    public void multiTranslationDDS() throws InterruptedException {

        ConnectionDetails mqttConnection;
        ConnectionDetails rabbitConnection;
        ConnectionDetails kafkaConnection;

        try {
            rabbitConnection = midSetup.loadBroker("rabbit");
            mqttConnection = midSetup.loadBroker("mqtt");
            kafkaConnection = midSetup.loadBroker("kafka");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        mqttInternalProducer = new MqttCustomProducer(mqttConnection, settings);
        kafkaInternalProducer = new KafkaCustomProducer(kafkaConnection, settings);
        rabbitInternalProducer = new RabbitCustomProducer(rabbitConnection, settings);

        rabbitExternalConsumer = new RabbitCustomConsumer(rabbitConnection, null, settings);
        kafkaExternalConsumer = new KafkaCustomConsumer(kafkaConnection, null, settings);
        mqttExternalConsumer = new MqttCustomConsumer(mqttConnection, null, settings);

        List<IProducer> producerList = new ArrayList<>();
        producerList.add(rabbitInternalProducer);
        producerList.add(kafkaInternalProducer);
        producerList.add(kafkaInternalProducer);

        ddsInternalConsumer = new DDSCustomConsumer(ddsConnection, producerList, settings);

        new Thread(ddsInternalConsumer).start();

        List<String> results = new ArrayList<>();
        results.add(kafkaExternalConsumer.getLastMessage());
        results.add(rabbitExternalConsumer.getLastMessage());

        final String finalMessage = "Test";

        Thread.sleep(10000);

        for(String s : results)
            Assertions.assertEquals(s, finalMessage);

    }

    @Test
    public void invalidQoSThrowsQoSException(){

        Map<String, String> settings = new HashMap<>();

        settings.put("topic","test");
        settings.put("qos", "-1");

        Assertions.assertThrows(InvalidQoSException.class, () -> new DDSCustomProducer(ddsConnection, settings));

        settings.put("topic","test");
        settings.put("qos","3");

        Assertions.assertThrows(InvalidQoSException.class, () -> new DDSCustomProducer(ddsConnection, settings));
    }

    @Test
    public void nullQoSThrowsQoSException(){

        Map<String, String> settings = new HashMap<>();

        settings.put("topic","test");

        Assertions.assertThrows(InvalidQoSException.class, () -> new DDSCustomProducer(ddsConnection, settings));

    }


}
