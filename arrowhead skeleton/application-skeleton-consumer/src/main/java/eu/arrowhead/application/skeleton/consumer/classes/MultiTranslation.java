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
    private final static String address = "192.168.1.214";
    private final static int qos = 0;

    public static void main(String[] args){

        CountDownLatch latch = new CountDownLatch(1);

        Map<String, String> ddsSettings = new HashMap<>();
        ddsSettings.put("topic", topic);
        ddsSettings.put("qos", String.valueOf(qos));

        Map<String, String> mqttSettings = new HashMap<>();
        mqttSettings.put("topic", topic);
        mqttSettings.put("qos", String.valueOf(qos));

        Map<String, String> kafkaSettings = new HashMap<>();
        kafkaSettings.put("topic", topic);
        kafkaSettings.put("qos", String.valueOf(qos));

        Map<String, String> rabbitSettings = new HashMap<>();
        rabbitSettings.put("topic", topic);
        rabbitSettings.put("qos", String.valueOf(qos));

        DDSCustomProducer dds = new DDSCustomProducer(new ConnectionDetails(address, 12345), ddsSettings);
        MqttCustomProducer mqtt = new MqttCustomProducer(new ConnectionDetails(address, 1883), mqttSettings);
        KafkaCustomProducer kafka = new KafkaCustomProducer(new ConnectionDetails(address, 9092), kafkaSettings);
        RabbitCustomProducer rabbit = new RabbitCustomProducer(new ConnectionDetails(address, 15672), rabbitSettings);

        new Thread(new MultiTranslationThread(dds, topic, message, latch)).start();
        new Thread(new MultiTranslationThread(mqtt, topic, message, latch)).start();
        new Thread(new MultiTranslationThread(kafka, topic, message, latch)).start();
        new Thread(new MultiTranslationThread(rabbit, topic, message, latch)).start();

        new Thread(new MultiTranslationRunnable(latch)).start();

    }

    public static class MultiTranslationRunnable implements Runnable {

        private final CountDownLatch latch;
        public MultiTranslationRunnable(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            this.latch.countDown();
        }
    }
}
