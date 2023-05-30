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

    private String publishToTopic;

    public MqttCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails,settings);
        this.settings = new MqttSettings(settings);
        publishToTopic = this.settings.getTopic();
        connect(connectionDetails.getAddress(),connectionDetails.getPort());
    }

    private void connect(String address, int port) {
        client = null;

        log.info("Connected to MQTT at " + address  + ":" + port);

        try {
            client = new MqttClient("tcp://" + address + ":" + port, "200", new MemoryPersistence());


            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(settings.isCleanSession());
            connOpts.setKeepAliveInterval(settings.getKeepAliveInternal());
            connOpts.setConnectionTimeout(settings.getConnectionTimeout());
            connOpts.setAutomaticReconnect(false);

            client.connect(connOpts);


            // log.info("Mqtt publisher connected to " + address + ":" + port + " with topic " + settings.getTopic());
            // log.info("With Settings \n" + settings);
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



        //ConnectionDetails cd = this.getConnectionDetails();

        /*
        String publishToTopic;


        if (settings.getTopic().equals("")) {
            if (!settings.getTopicOfConsumerWithoutPrefix().equals("")) {
                // Publish to consumer's topic minus the prefix

                publishToTopic = topic.replace(settings.getTopicOfConsumerWithoutPrefix(), "");
            } else {
                // Publish to consumer's topic

                publishToTopic = topic;
            }
        } else {
            // Publish to set topic

            publishToTopic = settings.getTopic();
        }
        */


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
}
