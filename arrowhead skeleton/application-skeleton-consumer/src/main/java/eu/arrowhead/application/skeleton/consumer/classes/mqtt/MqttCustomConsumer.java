package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
import java.util.Map;

public class MqttCustomConsumer extends IConsumer {

    private final IMqttMessageListener messageListener;

    private MqttSettings settings;

    private final Logger logger = LogManager.getLogger(MqttCustomConsumer.class);

    public MqttCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        messageListener = new MqttCustomMessageListener(this);
        this.settings = new MqttSettings(settings);
    }

    @Override
    public void run() {
        connect(getConnectionDetails().getAddress(),getConnectionDetails().getPort());
    }

    private void connect(String address, int port) {
        try {
            String topic = settings.getTopic();

            logger.info("Mqtt consumer connected to " + address + ":" + port);
            MqttClient client = new MqttClient("tcp://" + address + ":" + port, "100", new MemoryPersistence());

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setKeepAliveInterval(60);
            connOpts.setConnectionTimeout(70);
            connOpts.setAutomaticReconnect(true);

            client.connect(connOpts);

            client.subscribe(topic,messageListener);

            // logger.info("Successfully subscribed to MQTT on address " + address + ":" + port + " with topic " + topic);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    private static class MqttCustomMessageListener implements IMqttMessageListener {

        private final MqttCustomConsumer consumer;

        public MqttCustomMessageListener(MqttCustomConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {


            // logger.info("Message arrived at mqtt " + consumer.getConnectionDetails() + " at topic " + s + " with content \"" + mqttMessage.toString() + "\"");
            consumer.OnMessageReceived(s,mqttMessage.toString());
        }
    }

}
