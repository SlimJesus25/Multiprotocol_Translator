package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IProducer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MqttCustomProducer extends IProducer {

    private Logger log = LoggerFactory.getLogger(MqttCustomProducer.class);

    private MqttClient client;

    private MqttSettings settings;

    private int numberOfMessages = 1;

    long thing = System.currentTimeMillis();

    public MqttCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails,settings);
        this.settings = new MqttSettings(settings);
        connect(connectionDetails.getAddress(),connectionDetails.getPort());
    }

    private void connect(String address, int port) {
        client = null;

        log.info("Connected to MQTT at " + address  + ":" + port);

        try {
            client = new MqttClient("tcp://" + address + ":" + port, settings.getClientId(), new MemoryPersistence());

            client.connect(settings.getConnectOptions());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void produce(String topic, String message) {


        if (numberOfMessages == 1) {
            thing = System.currentTimeMillis();
        }

        numberOfMessages++;

        String publishToTopic;

        if (settings.getTopic().equals("")) {
            publishToTopic = this.topicFromConsumer(topic);
        } else {
            publishToTopic = settings.getTopic();
        }

        try {

            if (!client.isConnected()) client.connect();
            // log.info("Publishing mqtt Message to " + cd + " at topic " + publishToTopic);
            // log.info("Message content - " + message + "\n");

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(settings.getQos());
            client.publish(publishToTopic,mqttMessage);


        } catch (MqttException e) {
            // throw new RuntimeException(e);
            log.warn("\n" + new RuntimeException(e) + "\n");
        }



        if (numberOfMessages == 100000) {
            log.info("Messages per second + " + (100000f / ((System.currentTimeMillis() - thing) / 1000f)));
            numberOfMessages = 0;
        }
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public MqttClient getClient() {
        return client;
    }

    /*"key.deserializer": "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer": "org.apache.kafka.common.serialization.StringDeserializer",
            "group.id": "mid"
            */

}
