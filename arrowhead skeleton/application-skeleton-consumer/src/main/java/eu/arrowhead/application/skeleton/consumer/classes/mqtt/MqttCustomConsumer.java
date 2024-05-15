package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.JavaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.List;
import java.util.Map;

public class MqttCustomConsumer extends IConsumer {

    private MqttClient mqttClient;
    private final IMqttMessageListener messageListener;
    private final MqttSettings settings;
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
            mqttClient = new MqttClient("tcp://" + address + ":" + port, settings.getClientId(), new MemoryPersistence());


            mqttClient.connect(settings.getConnectOptions());

            logger.info("Mqtt consumer connected to {}:{} with topic {}", address, port, settings.getTopic());

            mqttClient.subscribe(topic,messageListener);


        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    private static class MqttCustomMessageListener implements IMqttMessageListener {

        private final MqttCustomConsumer consumer;
        private final JavaRepository qosRepository = new JavaRepository();

        public MqttCustomMessageListener(MqttCustomConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            if(mqttMessage.getQos() == 0 || mqttMessage.getQos() == 1) {
                consumer.lastMessage = mqttMessage.toString();
                consumer.numberOfMessages++;
                consumer.OnMessageReceived(topic, mqttMessage.toString());
            }else if(mqttMessage.getQos() == 2) {
                if(!this.qosRepository.messageExists(String.valueOf(mqttMessage.getId()))){
                    this.qosRepository.registerNewMessage(String.valueOf(mqttMessage.getId()));
                    consumer.numberOfMessages++;
                    consumer.OnMessageReceived(topic, mqttMessage.toString());
                }
            }
        }
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }
}
