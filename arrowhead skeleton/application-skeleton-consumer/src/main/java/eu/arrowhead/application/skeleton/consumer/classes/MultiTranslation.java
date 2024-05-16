package eu.arrowhead.application.skeleton.consumer.classes;

import common.ConnectionDetails;
import eu.arrowhead.application.skeleton.consumer.classes.dds.DDSCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class MultiTranslation {

    private final static String topic = "ABC";
    private final static String message = "Teste";
    private final static String address = "192.168.1.216";
    private final static int qos = 2;

    public static void main(String[] args) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        Map<String, String> settings = new HashMap<>();
        settings.put("topic", topic);
        settings.put("qos", String.valueOf(qos));
        settings.put("client.id", "middleware-producer");

        Map<String, String> rabbitSettings = new HashMap<>();
        rabbitSettings.put("topic", topic);
        rabbitSettings.put("qos", String.valueOf(qos));
        rabbitSettings.put("client.id", "middleware-producer");

        DDSCustomProducer dds = new DDSCustomProducer(new ConnectionDetails(address, 12345), settings);
        MqttCustomProducer mqtt = new MqttCustomProducer(new ConnectionDetails(address, 1883), settings);
        KafkaCustomProducer kafka = new KafkaCustomProducer(new ConnectionDetails(address, 9092), settings);
        RabbitCustomProducer rabbit = new RabbitCustomProducer(new ConnectionDetails(address, 15672), rabbitSettings);

        new Thread(new MultiTranslationThread(dds, topic, message, latch, latch2)).start();
        // new Thread(new MultiTranslationThread(mqtt, topic, message, latch, latch2)).start();
        // new Thread(new MultiTranslationThread(kafka, topic, message, latch, latch2)).start();
        // new MultiTranslationThread(kafka, topic, message, latch, latch2).run();
        // new Thread(new MultiTranslationThread(rabbit, topic, message, latch, latch2)).start();

        System.out.println("\n\tAll producers created, running them...");
        latch.countDown();

        latch2.await();

        System.out.println("All threads finished...");
        System.exit(0);

    }
}
